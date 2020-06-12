package be.ugent.mmlab.rml.processor.concrete;

import be.ugent.mmlab.rml.core.*;
import be.ugent.mmlab.rml.function.Function;
import be.ugent.mmlab.rml.function.FunctionFactory;
import be.ugent.mmlab.rml.model.*;
import be.ugent.mmlab.rml.model.reference.ReferenceIdentifierImpl;
import be.ugent.mmlab.rml.processor.AbstractRMLProcessor;
import be.ugent.mmlab.rml.processor.RMLProcessor;
import be.ugent.mmlab.rml.processor.RMLProcessorFactory;
import be.ugent.mmlab.rml.vocabulary.Vocab;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.tools.R2RMLToolkit;
import org.openrdf.model.*;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.spark.sql.Row;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import static be.ugent.mmlab.rml.model.TermMap.TermMapType.TEMPLATE_VALUED;

/**
 * RowProcessor is designed to convert Spark Rows into RDF triples.
 */
public  class RowProcessor extends AbstractRMLProcessor {

    private Set<String> headers;
    private ValueFactory factory;

    private List<String> subj_templates;
    private List<List<String>> subj_tokens;
    private List<List<String>> subj_replacements_pos;
    private List<String> obj_templates;
    private List<List<String>> obj_tokens;
    private List<List<String>> obj_replacements_pos;
    private Map<String, Integer> obj_templatesMap;
    private Map<String, String> config_replacements;

    private RMLProcessorFactory rml_factory ;

    /**
     * Getters
     */

    public Set<String> getHeaders() { return headers; }

    public List<String> getSubj_templates() {
        return subj_templates;
    }

    public List<List<String>> getSubj_tokens() {
        return subj_tokens;
    }

    public List<List<String>> getSubj_replacements_pos() {
        return subj_replacements_pos;
    }

    public List<String> getObj_templates() {
        return obj_templates;
    }

    public List<List<String>> getObj_tokens() {
        return obj_tokens;
    }

    public List<List<String>> getObj_replacements_pos() {
        return obj_replacements_pos;
    }

    public Map<String, Integer> getObj_templatesMap() {
        return obj_templatesMap;
    }

    /**
     * Initialize the fields of the input Rows.
     *
     * @param fields fields of the Spark DataFrame.
     */
    public void setFields(List<String> fields){
        headers = new HashSet<>(fields);
    }


    /**
     * Constructor
     */
    public RowProcessor(){
        super();
        config_replacements = Config.variables;
        subj_templates = new ArrayList<>();
        subj_tokens = new ArrayList<>();
        subj_replacements_pos = new ArrayList<>();

        obj_templates = new ArrayList<>();
        obj_tokens = new ArrayList<>();
        obj_templatesMap = new HashMap<>();
        obj_replacements_pos = new ArrayList<>();

        factory = ValueFactoryImpl.getInstance();
        rml_factory = new ConcreteRMLProcessorFactory();
    }


    /**
     * Construct a RowProcessor by initializing its fields with pre-calculated variables.
     * It is used in order to create a copy of an existing RowProcessor.
     *
     * @param in_headers input headers
     * @param subjTemplates input subject  templates
     * @param subjTokens input subject tokens
     * @param subjReplacements_pos input sublect replacements
     * @param objTemplates input object templates
     * @param objTokens input object tokens
     * @param objReplacements_pos input object replacements
     * @param objTemplatesMap input object templates map
     */
    public RowProcessor(Set<String> in_headers, List<String> subjTemplates, List<List<String>> subjTokens
                        ,List<List<String>> subjReplacements_pos, List<String> objTemplates, List<List<String>> objTokens
                        ,List<List<String>> objReplacements_pos,  Map<String, Integer> objTemplatesMap){
        super();
        config_replacements = Config.variables;
        factory = ValueFactoryImpl.getInstance();
        rml_factory = new ConcreteRMLProcessorFactory();

        headers = in_headers;

        subj_templates = subjTemplates;
        subj_tokens = subjTokens;
        subj_replacements_pos = subjReplacements_pos;

        obj_templates = objTemplates;
        obj_tokens = objTokens;
        obj_templatesMap = objTemplatesMap;
        obj_replacements_pos = objReplacements_pos;
    }


    /**
     * @param row the input row.
     * @param expression the field we seek inside the Row.
     * @return the value of the field in the Row.
     */
    public Object extractValueFromNode(Row row, String expression) {

        // call the right header in the row
        if (headers.contains(expression)) {
            return row.getAs(expression);
        }
        else {
            String lc_expression = expression.toLowerCase();
            if ( (lc_expression.contains("geom") || lc_expression.contains("wkt")) && headers.contains("geometry"))
                return row.getAs("geometry");
        }
        return null;
    }




    /**
     * Create the templates of the subjects, and find the positions of the replacements
     * of their tokens. They are calculated only once.
     *
     * @param tm the examined TripleMap.
     * @throws Exception
     */
    public void createSubjectsTemplate(TriplesMap tm) throws Exception {
        SubjectMap subj_map =  tm.getSubjectMap();
        if (subj_map.getTermMapType() == TEMPLATE_VALUED) {

            String template = subj_map.getStringTemplate().trim();
            if (template.contains("[")) template = template.replaceAll("\\[", "").replaceAll("\\]", "");
            if (template.contains("$")) template = template.replaceAll("\\$", "");

            List<String> tokens = new ArrayList<>(R2RMLToolkit.extractColumnNamesFromStringTemplate(template));

            List<String> replacements = new ArrayList<>();
            for (String expression : tokens) {

                if (expression.contains("[")) expression = expression.replaceAll("\\[", "").replaceAll("\\]", "");
                if (expression.contains("$")) expression = expression.replaceAll("\\$", "");

                if (config_replacements.containsKey(expression) || headers.contains(expression))
                    replacements.add(expression);
                else
                    throw new Exception("No replacement was found for expression: " + expression);
            }
            subj_templates.add(template);
            subj_tokens.add(tokens);
            subj_replacements_pos.add(replacements);
        }
        else   throw new Exception("Term type of Subject was not template based");
    }


    /**
     * Create the templates of the objects, and find the positions of the replacements
     * of their tokens. They are calculated only once.
     *
     * @param obj_map the examined object mapping.
     * @throws Exception
     */
    public void createObjectsTemplate(ObjectMap obj_map) throws Exception {

        if (obj_map.getTermMapType() == TEMPLATE_VALUED) {

            String template = obj_map.getStringTemplate().trim();
            if (template.contains("[")) template = template.replaceAll("\\[", "").replaceAll("\\]", "");
            if (template.contains("$")) template = template.replaceAll("\\$", "");

            List<String> tokens = new ArrayList<>(R2RMLToolkit.extractColumnNamesFromStringTemplate(template));

            List<String> replacements = new ArrayList<>();
            for (String expression : tokens) {

                if (expression.contains("[")) expression = expression.replaceAll("\\[", "").replaceAll("\\]", "");
                if (expression.contains("$")) expression = expression.replaceAll("\\$", "");

                if (config_replacements.containsKey(expression) || headers.contains(expression))
                    replacements.add(expression);
                else
                    throw new Exception("No replacement was found for expression: " + expression);
            }
            obj_templatesMap.put(obj_map.getStringTemplate(), obj_templates.size());
            obj_templates.add(template);
            obj_tokens.add(tokens);
            obj_replacements_pos.add(replacements);
        }
    }


    /**
     * Using the pre-calculated templates, construct the subject of the input Row.
     *
     * @param row input row.
     * @param index indicates the examined TripleMap.
     * @return the constructed subject.
     */
    public Resource createSubject(Row row, int index){

        Resource subject = null;

        String template = subj_templates.get(index);
        List<String> tokens = subj_tokens.get(index);
        List<String> replacements_pos = subj_replacements_pos.get(index);

        for(String expression : tokens){

            String quote = Pattern.quote(expression);

            for(String pos : replacements_pos) {
                String replacement;
                if (headers.contains(pos))
                    replacement = row.getAs(pos).toString();
                else
                    replacement = config_replacements.get(pos);
                replacement = replacement.trim();
                if (replacement.equals("")) continue;
                template = template.replaceAll("\\{" + quote + "\\}",
                        Matcher.quoteReplacement(replacement));
            }
        }

        if (template != null && !template.equals("")) {
            if (template.startsWith("www."))
                template = "http://" + template;
            subject = factory.createURI(template);
        }
        return subject;
    }


    /**
     * Construct predicate by the predicate map.
     *
     * @param predicateMap input predicate map.
     * @param node examined node.
     * @return the uri of the extracted predicate.
     * 
     */
    public URI RowProcessPredicateMap(PredicateMap predicateMap, Object node) {
        // Get the value
        List<Object> values = processTermMap(predicateMap, node, null, null,
                null, null, false);

        for (Object value : values) {
            if (value.toString().startsWith("www."))
                value = "http://" + value;
            return new URIImpl(value.toString());
        }
        return null;
    }


    /**
     * Generate Object according to the input object Map.
     *
     * @param objectMap the input object Map.
     * @param row the input Row.
     * @return the value of the Object.
     */
    public Value processObjectMap(ObjectMap objectMap, Row row, TriplesMap triplesMap, Resource subject, URI predicate,
                                        int index) {

        // Warning: A Term map returns one or more values (in case expression matche more)
        Object value = processTermMap(objectMap, row, triplesMap, subject, predicate, false, index);
        Value result = null;
        switch (objectMap.getTermType()) {
            case IRI:
                if (value != null && !value.equals("")) {
                    if (value.toString().startsWith("www."))
                        value = "http://" + value;
                    result = factory.createURI(value.toString());
                }
                break;
            case BLANK_NODE:
                result = factory.createBNode(value.toString());
                break;
            case LITERAL:
                if (objectMap.getLanguageTag() != null && !value.equals("")) {
                    result = factory.createLiteral(value.toString(), objectMap.getLanguageTag());

                } else if (value != null && !value.equals("") && objectMap.getDataType() != null) {
                    result = factory.createLiteral(value.toString(), objectMap.getDataType());

                } else if (value != null && !value.equals("")) {
                    result = factory.createLiteral(value.toString().trim());
                }
        }
        return result;
    }

    /**
     * Process any Term Map
     *
     * @param map current term map
     * @param row current node in iteration
     * @return the resulting value
     */
    public Object processTermMap(TermMap map, Row row, TriplesMap triplesMap, Resource subject, URI predicate,
                                 boolean ignoreOwnerBecauseWeAreInJoinInFirstLevel, int index) {
        Object value;
        // extra addition
        TriplesMap tm = map.getTriplesMap();

        RMLProcessor processor = null;
        String fileName = null;
        if (tm != null && !ignoreOwnerBecauseWeAreInJoinInFirstLevel
                && performersForFunctionInsideJoinCondition.size() == 0) {
            // Create the processor based on the owner triples map
            Vocab.QLTerm queryLanguage = tm.getLogicalSource()
                    .getReferenceFormulation();

            File file = new File(tm.getLogicalSource().getIdentifier());
            if (RMLEngine.getSourceProperties())
                fileName = RMLEngine.getFileMap().getProperty(file.toString());
            else if (!file.exists())
                fileName = getClass().getResource(
                        tm.getLogicalSource().getIdentifier()).getFile();
            else
                fileName = tm.getLogicalSource().getIdentifier();

            processor = rml_factory.create(queryLanguage);
        }
        switch (map.getTermMapType()) {
            case REFERENCE_VALUED:
                // Get the expression and extract the value
                ReferenceIdentifierImpl identifier = (ReferenceIdentifierImpl) map
                        .getReferenceValue();
                if (tm == null || (ignoreOwnerBecauseWeAreInJoinInFirstLevel)) {
                    return extractValueFromNode(row, identifier.toString().trim());
                } else {
                    // we are in join, and processing a function argument!
                    if (performersForFunctionInsideJoinCondition.size() > 0) {

                        return performersForFunctionInsideJoinCondition.get(tm)
                                .perform(identifier.toString().trim());
                    } else {
                        RMLPerformer performer = new JoinReferenceRMLPerformer(
                                processor, subject, predicate, identifier
                                .toString().trim());
                        processor.execute(null, tm, performer, fileName,false);
                        return new ArrayList<>();
                    }
                }
            case CONSTANT_VALUED:
                // Extract the value directly from the mapping
                value = map.getConstantValue().stringValue().trim();
                return value;

            case TEMPLATE_VALUED:

                Resource object = null;
                index = obj_templatesMap.get(map.getStringTemplate());
                String template = obj_templates.get(index);
                List<String> tokens = obj_tokens.get(index);
                List<String> replacements_pos = obj_replacements_pos.get(index);

                for(String expression : tokens){

                    String quote = Pattern.quote(expression);

                    for(String pos : replacements_pos) {
                        String replacement;
                        if (headers.contains(pos))
                            replacement = row.getAs(pos).toString();
                        else
                            replacement = config_replacements.get(pos);

                        replacement = replacement.replace("\"", "").trim();
                        if (replacement.equals("")) continue;
                        template = template.replaceAll("\\{" + quote + "\\}",
                                Matcher.quoteReplacement(replacement));
                    }
                }

                if (template != null && !template.equals("")) {
                        if (map.getTermType().equals(TermType.LITERAL))
                            return template;;

                    if (template.startsWith("www."))
                        template = "http://" + template;
                    object = factory.createURI(template);
                }
                return object;

            case TRANSFORMATION_VALUED:
                // Extract the value directly from the mapping
                try {
                    Function function ;
                    List<TermMap> term_arguments = map.getArgumentMap();
                    if (term_arguments.size() == 1){
                        Object argument = processTermMap(term_arguments.get(0), row, triplesMap,
                                subject, predicate, false, index);

                        function = FunctionFactory.get(map.getFunction());
                        value = function.execute(argument, Vocab.QLTerm.ROW_CLASS);
                        return value;
                    }
                    else{
                        List<Object> argumentsString = new ArrayList<Object>();
                        for (TermMap argument : map.getArgumentMap()) {
                            Object temp = processTermMap(argument, row, triplesMap,
                                    subject, predicate, false, index);
                            argumentsString.add(temp);
                        }
                        function = FunctionFactory.get(map.getFunction());
                        List<Vocab.QLTerm> argumentsQLTerms = new ArrayList<>();
                        argumentsQLTerms.add(Vocab.QLTerm.ROW_CLASS);
                        value = function.execute(argumentsString, argumentsQLTerms).get(0);
                        return value;
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                    System.exit(13);
                }
            default:
                return null;
        }
    }


    /**
     * Calculate the RDF Statements of the input row.
     *
     * @param subject the subject of the Statements, which represents the input Row.
     * @param pom_list list containing all the predicate-object maps
     * @param row input Spark Row.
     * @param map examined TripleMap.
     * @param list_predicates list containing all the predicates.
     * @param index index that indicates the examined TripleMap.
     * @return The Statements produced by the Row.
     */
    public Collection<Statement> processPredicateObjectMap(Resource subject, List<PredicateObjectMap> pom_list,
                                                           Row row, TriplesMap map, List<URI> list_predicates, int index) {
        List<Statement> statements = new LinkedList<>();
        ValueFactory myFactory = ValueFactoryImpl.getInstance();

        int predicate_index = 0;

        for (PredicateObjectMap pom : pom_list) {
            // Get predicate
            URI predicate = list_predicates.get(predicate_index);
            predicate_index++;

            //WARNING: I've deleted the join processes

            // process the objectmaps
            Set<ObjectMap> objectMaps = pom.getObjectMaps();
            for (ObjectMap objectMap : objectMaps) {
                Value object = processObjectMap(objectMap, row,
                        map, subject, predicate, index);
                if (object == null) continue;
                if (object.stringValue() != null || !object.stringValue().equals("")) {
                    Statement st = myFactory.createStatement((Resource) subject, predicate, (Value) object);
                    statements.add(st);
                }
            }
        }
        return statements;
    }

    @Override
    public List<Object> extractValueFromNode(Object node, String expression) {return null;}

    @Override
    public Collection<Statement> execute(SesameDataSet dataset, TriplesMap parentTriplesMap,
                                              RMLPerformer performer, String file, Boolean flag){
        return null;
    }

    @Override
    public Collection<Statement> execute_node(SesameDataSet dataset, String expression, TriplesMap parentTriplesMap,
                                              RMLPerformer performer, Object node, Resource subject) {
        throw new UnsupportedOperationException("[execute_node] Not applicable for Shapefile sources."); // To

    }

    @Override
    public Collection<Statement> execute_node_fromdependency(SesameDataSet dataset, String expression, TriplesMap map,
                                                             RMLPerformer performer, Object node) {

        return performer.perform(node, dataset, map);
    }


    @Override
    public List<Object> processTermMap(TermMap map, TriplesMap triplesMap, Resource subject, URI predicate, SesameDataSet dataset, boolean ignoreOwnerBecauseWeAreInJoin) {
        return null;
    }

    @Override
    public Resource processSubjectMap(SesameDataSet dataset, SubjectMap subjectMap) {
        return null;
    }

    @Override
    public Object getCurrentNode() {
        return null;
    }

    @Override
    public TriplesMap getCurrentTriplesMap() {
        return null;
    }

    @Override
    public Vocab.QLTerm getFormulation() {
        return Vocab.QLTerm.ROW_CLASS;
    }


}
