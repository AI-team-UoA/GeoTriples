package be.ugent.mmlab.rml.core;

import java.util.List;

import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.RMLProcessor;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;

/**
 * Performer to do joins without any join conditions
 *
 * @author mielvandersande, andimou
 */
public class JoinReferenceRMLPerformer extends NodeRMLPerformer{
    
    private static Log log = LogFactory.getLog(RMLMappingFactory.class);
    private Resource subject;
    private URI predicate;
    private String expr;

    public JoinReferenceRMLPerformer(RMLProcessor processor, Resource subject, URI predicate, String expr) {
        super(processor);
        this.subject = subject;
        this.predicate = predicate;
        this.expr = expr;
    }

    /**
     * Compare expressions from join to complete it
     * 
     * @param node current object in parent iteration
     * @param dataset
     * @param map 
     */
    @Override
    public void perform(Object node, SesameDataSet dataset, TriplesMap map) {
        //Value object = processor.processSubjectMap(dataset, map.getSubjectMap(), node);
    	List<Object> objects = processor.extractValueFromNode(node, expr);
    			
        if (objects == null){
            return;
        }       
        log.debug("[JoinReferenceRMLPerformer:addTriples] Subject "
                    + subject + " Predicate " + predicate + "Object " + objects.toString());
        
        //add the join triple
        for(Object obj:objects){
        	dataset.add(subject, predicate, new LiteralImpl(obj.toString().trim()));
        }
    }


}
