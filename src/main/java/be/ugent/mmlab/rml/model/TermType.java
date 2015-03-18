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
 * R2RML Model : TermType class
 *
 * The term type of a column-valued term map or 
 * template-valued term map determines the kind 
 * of generated RDF term (IRIs, blank nodes or literals).
 *
 ****************************************************************************/
package be.ugent.mmlab.rml.model;

import net.antidot.semantic.rdf.rdb2rdf.r2rml.core.R2RMLVocabulary;

public enum TermType {

	IRI(R2RMLVocabulary.R2RMLTerm.IRI.toString()), 
	BLANK_NODE(R2RMLVocabulary.R2RMLTerm.BLANK_NODE.toString()),
	LITERAL(R2RMLVocabulary.R2RMLTerm.LITERAL.toString());

	private String displayName;

	private TermType(String displayName) {
		// The value MUST be an IRI
		this.displayName = R2RMLVocabulary.R2RML_NAMESPACE + displayName;
	}

	public String toString() {
		return displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Converts a termType from its display name.
	 * 
	 * @param displayName
	 * @return
	 */
	public static TermType toTermType(String displayName) {
		for (TermType termType : TermType.values()) {
			if (termType.getDisplayName().equals(displayName))
				return termType;
		}
		return null;
	}

}
