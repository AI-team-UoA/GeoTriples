package eu.linkedeodata.geotriples.utils;


import be.ugent.mmlab.rml.core.Config;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.*;
import org.apache.spark.sql.api.java.UDF2;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.*;
import org.datasyslab.geospark.formatMapper.shapefileParser.ShapefileReader;
import org.datasyslab.geospark.spatialRDD.SpatialRDD;
import scala.Tuple2;
import scala.collection.mutable.WrappedArray;
import scala.reflect.ClassTag;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Read the input file as Spark's Dataset.
 */
public class SparkReader {

    private enum Source {
        SHP,
        CSV,
        TSV,
        GEOJSON,
        KML
    }

    private String[] headers;
    private String[] filenames;
    private Source source;
    private SparkSession spark;
    private Logger log;
    private boolean is_shp_folder;


    /**
     * Constructor
     * 
     * @param inputfile the file it will read
     */
    public SparkReader(String inputfile, boolean isShpFolder, SparkSession session){
        spark = session;
        log = Logger.getLogger("GEOTRIPLES-SPARK");
        log.setLevel(Level.INFO);

        if (inputfile.contains(","))
            filenames = inputfile.split(",");
        else {
            filenames = new String[]{inputfile};
        }
        if (isShpFolder) {
            source = Source.SHP;
            is_shp_folder = isShpFolder;
        }
        else if(filenames[0].endsWith(".csv"))
           source = Source.CSV;

        else if(filenames[0].endsWith(".tsv"))
            source = Source.TSV;

        else if(filenames[0].endsWith(".shp"))
            source = Source.SHP;

        else if(filenames[0].endsWith(".geojson"))
            source = Source.GEOJSON;

        else if(filenames[0].endsWith(".kml"))
            source = Source.KML;

        else {
            log.error("This file format is not supported yet");
            System.exit(0);
        }
    }
    
    /**
     * Call the corresponding reader regarding the source of the input file
     *
     * @return a Spark's Dataset containing the data
     */
    public JavaRDD<Row> read(String repartition){

        long startTime = System.currentTimeMillis();
        JavaRDD<Row> rowRDD = null;
        Dataset<Row> dt;
        try {
            switch (source) {
                case SHP:
                    int p = StringUtils.isNumeric(repartition) ? Integer.parseInt(repartition) : 0;
                    rowRDD = readSHP(p);
                    break;
                case CSV:
                    dt = readCSV();
                    // insert a column with ID
                    dt = dt.withColumn(Config.GEOTRIPLES_AUTO_ID, functions.monotonicallyIncreasingId());
                    headers = dt.columns();
                    rowRDD = dt.javaRDD();
                    break;
                case TSV:
                    dt = readTSV();
                    // insert a column with ID
                    dt = dt.withColumn(Config.GEOTRIPLES_AUTO_ID, functions.monotonicallyIncreasingId());
                    headers = dt.columns();
                    rowRDD = dt.javaRDD();
                    break;
                case GEOJSON:
                    dt = readGeoJSON();
                    // insert a column with ID
                    dt = dt.withColumn(Config.GEOTRIPLES_AUTO_ID, functions.monotonicallyIncreasingId());
                    headers = dt.columns();
                    rowRDD = dt.javaRDD();
                    break;
                case KML:
                    log.error("KML files are not Supported yet");
                    break;
            }

            /*
                 repartition the loaded dataset if it is specified by user.
                 if "repartition" is set to "defualt" the number of partitions is calculated based on input's size
                 else the number must be defined by the user
            */
            int partitions = rowRDD == null ? 0: rowRDD.getNumPartitions();
            log.info("The input data was read into " + partitions + " partitions");
            if (repartition != null && source != Source.SHP){
                int new_partitions = 0;
                if (repartition.equals("default")) {
                    try {
                        Configuration conf = new Configuration();
                        FileSystem fs = FileSystem.get(conf);
                        for (String filename : filenames) {
                            Path input_path = new Path(filename);
                            double file_size = fs.getContentSummary(input_path).getLength();
                            new_partitions += Math.ceil(file_size / 120000000) + 1;
                        }
                    }
                    catch(IOException e){
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
                else if (StringUtils.isNumeric(repartition))
                    new_partitions = Integer.parseInt(repartition);

                if(new_partitions > 0){
                    if(partitions > new_partitions)
                        rowRDD = rowRDD.coalesce(new_partitions);
                    else
                        rowRDD = rowRDD.repartition(new_partitions);
                    log.info("Dataset was repartitioned into: " + new_partitions + " partitions");
                }
            }
        }
        catch (NullPointerException ex){
            log.error("Not Supported file format");
            ex.printStackTrace();
            System.exit(1);
        }
        log.info("Input dataset(s) was loaded in " + (System.currentTimeMillis() - startTime) + " msec");
        return rowRDD;
    }


    /**
     * Read the CSV file.
     *
     * @return a Spark's Dataset containing the data.
     */
    private Dataset<Row> readCSV(){
        return spark.read()
                .format("csv")
                .option("header", "true")
                .option("delimiter", ",")
                .csv(filenames);
    }

    /**
     * Read the TSV file.
     *
     * @return a Spark's Dataset containing the data.
     */
    private Dataset<Row> readTSV(){
        return spark.read()
                .format("csv")
                .option("header", "true")
                .option("delimiter", "\t")
                .csv(filenames);
    }


    /**
     * Read multiple Shapefiles into a single JavaRDD.
     * The files are read using the GeoSpark library into spark's Datasets and
     * then are transformed into a JavaRDD of rows.
     * WARNING: GeoSpark always reads shapefiles into a single partition!
     *
     * @return a JavaRDD of Rows.
     */
    private JavaRDD<Row> readSHP(int partitions){
        boolean headers_set = false;
        JavaSparkContext jsc = JavaSparkContext.fromSparkContext(spark.sparkContext());
        List<JavaRDD<Row>> rdds = new ArrayList<>();
        StructType schema =  new StructType();
        if (is_shp_folder){
            // read a folder that contains folders of shapefiles
            try {
                String shp_folder = filenames[0];
                Configuration conf = new Configuration();
                FileSystem fs = FileSystem.get(conf);
                FileStatus[] fileStatus = fs.listStatus(new Path(shp_folder));
                for (FileStatus status : fileStatus) {
                    if (status.isDirectory()) {
                        String shapefile = status.getPath().toString();
                        SpatialRDD<Geometry> spatialRDD = ShapefileReader.readToGeometryRDD(jsc, shapefile);

                        /*
                            reads the headers of the first shapefile, and set row's schema
                            then repartition if requested, and transform the spatialRDD into
                            JavaRDD of rows.
                         */
                        if (!headers_set) {
                            headers = new String[spatialRDD.fieldNames.size() + 2];
                            headers[0] = "geometry";
                            schema = schema.add(DataTypes.createStructField("geometry", DataTypes.StringType, true));
                            headers[1] = Config.GEOTRIPLES_AUTO_ID;
                            schema = schema.add(DataTypes.createStructField(Config.GEOTRIPLES_AUTO_ID, DataTypes.LongType, false));
                            for (int i = 0; i < spatialRDD.fieldNames.size(); i++) {
                                headers[i+2] = spatialRDD.fieldNames.get(i);
                                schema = schema.add(DataTypes.createStructField(spatialRDD.fieldNames.get(i), DataTypes.StringType, true));
                            }
                            headers_set = true;
                        }
                        ClassTag<StructType> schemaCT = scala.reflect.ClassTag$.MODULE$.apply(StructType.class);
                        Broadcast<StructType> schemaBD = spark.sparkContext().broadcast(schema, schemaCT);
                        JavaRDD<Row> rowRDD = spatialRDD.rawSpatialRDD
                                .zipWithUniqueId()
                                .map((Tuple2<Geometry, Long> tuple) ->{
                                    Geometry geom = tuple._1;
                                    Long index = tuple._2;
                                    String[] userdata = geom.getUserData().toString().split("\t");

                                    Object[] values = new Object[userdata.length+2];
                                    values[0] = geom.toText();
                                    values[1] = (Object) (index);
                                    System.arraycopy(userdata, 0, values, 2, userdata.length);

                                    return (Row) new GenericRowWithSchema(values, schemaBD.value());
                                });
                        rdds.add(rowRDD);
                    }
                }
            }
            catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }
        }
        else {
            // read multiple shapefiles
            for (String filename : filenames) {
                String shapefile = filename.substring(0, filename.lastIndexOf('/'));
                SpatialRDD<Geometry> spatialRDD = ShapefileReader.readToGeometryRDD(jsc, shapefile);
                /*
                    reads the headers of the first shapefile, and set row's schema
                    then repartition if requested, and transform the spatialRDD into
                    JavaRDD of rows.
                 */
                if (!headers_set) {
                    headers = new String[spatialRDD.fieldNames.size() + 2];
                    headers[0] = "geometry";
                    schema = schema.add(DataTypes.createStructField("geometry", DataTypes.StringType, true));
                    headers[1] = Config.GEOTRIPLES_AUTO_ID;
                    schema = schema.add(DataTypes.createStructField(Config.GEOTRIPLES_AUTO_ID, DataTypes.LongType, false));
                    for (int i = 0; i < spatialRDD.fieldNames.size(); i++) {
                        headers[i+2] = spatialRDD.fieldNames.get(i);
                        schema = schema.add(DataTypes.createStructField(spatialRDD.fieldNames.get(i), DataTypes.StringType, true));
                    }
                    headers_set = true;
                }
                ClassTag<StructType> schemaCT = scala.reflect.ClassTag$.MODULE$.apply(StructType.class);
                Broadcast<StructType> schemaBD = spark.sparkContext().broadcast(schema, schemaCT);
                JavaRDD<Row> rowRDD = spatialRDD.rawSpatialRDD
                        .zipWithUniqueId()
                        .map((Tuple2<Geometry, Long> tuple) ->{
                            Geometry geom = tuple._1;
                            Long index = tuple._2;
                            String[] userdata = geom.getUserData().toString().split("\t");

                            Object[] values = new Object[userdata.length+2];
                            values[0] = geom.toText();
                            values[1] = (Object) (index);
                            System.arraycopy(userdata, 0, values, 2, userdata.length);

                            return (Row) new GenericRowWithSchema(values, schemaBD.value());
                        });
                rdds.add(rowRDD);
            }
        }
        JavaRDD<Row> rowRDD = rdds.remove(0);
        for (JavaRDD<Row> rdd: rdds)
            rowRDD = rowRDD.union(rdd);

        if (partitions > 0)
            rowRDD = rowRDD.repartition(partitions);

        return rowRDD;
    }


    /**
     * Read the input GeoJSON files into a Spark Dataset.
     * GeoJSON attributes are located in the column "Properties" and the geometry in the column "Geometry",
     * and hence it expands them. Then convert the GeoJSON Geometry into WKT using a UDF.
     *
     * @return a Spark's Dataset containing the data.
     */
    private Dataset<Row> readGeoJSON(){
        Dataset<Row> dataset = spark.read()
                .option("multyLine", true)
                .format("json")
                .json(filenames);

        //Expand the fields
        dataset = dataset.drop("_corrupt_record").filter(dataset.col("geometry").isNotNull());
        StructType schema = dataset.schema();
        StructField[] gj_fields =  schema.fields();
        for (StructField sf : gj_fields){
            DataType dt =  sf.dataType();
            if (dt instanceof  StructType) {
                StructType st = (StructType) dt;
                if (st.fields().length > 0) {
                    String column_name = sf.name();
                    for (String field : st.fieldNames())
                        dataset = dataset.withColumn(field, functions.explode(functions.array(column_name + "." + field)));
                    dataset = dataset.drop(column_name);
                }
            }
        }
        //Convert GeoJSON Geometry into WKT
        UDF2<String, WrappedArray, String> coords2WKT =
                (String type, WrappedArray coords) ->{ return Coordinates2WKT.convert.apply(type, coords); };

        spark.udf().register("coords2WKT", coords2WKT, DataTypes.StringType);
        dataset = dataset.withColumn("geometry",
                functions.callUDF("coords2WKT", dataset.col("type"), dataset.col("coordinates")));
        dataset = dataset.drop(dataset.col("type")).drop(dataset.col("coordinates"));

        return dataset;
    }

    public String[] getHeaders() {
        return headers;
    }

}
