# GeoTriples
 Publishing geospatial data as Linked Open Geospatial Data 

## Quickstart ##
Assuming git, [Maven](http://maven.apache.org/download.cgi) and [Java](https://www.java.com/en/download/) installed:
```bash
$ git clone https://github.com/LinkedEOData/GeoTriples.git
$ cd GeoTriples
$ mvn initialize
$ mvn package
$ java -jar target/geotriples-1.0-SNAPSHOT-cmd.one-jar.jar [Options] [Argument]
```

### GeoTriples Modes ###
GeoTriples consists of two modules. The Mapping Generator which automatically produces an R2RML mapping file according to the input source, 
and the R2RML processor which processes an R2RML mapping and exports the RDF graph.

#### Automatic generation of R2RML mapping ####
- Relational Database
```bash
$ java -jar target/geotriples-1.0-SNAPSHOT-cmd.one-jar.jar generate_mapping [-u user] [-p password] [-d driver] [-b baseURI] [-o r2rmloutfile] [-r2rml] jdbcURL
```
- Shapefile
```bash
$ java -jar target/geotriples-1.0-SNAPSHOT-cmd.one-jar.jar generate_mapping [-b baseURI] [-o r2rmloutfile] fileURL
```

#### Transformation into RDF ####
- Relational Database
```bash
$ java -jar target/geotriples-1.0-SNAPSHOT-cmd.one-jar.jar dump_rdf [-f format] [-b baseURI] [-o rdfoutfile] -u user -p password -d driver -j jdbcURL inputmappingfile
```
- Shapefile
```bash
$ java -jar target/geotriples-1.0-SNAPSHOT-cmd.one-jar.jar dump_rdf [-f format] [-b baseURI] [-o rdfoutfile] -sh fileURL inputmappingfile
```

### GeoTriples Architecture ###
GeoTriples comprises two main
components: the mapping generator and the R2RML processor. The mapping
generator takes as input a data source and creates automatically an R2RML
mapping that transforms it into an RDF graph. The generated mapping is
enriched with subject and predicate-object maps, in order to take into account
the specifities of geospatial data and cater for all transformations that are
needed to produce an RDF graph that is compliant with the GeoSPARQL
vocabulary. To accomplish this task, we extend R2RML mappings to allow
the representation of a transformation function over input data. Afterwards,
the user may edit the generated R2RML mapping document to comply with
her requirements (e.g., use a different vocabulary).

![Architecture](http://drive.google.com/uc?export=view&id=0ByyHFR-5IXfpX3ZyNF9rMTgxcHc "The architecture of GeoTriples")

### RML Processor ###
GeoTriples now supports the [RML](http://rml.io/) mapping language by extending the [RML processor](https://github.com/mmlab/RMLProcessor) to support transformation functions.
[RML](http://rml.io/) is a mapping language, very similar to [R2RML](http://www.w3.org/TR/r2rml/). The main difference is that RML is designed to allow the process of data that *do not necessarily* rely in tables and thus not having an explicit iteration pattern.

For example, the books.xml (see below) cannot be iterated in a row by row fashion, because it has nested elements.
```xml
<Books>
    <Book>
        <Name>The Global Minotaur: America, the True Causes of the Financial Crisis and the Future of the World Economy</Name>
        <Pages>700</Pages>
        <Author id="1">
            <Name>Yanis</Name>
            <Surname>Varoufakis</Surname>
            <Address>
                <Street>Unknown</Street>
                <Country>Greece</Country>
            </Address>
        </Author>
    </Book>
</Books>
```

R2RML uses <code>rr:tableName</code> to define the table from the input file or the relational database as the source table for the mappings. RML has the equivalent rml:source to define the source file as the source for the mappings.
The iterator property <code>rml:iterator</code> defines the iterating pattern in order to process non-relational structured files. For the above example the iterator should be an XPath query.

<p>An example RML mapping can be the following</p>
```
<#Authors>
    rml:logicalSource [
        rml:source "books.xml";
        rml:referenceFormulation ql:XPath;
        rml:iterator "/Books/Book" ];

    rr:subjectMap [ 
        rr:template "http://example.com/Author/{@id}" ];

    rr:predicateObjectMap [ 
        rr:predicate ex:location;
        rr:objectMap [ 
        rml:reference "Pages" ] ].

    rr:predicateObjectMap [ 
        rr:predicate ex:location;
        rr:objectMap [ 
        rml:reference "Author/Name" ] ].
```

This mapping  uses an XPath iterator denoted by `rml:referenceFormulation` as the base iterator pattern for the processor. The `rml:reference` is used instead of `rr:column` R2RML's property . It extends the iterator in order to point at an element.

##Transformation of TalkingFields XML files ([Project LEO](http://linkedeodata.eu))##
You can transform any talkingfields xml file into RDF using the custom [mapping file](https://github.com/LinkedEOData/GeoTriples/blob/master/resources/rml/talkingfields-rml/example.rml.ttl) that we developed for the talkingfields project. In GeoTriple's command line interface you have to only use the -rml option to enable the RML processor.
This mapping can transform into RDF the talkingfield XML files that have been given to us and does not cover the complete ontology.

<p>A typical rml-execution is the following</p>
```bash
$ java -jar target/geotriples-1.0-SNAPSHOT-cmd.one-jar.jar dump_rdf -rml -o output.txt talkingfields.mapping.ttl
```

- Note that there is *no input file* as you might expect using the GeoTriples with the default R2RML processor, because RML mappings are self-contained, meaning that they read the input from the special property `rml:source`.
For example a mapping for the example-tf.xml talkingfields file should have triples maps starting with 
```
rml:logicalSource [
rml:source "example-tf.xml";
```
- It's better to use the full path of source file because relative paths are evaluated against the working direcotry; not the mapping file's directory.

### Test an example ###
Go to directory that contains the RML mapping and talkingfields XML files

```bash
$ cd resources/rml/talkingfields-rml/
```

Then invoke the RML processor with the RML mapping 

```bash
$ java -jar ../../../target/geotriples-1.0-SNAPSHOT-cmd.one-jar.jar dump_rdf -rml -o output.txt tf.rml.ttl
```

That's it! The RDF graph is in the output.txt file, in the same directory.










