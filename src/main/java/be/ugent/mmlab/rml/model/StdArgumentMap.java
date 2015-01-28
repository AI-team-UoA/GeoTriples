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
/***************************************************************************
 *
 * R2RML Model : ArgumentMap Class
 *
 * An argument map is a specific term map used for 
 * representing the argument of a trasformation function object. 
 * 
 * @author: dimis (dimis@di.uoa.gr)
 * 
 ****************************************************************************/
package be.ugent.mmlab.rml.model;

import be.ugent.mmlab.rml.model.reference.ReferenceIdentifier;
import be.ugent.mmlab.rml.model.transformation.TransformationFunction;

import java.util.HashSet;

import net.antidot.semantic.rdf.model.tools.RDFDataValidator;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLStructureException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLSyntaxException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.R2RMLDataError;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

public class StdArgumentMap extends AbstractTermMapTrans implements TermMap, ArgumentMap {

	private TransformationObjectMap transformationObjectMap;

	public StdArgumentMap(TransformationObjectMap transformationObjectMap,
			Value constantValue, URI dataType, String languageTag,
			String stringTemplate, URI termType, String inverseExpression,
			ReferenceIdentifier referenceValue) throws R2RMLDataError,
			InvalidR2RMLStructureException, InvalidR2RMLSyntaxException {
		super(constantValue, dataType, languageTag, stringTemplate, termType,
				inverseExpression, referenceValue,null);
		setTransformationObjectMap(transformationObjectMap);
	}

	protected void checkSpecificTermType(TermType tt)
			throws InvalidR2RMLStructureException {
		// If the term map is a subject map: rr:IRI or rr:BlankNode or
		// rr:Literal
		if ((tt != TermType.IRI) && (tt != TermType.BLANK_NODE)
				&& (tt != TermType.LITERAL)) {
			throw new InvalidR2RMLStructureException(
					"[StdObjectMap:checkSpecificTermType] If the term map is a "
							+ "object map: only rr:IRI or rr:BlankNode is required");
		}
	}

	protected void checkConstantValue(Value constantValue)
			throws R2RMLDataError {
		if (!RDFDataValidator.isValidURI(constantValue.stringValue())
				&& !RDFDataValidator
						.isValidLiteral(constantValue.stringValue()))
			throw new R2RMLDataError(
					"[StdObjectMap:checkConstantValue] Not a valid URI or literal : "
							+ constantValue);
	}

	public TransformationObjectMap getPredicateObjectMap() {
		return transformationObjectMap;
	}

	public void setTransformationObjectMap(TransformationObjectMap transformationObjectMap) {
		/*
		 * if (predicateObjectMap.getObjectMaps() != null) { if
		 * (!predicateObjectMap.getObjectMaps().contains(this)) throw new
		 * IllegalStateException( "[StdObjectMap:setPredicateObjectMap] " +
		 * "The predicateObject map parent " +
		 * "already contains another Object Map !"); } else {
		 */
		// Update predicateObjectMap if not contains this object map
		if (transformationObjectMap != null) {
			if (transformationObjectMap.getArgumentMaps() == null)
				transformationObjectMap.setArgumentMaps(new HashSet<ArgumentMap>());
			transformationObjectMap.getArgumentMaps().add(this);
			// }
		}
		this.transformationObjectMap = transformationObjectMap;
	}

	
	public TransformationObjectMap getTransformationObjectMap() {
		return this.transformationObjectMap;
	}

}
