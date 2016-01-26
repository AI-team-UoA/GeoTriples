package eu.linkedeodata.geotriples;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.d2rq.SystemLoader;
import org.d2rq.mapgen.MappingGenerator;

import be.ugent.mmlab.rml.main.MainTrans;
import be.ugent.mmlab.rml.tools.PrintTimeStats;
import jena.cmdline.ArgDecl;
import jena.cmdline.CommandLine;


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
	final CommandLine cmd = new CommandLine();
	List<ArgDecl> argdecls=new ArrayList<ArgDecl>();
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
			
			final ArgDecl gml3Arg = new ArgDecl(false, "gml3", "Use gml3 reader for the geometries");
			final ArgDecl gml2Arg = new ArgDecl(false, "gml2", "Use gml2 reader for the geometries");
			final ArgDecl kmlArg = new ArgDecl(false, "kml", "Use kml reader for the geometries");
			
			final ArgDecl useReadyEOPMappingArg = new ArgDecl(false, "readyeop", "Use GeoTriples' EOP 2.0 mapping document");
			final ArgDecl useReadyKMLMappingArg = new ArgDecl(false, "readykml", "Use GeoTriples' KML 2.2 mapping document");
			final ArgDecl useReadyNetCDFMappingArg = new ArgDecl(false, "readynetcdf", "Use GeoTriples' NetCDF 2.2 mapping document");
			
			final ArgDecl inputFile=new ArgDecl(true, "i", "Input source. Use with -readyeop or -readykml");
			final ArgDecl useGDALLibraryArg = new ArgDecl(false, "gdal", "Use GDAL as the library for the manipulation of the Geometries (Default is GeoTools)");
			final ArgDecl useOldDBProcessorArg = new ArgDecl(false, "olddbprocessor", "Use old DB processor, it executes only one query per triples map with all projections in place, the alternative is to pose multiple queries with one projection at a time over the effective query");
			cmd.add(rmlArg);
			argdecls.add(rmlArg);
			
			cmd.add(epsgArg);
			argdecls.add(epsgArg);
			
			cmd.add(outfileArg);
			argdecls.add(outfileArg);
			
			cmd.add(formatArg);
			argdecls.add(formatArg);
			
			cmd.add(namespacesArg);
			argdecls.add(namespacesArg);
			
			cmd.add(gml3Arg);
			argdecls.add(gml3Arg);
			
			cmd.add(gml2Arg);
			argdecls.add(gml2Arg);
			
			cmd.add(kmlArg);
			argdecls.add(kmlArg);
			
			
			
			cmd.add(useReadyEOPMappingArg);
			argdecls.add(useReadyEOPMappingArg);
			
			cmd.add(useReadyKMLMappingArg);
			argdecls.add(useReadyKMLMappingArg);
			
			cmd.add(useReadyNetCDFMappingArg);
			argdecls.add(useReadyNetCDFMappingArg);
			
			cmd.add(useGDALLibraryArg);
			argdecls.add(useGDALLibraryArg);
			
			cmd.add(useOldDBProcessorArg);
			argdecls.add(useOldDBProcessorArg);
			
			cmd.add(inputFile);
			argdecls.add(inputFile);
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
			
			if (cmd.numItems() == 0 && !(cmd.contains(useReadyKMLMappingArg) || cmd.contains(useReadyEOPMappingArg))) {
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
			if(cmd.contains(gml3Arg)){
				arglist.add("-gml3");
			}
			if(cmd.contains(gml2Arg)){
				arglist.add("-gml2");
			}
			if(cmd.contains(kmlArg)){
				arglist.add("-kml");
			}
			
			if(cmd.contains(useReadyEOPMappingArg)){
				arglist.add("-readyeop");
			}
			if(cmd.contains(useReadyKMLMappingArg)){
				arglist.add("-readykml");
			}
			if(cmd.contains(useReadyKMLMappingArg)){
				arglist.add("-readynetcdf");
			}
			if(cmd.contains(useGDALLibraryArg)){
				arglist.add("-gdal");
			}
			if(cmd.contains(useOldDBProcessorArg)){
				arglist.add("-olddbprocessor");
			}
			if(cmd.contains(inputFile)){
				arglist.add("-i");
				arglist.add(cmd.getArg(inputFile).getValue());
			}

			if(!cmd.contains(useReadyEOPMappingArg) && !cmd.contains(useReadyKMLMappingArg)){
				arglist.add(cmd.getItem(0));
			}
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
			HelpFormatter hh=new HelpFormatter();
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
						System.err.println("Currently GeoPDF is not implemented within WP2 (soon)");
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
			PrintTimeStats.printTime("Total time for reading results from database",PrintTimeStats.getDuration());
		}
	}
	
	public void usage() {
		/*System.err.println("usage:");
		System.err.println("  dump-rdf [output-options] mappingFile");
		System.err.println();
		for(ArgDecl ad:argdecls){
			
			System.err.println(ad.toString());
		}*/
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
