package d2rq;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import jena.cmdline.ArgDecl;
import jena.cmdline.CommandLine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.d2rq.CommandLineTool;
import org.d2rq.SystemLoader;
import org.d2rq.db.schema.TableName;
import org.d2rq.mapgen.MappingGenerator;
import org.d2rq.mapgen.OntologyTarget;

import eu.linkedeodata.geotriples.Config;
import eu.linkedeodata.geotriples.gui.ColumnReceipt;


/**
 * Command line interface for {@link MappingGenerator}.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class generate_mapping extends CommandLineTool {
	private final static Log log = LogFactory.getLog(generate_mapping.class);
	
	private java.util.Map<TableName, java.util.List<ColumnReceipt>> tablesAndColumns = null;
	private PrintStream printStream = null;
	
	public static void main(String[] args) {
		new generate_mapping().process(args);
	}
	
	public generate_mapping() {
		
	}
	
	public generate_mapping(java.util.Map<TableName, java.util.List<ColumnReceipt>> tablesAndColumns, java.util.Map<TableName, String> tablesAndClasses, PrintStream printStream) {
		this.setTablesAndColumns(tablesAndColumns);
		this.printStream = printStream;
		Config.tablesAndColumns = tablesAndColumns;
		Config.tablesAndClasses = tablesAndClasses;
	}
	
	public void usage() {
		System.err.println("usage: generate-mapping [options] jdbcURL");
		System.err.println();
		printStandardArguments(false, false);
		System.err.println("  Options:");
		printConnectionOptions(true);
		System.err.println("    -o outfile.ttl  Output file name (default: stdout)");
		System.err.println("    --r2rml         Generate R2RML mapping file");
		System.err.println("    -v              Generate RDFS+OWL vocabulary instead of mapping file");
		System.err.println("    -s SRID (EPSG)  EPSG code (default: 4326)");
		System.err.println("    --verbose       Print debug information");
		System.err.println("    -b baseURI      BaseURI");
		System.err.println();
		System.exit(1);
	}

	private ArgDecl outfileArg = new ArgDecl(true, "o", "out", "outfile");
	private ArgDecl r2rmlArg = new ArgDecl(false, "r2rml");
	private ArgDecl vocabAsOutput = new ArgDecl(false, "v", "vocab");
	private ArgDecl baseIRI = new ArgDecl(true, "b", "baseIRI");
	
	public void initArgs(CommandLine cmd) {
		cmd.add(r2rmlArg);
		cmd.add(outfileArg);
		cmd.add(vocabAsOutput);
		cmd.add(baseIRI);
	}

	public void run(CommandLine cmd, SystemLoader loader) throws IOException {
		String baseIRIstr = null;
		if (cmd.numItems() == 1) {
			loader.setJdbcURL(cmd.getItem(0));
		}
		
		if (cmd.contains(r2rmlArg) || true) { //geotriples only focuses on r2rm and rml so ||true
			loader.setGenerateR2RML(true);
		}
		if (cmd.contains(baseIRI)) {
			baseIRIstr = cmd.getValue(baseIRI);
			loader.setSystemBaseURI(baseIRIstr);
		}
		else {
			log.error("No base IRI specified");
			System.exit(-1);
		}
		
		PrintStream out;
		if (cmd.contains(outfileArg)) {
			File f = new File(cmd.getArg(outfileArg).getValue());
			log.info("Writing to " + f);
			out = new PrintStream(new FileOutputStream(f));
		} else {
			log.info("Writing to stdout");
			out = System.out;
		}
		if(printStream != null) {
			out = printStream;
		}
		

		MappingGenerator generator = loader.getMappingGenerator();
		try {
			if (cmd.contains(vocabAsOutput)) {
				OntologyTarget target = new OntologyTarget();
				generator.generate(target);
				target.getOntologyModel().write(out, "TURTLE");
			} else {
				loader.getWriter().write(out);
			}
		} finally {
			loader.close();
		}
	}

	public java.util.Map<TableName, java.util.List<ColumnReceipt>> getTablesAndColumns() {
		return tablesAndColumns;
	}

	public void setTablesAndColumns(java.util.Map<TableName, java.util.List<ColumnReceipt>> tablesAndColumns) {
		this.tablesAndColumns = tablesAndColumns;
	}
}
