package eu.linkedeodata.geotriples.utils;


import be.ugent.mmlab.rml.core.Config;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF2;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.*;
import org.datasyslab.geospark.formatMapper.shapefileParser.ShapefileReader;
import org.datasyslab.geospark.spatialRDD.SpatialRDD;
import org.datasyslab.geosparksql.utils.Adapter;
import scala.collection.mutable.WrappedArray;
import java.io.IOException;
import java.util.LinkedList;
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
    public SparkReader(String inputfile, boolean isShpFolder){
        log = Logger.getLogger("GeoTriples");
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
            // TODO throw and handle exception & write to logger
            log.error("This file format is not supported yet");
            System.exit(0);
        }
    }

    
    public boolean isSHP(){return source.equals(Source.SHP);}

    
    /**
     * Call the corresponding reader regarding the source of the input file
     *
     * @return a Spark's Dataset containing the data
     * @param session Spark Session
     */
    public Dataset<Row> read(SparkSession session, String repartition){

        spark = session;
        Dataset<Row> dt = null;
        long startTime = System.currentTimeMillis();
        try {
            switch (source) {
                case CSV:
                    dt = readCSV();
                    break;
                case TSV:
                    dt = readTSV();
                    break;
                case SHP:
                    dt = readSHP();
                    break;
                case GEOJSON:
                    dt = readGeoJSON();
                    break;
                case KML:
                    log.error("KML files are not Supported yet");
                    break;
            }
            // insert a column with ID
            dt = dt.withColumn(Config.GEOTRIPLES_AUTO_ID, functions.monotonicallyIncreasingId());
            log.info("The input data was read into " + dt.javaRDD().getNumPartitions() + " partitions");
            log.info("The  reading procedure completed and took " + (System.currentTimeMillis() - startTime) + " msec\n");
            // repartition the loaded dataset if it is specified by the user.
            // if "repartition" is set to "defualt" the number of partitions is calculated based on input's size
            // else the number must be defined by the user
            if (repartition != null){
                int new_partitions = 0;
                if (repartition.equals("default")) {
                    try {
                        Configuration conf = new Configuration();
                        FileSystem fs = FileSystem.get(conf);
                        for (String filename : filenames) {
                            Path input_path = new Path(filename);
                            double shp_size = fs.getContentSummary(input_path).getLength();
                            new_partitions += Math.ceil(shp_size / 120000000) + 1;
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
                    int partitions = dt.javaRDD().getNumPartitions();
                    if(partitions > new_partitions)
                        dt = dt.coalesce(new_partitions);
                    else
                        dt = dt.repartition(new_partitions);
                    log.info("The input Dataset will be repartitioned into: " + new_partitions + " partitions");
                    dt.persist();
                }
            }
        }
        catch (NullPointerException ex){
            log.error("Not Supported file format");
            ex.printStackTrace();
            System.exit(1);
        }
        return dt;
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
     * Read multiple Shapefiles into one Dataset using the GeoSpark library.
     * WARNING: GeoSpark always reads shapefiles into one partition and therefore
     * it requires repartitioning which is a time-consuming method.
     *
     * @return a Spark's Dataset containing the data of the input shapefile(s).
     */
    private Dataset<Row> readSHP(){

        JavaSparkContext jsc = JavaSparkContext.fromSparkContext(spark.sparkContext());
        List<Dataset<Row>> dt_list = new LinkedList<>();


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
                        dt_list.add(Adapter.toDf(spatialRDD, spark));
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
                dt_list.add(Adapter.toDf(spatialRDD, spark));
            }
        }
        Dataset<Row> dt = dt_list.get(0);
        dt_list.remove(0);
        for (Dataset<Row> dataset: dt_list)
            dt = dt.union(dataset);

        return dt;
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

}
