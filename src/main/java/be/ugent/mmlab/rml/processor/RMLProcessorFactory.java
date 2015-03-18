
package be.ugent.mmlab.rml.processor;

import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

/**
 * Interface for creating processors
 * @author mielvandersande
 */
public interface RMLProcessorFactory {
    
    public  RMLProcessor create(QLTerm term);
    
}
