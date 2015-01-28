package be.ugent.mmlab.rml.model.transformation;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLStructureException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLSyntaxException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.tools.R2RMLToolkit;
import be.ugent.mmlab.rml.model.AbstractTermMap;
import be.ugent.mmlab.rml.model.TermType;
import be.ugent.mmlab.rml.model.TermMap.TermMapType;
import be.ugent.mmlab.rml.model.reference.ReferenceIdentifier;
import be.ugent.mmlab.rml.model.reference.ReferenceIdentifierImpl;

/**
 * 
 * This class holds an expression that refers to a certain value.
 * It has lost most of its use, maybe replace it?
 *
 * @author dimis
 */
public class TransformationFunctionImpl implements TransformationFunction {
	// Log
    private static Log log = LogFactory.getLog(TransformationFunctionImpl.class);
    
		private Value constantValue;
	    private TermType termType;
	    private String stringTemplate;
		private ReferenceIdentifier referenceValue;
        private String transformation = null;
        private TransformationFunctionImpl(String transformation) {
                this.transformation = transformation;
        }

        @Override
        public String toString() {
                return transformation;
        }

		@Override
		public String replaceAll(String input, String replaceValue) {
			// TODO Auto-generated method stub
			return null;
		}
		private void setReferenceValue(ReferenceIdentifier referenceValue)
                throws InvalidR2RMLSyntaxException, InvalidR2RMLStructureException {
                // The value of the rml:reference property MUST be a valid reference for this queryLanguage.
//		if (columnValue != null)
//			checkColumnValue(columnValue);
                this.referenceValue = referenceValue;
        }
		public TermMapType getTermMapType() {
            if (constantValue != null) {
                    return TermMapType.CONSTANT_VALUED;
            } else if (referenceValue != null) {
                    return TermMapType.REFERENCE_VALUED;
            } else if (stringTemplate != null) {
                    return TermMapType.TEMPLATE_VALUED;
            } else if (termType == TermType.BLANK_NODE) {
                    return TermMapType.NO_VALUE_FOR_BNODE;
            }
            return null;
    }
		public TermType getTermType() {
            return termType;
    }
		public ReferenceIdentifier getReferenceValue() {
            return referenceValue;
    }

    public Set<ReferenceIdentifier> getReferencedSelectors() {
        Set<ReferenceIdentifier> referencedColumns = new HashSet<ReferenceIdentifier>();
        switch (getTermMapType()) {
                case CONSTANT_VALUED:
                        // The referenced columns of a constant-valued term map is the
                        // empty set.
                        break;

                case REFERENCE_VALUED:
                        // The referenced columns of a column-valued term map is
                        // the singleton set containing the value of rr:column.
                        // referencedColumns.add(R2RMLToolkit.deleteBackSlash(columnValue));
                        referencedColumns.add(referenceValue);
                        break;

                case TEMPLATE_VALUED:
                        // The referenced columns of a template-valued term map is
                        // the set of column names enclosed in unescaped curly braces
                        // in the template string.
                        for (String colName : R2RMLToolkit
                                .extractColumnNamesFromStringTemplate(stringTemplate)) {
                                referencedColumns.add(ReferenceIdentifierImpl.buildFromR2RMLConfigFile(colName));
                        }
                        break;

                default:
                        break;
        }
        log.debug("[AbstractTermMap:getReferencedColumns] ReferencedColumns are now : "
                + referencedColumns);
        return referencedColumns;
}
}
