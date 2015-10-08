/**
 * @author Dimitrianos Savva National and Kapodistrian University of Athens
 * @author Giannis Vlachopoulos National and Kapodistrian University of Athens
 */
package eu.linkedeodata.geotriples.shapefile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.d2rq.db.op.SQLOp;
import org.d2rq.db.op.TableOp;
import org.d2rq.db.schema.TableDef;
import org.d2rq.db.schema.TableName;
import org.d2rq.lang.Database;

import be.ugent.mmlab.rml.function.Config;
import eu.linkedeodata.geotriples.GeneralConnection;
import eu.linkedeodata.geotriples.GeneralParser;
import eu.linkedeodata.geotriples.GeneralResultSet;
import eu.linkedeodata.geotriples.TableDefUtils;

public class ShapefileConnection implements GeneralConnection {

	private final String fileURL;
	private int limit;
	private int fetchSize = Database.NO_FETCH_SIZE;
	private int defaultFetchSize = Database.NO_FETCH_SIZE;
	private ShapeFileParser parser = null;

	public ShapefileConnection(String fileURL) {
		this.fileURL = fileURL;
		if (Config.useDGALLibrary)
			parser = new ShapeFileParserGDAL(new File(fileURL), "gid");
		else
			parser = new ShapeFileParser(new File(fileURL), "gid");
	}

	public String getFileURL() {
		return fileURL;
	}

	public ShapeFileParser getParser() {
		return parser;
	}

	public void setParser(ShapeFileParser parser) {
		this.parser = parser;
	}

	private Map<TableName, TableOp> tableCache = null;

	private Collection<TableName> tableNames = null;

	/**
	 * Lists available table names. Caches results.
	 * 
	 * @param searchInSchema
	 *            Schema to list tables from; <tt>null</tt> to list tables from
	 *            all schemas
	 * @return A list of table names
	 * @throws Exception
	 */
	public Collection<TableName> getTableNames() throws Exception {
		if (tableNames == null) {
			List<TableDef> tableDefs = TableDefUtils.addPrimaryKeyDef(parser.getTablesDefs());

			tableNames = new ArrayList<TableName>();
			for (TableDef t : tableDefs) {
				tableNames.add(t.getName());
				// TODO add the geometryname for each!!!!!
			}

		}
		return tableNames;
	}

	private void cacheTable() throws Exception {
		if (tableCache != null)
			return;
		tableCache = new HashMap<TableName, TableOp>();

		List<TableDef> tableDefs = TableDefUtils.addPrimaryKeyDef(parser.getTablesDefs());
		for (TableDef tabledef : tableDefs) {
			if (tabledef != null) {
				tableCache.put(tabledef.getName(), new TableOp(tabledef));
				tableNames = new ArrayList<TableName>();
				tableNames.add(tabledef.getName());

				/* put the default geometry logical table here!!! */
				TableDef geometrytable = TableDefUtils.generateVirtualGeometryTable(tabledef);
				tableCache.put(geometrytable.getName(), new TableOp(geometrytable));
				tableNames.add(geometrytable.getName());
			}
		}

	}

	public boolean isTable(TableName table) throws Exception {
		cacheTable();
		return tableCache.get(table) != null;// tableCache.getTableName().equals(table);
	}

	/**
	 * @param table
	 *            A table name
	 * @return Metadata about the table, or <code>null</code> if it doesn't
	 *         exist
	 * @throws Exception
	 */
	public TableOp getTable(TableName table) throws Exception {
		cacheTable();
		return tableCache.get(table);
		// return tableCache.getTableName().equals(table)?tableCache:null;
	}

	/**
	 * @return <code>true</code> if another table has a foreign key referencing
	 *         this table's primary key
	 */
	public boolean isReferencedByForeignKey(TableName table) {
		return false;
	}

	/* Dont know if we want this */
	public int limit() {
		return this.limit;
	}

	public void setLimit(int resultSizeLimit) {
		this.limit = resultSizeLimit;
	}

	/**
	 * @param fetchSize
	 *            Value specified in user config or mapping file
	 */
	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	/**
	 * @param value
	 *            Default value for the current operation mode (e.g., increase
	 *            for dumps)
	 */
	public void setDefaultFetchSize(int value) {
		defaultFetchSize = value;
	}

	public int fetchSize() {
		if (fetchSize == Database.NO_FETCH_SIZE) {
			return defaultFetchSize;
		}
		return fetchSize;
	}

	public int hashCode() {
		return fileURL.hashCode();
	}

	public SQLOp getSelectStatement(String sql) {
		try {
			throw new Exception("Unimplemented Method mple");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public GeneralResultSet getData(String querytable) throws Exception {
		return new GeneralResultSet(parser.getData(querytable));
	}

	public GeneralResultSet getData(String querytable, int primarykey) {
		return new GeneralResultSet(parser.getData(querytable, primarykey));
	}

	@Override
	public void setParser(GeneralParser parser) {
		this.parser = (ShapeFileParser) parser;

	}

	@Override
	public boolean isGuiConnection() {
		return false;
	}

	@Override
	public String getCRS() {
		return this.parser.getCrsString();
	}

	/**
	 * Lists available table names. Caches results.
	 * 
	 * @param searchInSchema
	 *            Schema to list tables from; <tt>null</tt> to list tables from
	 *            all schemas
	 * @return A list of table names
	 * @throws Exception
	 */
	public Collection<TableName> getTableNames(String searchInSchema) throws Exception {
		return getTableNames();
	}
}
