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
 * Model : Extended PredicateObjectMap Class
 *
 * 
 * @author: dimis (dimis@di.uoa.gr)
 ****************************************************************************/
package be.ugent.mmlab.rml.model;

import java.util.HashSet;
import java.util.Set;

public class StdPredicateObjectMapTrans extends StdPredicateObjectMap implements PredicateObjectMapTrans {

	protected Set<TransformationObjectMap> transObjectMaps; //dimis


	public StdPredicateObjectMapTrans(Set<PredicateMap> predicateMaps,
			Set<ObjectMap> objectMaps) {
		super(predicateMaps);
		setObjectMaps(objectMaps);
	}
	public StdPredicateObjectMapTrans(Set<PredicateMap> predicateMaps,
			Set<ObjectMap> objectMaps, Set<ReferencingObjectMap> referencingObjectMaps) {
		this(predicateMaps, objectMaps);
		setReferencingObjectMap(referencingObjectMaps);
	}
	
	public StdPredicateObjectMapTrans(Set<PredicateMap> predicateMaps,
			Set<ObjectMap> objectMaps, Set<ReferencingObjectMap> referencingObjectMaps , Set<TransformationObjectMap> transformationObjectMaps) {
		this(predicateMaps, objectMaps);
		setReferencingObjectMap(referencingObjectMaps);
		setTransformationObjectMap(transformationObjectMaps);
	}

        
        @Override
    	public void setTransformationObjectMap(Set<TransformationObjectMap> transObjectMaps) {
    		if (transObjectMaps == null)
    			this.transObjectMaps = new HashSet<TransformationObjectMap>();
    		else {
    			for (TransformationObjectMap transObjectMap : transObjectMaps) {
    				if (transObjectMap != null)
    					transObjectMap.setPredicateObjectMap(this);
    			}
    			this.transObjectMaps = transObjectMaps;
    		}
    	}

        
        @Override
    	public Set<TransformationObjectMap> getTransformationObjectMaps() {
    		return transObjectMaps;
    	}

        
        @Override
    	public boolean hasTransformationObjectMaps() {
    		return transObjectMaps != null && !transObjectMaps.isEmpty();
    	}

        @Override
	public TriplesMap getOwnTriplesMap() {
		return ownTriplesMap;
	}

}
