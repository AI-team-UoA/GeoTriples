package eu.linkedeodata.geotriples.shapefile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import jena.cmdline.ArgDecl;
import jena.cmdline.CommandLine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.d2rq.mapgen.MappingGenerator;
import org.d2rq.mapgen.OntologyTarget;

import eu.linkedeodata.geotriples.Config;
import eu.linkedeodata.geotriples.GeneralMappingGenerator;
import eu.linkedeodata.geotriples.gui.RecipeMapping;

/**
 * Command line interface for {@link MappingGenerator}.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class generate_mapping extends ShapefileCommandLineTool {
	private final static Log log = LogFactory.getLog(generate_mapping.class);
	protected static RecipeMapping receipt=null;
	protected java.io.PrintStream guiOutput=null;
	
	/**
	 * @param receipt the details of mapping rules (from gui)
	 * @param guioutstream the output stream to generate mapping - (from gui)
	 */
	@SuppressWarnings("static-access")
	public generate_mapping(RecipeMapping receipt,PrintStream guioutstream)
	{
		this.receipt=receipt; //set the receipt for mapping
		this.guiOutput=guioutstream;
	}
	/**
	 * Default constructor
	 */
	public generate_mapping() {
		//do nothing
	}
	public static void main(String[] args) {
		try {
			new generate_mapping().process(args,receipt);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(13);
		}
	}
	
	public void usage() {
		/*System.err.println("usage: generate-mapping [options] shapefileURL");
		System.err.println();
		printStandardArguments(false, false);
		System.err.println("  Options:");
		printConnectionOptions(true);
		System.err.println("    -o outfile.ttl  Output file name (default: stdout)");
		System.err.println("    -v              Generate RDFS+OWL vocabulary instead of mapping file");
		System.err.println("    --verbose       Print debug information");
		System.err.println("    -b <baseIR> e.g. http://geo.linkedopendata.gr/natura");
		System.err.println();*/
		System.exit(1);
	}

	private ArgDecl outfileArg = new ArgDecl(true, "o", "out", "outfile");
	private ArgDecl vocabAsOutput = new ArgDecl(false, "v", "vocab");
	private ArgDecl geoVocabulary = new ArgDecl(true, "geov", "geoVocabulary");
	private ArgDecl baseIri = new ArgDecl(true, "b", "baseIRI");
	
	public void initArgs(CommandLine cmd) {
		cmd.add(outfileArg);
		cmd.add(vocabAsOutput);
		cmd.add(baseIri);
		cmd.add(geoVocabulary);
	}

	public void run(CommandLine cmd, ShapefileSystemLoader loader) throws Exception {
		if (cmd.numItems() == 1) {
			loader.setURL(cmd.getItem(0));
		}
		
		
		loader.setGenerateR2RML(true);
		
		PrintStream out;
		if(guiOutput!=null)
		{
			out=guiOutput;
		}
		else if (cmd.contains(outfileArg)) {
			File f = new File(cmd.getArg(outfileArg).getValue());
			log.info("Writing to " + f);
			out = new PrintStream(new FileOutputStream(f));
		} else {
			log.info("Writing to stdout");
			out = System.out;
		}
		
		GeneralMappingGenerator generator = null;
		if (cmd.contains(geoVocabulary)) {
			String voc = cmd.getArg(geoVocabulary).getValue();
			if (voc != null) {
				if (voc.equals("GeoSPARQL") || voc.equals("stRDF")) {
					Config.VOCABULARY = voc;
				}
			}
		}
		if (cmd.contains(baseIri)) {
			String fileName = cmd.getItem(0);
			File file = new File(fileName);
			fileName = file.getName();
			generator = loader.getMappingGenerator(cmd.getArg(baseIri).getValue(), fileName);
		}
		else {
			log.error("No base IRI specified");
			usage();
			System.exit(-1);
		}
		try {
			if (cmd.contains(vocabAsOutput)) {
				OntologyTarget target = new OntologyTarget();
				generator.generate(target, receipt);
				target.getOntologyModel().write(out, "TURTLE");
			} else {
				loader.getWriter().write(out);
			}
		} finally {
			loader.close();
		}
	}

}
