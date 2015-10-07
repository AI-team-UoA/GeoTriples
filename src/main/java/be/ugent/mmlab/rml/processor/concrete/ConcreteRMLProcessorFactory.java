package be.ugent.mmlab.rml.processor.concrete;

import be.ugent.mmlab.rml.function.Config;
import be.ugent.mmlab.rml.processor.RMLProcessor;
import be.ugent.mmlab.rml.processor.RMLProcessorFactory;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

/**
 * This factory class creates language-dependent processors processors
 *
 * @author mielvandersande
 */
public class ConcreteRMLProcessorFactory implements RMLProcessorFactory{

    /**
     * Create the language-dependent processor based on the given language
     * @param term Expression language
     * @return processor able to process the specified language
     */
    @Override
    public RMLProcessor create(QLTerm term) {
        switch (term){
            case XPATH_CLASS:
                return new XPathProcessor();
            case CSV_CLASS:
                return new CSVProcessor();
            case JSONPATH_CLASS:
                return new JSONPathProcessor();
            case SHP_CLASS:
            	if(Config.useDGALLibrary)
            		return new ShapefileProcessorGDAL();
            	else
            		return new ShapefileProcessor();
            case SQL_CLASS:
            	return new DatabaseProcessorWithManyQueries();
                //return new DatabaseProcessor();
        }
        return null;
    }
    
}
