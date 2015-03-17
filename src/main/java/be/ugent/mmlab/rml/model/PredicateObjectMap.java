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
 * R2RML Model : PredicateObjectMap Interface
 *
 * A predicate-object map is a function
 * that creates predicate-object pairs from logical 
 * table rows. It is used in conjunction with a subject
 * map to generate RDF triples in a triples map.
 * 
 ****************************************************************************/
package be.ugent.mmlab.rml.model;

import java.util.Set;

public interface PredicateObjectMap {

	/**
	 * A predicate-object map is represented by a resource that references
	 * one or more predicate maps.
	 */
	public Set<PredicateMap> getPredicateMaps();

	public void setPredicateMaps(Set<PredicateMap> predicateMaps);

	/**
	 * A predicate-object map is represented by a resource that references
	 * one or more object map or one referencing object map. If this method
	 * returns NULL therefore getReferencingObjectMap method will not.
	 */
	public Set<ObjectMap> getObjectMaps();

	public void setObjectMaps(Set<ObjectMap> objectMaps);

	/**
	 * A predicate-object map is represented by a resource that references
	 * exactly one object map or one referencing object map. If this method
	 * returns NULL therefore getObjectMap method will not.
	 */
	public Set<ReferencingObjectMap> getReferencingObjectMaps();

	public void setReferencingObjectMap(Set<ReferencingObjectMap> referencingOjectMap);

	/**
	 * Indicates if a ReferencingObjectMap is associated with this
	 * PredicateObjectMap. If true, it is a ReferencingObjectMap, a "simple"
	 * ObjectMap otherwise.
	 */
	public boolean hasReferencingObjectMaps();

	/**
	 * A Predicate Object Map knows in own Triples Map container.
	 */
	public TriplesMap getOwnTriplesMap();

	public void setOwnTriplesMap(TriplesMap ownTriplesMap);
	
	/**
	 * Any predicate-object map may have one or more associated graph maps.
	 */
	public Set<GraphMap> getGraphMaps();
	public void setGraphMaps(Set<GraphMap> graphmaps);
	

	
}
