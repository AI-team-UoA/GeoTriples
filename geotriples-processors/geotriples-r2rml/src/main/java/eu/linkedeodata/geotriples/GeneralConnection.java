package eu.linkedeodata.geotriples;

import java.util.Collection;

import org.d2rq.db.op.SQLOp;
import org.d2rq.db.op.TableOp;
import org.d2rq.db.schema.TableName;

public interface GeneralConnection {

	public String getFileURL();					//TODO do the same for the parser
	
	public GeneralParser getParser();
	
	public void setParser(GeneralParser parser);
	
	public Collection<TableName> getTableNames() throws Exception;
	public Collection<TableName> getTableNames(String searchInSchema) throws Exception;
	
	public boolean isTable(TableName table) throws Exception;
	
	public TableOp getTable(TableName table) throws Exception;
	
	public boolean isReferencedByForeignKey(TableName table);
	
	public int limit();
	
	public void setLimit(int resultSizeLimit);

	public void setFetchSize(int fetchSize);

	public void setDefaultFetchSize(int value);
	
	public int fetchSize();
	
    public int hashCode();

	public SQLOp getSelectStatement(String sql);

	/**
	 * @param querytable a table to search
	 * @return a result set containing all rows from querytable
	 * @throws Exception
	 */
	public GeneralResultSet getData(String querytable) throws Exception;

	/**
	 * @param querytable a table to search
	 * @param primaryke the primary key to limit result-rows
	 * @return	a result set containing the selected rows from querytable
	 */
	public GeneralResultSet getData(String querytable, int primarykey);
		
	public boolean isGuiConnection();

	public String getCRS();
	
	public enum ConnectionType{
		SQL,
		SHAPEFILE,
		KML,
		RML
	}
}
