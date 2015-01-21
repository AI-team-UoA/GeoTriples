package d2rq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import jena.cmdline.ArgDecl;
import jena.cmdline.CommandLine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.d2rq.CommandLineTool;
import org.d2rq.CompiledMapping;
import org.d2rq.D2RQException;
import org.d2rq.SystemLoader;
import org.d2rq.db.SQLConnection;
import org.d2rq.lang.D2RQReader;
import org.d2rq.mapgen.MappingGenerator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.shared.NoWriterForLangException;


/**
 * Command line utility for dumping a database to RDF, using the
 * {@link MappingGenerator} or a mapping file.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class dump_rdf extends CommandLineTool {
	private final static Log log = LogFactory.getLog(dump_rdf.class);
	
	private final static int DUMP_DEFAULT_FETCH_SIZE = 500;
	
	private String mapping = null;
	private String rdfFormat = null;
	
	public dump_rdf() {
		//do nothing
	}
	public dump_rdf(String guimapping, String format) {
		this.setMapping(guimapping);
		this.rdfFormat = format;
	}

	public static void main(String[] args) {
		new dump_rdf().process(args);
	}
	
	public void usage() {
		System.err.println("usage:");
		System.err.println("  dump-rdf [output-options] mappingFile");
		System.err.println("  dump-rdf [output-options] [connection-options] jdbcURL");
		System.err.println("  dump-rdf [output-options] [connection-options] -l script.sql");
		System.err.println();
		printStandardArguments(true, false);
		System.err.println();
		System.err.println("  RDF output options:");
		System.err.println("    -b baseURI      Base URI for RDF output (default: " + SystemLoader.DEFAULT_BASE_URI + ")");
		System.err.println("    -f format       One of N-TRIPLE (default), RDF/XML, RDF/XML-ABBREV, TURTLE");
		System.err.println("    -o outfile      Output file name (default: stdout)");
		System.err.println("    -s SRID (EPSG)  EPSG code (default: 4326)");
		System.err.println("    --verbose       Print debug information");
		System.err.println();
		System.err.println("  Database connection options (only with jdbcURL):");
		printConnectionOptions(true);
		System.err.println();
		System.exit(1);
	}
	
	private ArgDecl baseArg = new ArgDecl(true, "b", "base");
	private ArgDecl formatArg = new ArgDecl(true, "f", "format");
	private ArgDecl outfileArg = new ArgDecl(true, "o", "out", "outfile");

	public void initArgs(CommandLine cmd) {
		cmd.add(baseArg);
		cmd.add(formatArg);
		cmd.add(outfileArg);
	}
	
	public void run(CommandLine cmd, SystemLoader loader) throws IOException {
		if (cmd.numItems() == 1) {
			loader.setMappingFileOrJdbcURL(cmd.getItem(0));
		}

		String format = "N-TRIPLE";
		if (cmd.hasArg(formatArg)) {
			format = cmd.getArg(formatArg).getValue();
		}
		if (rdfFormat != null) {
			format = rdfFormat;
		}
		
		PrintStream out;
		if (cmd.hasArg(outfileArg)) {
			File f = new File(cmd.getArg(outfileArg).getValue());
			log.info("Writing to " + f);
			out = new PrintStream(new FileOutputStream(f));
			loader.setSystemBaseURI(D2RQReader.absolutizeURI(f.toURI().toString() + "#"));
		} else {
			log.info("Writing to stdout");
			out = System.out;
		}
		if (cmd.hasArg(baseArg)) {
			String baseIRI = cmd.getArg(baseArg).getValue();
			if (!baseIRI.endsWith("/")) {
				baseIRI = baseIRI + "/";
			}
			loader.setSystemBaseURI(baseIRI);
		}
		CompiledMapping mapping = loader.getMapping();
		try {
			// Trigger compilation
			mapping.getTripleRelations();
			for (SQLConnection db: mapping.getSQLConnections()) {
				db.setDefaultFetchSize(DUMP_DEFAULT_FETCH_SIZE);
			}	

			Model d2rqModel = loader.getModelD2RQ();

			try {
				RDFWriter writer = d2rqModel.getWriter(format.toUpperCase());
				if (format.equals("RDF/XML") || format.equals("RDF/XML-ABBREV")) {
					writer.setProperty("showXmlDeclaration", "true");
					if (loader.getResourceBaseURI() != null) {
						writer.setProperty("xmlbase", loader.getResourceBaseURI());
					}
				}
				writer.write(d2rqModel, new OutputStreamWriter(out, "utf-8"), loader.getResourceBaseURI());
			} catch (NoWriterForLangException ex) {
				throw new D2RQException("Unknown format '" + format + "'", D2RQException.STARTUP_UNKNOWN_FORMAT);
			} catch (UnsupportedEncodingException ex) {
				throw new RuntimeException("Can't happen -- utf-8 is always supported");
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			out.close();
			mapping.close();
		}
	}
	public String getMapping() {
		return mapping;
	}
	public void setMapping(String mapping) {
		this.mapping = mapping;
	}
}
