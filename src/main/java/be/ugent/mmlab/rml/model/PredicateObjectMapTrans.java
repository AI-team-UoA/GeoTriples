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
 * R2RML Model : PredicateObjectMapTrans Interface
 *
 * Provides extra functionality for transformation functions
 * 
 * @author: dimis (dimis@di.uoa.gr)
 ****************************************************************************/
package be.ugent.mmlab.rml.model;

import java.util.Set;

public interface PredicateObjectMapTrans extends PredicateObjectMap {

	void setTransformationObjectMap(Set<TransformationObjectMap> transObjectMaps);

	Set<TransformationObjectMap> getTransformationObjectMaps();

	boolean hasTransformationObjectMaps();
	

	
}
