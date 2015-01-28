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
 * @author: dimis (dimis@di.uoa.gr)
 *
 ****************************************************************************/
package be.ugent.mmlab.rml.model;

public interface Transformation {
	
	/**
	 * Function that transforms a value (maybe reference value)
	 */
	public String getFunction();
	
	/**
	 * Argument Map.  
	 */
	public ArgumentMap getArgumentMap();

}
