package be.ugent.mmlab.rml.core;

import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.RMLProcessor;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Performer to do joins without any join conditions
 *
 * @author mielvandersande, andimou
 */
public class JoinRMLPerformer extends NodeRMLPerformer{
    
    private static Logger log = LoggerFactory.getLogger(RMLMappingFactory.class);
    private Resource subject;
    private URI predicate;

    public JoinRMLPerformer(RMLProcessor processor, Resource subject, URI predicate) {
        super(processor);
        this.subject = subject;
        this.predicate = predicate;
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
    	
        Value object = processor.processSubjectMap(dataset, map.getSubjectMap(), node);

        if (object == null){
            return statements;
        }       
        log.debug("[JoinRMLPerformer:addTriples] Subject "
                    + subject + " Predicate " + predicate + "Object " + object.toString());
        
        //add the join triple
        ValueFactory myFactory = ValueFactoryImpl.getInstance();
		Statement st = myFactory.createStatement((Resource) subject, predicate,
				(Value) object);
        //dataset.add(subject, predicate, object);
        dataset.addStatement(st);
        
        statements.add(st);
        return statements;
    }


}
