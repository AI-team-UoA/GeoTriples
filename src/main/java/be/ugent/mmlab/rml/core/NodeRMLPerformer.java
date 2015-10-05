package be.ugent.mmlab.rml.core;

import java.util.Set;

import jlibs.xml.sax.dog.NodeItem;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import nu.xom.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Resource;

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
    
    private static Log log = LogFactory.getLog(RMLMappingFactory.class);
    
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
    public void perform(Object node, SesameDataSet dataset, TriplesMap map) {
        Resource subject = processor.processSubjectMap(dataset, map.getSubjectMap(), node);
        processor.processSubjectTypeMap(dataset, subject, map.getSubjectMap(), node);
        if (subject == null){
//            System.out.println();
//            System.out.println(map.getSubjectMap().getStringTemplate());
//            System.out.println(((Node)((NodeItem)node).xml).toXML());
//            System.out.println("OPA");
            return;
        }
        Set<GraphMap> graph = map.getSubjectMap().getGraphMaps();

        for (PredicateObjectMap pom : map.getPredicateObjectMaps()) {
            current_node=node;
            processor.processPredicateObjectMap(dataset, subject, pom, node, map);
        }
    }
    
    /**
     *
     * @param node
     * @param dataset
     * @param map
     * @param subject
     */
    @Override
    public void perform(Object node, SesameDataSet dataset, TriplesMap map, Resource subject) {
        processor.processSubjectTypeMap(dataset, subject, map.getSubjectMap(), node);
        for (PredicateObjectMap pom : map.getPredicateObjectMaps()) 
            processor.processPredicateObjectMap(dataset, subject, pom, node, map);
    }

    @Override
    public Object getCurrentNode() {
        return current_node;
    }
}
