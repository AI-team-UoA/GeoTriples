package eu.linkedeodata.geotriples.Converters;

import be.ugent.mmlab.rml.core.Config;
import be.ugent.mmlab.rml.core.NodeRMLPerformer;
import be.ugent.mmlab.rml.function.*;
import be.ugent.mmlab.rml.model.*;
import be.ugent.mmlab.rml.processor.concrete.RowProcessor;
import eu.linkedeodata.geotriples.utils.NTriplesAlternative;
import org.apache.spark.sql.Row;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;

import java.util.*;


/**
 * Perform the conversion of data into RDF  using RML.
 */
public class RML_Converter implements java.io.Serializable {

    private ArrayList<TriplesMap> mappingList;
    private List<List<PredicateObjectMap>> listPOM;
    private List<List<URI>> tm_predicates;
    private NTriplesAlternative rdfWriter;
    private RowProcessor processor;
    private NodeRMLPerformer performer;


    private URI func_equi = new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/equi");
    private URI func_asWKT = new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/asWKT");
    private URI func_hasSerialization = new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/hasSerialization");
    private URI func_asGML = new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/asGML");
    private URI func_isSimple = new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/isSimple");
    private URI func_isEmpty = new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/isEmpty");
    private URI func_is3D = new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/is3D");
    private URI func_spatialDimension = new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/spatialDimension");
    private URI func_dimension = new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/dimension");
    private URI func_coordinateDimension = new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/coordinateDimension");
    private URI func_area = new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/area");
    private URI func_length = new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/length");
    private URI func_centroidx = new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/centroidx");
    private URI func_centroidy = new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/centroidy");

    /**
     * Constructor.
     *
     * @param mapping_list: the TriplesMap as was parsed by the input RML file.
     * @param headers the headers of the expected rows.
     */
    public RML_Converter(ArrayList<TriplesMap> mapping_list, List<String> headers)  {
        registerFunctions();
        Config.EPSG_CODE = 4326;
        mappingList = mapping_list;

        processor = new RowProcessor();
        processor.setFields(headers);
        performer = new NodeRMLPerformer(processor);

        listPOM = new ArrayList<>();
        tm_predicates = new ArrayList<>();
        try {
            for (TriplesMap tm : mappingList) {
                processor.createSubjectsTemplate(tm);
                List<PredicateObjectMap> list_pom = new ArrayList<>(tm.getPredicateObjectMaps());
                List<URI> predicates_list = new ArrayList<>();
                for (PredicateObjectMap pom : list_pom) {
                    for (PredicateMap pm : pom.getPredicateMaps())
                        predicates_list.add(processor.RowProcessPredicateMap(pm, null));
                    for(ObjectMap obj_map : pom.getObjectMaps())
                        processor.createObjectsTemplate(obj_map);
                }
                tm_predicates.add(predicates_list);
                listPOM.add(list_pom);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        rdfWriter = new NTriplesAlternative();
    }

    /**
     * Start RDF Writer
     */
    public void start() { rdfWriter.startRDF();}

    /**
     * Stop RDF Writer
     */
    public void stop() { rdfWriter.endRDF();}

    /**
     * Convert a Spark Partition into triples.
     *
     * @param partition_iter: an iterator of rows. The rows must follow the predefined headers.
     * @return a String of triples
     */
    public Iterator<String> convertPartition(Iterator<Row> partition_iter){

        List<String> partitionTriples = new ArrayList<>();
        partition_iter.forEachRemaining(row -> {
            try {
                for (int i = 0; i < mappingList.size(); i++) {
                    rdfWriter.handleStatementIter(performer.perform(row, mappingList.get(i), tm_predicates.get(i), listPOM.get(i), processor, i));
                }
                partitionTriples.add(rdfWriter.getString());
            }
            catch (RDFHandlerException e) {
                System.out.println("ERROR while Handling Statement");
                e.printStackTrace();
                System.exit(1);
            }
        });
        return  partitionTriples.iterator();
    }


    /**
     * Convert a Spark Row into triples.
     * @param row the input row.
     * @param functionHashMap a hash map containing all the functions needed for the conversion.
     * @return String of triples
     */
   public String convertRow(Row row, HashMap<URI, Function> functionHashMap){
        FunctionFactory.availableFunctions = functionHashMap;
        String triples = null;
        try {
            for (int i = 0; i < mappingList.size(); i++)
                rdfWriter.handleStatementIter(performer.perform(row, mappingList.get(i), tm_predicates.get(i), listPOM.get(i), processor, i));
            triples = rdfWriter.getString();
        }
        catch (RDFHandlerException e) {
           System.out.println("ERROR while Handling Statement");
           e.printStackTrace();
           System.exit(1);
       }
       return triples;
   }


    /**
     * Register functions.
     */
    public void registerFunctions()   {
        try {
            FunctionFactory.registerFunction(func_equi, new FunctionEQUI());
            FunctionFactory.registerFunction(func_asWKT, new FunctionAsWKT());
            FunctionFactory.registerFunction(func_hasSerialization, new FunctionHasSerialization());
            FunctionFactory.registerFunction(func_asGML, new FunctionAsGML());
            FunctionFactory.registerFunction(func_isSimple, new FunctionIsSimple());
            FunctionFactory.registerFunction(func_isEmpty, new FunctionIsEmpty());
            FunctionFactory.registerFunction(func_is3D, new FunctionIs3D());
            FunctionFactory.registerFunction(func_spatialDimension, new FunctionSpatialDimension());
            FunctionFactory.registerFunction(func_dimension, new FunctionDimension());
            FunctionFactory.registerFunction(func_coordinateDimension, new FunctionCoordinateDimension());
            FunctionFactory.registerFunction(func_area, new FunctionArea());
            FunctionFactory.registerFunction(func_length, new FunctionLength());
            FunctionFactory.registerFunction(func_centroidx, new FunctionCentroidX());
            FunctionFactory.registerFunction(func_centroidy, new FunctionCentroidY());

        } catch (FunctionAlreadyExists ignored) {  }
    }
}
