package eu.linkedeodata.geotriples;

import java.io.PrintStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.d2rq.mapgen.MappingGenerator;

import eu.linkedeodata.geotriples.gui.RecipeMapping;


/**
 * Command line interface for {@link MappingGenerator}.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class generate_mapping {
	private final static Log log = LogFactory.getLog(generate_mapping.class);
	private RecipeMapping recipe=null;
	protected java.io.PrintStream guiOutput=null;
	
	public static void main(String[] args) throws Exception {
		//invoke the appropriate generate mapping
		new generate_mapping().process(args);
	}
	
	public generate_mapping(RecipeMapping receipt,PrintStream guiOutput)
	{
		this.recipe=receipt; //set the receipt for mapping
		this.guiOutput=guiOutput;
	}
	
	public generate_mapping() {
		//do nothing
	}

	public void process(String [] args) throws Exception {
		if (args.length >= 1) {
			String lastToken = args[args.length -1];
			if (lastToken.endsWith(".kml")) {
				log.info("KML detected for processing");
				//System.out.println("Currently KML is not implemented within WP2 (soon)");
				(new eu.linkedeodata.geotriples.kml.generate_mapping(this.recipe,this.guiOutput)).process(args);
			}
			else if (lastToken.endsWith(".shp")) {
				log.info("Shapefile detected for processing");
				if(this.recipe!=null)
				{
					(new eu.linkedeodata.geotriples.shapefile.generate_mapping(this.recipe,this.guiOutput)).process(args,this.recipe);
				}
				else
				{
					(new eu.linkedeodata.geotriples.shapefile.generate_mapping(this.recipe,this.guiOutput)).process(args);
				}
			}
			else if (lastToken.endsWith(".pdf")) {
				log.info("GeoPDF detected for processing");
				System.out.println("Currently GeoPDF is not implemented within WP2 (soon)");
			}
			else {
				log.info("Database detected for processing");
				(new d2rq.generate_mapping()).process(args);
			}
		}
		else {
			usage();
		}
	}
	
	public static void usage() {
		System.err.println("usage: generate-mapping [options] inputFileURL");
		System.err.println();
		System.err.println("  Options:");
		System.err.println("    -o outfile.ttl  Output file name (default: stdout)");
		System.err.println("    -v              Generate RDFS+OWL vocabulary instead of mapping file");
		System.err.println("    --verbose       Print debug information");
		System.err.println("    -b <baseIR> e.g. http://geo.linkedopendata.gr/natura");
		System.err.println();
		System.exit(1);
	}

}
