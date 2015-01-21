package eu.linkedeodata.geotriples;

import org.d2rq.db.SelectStatementBuilder;
import org.d2rq.db.op.DatabaseOp;
import org.d2rq.db.vendor.Vendor;

/**
 * Turns a {@link DatabaseOp} into a SQL SELECT statement.
 * 
 * Works by doing a depth-first traversal of the query tree, collecting
 * information in a <code>SimpleQuery</code> object. Such an object can contain
 * a non-nested SQL query. Once nesting is necessary, the current
 * <code>SimpleQuery</code> is put on a stack, a new one is allocated and
 * information collected in it, and then it is "flattened" by turning
 * it into a raw SQL string that becomes a FROM clause in the upper
 * instance.
 * 
 * The list of SELECT clauses is not built while working the tree, but
 * computed in the end from the {@link DatabaseOp}'s column list.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class GeneralSelectStatementBuilder extends SelectStatementBuilder {
	DatabaseOp input;
	public GeneralSelectStatementBuilder(DatabaseOp input, Vendor vendor)
	{
		super(input,vendor);
		this.input=input;
	}
	public String getTable()
	{
		return input.getTableName().toString();
	}
}
