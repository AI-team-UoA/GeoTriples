/***************************************************************************
 *
 * RMLModel : RML Mapping class
 *
 * Represents a set of TriplesMap objects which can compare
 * with a mapping of a all tables of a database and files in different sources.
 * 
 * based on R2RMLMapping class in https://github.com/antidot/db2triples
 * 
 ****************************************************************************/
package be.ugent.mmlab.rml.model;

import java.util.Collection;
import java.util.HashSet;


public class RMLMapping {
	
	private Collection<TriplesMap> triplesMaps;

	public RMLMapping(Collection<TriplesMap> triplesMaps) {
		super();
		this.triplesMaps = new HashSet<TriplesMap>();
		this.triplesMaps.addAll(triplesMaps);
	}

	/**
	 * @return
	 */
	public Collection<TriplesMap> getTriplesMaps() {
		return triplesMaps;
	}
}
