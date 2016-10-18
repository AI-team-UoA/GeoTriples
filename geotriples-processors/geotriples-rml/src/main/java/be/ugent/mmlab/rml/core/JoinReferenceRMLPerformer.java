package be.ugent.mmlab.rml.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.RMLProcessor;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Performer to do joins without any join conditions
 *
 * @author mielvandersande, andimou
 */
public class JoinReferenceRMLPerformer extends NodeRMLPerformer{
    
    private static Logger log = LoggerFactory.getLogger(RMLMappingFactory.class);
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
    public Collection<Statement> perform(Object node, SesameDataSet dataset, TriplesMap map) {
    	List<Statement> statements=new LinkedList<>();
        //Value object = processor.processSubjectMap(dataset, map.getSubjectMap(), node);
    	List<Object> objects = processor.extractValueFromNode(node, expr);
    			
        if (objects == null){
            return statements;
        }       
        log.debug("[JoinReferenceRMLPerformer:addTriples] Subject "
                    + subject + " Predicate " + predicate + "Object " + objects.toString());
        
        ValueFactory myFactory = ValueFactoryImpl.getInstance();		
        
        //add the join triple
        for(Object obj:objects){
        	Statement st = myFactory.createStatement((Resource) subject, predicate,
    				(Value) new LiteralImpl(obj.toString().trim()));
            //dataset.add(subject, predicate, object);
            dataset.addStatement(st);
            
            statements.add(st);
            
        	//dataset.add(subject, predicate, new LiteralImpl(obj.toString().trim()));
        }
        return statements;
    }


}
