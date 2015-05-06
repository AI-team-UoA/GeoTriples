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
 * R2RML Model : Standard Join Condition Class
 *
 * A join condition is a resource that has 
 * exactly two properties: 
 * 	- rr:child, whose value is known as the
 * 	  join condition's child column
 * 	- rr:parent, whose value is known as the
 * 	  join condition's parent column 
 * 
 ****************************************************************************/
package be.ugent.mmlab.rml.model;

import java.util.List;

import org.openrdf.model.URI;

import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLStructureException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLSyntaxException;
import net.antidot.sql.model.tools.SQLDataValidator;

public class StdJoinCondition implements JoinCondition {

	private String child;
	private String parent;
	private URI function = null;
    private List<TermMap> argumentMap =null;

	public StdJoinCondition(String child, String parent)
			throws InvalidR2RMLStructureException, InvalidR2RMLSyntaxException {
		setChild(child);
		setParent(parent);
	}
	public StdJoinCondition(String child, String parent,URI function,List<TermMap> argumentMap)
			throws InvalidR2RMLStructureException, InvalidR2RMLSyntaxException {
		setFunction(function);
		setArgumentMap(argumentMap);
		setChild(child);
		setParent(parent);
	}

	@Override
	public String toString() {
		return "StdJoinCondition [child=" + child + ", parent=" + parent
				+ ", function=" + function + ", argumentMap=" + argumentMap
				+ "]";
	}
	private void setParent(String parent)
			throws InvalidR2RMLStructureException, InvalidR2RMLSyntaxException {
		if (parent == null && function==null)
			throw new InvalidR2RMLStructureException(
					"[StdJoinCondition:setParent] A join condition must "
							+ "have a parent column name  or a function.");
		// old code
//		if (!SQLDataValidator.isValidSQLIdentifier(parent))
//			throw new InvalidR2RMLSyntaxException(
//					"[StdJoinCondition:setParent] Not a valid column "
//							+ "value : " + parent);
                
                // TODO check if reference is valid

		this.parent = parent;
	}

	private void setChild(String child) throws InvalidR2RMLStructureException,
			InvalidR2RMLSyntaxException {
		if (child == null && function==null)
			throw new InvalidR2RMLStructureException(
					"[StdJoinCondition:construct] A join condition must "
							+ "have a child column name or a function.");
		// old code
//		if (!SQLDataValidator.isValidSQLIdentifier(child))
//			throw new InvalidR2RMLSyntaxException(
//					"[StdJoinCondition:setParent] Not a valid column "
//							+ "value : " + child);
                // TODO check if reference is valid
                
                
		this.child = child;

	}

	public String getChild() {
		return child;
	}

	public String getParent() {
		return parent;
	}
	public URI getFunction() {
		return function;
	}
	public void setFunction(URI function) {
		this.function = function;
	}
	public List<TermMap> getArgumentMap() {
		return argumentMap;
	}
	public void setArgumentMap(List<TermMap> argumentMap) {
		this.argumentMap = argumentMap;
	}

}
