package be.ugent.mmlab.rml.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jlibs.xml.sax.dog.NodeItem;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import nu.xom.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

import be.ugent.mmlab.rml.model.GraphMap;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.RMLProcessor;

/**
 * Performs the normal handling of an object in the iteration.
 * 
 * @author mielvandersande
 */
public class NodeRMLPerformer implements RMLPerformer{
    
    private static Logger log = LoggerFactory.getLogger(RMLMappingFactory.class);
    
    protected RMLProcessor processor;

    private Object current_node;
    
    /**
     * 
     * @param processor the instance processing these nodes
     */
    public NodeRMLPerformer(RMLProcessor processor) {
        this.processor = processor;
    }

    /**
     * Process the subject map and predicate-object maps
     * 
     * @param node current object in the iteration
     * @param dataset dataset for endresult
     * @param map current triple map that is being processed
     */
    @Override
    public Collection<Statement> perform(Object node, SesameDataSet dataset, TriplesMap map) {
    	List<Statement> statements=new LinkedList<>();
    	
        Resource subject = processor.processSubjectMap(dataset, map.getSubjectMap(), node);
        processor.processSubjectTypeMap(dataset, subject, map.getSubjectMap(), node);
        if (subject == null){
//            System.out.println();
//            System.out.println(map.getSubjectMap().getStringTemplate());
//            System.out.println(((Node)((NodeItem)node).xml).toXML());
//            System.out.println("OPA");
            return statements;
        }
        Set<GraphMap> graph = map.getSubjectMap().getGraphMaps();

        for (PredicateObjectMap pom : map.getPredicateObjectMaps()) {
            current_node=node;
            statements.addAll(processor.processPredicateObjectMap(dataset, subject, pom, node, map));
        }
        return statements;
    }
    
    /**
     *
     * @param node
     * @param dataset
     * @param map
     * @param subject
     */
    @Override
    public Collection<Statement> perform(Object node, SesameDataSet dataset, TriplesMap map, Resource subject) {
    	List<Statement> statements=new LinkedList<>();
    	
    	statements.addAll(processor.processSubjectTypeMap(dataset, subject, map.getSubjectMap(), node));
        for (PredicateObjectMap pom : map.getPredicateObjectMaps()) 
            statements.addAll(processor.processPredicateObjectMap(dataset, subject, pom, node, map));
        
        return statements;
    }

    @Override
    public Object getCurrentNode() {
        return current_node;
    }
}
