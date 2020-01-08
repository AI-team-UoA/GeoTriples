package eu.linkedeodata.geotriples;




import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.function.Function;
import be.ugent.mmlab.rml.function.FunctionFactory;
import be.ugent.mmlab.rml.model.RMLMapping;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.concrete.RowProcessor;
import eu.linkedeodata.geotriples.Converters.RML_Converter;
import eu.linkedeodata.geotriples.utils.SerializedLogger;
import eu.linkedeodata.geotriples.utils.SparkReader;
import jena.cmdline.ArgDecl;
import jena.cmdline.CommandLine;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.serializer.KryoSerializer;
import org.apache.spark.sql.*;
import org.datasyslab.geospark.serde.GeoSparkKryoRegistrator;
import org.openrdf.model.URI;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

import java.util.concurrent.TimeUnit;


import org.apache.spark.rdd.DoubleRDDFunctions;


/*
 * Generate Mapping file:
 *      java -cp geotriples-core/target/geotriples-dependencies.jar eu.linkedeodata.geotriples.GeoTriplesCMD generate_mapping -rml  -o <out> -b http://example.com <in>
 * Dump RDF:
 *      java -cp geotriples-core/target/geotriples-dependencies.jar eu.linkedeodata.geotriples.GeoTriplesCMD dump_rdf -o <out> -b http://example.com -sh <shp> <rml>
 *
 * Execute Spark implementation:
 *      spark-submit  --master local[*]   --class eu.linkedeodata.geotriples.GeoTriplesCMD geotriples-core/target/geotriples-dependencies.jar  spark  -i <in_file> -o <out_folder> <rml>
 * Execute in Hadoop cluster using YARN:
 *      spark-submit  --master yarn --deploy-mode cluster --class eu.linkedeodata.geotriples.GeoTriplesCMD geotriples-core/target/geotriples-dependencies.jar  spark  -i <hdfs_path> -o <hdfs_path> <hdfs_path>
 * Debug:
 *      spark-submit  --master local[*] --conf spark.driver.extraJavaOptions=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005  --class eu.linkedeodata.geotriples.GeoTriplesCMD geotriples-core/target/geotriples-dependencies.jar  spark  -i <in_file> -o <out_folder> <rml>
 * Execute using VisualVM profiler:
 *      spark-submit   --executor-memory 10g --driver-memory 10g --conf "spark.driver.extraJavaOptions=-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=8090 -Dcom.sun.management.jmxremote.rmi.port=8091 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=localhost"  --master local[*] --class eu.linkedeodata.geotriples.GeoTriplesCMD geotriples-core/target/geotriples-dependencies.jar  spark -i <in_file> -o <out_folder> <rml>
 *
 * eu.linkedeodata.geotriples.GeoTriplesCMD
 * hops -i hdfs:///Projects/testHops/Experiments/Cyprus_pois.csv -o test hdfs:///Projects/testHops/Experiments/cyprus_csv.ttl
 *
 *
 */

/**
 *  Perform the conversion big geo-spatial data into RDF triples using the Apache Spark.
 *  It is designed to be executed in HopsWork.
 */
public class SparkMaster  {

    private enum Mode {
        ROW,
        PARTITION
    }

    private SparkSession spark;
    private SparkReader reader;

    private String inputFile;
    private String outputDir;
    private String mappingFile;
    private Dataset<Row> inputDataset;
    private FileSystem fs;
    private Mode mode;
    private String repartition = null;
    private Logger log;

    private boolean count;



    /**
     * Constructor. Parses the input arguments and configure Spark.
     *
     * @param inputArgs: input command line arguments.
     */
    SparkMaster(String[] inputArgs){

        /*try{
            System.out.println("Sleeping...zzZZzzzZzZZZzz..");
            TimeUnit.SECONDS.sleep(10);
            System.out.println("Woke UP!");
        }
        catch (InterruptedException ignored){}*/

        // set loggers level
        log = Logger.getLogger("GeoTriples");
        log.setLevel(Level.INFO);
        Logger.getLogger("org").setLevel(Level.INFO);
        Logger.getRootLogger().setLevel(Level.INFO);

        boolean is_shp_folder = false;
        count = false;

        String outputDirArg = null;
        try {

            // parse the input arguments
            CommandLine cmd = new CommandLine();
            ArgDecl outDirArg = new ArgDecl(true, "-o", "out", "outfile");
            ArgDecl infileArg = new ArgDecl(true, "-i", "in", "infile");
            ArgDecl inSHFArg = new ArgDecl(true, "-sh", "sh", "shapefileForlder");
            ArgDecl repartitionArg = new ArgDecl(true, "-r", "re", "repartition");
            ArgDecl helpArg = new ArgDecl(false, "-h", "help");
            ArgDecl modeArg = new ArgDecl(true, "-m", "mode");
            ArgDecl timesArg = new ArgDecl(true, "-times", "times");
            ArgDecl countArg = new ArgDecl(false, "-count", "count");



            cmd.add(outDirArg);
            cmd.add(infileArg);
            cmd.add(inSHFArg);
            cmd.add(repartitionArg);
            cmd.add(helpArg);
            cmd.add(modeArg);
            cmd.add(timesArg);
            cmd.add(countArg);
            cmd.process(inputArgs);

            boolean usage = false;
            List<String> errors = new ArrayList<>();

            if (cmd.hasArg(helpArg)) usage(null);

            if (cmd.hasArg(outDirArg)) {
                outputDirArg = cmd.getArg(outDirArg).getValue();

                outputDir = outputDirArg;
                if(outputDir.contains("/") && outputDir.lastIndexOf("/") == outputDir.length() -1)
                    outputDir = outputDir.substring(0, outputDir.length() - 1);

                Path outputDirArg_path = new Path(outputDir);
                Configuration conf = new Configuration();
                fs = FileSystem.get(conf);

                // Create the output folder
                // if the specified direcotory does not exist do nothing
                // if if the specified direcotory does exist it creates a new one inside the specified one
                if (fs.exists(outputDirArg_path)) {
                    if (!fs.isDirectory(outputDirArg_path)) {
                        usage = true;
                        errors.add("ERROR: \"-o\" flag must point to a directory");
                    } else {
                        outputDir = outputDir + "/GeoTriples_results";
                        outputDirArg_path = new Path(outputDir);
                    }
                }
                try {
                    int i = 1;
                    String temp_name = outputDir;
                    while (fs.exists(outputDirArg_path)) {
                        outputDir = temp_name + "_" + i;
                        outputDirArg_path = new Path(outputDir);
                        i++;
                    }
                    if (!outputDirArg.equals(outputDir))
                        log.warn("Because the " + outputDirArg + " already exists, the results will be located in " + outputDir);
                    else log.info("The results will be located in " + outputDir);
                }
                catch (Exception e){
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            else {
                usage = true;
                errors.add("ERROR: You need to specify the output directory using the \"-o\" flag");
            }

            if (cmd.hasArg(infileArg)){
                inputFile = cmd.getArg(infileArg).getValue();
                if (cmd.hasArg(timesArg)){
                    // it is used in order to read multiple times a file
                    StringBuilder sb = new StringBuilder();
                    int times_to_load = Integer.parseInt(cmd.getArg(timesArg).getValue());
                    for (int i = 0; i < times_to_load - 1; i++)
                        sb.append(inputFile).append(",");
                    sb.append(inputFile);
                    inputFile = sb.toString();
                }
            }
            else if (cmd.hasArg(inSHFArg)){
                inputFile = cmd.getArg(inSHFArg).getValue();
                Path shp_folder = new Path(inputFile);
                Configuration conf = new Configuration();
                fs = FileSystem.get(conf);

                if (!fs.isDirectory(shp_folder)) {
                    usage = true;
                    errors.add("ERROR: \"-sh\" flag must point to a directory");
                }
                is_shp_folder = true;
            }
            else {
                usage = true;
                errors.add("ERROR: You need to specify the input file using the \"-i\" flag");
            }


            if (cmd.hasArg(modeArg)){
                String in_mode = cmd.getArg(modeArg).getValue();
                if (in_mode.equals("row"))
                    mode = Mode.ROW;
                else if (in_mode.equals("partition"))
                    mode = Mode.PARTITION;
                else {
                    log.warn("No such mode \"" + in_mode + "\". The conversion mode is set to  \"per ROW conversion\"");
                    mode = Mode.ROW;
                }
            }
            else {
                mode = Mode.ROW;
            }
            log.info("The conversion mode is set to \"per " + mode.name() + " conversion\"");

            if (cmd.hasArg(repartitionArg)){
                repartition = cmd.getArg(repartitionArg).getValue();
            }

            if (inputArgs[inputArgs.length - 1].endsWith(".ttl"))
                mappingFile = inputArgs[inputArgs.length - 1];
            else {
                usage = true;
                errors.add("ERROR: The last argument must be the mapping file and it must end with the extension .ttl");
            }

            if (cmd.hasArg(countArg)){
                count = true;
            }

            if (usage)
                usage(errors);
        }
        catch (IllegalArgumentException | IOException e){
            e.printStackTrace();
            usage(null);
        }

        reader = new SparkReader(inputFile, is_shp_folder);

        // configure spark
        SparkConf conf = new SparkConf()
                .set("spark.serializer", KryoSerializer.class.getName())
                .set("spark.kryo.registrator", GeoSparkKryoRegistrator.class.getName())
                .set("spark.io.compression.codec", "lz4");

        //Shapefiles require more SparkMemory for shuffling
        if (repartition != null)
            conf.set("spark.memory.fraction", "0.5");
        else
            conf.set("spark.memory.fraction", "0.2");

        spark = SparkSession
                .builder()
                .appName("GeoTriples-HOPS")//Hops.getJobName())
                .config(conf)
                .getOrCreate();

        System.setProperty("geospark.global.charset", "utf8");
    }




    /**
     * Read input according to its file-type and store it as a spark Dataset.
     */
    public void readInput() {
        inputDataset = reader.read(spark, repartition);
        inputDataset.printSchema();
    }


    /**
     * Convert the produced Dataset into rdf triples and store the results in a directory.
     * The Conversion can be either per row or per partition and it is defined by the user.
     * If the user did not define the conversion mode, the conversion mode will be per row conversion.
     */
    public void convert2RDF() {
        // data that will be passed to the conversion
        List<String> headers = Arrays.asList(inputDataset.columns());
        ArrayList<TriplesMap> mapping_list = RML_Parser(mappingFile);
        long startTime = System.currentTimeMillis();
        log.info("Starts the conversion");
        if (count) count(headers, mapping_list);
        else {
            switch (mode) {
                case ROW:
                    log.info("Conversion mode: Per Row Conversion");
                    convert_row(headers, mapping_list);
                    break;
                case PARTITION:
                    log.info("Conversion mode: Per Partition Conversion");
                    convert_partition(headers, mapping_list);
                    break;
            }
        }
        log.info("The conversion completed and took " + (System.currentTimeMillis() - startTime) + " msec.\n");
    }


    /**
     * Convert the input Dataset into RDF triples and store the results.
     * The conversion is taking place per Partitions using the mapPartition Spark transformation.
     * @param headers of the Dataset
     * @param mapping_list list of TripleMaps
     */
    private void convert_partition(List<String> headers, ArrayList<TriplesMap> mapping_list){
        SparkContext sc = SparkContext.getOrCreate();
        RML_Converter rml_converter = new RML_Converter(mapping_list, headers);
        rml_converter.start();
        ClassTag<RML_Converter> classTagRML_Converter = scala.reflect.ClassTag$.MODULE$.apply(RML_Converter.class);
        ClassTag<HashMap<URI, Function>> classTag_hashMap = scala.reflect.ClassTag$.MODULE$.apply(HashMap.class);

        Broadcast<RML_Converter> bc_converter = sc.broadcast(rml_converter, classTagRML_Converter);
        Broadcast<HashMap<URI, Function>> bc_functionsHashMap = sc.broadcast(FunctionFactory.availableFunctions, classTag_hashMap);

        inputDataset
            .javaRDD()
            .mapPartitions((Iterator<Row> rows_iter) ->
                    bc_converter.value().convertPartition(rows_iter, bc_functionsHashMap.value()))
            .saveAsTextFile(outputDir);
        rml_converter.stop();
    }


    /**
     * Convert the input Dataset into RDF triples and store the results.
     * The conversion is taking place per Per using the map Spark transformation.
     * @param headers of the Dataset
     * @param mapping_list list of TripleMaps
     */
    private void convert_row(List<String> headers, ArrayList<TriplesMap> mapping_list){

        SparkContext sc = SparkContext.getOrCreate();
        RML_Converter rml_converter = new RML_Converter(mapping_list, headers);
        rml_converter.start();
        ClassTag<RML_Converter> classTagRML_Converter = scala.reflect.ClassTag$.MODULE$.apply(RML_Converter.class);
        ClassTag<HashMap<URI, Function>> classTag_hashMap = scala.reflect.ClassTag$.MODULE$.apply(HashMap.class);

        Broadcast<RML_Converter> bc_converter = sc.broadcast(rml_converter, classTagRML_Converter);
        Broadcast<HashMap<URI, Function>> bc_functionsHashMap = sc.broadcast(FunctionFactory.availableFunctions, classTag_hashMap);
        inputDataset
                .javaRDD()
                .map(row -> bc_converter.value().convertRow(row, bc_functionsHashMap.value()))
                .saveAsTextFile(outputDir);
        rml_converter.stop();
    }


    /**
     * Count the input records and the produced triples
     * The conversion is taking place per Per using the map Spark transformation.
     * @param headers of the Dataset
     * @param mapping_list list of TripleMaps
     */
    private void count(List<String> headers, ArrayList<TriplesMap> mapping_list) {

        long records = inputDataset.count();
        log.info("The input dataset(s) contain: " + records + " records");
        SparkContext sc = SparkContext.getOrCreate();
        RML_Converter rml_converter = new RML_Converter(mapping_list, headers);
        rml_converter.start();
        ClassTag<RML_Converter> classTagRML_Converter = scala.reflect.ClassTag$.MODULE$.apply(RML_Converter.class);
        ClassTag<HashMap<URI, Function>> classTag_hashMap = scala.reflect.ClassTag$.MODULE$.apply(HashMap.class);

        Broadcast<RML_Converter> bc_converter = sc.broadcast(rml_converter, classTagRML_Converter);
        Broadcast<HashMap<URI, Function>> bc_functionsHashMap = sc.broadcast(FunctionFactory.availableFunctions, classTag_hashMap);
        Long triples_count = inputDataset
                .javaRDD()
                .map(row -> bc_converter.value().convertRow(row, bc_functionsHashMap.value()))
                .map((String s) -> s.codePoints().filter(ch -> ch == '\n').count())
                .reduce(Long::sum);
        rml_converter.stop();
        log.info("The input dataset(s) transformed into " + triples_count + " RDF triples");
    }


    /**
     * Close the Spark session.
     */
    public void endSpark(){ spark.close();}


    /**
     *
     * Parse the RML file and produce the Triple Map.
     *
     * It used to be inside the converter but, but it is better here because
     * we want to avoid every Executor to read the mapping file.
     * Reading invokes disk I/O and Network I/O and therefore is expensive!
     *
     * @param mappingFile: the input mapping file.
     */
    private ArrayList<TriplesMap> RML_Parser(String mappingFile)  {
        try {

            Path inFile = new Path(mappingFile);
            FSDataInputStream in = fs.open(inFile);
            RMLMapping mapping = RMLMappingFactory.extractRMLMapping(in);
            return new ArrayList<>(mapping.getTriplesMaps());
        } catch (Exception e) {
            log.error("ERROR Initializing RML_Converter");
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }


    /**
     * Print Usage.
     *
     * @param errors: list of errors.
     */
    private void usage(List<String> errors){
        if (errors != null ){
            log.error("\n");
            for (String error : errors) log.error(error);
            log.error("\n");
        }
        log.info(".___________________________________________________________.");
        log.info("|\\._______________________________________________________./|");
        log.info("|\\|                       GeoTriples                      |/|");
        log.info("|\\|  a tool for transforming EO/geospatial data into RDF  |/|");
        log.info(" \\._______________________________________________________./ ");

        log.info("\n");
        log.info("Usage for Spark mode:  [arguments] <source mapping>");
        log.info("\tArguments:");
        log.info("\t\t-o <outDir>\t\tOutput directory name");
        log.info("\t\t-i <inFile>\t\tPath to the input file");
        log.info("\t\t-m mode\t\tDefine the conversion mode. Accepted values: \"partition\", \"row\"(default)");
        log.info("\t\t-r <partitions>\t\t(Optional) Specify the number of the requested partitions. If it is set to \"default\", then the number of partitions will be calculated based on the size of the input.");
        log.info("\t\t-sh <directory>\t\t\t Path that points to a directory containing multiple folders of shapefiles. Used to load multiple shapefiles");
        log.info("\t\t-times <n>\t\t\t Load the input dataset <n> times");
        log.info("\t\t-h \t\t\tPrint usage");
        log.info("\n");
        log.info("\n");
        log.info("\tThe last argument must be the path to the mapping file");

        System.exit(0);

    }

}