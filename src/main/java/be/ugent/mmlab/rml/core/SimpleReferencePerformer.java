package be.ugent.mmlab.rml.core;

import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.RMLProcessor;
import be.ugent.mmlab.rml.processor.RMLProcessorFactory;
import be.ugent.mmlab.rml.processor.concrete.ConcreteRMLProcessorFactory;
import java.util.List;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 *
 * @author andimou
 */
public class SimpleReferencePerformer extends NodeRMLPerformer {
    
    private static Log log = LogFactory.getLog(NodeRMLPerformer.class);
    private Resource subject;
    private URI predicate;
    
    public SimpleReferencePerformer(RMLProcessor processor, Resource subject, URI predicate) {
        super(processor);
        this.subject = subject;
        this.predicate = predicate;
    }
    
    @Override
    public void perform(Object node, SesameDataSet dataset, TriplesMap map) {
        if(map.getSubjectMap().getTermType() == be.ugent.mmlab.rml.model.TermType.BLANK_NODE || map.getSubjectMap().getTermType() == be.ugent.mmlab.rml.model.TermType.IRI){
            RMLProcessorFactory factory = new ConcreteRMLProcessorFactory();
            RMLProcessor subprocessor = factory.create(map.getLogicalSource().getReferenceFormulation());
            RMLPerformer performer = new NodeRMLPerformer(subprocessor);
            Resource object = processor.processSubjectMap(dataset, map.getSubjectMap(), node); 
            dataset.add(subject, predicate, object);
            log.debug("[SimpleReferencePerformer:addTriples] Subject "
                        + subject + " Predicate " + predicate + "Object " + object.toString());
            if((map.getLogicalSource().getReferenceFormulation().toString().equals("CSV")) || (map.getLogicalSource().getReference().equals(map.getLogicalSource().getReference())))
                performer.perform(node, dataset, map, object);
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
                processor.execute_node(dataset, expression, map, performer, node, object);
            }
        }
        else{
            List<String> values = processor.processTermMap(map.getSubjectMap(), node);    
            for(String value : values){
                Resource object = new URIImpl(value);

                dataset.add(subject, predicate, object);
                log.debug("[SimpleReferencePerformer:addTriples] Subject "
                        + subject + " Predicate " + predicate + "Object " + object.toString());
            }   
        }    
    }
}
