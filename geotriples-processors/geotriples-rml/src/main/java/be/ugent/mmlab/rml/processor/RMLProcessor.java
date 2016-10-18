package be.ugent.mmlab.rml.processor;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import be.ugent.mmlab.rml.core.RMLPerformer;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.SubjectMap;
import be.ugent.mmlab.rml.model.TermMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;



/**
 * Interface for processing a certain term map
 * @author mielvandersande, andimou
 */
public interface RMLProcessor {
    
    /**
     * Iterate a list of nodes (objects, elements, rows) from the source and call the performer to handle the triplemap
     * @param dataset the ouput rdf dataset
     * @param map the triplemap
     * @param performer the performer handling the action done on the triplemap
     */
    public Collection<Statement> execute(SesameDataSet dataset, TriplesMap map, RMLPerformer performer, String filename,Boolean RETURN_ALL_STATEMENTS);
    public boolean isInMemoryInput();
    void setInMemoryInput(boolean memory);
    String getMemoryInput();
    void setMemoryInput(String input);
    
    public Collection<Statement> execute_node(SesameDataSet dataset, String expression, TriplesMap parentTriplesMap, RMLPerformer performer, Object node, Resource subject);
    public Collection<Statement> execute_node_fromdependency(SesameDataSet dataset, String expression,TriplesMap map, RMLPerformer performer, Object node);
    /**
     * Resolve an expression and extract a single string value from a node
     * @param node current object
     * @param expression reference to value
     * @return extracted value
     */
    public List<Object> extractValueFromNode(Object node, String expression); //changed return value as List<Object> instead of List<String>
    /**
     * process a subject map
     * @param dataset
     * @param subjectMap
     * @param node
     * @return 
     */
    public Resource processSubjectMap(SesameDataSet dataset, SubjectMap subjectMap, Object node) ;
    
    public Collection<Statement> processSubjectTypeMap(SesameDataSet dataset, Resource subject, SubjectMap subjectMap, Object node);
    /**
     * process a predicate object map
     * @param dataset
     * @param subject the subject created by the subject map
     * @param pom the predicate object map
     * @param node 
     */
    public Collection<Statement> processPredicateObjectMap(SesameDataSet dataset, Resource subject, PredicateObjectMap pom, Object node, TriplesMap map);

    /**
     *
     * @param map
     * @param node
     * @return
     */
    public List<Object> processTermMap(TermMap map, Object node, TriplesMap triplesMap, Resource subject, URI predicate ,SesameDataSet dataset,boolean ignoreOwnerBecauseWeAreInJoin) ; //extra addition the argument TriplesMap triplesMap
    public List<Object> processTermMap(TermMap map, TriplesMap triplesMap, Resource subject, URI predicate ,SesameDataSet dataset,boolean ignoreOwnerBecauseWeAreInJoin) ; //extra addition the argument TriplesMap triplesMap
    
    public QLTerm getFormulation(); //dd

	public Resource processSubjectMap(SesameDataSet dataset,
			SubjectMap subjectMap); //if we want to execute in other processor with current node
	
	public void setDependencyTriplesMap(TriplesMap dependencyTriplesMap);

	public void setDependencyProcessor(RMLProcessor dependencyProcessor);

	Object getCurrentNode();

	public TriplesMap getCurrentTriplesMap();
	
}
