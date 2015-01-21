package eu.linkedeodata.geotriples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.d2rq.SystemLoader;
import org.d2rq.mapgen.MappingGenerator;


/**
 * Command line utility for dumping a database to RDF, using the
 * {@link MappingGenerator} or a mapping file.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class dump_rdf {
	private final static Log log = LogFactory.getLog(dump_rdf.class);
	protected String guimapping =null;
	public static void main(String[] args) throws Exception {
		new dump_rdf().process(args);
		
	}
	public dump_rdf()
	{
		//do nothing
	}
	public dump_rdf(String guimapping)
	{
		this.guimapping=guimapping;
	}
	public void process(String [] args) throws Exception {
		boolean shFound = false;
		for (int i=0 ; i<args.length ; i++) {
			if (args[i].equals("-sh")) {
				shFound = true;
				if (i == args.length - 1) {
					usage();
				}
				else {
					String inputFile = args[i+1];
					if (inputFile.endsWith(".kml")) {
						log.info("KML detected for processing");
						System.out.println("Currently KML is not implemented within WP2 (soon)");
						//(new eu.linkedeodata.geotriples.kml.dump_rdf()).process(args);
					}
					else if (inputFile.endsWith(".shp")) {
						log.info("Shapefile detected for processing");
						if(guimapping!=null)
						{
							(new eu.linkedeodata.geotriples.shapefile.dump_rdf()).process(args,guimapping);
						}
						else
						{
							(new eu.linkedeodata.geotriples.shapefile.dump_rdf()).process(args);
						}
						
					}
					else if (inputFile.endsWith(".pdf")) {
						log.info("GeoPDF detected for processing");
						System.out.println("Currently GeoPDF is not implemented within WP2 (soon)");
					}
					else {
						//log.info("Database detected for processing");
						//(new d2rq.dump_rdf()).process(args);
						usage();
					}
				}
			}
		}
		if (!shFound) {
			log.info("Database detected for processing");
			(new d2rq.dump_rdf()).process(args);
		}
	}
	
	public static void usage() {
		System.err.println("usage:");
		System.err.println("  dump-rdf [output-options] mappingFile");
		System.err.println();
		System.err.println("  RDF output options:");
		System.err.println("    -sh       		data source file");
		System.err.println("    -b baseURI      Base URI for RDF output (default: " + SystemLoader.DEFAULT_BASE_URI + ")");
		System.err.println("    -f format       One of N-TRIPLE (default), RDF/XML, RDF/XML-ABBREV, TURTLE");
		System.err.println("    -o outfile      Output file name (default: stdout)");
		System.err.println("    --verbose       Print debug information");
		System.err.println();
		System.err.println("  Database connection options (only with jdbcURL):");
		System.err.println();
		System.exit(1);
	}
	
}
