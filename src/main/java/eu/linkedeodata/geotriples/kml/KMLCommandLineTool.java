package eu.linkedeodata.geotriples.kml;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import jena.cmdline.ArgDecl;
import jena.cmdline.CmdLineUtils;
import jena.cmdline.CommandLine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.d2rq.D2RQException;
import org.d2rq.Log4jHelper;
import org.d2rq.mapgen.Filter;
import org.d2rq.mapgen.FilterIncludeExclude;
import org.d2rq.mapgen.FilterMatchAny;

import eu.linkedeodata.geotriples.gui.RecipeMapping;



/**
 * Base class for the D2RQ command line tools. They share much of their
 * argument list and functionality, therefore this is extracted into
 * this superclass.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public abstract class KMLCommandLineTool {
	private final static Log log = LogFactory.getLog(KMLCommandLineTool.class);

	private final CommandLine cmd = new CommandLine();
	private final ArgDecl verboseArg = new ArgDecl(false, "verbose");
	private final ArgDecl debugArg = new ArgDecl(false, "debug");
	/*\dimis Added argument*/
	private final ArgDecl shapefileArg = new ArgDecl(true, "sh", "shpf"); 
	/*\dimis changed from SystemLoader to GenericLoader ,and SystemLoader to SystemLoader_pyravlos*/
	//private final GenericSystemLoader loader = new SystemLoader();
	private KMLSystemLoader loader = new KMLSystemLoader();

	private boolean supportImplicitJdbcURL = true;
	private int minArguments = 0;
	private int maxArguments = 1;
	
	public abstract void usage();

	public abstract void initArgs(CommandLine cmd);
	/*\dimis changed from SystemLoader to GenericSystemLoader, added Exception*/
	public abstract void run(CommandLine cmd, KMLSystemLoader loader) throws Exception;
	
	public void setMinMaxArguments(int min, int max) {
		minArguments = min;
		maxArguments = max;
	}
	
	public void setSupportImplicitJdbcURL(boolean flag) {
		supportImplicitJdbcURL = flag;
	}
	public void process(String[] args) throws Exception
	{
		this.process(args, null, null);
	}
	public void process(String[] args,RecipeMapping receipt) throws Exception
	{
		if(receipt==null)
		{
			throw new Exception("reciept is null, aborting..");
		}
		this.process(args, receipt, null);
	}
	public void process(String[] args,String input_mapping) throws Exception
	{
		if(input_mapping==null)
		{
			throw new Exception("input mapping is null, aborting..");
		}
		this.process(args, null, input_mapping);
	}
	
	public void process(String[] args,RecipeMapping receipt,String input_mapping) {
		
		loader=new KMLSystemLoader(receipt,input_mapping);

		cmd.add(verboseArg);
		cmd.add(debugArg);

		/*\dimis Added argument*/
		cmd.add(shapefileArg);
		
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
		
		if (cmd.contains(shapefileArg)) {
			((KMLSystemLoader)loader).setURL(cmd.getArg(shapefileArg).getValue());
		}

		if (cmd.numItems() == 0) {
			usage();
			System.exit(1);
		}
		if (cmd.numItems() < minArguments) {
			reportException(new IllegalArgumentException("Not enough arguments"));
		} else if (cmd.numItems() > maxArguments) {
			reportException(new IllegalArgumentException("Too many arguments"));
		}
		
		
		try {
			Collection<Filter> includes = new ArrayList<Filter>();
			Collection<Filter> excludes = new ArrayList<Filter>();
			if (!includes.isEmpty() || !excludes.isEmpty()) {
				loader.setFilter(new FilterIncludeExclude(
						includes.isEmpty() ? Filter.ALL : FilterMatchAny.create(includes), 
								FilterMatchAny.create(excludes)));
			}
			run(cmd, (KMLSystemLoader) loader);
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
		//do nothing
	}
	
	@SuppressWarnings("unused")
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
