/* 
 * Copyright 2011 Antidot opensource@antidot.net
 * https://github.com/antidot/db2triples
 * 
 * DB2Triples is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * DB2Triples is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * *************************************************************************
 *
 * R2RML Model : Abstract Term Map Trans
 *
 *
 * @author: dimis (dimis@di.uoa.gr)
 *
 ***************************************************************************
 */
package be.ugent.mmlab.rml.model;

import be.ugent.mmlab.rml.model.reference.ReferenceIdentifier;
import be.ugent.mmlab.rml.model.reference.ReferenceIdentifierImpl;
import be.ugent.mmlab.rml.tools.CustomRDFDataValidator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.antidot.semantic.rdf.model.tools.RDFDataValidator;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLStructureException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLSyntaxException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.R2RMLDataError;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.tools.R2RMLToolkit;
import net.antidot.semantic.xmls.xsd.XSDLexicalTransformation;
import net.antidot.semantic.xmls.xsd.XSDType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public abstract class AbstractTermMapTrans extends AbstractTermMap {

        // Log
        private static Log log = LogFactory.getLog(AbstractTermMapTrans.class);

        private URI function = null;
        public URI getFunction() {
			return function;
		}

		public void setFunction(URI function) {
			this.function = function;
		}

		public List<TermMap> getArgumentMap() {
			return argumentMap;
		}

		public void setArgumentMap(List<TermMap> argumentMap) {
			this.argumentMap = argumentMap;
		}
		private List<TermMap> argumentMap =null;
        protected AbstractTermMapTrans(Value constantValue, URI dataType,
                String languageTag, String stringTemplate, URI termType,
                String inverseExpression, ReferenceIdentifier referenceValue)
                throws R2RMLDataError, InvalidR2RMLStructureException,
                InvalidR2RMLSyntaxException {
        		super(constantValue, dataType, languageTag, stringTemplate, termType, inverseExpression, referenceValue);
        }
        
        protected AbstractTermMapTrans(Value constantValue, URI dataType,
                String languageTag, String stringTemplate, URI termType,
                String inverseExpression, ReferenceIdentifier referenceValue ,URI function,List<TermMap> argumentMap )
                throws R2RMLDataError, InvalidR2RMLStructureException,
                InvalidR2RMLSyntaxException {
    			super(constantValue, dataType, languageTag, stringTemplate, termType, inverseExpression, referenceValue);
                this.function=function;
                this.argumentMap=argumentMap;
                //checkGlobalConsistency();
        }
        @Override
        public TermMapType getTermMapType() {
        	if (constantValue != null) {
                return TermMapType.CONSTANT_VALUED;
        } else if (referenceValue != null) {
                return TermMapType.REFERENCE_VALUED;
        } else if (stringTemplate != null) {
                return TermMapType.TEMPLATE_VALUED;
        } else if (function != null)
        {
        	return TermMapType.TRANSFORMATION_VALUED;
        }
        
        else if (termType == TermType.BLANK_NODE) {
                return TermMapType.NO_VALUE_FOR_BNODE;
        } 
                
                return null;
        }

//	public String getValue(Map<ColumnIdentifier, byte[]> dbValues,
//			ResultSetMetaData dbTypes) throws R2RMLDataError, SQLException,
//			UnsupportedEncodingException {
//
//		log.debug("[AbstractTermMap:getValue] Extract value of termType with termMapType : "
//				+ getTermMapType());
//		switch (getTermMapType()) {
//		case CONSTANT_VALUED:
//			return constantValue.stringValue();
//
//		case REFERENCE_VALUED:
//			if (dbValues.keySet().isEmpty())
//				throw new IllegalStateException(
//						"[AbstractTermMap:getValue] impossible to extract from an empty database value set.");
//			byte[] bytesResult = dbValues.get(referenceValue);
//			/* Extract the SQLType in dbValues from the key which is
//			 * equals to "columnValue" */
//			SQLType sqlType = null;			
//			for(ColumnIdentifier colId : dbValues.keySet()) {
//			    if(colId.equals(referenceValue)) {
//				sqlType = colId.getSqlType();
//				break;
//			    }
//			}			    
//			// Apply cast to string to the SQL data value
//			String result;
//			if (sqlType != null) {
//				XSDType xsdType = SQLToXMLS.getEquivalentType(sqlType);
//				result = XSDLexicalTransformation.extractNaturalRDFFormFrom(
//						xsdType, bytesResult);
//			}
//			else
//			{
//			    result = new String(bytesResult, "UTF-8");
//			}
//			return result;
//
//		case TEMPLATE_VALUED:
//			if (dbValues.keySet().isEmpty())
//				throw new IllegalStateException(
//						"[AbstractTermMap:getValue] impossible to extract from an empty database value set.");
//			result = R2RMLToolkit.extractColumnValueFromStringTemplate(
//					stringTemplate, dbValues, dbTypes);
//			return result;
//
//		default:
//			return null;
//		}
//}
}
