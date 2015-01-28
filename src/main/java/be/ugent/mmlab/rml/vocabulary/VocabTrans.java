
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

    public enum R2RMLTerm {
        // CLASSES

        GRAPH_MAP_CLASS("GraphMap"),
        JOIN_CLASS("Join"),
        LOGICAL_TABLE_CLASS("LogicalTable"),
        OBJECT_MAP_CLASS("ObjectMap"),
        PREDICATE_MAP_CLASS("PredicateMap"),
        PREDICATE_OBJECT_MAP_CLASS("PredicateObjectMap"),
        REF_OBJECT_MAP_CLASS("RefObjectMap"),
        SUBJECT_MAP_CLASS("SubjectMap"),
        TRIPLES_MAP_CLASS("TriplesMap"),
        // PROPERTIES
        CLASS("class"),
        CHILD("child"),
        COLUMN("column"),
        DATATYPE("datatype"),
        CONSTANT("constant"),
        GRAPH("graph"),
        GRAPH_MAP("graphMap"),
        GRAPH_COLUMN("graphColumn"),
        GRAPH_TEMPLATE("graphTemplate"),
        INVERSE_EXPRESSION("inverseExpression"),
        JOIN_CONDITION("joinCondition"),
        LANGUAGE("language"),
        LOGICAL_TABLE("logicalTable"),
        OBJECT("object"),
        OBJECT_MAP("objectMap"),
        PARENT("parent"),
        PARENT_TRIPLES_MAP("parentTriplesMap"),
        PREDICATE("predicate"),
        PREDICATE_MAP("predicateMap"),
        PREDICATE_OBJECT_MAP("predicateObjectMap"),
        SQL_QUERY("sqlQuery"),
        SQL_VERSION("sqlVersion"),
        SUBJECT("subject"),
        SUBJECT_MAP("subjectMap"),
        TABLE_NAME("tableName"),
        TEMPLATE("template"),
        TERM_TYPE("termType"),
        // SPECIAL
        DEFAULT_GRAPH("defaultGraph"),
        IRI("IRI"),
        BLANK_NODE("BlankNode"),
        LITERAL("Literal"),
        //FROM ANOTHER ONTOLOGY
        TYPE("type")
        ,
        
        
        TRANSFORMATION("transformation"),
        FUNCTION("function"),
        ARGUMENTMAP("argumentMap")
        ;
        private String displayName;

        private R2RMLTerm(String displayName) {
            this.displayName = displayName;
        }

        public String toString() {
            return displayName;
        }
        
    }
}
