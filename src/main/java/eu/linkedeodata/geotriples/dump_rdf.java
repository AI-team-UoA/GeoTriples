package eu.linkedeodata.geotriples;

import java.util.ArrayList;
import java.util.List;

import jena.cmdline.ArgDecl;
import jena.cmdline.CommandLine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.d2rq.SystemLoader;
import org.d2rq.mapgen.MappingGenerator;

import be.ugent.mmlab.rml.main.MainTrans;


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
		boolean rmlFound=false;
		for (int i=0 ; i<args.length ; i++) {
			if (args[i].equals("-rml")) {
				rmlFound = true;
			}
		}
		if(rmlFound)
		{
			log.info("RML processor selected.");
			
			/*Trasfer correct argument to RML processor*/
			final ArgDecl rmlArg = new ArgDecl(false, "rml", "rmlprocessor");
			final ArgDecl epsgArg = new ArgDecl(true, "s", "srid");
			final ArgDecl outfileArg = new ArgDecl(true, "o", "out", "outfile");
			final ArgDecl formatArg = new ArgDecl(true, "f", "format", "output RDF Fromat");
			final ArgDecl namespacesArg = new ArgDecl(true, "ns", "namespace", "input file of namespaces");
			final CommandLine cmd = new CommandLine();
			cmd.add(rmlArg);
			cmd.add(epsgArg);
			cmd.add(outfileArg);
			cmd.add(formatArg);
			cmd.add(namespacesArg);
			
			try {
				cmd.process(args);
			} catch (IllegalArgumentException ex) {
				if (ex.getMessage() == null) {
					ex.printStackTrace(System.err);
				} else {
					System.err.println(ex.getMessage());
				}
				log.info("Command line tool exception", ex);
				System.exit(1);
			}
			
			if (cmd.numItems() == 0) {
				usage();
				System.exit(1);
			}
			String[] pipeargs;
			if(!cmd.contains(outfileArg))
			{
				usage();
				System.exit(1);
			}
			List<String> arglist=new ArrayList<>();
			if(cmd.contains(epsgArg)){
				arglist.add("-epsg");
				arglist.add(cmd.getArg(epsgArg).getValue());
			}
			if(cmd.contains(formatArg)){
				arglist.add("-f");
				arglist.add(cmd.getArg(formatArg).getValue());
			}
			if(cmd.contains(namespacesArg)){
				arglist.add("-ns");
				arglist.add(cmd.getArg(namespacesArg).getValue());
			}
			arglist.add(cmd.getItem(0));
			arglist.add(cmd.getArg(outfileArg).getValue());
			
			pipeargs=arglist.toArray(new String[arglist.size()]);
			
			/*if(cmd.contains(epsgArg) && !cmd.contains(formatArg))
			{
				pipeargs=new String[]{"-epsg",cmd.getArg(epsgArg).getValue(),cmd.getItem(0),cmd.getArg(outfileArg).getValue()};
				//log.info(pipeargs.length);
			}
			
			if(cmd.contains(epsgArg) && cmd.contains(formatArg) && cmd.contains(namespacesArg))
			{
				pipeargs=new String[]{"-epsg",cmd.getArg(epsgArg).getValue(),"-f", cmd.getArg(formatArg).getValue(),"-ns",cmd.getArg(namespacesArg).getValue(), cmd.getItem(0),cmd.getArg(outfileArg).getValue()};
			}
			else if(cmd.contains(epsgArg) && cmd.contains(formatArg) && !cmd.contains(namespacesArg)){
				pipeargs=new String[]{"-epsg",cmd.getArg(epsgArg).getValue(),"-f", cmd.getArg(formatArg).getValue(), cmd.getItem(0),cmd.getArg(outfileArg).getValue()};
			}
			else
			{
				pipeargs=new String[]{cmd.getItem(0),cmd.getArg(outfileArg).getValue()};
				//log.info("DERP");
			}*/
			
			MainTrans.main(pipeargs);
			
			return;
		}
		for (int i=0 ; i<args.length ; i++) {
			if (args[i].equals("-sh")) {
				shFound = true;
				if (i == args.length - 1) {
					usage();
				}
				else {
					String inputFile = args[i+1];
					if (inputFile.endsWith(".kml") || inputFile.endsWith(".xml") || inputFile.endsWith(".gml")) {
						log.info("XML-like file detected for processing");
						log.info("RML processor selected.");
						
						/*Trasfer correct argument to RML processor*/
						final ArgDecl epsgArg = new ArgDecl(true, "s", "srid");
						final ArgDecl outfileArg = new ArgDecl(true, "o", "out", "outfile");
						final CommandLine cmd = new CommandLine();
						
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
		System.err.println("    -rml            Use the RML processor(XML,JSON,csv files), IMPORTANT: -o option is necessary(for now)");
		System.err.println();
		System.err.println("  Database connection options (only with jdbcURL):");
		System.err.println();
		System.exit(1);
	}
	
}
