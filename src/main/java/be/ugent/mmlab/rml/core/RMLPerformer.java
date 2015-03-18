package be.ugent.mmlab.rml.core;

import be.ugent.mmlab.rml.model.TriplesMap;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import org.openrdf.model.Resource;

/**
 *
 * Interface for executing context-dependent operations like a regular object map, or a join
 * 
 * @author mielvandersande
 */
public interface RMLPerformer {
    /**
     * Perform the action
     * 
     * @param node current object in the iteration
     * @param dataset dataset for endresult
     * @param map current triple map that is being processed
     */
    public void perform(Object node, SesameDataSet dataset, TriplesMap map);
    
    public void perform(Object node, SesameDataSet dataset, TriplesMap map, Resource subject);
}
