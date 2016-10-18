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
 * R2RML Model : Standard PredicateMap Class
 *
 * A predicate map is a specific term map used for 
 * representing RDF predicate. 
 * 
 * modified by mielvandersande
 * 
 ****************************************************************************/
package be.ugent.mmlab.rml.model;

import be.ugent.mmlab.rml.model.reference.ReferenceIdentifier;
import java.util.HashSet;

import net.antidot.semantic.rdf.model.tools.RDFDataValidator;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLStructureException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLSyntaxException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.R2RMLDataError;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

public class StdPredicateMap extends AbstractTermMap implements TermMap,
		PredicateMap {

	private PredicateObjectMap predicateObjectMap;

	public StdPredicateMap(PredicateObjectMap predicateObjectMap,
			Value constantValue, String stringTemplate,
			String inverseExpression, ReferenceIdentifier referenceValue, URI termType)
			throws R2RMLDataError, InvalidR2RMLStructureException,
			InvalidR2RMLSyntaxException {
		// No Literal term type
		// ==> No datatype
		// ==> No specified language tag
		// No class IRI
		super(constantValue, null, null, stringTemplate, termType,
				inverseExpression, referenceValue);
		setPredicateObjectMap(predicateObjectMap);
	}

	protected void checkSpecificTermType(TermType tt)
			throws InvalidR2RMLStructureException {
		// If the term map is a predicate map: rr:IRI
		if (tt != TermType.IRI) {
			throw new InvalidR2RMLStructureException(
					"[StdPredicateMap:checkSpecificTermType] If the term map is a "
							+ "predicate map: only rr:IRI  is required");
		}
	}

	protected void checkConstantValue(Value constantValue)
			throws R2RMLDataError {
		// If the constant-valued term map is a predicate map then its constant
		// value must be an IRI.
		if (!RDFDataValidator.isValidURI(constantValue.stringValue()))
			throw new R2RMLDataError(
					"[StdPredicateMap:checkConstantValue] Not a valid URI : "
							+ constantValue);
	}

	public PredicateObjectMap getPredicateObjectMap() {
		return predicateObjectMap;
	}

	public void setPredicateObjectMap(PredicateObjectMap predicateObjectMap) {
		/*
		 * if (predicateObjectMap.getPredicateMaps() != null) { if
		 * (!predicateObjectMap.getPredicateMaps().contains(this)) throw new
		 * IllegalStateException(
		 * "[StdPredicateObjectMap:setPredicateObjectMap] " +
		 * "The predicateObject map parent " +
		 * "already contains another Predicate Map !"); } else {
		 */
		if (predicateObjectMap != null) {
			// Update predicateObjectMap if not contains this object map
			if (predicateObjectMap.getPredicateMaps() == null) {
                        predicateObjectMap
                                        .setPredicateMaps(new HashSet<PredicateMap>());
                    }
			predicateObjectMap.getPredicateMaps().add(this);
		}
		// }
		this.predicateObjectMap = predicateObjectMap;

	}

}
