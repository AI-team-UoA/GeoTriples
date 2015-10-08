package org.d2rq;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import jena.cmdline.ArgDecl;
import jena.cmdline.CmdLineUtils;
import jena.cmdline.CommandLine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.d2rq.mapgen.Filter;
import org.d2rq.mapgen.FilterIncludeExclude;
import org.d2rq.mapgen.FilterMatchAny;
import org.d2rq.mapgen.FilterParser;

import eu.linkedeodata.geotriples.Config;



/**
 * Base class for the D2RQ command line tools. They share much of their
 * argument list and functionality, therefore this is extracted into
 * this superclass.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public abstract class CommandLineTool {
	private final static Log log = LogFactory.getLog(CommandLineTool.class);

	private final CommandLine cmd = new CommandLine();
	private final ArgDecl userArg = new ArgDecl(true, "u", "user", "username");
	private final ArgDecl passArg = new ArgDecl(true, "p", "pass", "password");
	private final ArgDecl driverArg = new ArgDecl(true, "d", "driver");
	private final ArgDecl sqlFileArg = new ArgDecl(true, "l", "load-sql");
	private final ArgDecl w3cArg = new ArgDecl(false, "w3c", "direct-mapping");
	private final ArgDecl verboseArg = new ArgDecl(false, "verbose");
	private final ArgDecl debugArg = new ArgDecl(false, "debug");
	private final ArgDecl schemasArg = new ArgDecl(true, "schema", "schemas");
	private final ArgDecl tablesArg = new ArgDecl(true, "table", "tables");
	private final ArgDecl columnsArg = new ArgDecl(true, "column", "columns");
	private final ArgDecl skipSchemasArg = new ArgDecl(true, "skip-schema", "skip-schemas");
	private final ArgDecl skipTablesArg = new ArgDecl(true, "skip-table", "skip-tables");
	private final ArgDecl skipColumnsArg = new ArgDecl(true, "skip-column", "skip-columns");
	private final ArgDecl jdbcURLArg = new ArgDecl(true, "j", "jdbc");
	private final ArgDecl epsgArg = new ArgDecl(true, "s", "srid");
	private final ArgDecl geoVocabulary = new ArgDecl(true, "geov", "geoVocabulary");
	private final SystemLoader loader = new SystemLoader();
	private boolean supportImplicitJdbcURL = true;
	private int minArguments = 0;
	private int maxArguments = 1;

	
	
	public abstract void usage();

	public abstract void initArgs(CommandLine cmd);
	
	public abstract void run(CommandLine cmd, SystemLoader loader)
	throws D2RQException, IOException;
	
	public void setMinMaxArguments(int min, int max) {
		minArguments = min;
		maxArguments = max;
	}
	
	public void setSupportImplicitJdbcURL(boolean flag) {
		supportImplicitJdbcURL = flag;
	}
	
	public void process(String[] args) {
		cmd.add(userArg);
		cmd.add(passArg);
		cmd.add(driverArg);
		cmd.add(sqlFileArg);
		cmd.add(w3cArg);
		cmd.add(verboseArg);
		cmd.add(debugArg);
		cmd.add(schemasArg);
		cmd.add(tablesArg);
		cmd.add(columnsArg);
		cmd.add(skipSchemasArg);
		cmd.add(skipTablesArg);
		cmd.add(skipColumnsArg);
		cmd.add(jdbcURLArg);
		cmd.add(epsgArg);
		cmd.add(geoVocabulary);
		
		initArgs(cmd);
		
		try {
			cmd.process(args);
		} catch (IllegalArgumentException ex) {
			reportException(ex);
		}
		
		if (cmd.hasArg(verboseArg)) {
			Log4jHelper.setVerboseLogging();
		}
		if (cmd.hasArg(debugArg)) {
			Log4jHelper.setDebugLogging();
		}

		if (supportImplicitJdbcURL && cmd.hasArg(sqlFileArg)) {
			loader.setJdbcURL(SystemLoader.DEFAULT_JDBC_URL);
		} else if (cmd.numItems() == 0) {
			usage();
			System.exit(1);
		}
		if (cmd.numItems() < minArguments) {
			reportException(new IllegalArgumentException("Not enough arguments"));
		} else if (cmd.numItems() > maxArguments) {
			reportException(new IllegalArgumentException("Too many arguments"));
		}
		
		if (cmd.contains(userArg)) {
			loader.setUsername(cmd.getArg(userArg).getValue());
		}
		if (cmd.contains(epsgArg)) {
			Config.EPSG_CODE = Integer.parseInt(cmd.getArg(epsgArg).getValue());
		}
		if (cmd.contains(geoVocabulary)) {
			String voc = cmd.getArg(geoVocabulary).getValue();
			if (voc != null) {
				if (voc.equals("GeoSPARQL") || voc.equals("stRDF")) {
					Config.VOCABULARY = voc;
				}
			}
		}
		if (cmd.contains(passArg)) {
			loader.setPassword(cmd.getArg(passArg).getValue());
		}
		if (cmd.contains(driverArg)) {
			loader.setJDBCDriverClass(cmd.getArg(driverArg).getValue());
		}
		if (cmd.contains(sqlFileArg)) {
			loader.setStartupSQLScript(cmd.getArg(sqlFileArg).getValue());
		}
		if (cmd.contains(w3cArg)) {
			loader.setGenerateW3CDirectMapping(true);
		}
		if (cmd.contains(jdbcURLArg)) {
			loader.setJdbcURL(cmd.getArg(jdbcURLArg).getValue());
		}
		try {
			Collection<Filter> includes = new ArrayList<Filter>();
			Collection<Filter> excludes = new ArrayList<Filter>();
			if (cmd.contains(schemasArg)) {
				String spec = withIndirection(cmd.getArg(schemasArg).getValue());
				includes.add(new FilterParser(spec).parseSchemaFilter());
			}
			if (cmd.contains(tablesArg)) {
				/*String spec = withIndirection(cmd.getArg(tablesArg).getValue());
				includes.add(new FilterParser(spec).parseTableFilter(true));*/
				loader.setOnlyTable(cmd.getArg(tablesArg).getValue());
			}
			if (cmd.contains(columnsArg)) {
				String spec = withIndirection(cmd.getArg(columnsArg).getValue());
				includes.add(new FilterParser(spec).parseColumnFilter(true));
			}
			if (cmd.contains(skipSchemasArg)) {
				String spec = withIndirection(cmd.getArg(skipSchemasArg).getValue());
				excludes.add(new FilterParser(spec).parseSchemaFilter());
			}
			if (cmd.contains(skipTablesArg)) {
				String spec = withIndirection(cmd.getArg(skipTablesArg).getValue());
				excludes.add(new FilterParser(spec).parseTableFilter(false));
			}
			if (cmd.contains(skipColumnsArg)) {
				String spec = withIndirection(cmd.getArg(skipColumnsArg).getValue());
				excludes.add(new FilterParser(spec).parseColumnFilter(false));
			}
			if (!includes.isEmpty() || !excludes.isEmpty()) {
				loader.setFilter(new FilterIncludeExclude(
						includes.isEmpty() ? Filter.ALL : FilterMatchAny.create(includes), 
								FilterMatchAny.create(excludes)));
			}
			run(cmd, loader);
		} catch (D2RQException ex) {
			reportException(ex);
		} catch (Exception ex) {
			reportException(ex);
		}
	}
	
	public void reportException(D2RQException ex) {
		if (ex.getMessage() == null && ex.getCause() != null && ex.getCause().getMessage() != null) {
			if (ex.getCause() instanceof SQLException) {
				System.err.println("SQL error " + ex.getCause().getMessage());
			} else {
				System.err.println(ex.getCause().getMessage());
			}
		} else {
			System.err.println(ex.getMessage());
		}
		log.info("Command line tool exception", ex);
		System.exit(1);
	}
	
	public void reportException(Exception ex) {
		if (ex.getMessage() == null) {
			ex.printStackTrace(System.err);
		} else {
			System.err.println(ex.getMessage());
		}
		log.info("Command line tool exception", ex);
		System.exit(1);
	}

	public void printStandardArguments(boolean withMappingFile, boolean r2rml) {
		System.err.println("  Arguments:");
		if (withMappingFile) {
			System.err.println("    mappingFile     Filename or URL of " + 
					(r2rml ? "an R2RML" : "a D2RQ") + " mapping file");
		}
		System.err.println("    jdbcURL         JDBC URL for the DB, e.g. jdbc:mysql://localhost/dbname");
		if (supportImplicitJdbcURL) {
			System.err.println("                    (If omitted with -l, set up a temporary in-memory DB)");
		}
	}
	
	public void printConnectionOptions(boolean withMappingGenerator) {
		System.err.println("    -u username     Database user for connecting to the DB");
		System.err.println("    -p password     Database password for connecting to the DB");
		System.err.println("    -d driverclass  Java class name of the JDBC driver for the DB");
		System.err.println("    -l script.sql   Load a SQL script before processing");
		if (withMappingGenerator) {
			System.err.println("    --w3c           Produce W3C Direct Mapping compatible mapping file");
			System.err.println("    --[skip-](schemas|tables|columns) [schema.]table[.column]");
			System.err.println("                    Include or exclude specific database objects");
		}
	}
	
	private static String withIndirection(String value) {
		if (value.startsWith("@")) {
			value = value.substring(1);
			try {
				value = CmdLineUtils.readWholeFileAsUTF8(value);
			} catch (Exception ex) {
				throw new IllegalArgumentException("Failed to read '" + value + "': " + ex.getMessage());
			}
		}
		return value;
	}
}
