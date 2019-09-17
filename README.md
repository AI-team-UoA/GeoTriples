# GeoTriples
Publishing geospatial data as Linked Open Geospatial Data. GeoTriples generates and processes extended R2RML and RML 
mappings that transform geospatial data from many input formats into RDF. GeoTriples allows the transformation of 
geospatial data stored in raw files (shapefiles, CSV, KML, XML, GML and GeoJSON) and spatially-enabled RDBMS 
(PostGIS and MonetDB) into RDF graphs using well-known vocabularies like GeoSPARQL and stSPARQL, but without being tightly
 coupled to a specific vocabulary. 

## Quickstart ##
### Use GeoTriples binaries (Unix) ###
Assuming [Java >=7](https://www.java.com/en/download/) installed:

Download GeoTriples binaries [here](http://geotriples.di.uoa.gr/downloads/geotriples-1.1.6-bin.zip)
*	Unzip the downloaded file `geotriples-<version>-bin.zip`
*	Change directory to `geotriples-<version>-bin`
*	Under the `bin` directory you can find the available starter script for GeoTriples

	> `bin/geotriples-cmd`
	
See more at Wiki pages

## Execution by source

Install the source code by using
    
    mvn package 

**Generate Mapping files:**

    java -cp <geotriples-dependencies.jar> eu.linkedeodata.geotriples.GeoTriplesCMD generate_mapping -o <output file(.ttl)> -b <URI base> <input file>

* **-o output_file** the name of the produced mapping file (RML/R2RML)
* **-b URI_base** the base URI that will describe the entities

<br/>

**Transform file into RDF**

    java -cp <geotriples-dependencies.jar> eu.linkedeodata.geotriples.GeoTriplesCMD dump_rdf -o <output file> -b <URI base> (-sh <shp file>) <(produced) mapping file (.ttl)>
    
* **-o output_file** the path of the produced file
* **-b URI_base** the base URI that will describe the entities
* **-sh shp_file** if the input is a shapefile specify the .shp path using this flag 

## Execution by binaries

**Generate Mapping files:**

    bin/geotriples-all generate_mapping -o <output_file (.ttl)> -b <URI base> (-sh <shp file>) <(produced) mapping file (.ttl)>
    
<br/>

**Transform file into RDF**
    
    bin/geotriples-all dump_rdf -o <output_file> -b http://example.com (-sh <shp file>) <path_to_the_mapping_file>


---

# GeoTriples-Spark

GeoTriples-Spark is an extended version of GeoTriples capable of transforming big geospatial data into RDF graphs.
To enable the transformation of big geospatial
data we extended GeoTriples to run on top of Apache Spark and Hops (a new distribution of Apache Hadoop developed by KTH, RISE SICS, and Logical Clocks AB).This implementation can
run in a standalone machine or in a Hadoop based cluster, but it is more efficient when it runs in Hops as it is a more write-intensive application.

### Requirements
* Java 8
* Maven 3
* Apache Spark 2.4.0 or 2.4.1
* Apache Hadoop 2.6.0 or 2.6.7 or Hops

### Build
    mvn package

### Execute
    spark-submit --class eu.linkedeodata.geotriples.GeoTriplesCMD <path to geotriples-spark.jar> spark -i <in_file> -o <out_folder> <rml>

* **-i input_file**: path to the input dataset. Enter multiple input files seperated by comma ",".

* **-o out_folder**: path to the folder in which the results will be stores. The folder must not exist but in case it does, a new folder inside of it will be created.

* The **rml** file must be the file generates by the "gerate_mapping" process of GeoTriples.

### Additional flags

* **-m mode**: It sets the transformation mode. "mode" can be "partition" or "row"(default). In the "partition" mode the RDF triples are written to the targeted file after the
competition of the transformation of the whole partition. In the "row" mode, each line is transformed into RDF triples and are directly written to the files.
For small datasets the "partition" mode is faster, but otherwise we advise to use "row" mode as it is more memory friendly.
 
* **-r partitions**: Using -r flag you can re-partition the input dataset into "partitions" partitions. 
**WARNING** re-partitionig triggers data shuffling and therefore it can significantly increase the execution time.

* **-sh folder_path**: It is used in order to load a multiples ESRI shapefiles (each one strored in a seperate folder) that exist in the "folder_path".

* **-times n**: Load the input dataset "n" times.

* **help**: Print help   