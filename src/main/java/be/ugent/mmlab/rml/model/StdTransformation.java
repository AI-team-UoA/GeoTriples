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
 * @author: dimis (dimis@di.uoa.gr)
 * 
 ****************************************************************************/
package be.ugent.mmlab.rml.model;

import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLStructureException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLSyntaxException;
import net.antidot.sql.model.tools.SQLDataValidator;

public class StdTransformation implements Transformation {

	private String function;
	private ArgumentMap argument;

	public StdTransformation(String function, ArgumentMap argument)
			throws InvalidR2RMLStructureException, InvalidR2RMLSyntaxException {
		setFunction(function);
		setArgument(argument);
	}

	private void setArgument(ArgumentMap argument)
			throws InvalidR2RMLStructureException, InvalidR2RMLSyntaxException {
		if (argument == null)
			throw new InvalidR2RMLStructureException(
					"[StdTransformation:construct] A transformation must "
							+ "have an argument name.");
		// old code
//		if (!SQLDataValidator.isValidSQLIdentifier(parent))
//			throw new InvalidR2RMLSyntaxException(
//					"[StdJoinCondition:setParent] Not a valid column "
//							+ "value : " + parent);
                
                // TODO check if reference is valid

		this.argument = argument;
	}

	private void setFunction(String function) throws InvalidR2RMLStructureException,
			InvalidR2RMLSyntaxException {
		if (function == null)
			throw new InvalidR2RMLStructureException(
					"[StdTransformation:construct] A transformation must "
							+ "have function name.");
		// old code
//		if (!SQLDataValidator.isValidSQLIdentifier(child))
//			throw new InvalidR2RMLSyntaxException(
//					"[StdJoinCondition:setParent] Not a valid column "
//							+ "value : " + child);
                // TODO check if reference is valid
                
                
		this.function = function;

	}

	public String getFunction() {
		return function;
	}

	public ArgumentMap getArgumentMap() {
		return argument;
	}

}
