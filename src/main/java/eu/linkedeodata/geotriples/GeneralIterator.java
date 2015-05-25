package eu.linkedeodata.geotriples;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.d2rq.db.ResultRow;
import org.d2rq.db.op.ProjectionSpec;

import com.hp.hpl.jena.query.QueryCancelledException;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

/**
 * Executes an SQL query and delivers result rows as an iterator over {@link ResultRow}s.
 * The query is executed lazily. This class logs all executed SQL queries.
 *
 * @author Dimitrianos Savva
 * @author Giannis Vlachopoulos
 */
public class GeneralIterator implements ClosableIterator<GeneralResultRow> {
	private final static Log log = LogFactory.getLog(GeneralIterator.class);
	private String querytable;
	private GeneralConnection connection;
	private Statement statement = null;
	
	private GeneralResultSet resultSet = null;
	private GeneralResultRow prefetchedRow = null;
	private boolean queryExecuted = false;
	private boolean explicitlyClosed = false;
	private boolean cancelled = false;
	private String sqlquery;
	private List<String> columns=new ArrayList<String>();

	public GeneralIterator(String sqlquery,String table, List<ProjectionSpec> columns, GeneralConnection db) {
		for(ProjectionSpec pr: columns)
		{
			String column=pr.getColumn().getColumn().getName();
			this.columns.add(column);
			//System.out.println(column);
		}
		this.sqlquery=sqlquery;
		this.querytable = table;
		this.connection = db;
    }

	public boolean hasNext() {
		if (cancelled) {
			throw new QueryCancelledException();
		}
		if (explicitlyClosed) {
			return false;
		}
		if (prefetchedRow == null) {
		    try {
				ensureQueryExecuted();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			tryFetchNextRow();
		}
		return prefetchedRow != null;
	}

	/**
	 * @return The next query ResultRow.
	 */
	public GeneralResultRow next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		GeneralResultRow result = this.prefetchedRow;
		this.prefetchedRow = null;
		return result;
	}

	/**
	 * @deprecated Use {@link #next()} instead
	 */
	public GeneralResultRow nextRow() {
		return next();
	}

	private synchronized void tryFetchNextRow(){
	    if (this.resultSet == null) {
	    	this.prefetchedRow = null;
	    	return;
	    }
		if (!this.resultSet.next()) {
			this.resultSet = null;
			this.prefetchedRow = null;
			return;
		}
		//prefetchedRow = resultSet.getCurrent(this.columns);
		prefetchedRow = resultSet.getCurrent();

	}
	
	public synchronized void cancel() {
		cancelled = true;
		if (statement != null) {
			try {
				statement.cancel();
			} catch (SQLException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
	
	public void remove() {
		throw new RuntimeException("Operation not supported");
	}

	private void ensureQueryExecuted() throws SQLException {
	    if (this.queryExecuted) {
	    	return;
	    }
    	this.queryExecuted = true;
    	/*log.info(querytable);*/
		try {
			if(this.sqlquery.toLowerCase().contains("where"))
			{
				int primarykey=Integer.valueOf(this.sqlquery.substring((this.sqlquery.indexOf('=')+1)));
				//System.out.println(primarykey);
				this.resultSet = this.connection.getData(querytable,primarykey);
			}
			else
			{
				this.resultSet = this.connection.getData(querytable);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.debug("SQL result set created");
    }

	@Override
	public void close() {
		return;
	}
}
