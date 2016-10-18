package be.ugent.mmlab.rml.core;

import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.RMLProcessor;
import be.ugent.mmlab.rml.processor.RMLProcessorFactory;
import be.ugent.mmlab.rml.processor.concrete.ConcreteRMLProcessorFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 *
 * @author andimou
 */
public class SimpleReferencePerformer extends NodeRMLPerformer {
    
    private static Logger log = LoggerFactory.getLogger(NodeRMLPerformer.class);
    private Resource subject;
    private URI predicate;
    
    public SimpleReferencePerformer(RMLProcessor processor, Resource subject, URI predicate) {
        super(processor);
        this.subject = subject;
        this.predicate = predicate;
    }
    
    @Override
    public Collection<Statement> perform(Object node, SesameDataSet dataset, TriplesMap map) {
        List<Statement> statements=new LinkedList<>();
        ValueFactory myFactory = ValueFactoryImpl.getInstance();
        
        if(map.getSubjectMap().getTermType() == be.ugent.mmlab.rml.model.TermType.BLANK_NODE || map.getSubjectMap().getTermType() == be.ugent.mmlab.rml.model.TermType.IRI){
            RMLProcessorFactory factory = new ConcreteRMLProcessorFactory();
            RMLProcessor subprocessor = factory.create(map.getLogicalSource().getReferenceFormulation());
            RMLPerformer performer = new NodeRMLPerformer(subprocessor);
            Resource object = processor.processSubjectMap(dataset, map.getSubjectMap(), node); 
            
            Statement st = myFactory.createStatement((Resource) subject, predicate,
    				(Value) object);
            //dataset.add(subject, predicate, object);
            dataset.addStatement(st);
            
            statements.add(st);
            //dataset.add(subject, predicate, object);
            
            
            log.debug("[SimpleReferencePerformer:addTriples] Subject "
                        + subject + " Predicate " + predicate + "Object " + object.toString());
            if((map.getLogicalSource().getReferenceFormulation().toString().equals("CSV")) || (map.getLogicalSource().getReference().equals(map.getLogicalSource().getReference())))
                statements.addAll(performer.perform(node, dataset, map, object));
            else {
                int end = map.getLogicalSource().getReference().length();
                log.info("[RML:SimpleReferencePerformer] " + map.getLogicalSource().getReference().toString());
                String expression = "";
                switch (map.getLogicalSource().getReferenceFormulation().toString()) {
                    case "XPath":
                        expression = map.getLogicalSource().getReference().toString().substring(end);
                        break;
                    case "JSONPath":
                        expression = map.getLogicalSource().getReference().toString().substring(end + 1);
                        break;
                }
                statements.addAll(processor.execute_node(dataset, expression, map, performer, node, object));
            }
        }
        else{
            List<Object> values = processor.processTermMap(map.getSubjectMap(), node , map, subject, predicate,dataset,false);    
            for(Object value : values){
                Resource object = new URIImpl(value.toString());

                Statement st = myFactory.createStatement((Resource) subject, predicate,
        				(Value) object);
                //dataset.add(subject, predicate, object);
                dataset.addStatement(st);
                
                statements.add(st);
                
                //dataset.add(subject, predicate, object);
                log.debug("[SimpleReferencePerformer:addTriples] Subject "
                        + subject + " Predicate " + predicate + "Object " + object.toString());
            }   
        }
        return statements;
    }
}
