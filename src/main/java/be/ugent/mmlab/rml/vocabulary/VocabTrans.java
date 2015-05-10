
/**
 * *************************************************************************
 *
 * RML, R2RML & QL Vocabulary
 *
 * The R2RML vocabulary is the set of IRIs defined in this specification that
 * start with the rr: namespace IRI: http://www.w3.org/ns/r2rml#
 *
 ***************************************************************************
 */
package be.ugent.mmlab.rml.vocabulary;
/***************************************************************************
*
* @author: dimis (dimis@di.uoa.gr)
* 
****************************************************************************/
public class VocabTrans extends Vocab{
    
    public static String RRX_NAMESPACE = "http://www.w3.org/ns/r2rml-ext#";
    public static String RRXF_NAMESPACE ="http://www.w3.org/ns/r2rml-ext/functions/def/";

    public enum RRXTerm {
        // CLASSES

        
        
        
        TRANSFORMATION("transformation"),
        FUNCTION("function"),
        ARGUMENTMAP("argumentMap"),
        TRIPLES_MAP("triplesMap");
        ;
        
        private String displayName;

        private RRXTerm(String displayName) {
            this.displayName = displayName;
        }

        public String toString() {
            return displayName;
        }
        
    }
}
