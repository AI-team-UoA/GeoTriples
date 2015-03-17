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
 * R2RML Model : Standard TriplesMap Class
 *
 * A triples map specifies a rule for translating each
 * row of a logical table to zero or more RDF triples.
 * 
 ****************************************************************************/
package be.ugent.mmlab.rml.model;

import java.util.HashSet;
import java.util.Set;

import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLStructureException;

public class StdTriplesMap implements TriplesMap {

	private Set<PredicateObjectMap> predicateObjectMaps;
	private SubjectMap subjectMap;
	private LogicalSource logicalSource;
	private String name;

	public StdTriplesMap(LogicalSource logicalSource,
			Set<StdPredicateObjectMap> predicateObjectMaps,
			StdSubjectMap subjectMap, String name) throws InvalidR2RMLStructureException {
		setSubjectMap(subjectMap);
		setLogicalSource(logicalSource);
		setPredicateObjectMap(predicateObjectMaps);
		setName(name);
	}

        @Override
	public void setLogicalSource(LogicalSource logicalSource) {
		this.logicalSource = logicalSource;
	}

	public void setPredicateObjectMap(
			Set<StdPredicateObjectMap> predicateObjectMaps) {
		this.predicateObjectMaps = new HashSet<PredicateObjectMap>();
		if (predicateObjectMaps == null) return;
		this.predicateObjectMaps.addAll(predicateObjectMaps);
		// Update prediacte object map
		for (PredicateObjectMap pom : predicateObjectMaps) {
			if (pom.getOwnTriplesMap() != null) {
				if (pom.getOwnTriplesMap() != this)
					throw new IllegalStateException(
							"[StdTriplesMap:setPredicateObjectMap] "
									+ "The predicateObject map child "
									+ "already contains another triples Map !");
			} else
				pom.setOwnTriplesMap(this);
		}
	}

	public LogicalSource getLogicalSource() {
		return logicalSource;
	}

	public Set<PredicateObjectMap> getPredicateObjectMaps() {
		return predicateObjectMaps;
	}

	public SubjectMap getSubjectMap() {
		return subjectMap;
	}

	public void setSubjectMap(SubjectMap subjectMap) throws InvalidR2RMLStructureException {
		this.subjectMap = subjectMap;

	}

	public void addPredicateObjectMap(PredicateObjectMap predicateObjectMap) {
		if (predicateObjectMap != null)
			predicateObjectMaps.add(predicateObjectMap);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		if (name != null)
			this.name = name;
		
	}

}
