package be.ugent.mmlab.rml.processor.concrete;
/***************************************************************************
*
* @author: dimis (dimis@di.uoa.gr)
* 
****************************************************************************/
import be.ugent.mmlab.rml.processor.RMLProcessor;
import be.ugent.mmlab.rml.processor.RMLProcessorFactory;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

/**
 * This factory class creates language-dependent processors processors
 *
 * @author mielvandersande
 */
public class ConcreteRMLProcessorFactoryTrans implements RMLProcessorFactory{

    /**
     * Create the language-dependent processor based on the given language
     * @param term Expression language
     * @return processor able to process the specified language
     */
    @Override
    public RMLProcessor create(QLTerm term) {
        switch (term){
            case XPATH_CLASS:
                return new XPathProcessorTrans();
            case CSV_CLASS:
                return new CSVProcessorTrans();
            case JSONPATH_CLASS:
                return new JSONPathProcessorTrans();
        }
        return null;
    }
    
}
