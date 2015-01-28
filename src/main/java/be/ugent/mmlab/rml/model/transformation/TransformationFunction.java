package be.ugent.mmlab.rml.model.transformation;

/**
 * 
 * This interface offers a method for replacing a template with the expression it holds
 *
 * @author dimis
 */
public interface TransformationFunction {
    
    /** Made a replaceAll on the input String to replace all occurrence of
     * the "{parameter}" in.
     * @param input The input String
     * @return
     */
    public String replaceAll(String input, String replaceValue);
    
}
