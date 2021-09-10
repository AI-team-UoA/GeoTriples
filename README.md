# GeoTriples [![DOI](https://zenodo.org/badge/29589129.svg)](https://zenodo.org/badge/latestdoi/29589129)
Publishing geospatial data as Linked Geospatial Data. GeoTriples generates and processes extended R2RML and RML 
mappings that transform geospatial data from many input formats into RDF. GeoTriples allows the transformation of 
geospatial data stored in raw files (shapefiles, CSV, KML, XML, GML and GeoJSON) and spatially-enabled RDBMS 
(PostGIS and MonetDB) into RDF graphs using well-known vocabularies like GeoSPARQL and stSPARQL, but without being tightly
 coupled to a specific vocabulary. 

## Quickstart ##
### Use GeoTriples binaries (Unix) ###
Assuming [Java >=7](https://www.java.com/en/download/) is installed:

Download GeoTriples binaries from [here](http://geotriples.di.uoa.gr/downloads/geotriples-1.1.6-bin.zip)
*	Unzip the downloaded file `geotriples-<version>-bin.zip`
*	Change directory to `geotriples-<version>-bin`
*	Under the `bin` directory you can find the available starter script for GeoTriples

**Generate Mapping files:**

    bin/geotriples-all generate_mapping -o <output_file (.ttl)> -b <URI base> <input file>

**Transform file into RDF**
    
    bin/geotriples-all dump_rdf -o <output_file> -b http://example.com (-sh <shp file>) <path_to_the_mapping_file (.ttl)>

	
See more at Wiki pages

## Execution by source

Clone this repository and install the source code by using
    
    mvn package 

**Generate Mapping files:**

    java -cp <geotriples-core/ dependencies jar> eu.linkedeodata.geotriples.GeoTriplesCMD generate_mapping -o <output file(.ttl)> -b <URI base> <input file>

* **-o output_file** the name of the produced mapping file (RML/R2RML)
* **-b URI_base** the base URI that will describe the entities
* use the option **-rml** to force the generation of an RML file

<br/>

**Transform file into RDF**

    java -cp <geotriples-core/ dependencies jar> eu.linkedeodata.geotriples.GeoTriplesCMD dump_rdf -o <output file> -b <URI base> (-sh <shp file>) <(produced) mapping file (.ttl)>
    
* **-o output_file** the path of the produced file
* **-b URI_base** the base URI that will describe the entities
* **-sh shp_file** if the input is a shapefile specify the .shp path using this flag 
* use the **-rml** option if the input mapping file is expected to be an RML file 



---

# GeoTriples-Spark

GeoTriples-Spark is an extended version of GeoTriples capable of transforming big geospatial data into RDF graphs.
To enable the transformation of big geospatial
data, we extended GeoTriples to run on top of Apache Spark and Hadoop or [Hops](https://github.com/hopshadoop/hops) (a new distribution of Apache Hadoop developed by KTH, RISE SICS, and Logical Clocks AB). GeoTriples-Spark can
run in a standalone machine or in a Hadoop based cluster, but it is more efficient when it runs on Hops as it is a write-intensive application. GeoTriples-Sparks supports the transformation
of CSV, GeoJSON and Shapefiles. You can examine the performance of GeoTriples-Spark in [ISWC-experiments](https://docs.google.com/spreadsheets/d/1kTQFSUhLVtBTo9zWv184jlVSsY2gCLyfLZ4-UKsvm7A/edit?usp=sharing)

### Requirements
* Java 8
* Maven 3
* Apache Spark 2.4.0 or greater
* Apache Hadoop 2.7.0 or Hops

### Build
    mvn package

### Execute
    spark-submit --class eu.linkedeodata.geotriples.GeoTriplesCMD <geotriples-core/ dependencies jar> spark -i <in_file> -o <out_folder> <rml>

* **-i input_file**: path to input dataset. You can enter multiple files, separated by ","

* **-o out_folder**: path to the folder where the results will be stored. In case the folder exists, a new folder inside it will be created.

* The **rml**  indicates to the RML mapping file, produced by the *generate_mapping* procedure of GeoTriples.

### Additional flags

* **-m mode**: set the transformation mode. It can be either `partition` or `row` (default mode). In the `partition` mode the RDF triples are written to the target file after the transformation of the whole partition. In the `row` mode, each record is transformed into RDF triples which are directly written to the target files. For small datasets the `partition` mode is faster, but we advise to use the `row` mode as it is more memory friendly.
 
* **-r partitions**: re-partition the input dataset. 
**WARNING** re-partitionig triggers data shuffling and therefore it can negative effects in the performance.

* **-sh folder_path**: Load multiple ESRI shapefiles, that exist in the `folder_path` (each one must be stored in a separate folder). For example the structure of the folder must look like:
    
        folder_path/shapefile1/shapefile1.(shp, dbf, shx, etc)
        folder_path/shapefile2/shapefile2.(shp, dbf, shx, etc)
        ...
    For each Shapefile, a different RDF dataset will be created. Furthermore, the RML mapping file must support all the input datasets.
     
* **-times n**: Load the input dataset "n" times.

* **help**: Print instrcuctions   
