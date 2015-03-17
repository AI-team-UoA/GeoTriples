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
 * R2RML Model : SubjectMap Interface
 *
 * A subject map is a term map. It specifies a rule
 * for generating the subjects of the RDF triples generated 
 * by a triples map.
 *
 ****************************************************************************/
package be.ugent.mmlab.rml.model;

import java.util.Set;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLStructureException;
import org.openrdf.model.URI;

public interface SubjectMap extends TermMap {

	/**
	 * A subject map may have one or more class IRIs.
	 */
	public Set<URI> getClassIRIs();
	
	/**
	 * Any subject map may have one or more associated graph maps.
	 */
	public Set<GraphMap> getGraphMaps();
	
	/**
	 * A Term Map knows in own Triples Map container.
	 * In 7.7 Inverse Expressions : "Let t be the logical table
	 * associated with this term map" suggests this feature.
	 */
	public TriplesMap getOwnTriplesMap();
	public void setOwnTriplesMap(TriplesMap ownTriplesMap) throws InvalidR2RMLStructureException;

}
