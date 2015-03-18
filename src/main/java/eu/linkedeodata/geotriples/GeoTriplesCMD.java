package eu.linkedeodata.geotriples;

import java.util.Arrays;

public class GeoTriplesCMD {
	
	public static void main(String [] args) throws Exception {
		if (args.length == 0) {
			usage();
		}
		String mode = args[0];
		String[] yourArray = Arrays.copyOfRange(args, 1, args.length);
		if (mode.equals("generate_mapping")) {
			new generate_mapping().process(yourArray);
		}
		else if (mode.equals("dump_rdf")) {
			try {
				new dump_rdf().process(yourArray);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			usage();
		}
	}
	
	public static void usage() {
		System.err.println("Usage: geotriples-cmd [mode] [options] source/mapping");
		System.err.println("\tmode:");
		System.err.println("\t\tgenerate_mapping");
		System.err.println("\t\tdump_rdf");
		System.err.println();
		System.err.println("Usage for generate_mapping: geotriples-cmd generate_mapping --r2rml [options] source ");
		System.err.println();
		System.err.println("\toptions:");
		System.err.println("\t\t-o outfile\tOutput mapping file name (default: stdout)");
		System.err.println("\t\t-b base IRI\te.g., http://data.linkedeodata.eu/natura");
		System.err.println("\t\t-u username\tDatabase Username");
		System.err.println("\t\t-p password\tDatabase Password");
		System.err.println("\tsource\t input shape file path or jdbc URL");
		System.err.println();
		System.err.println("Usage for dump_rdf: geotriples-cmd dump_rdf [options] mapping");
		System.err.println();
		System.err.println("\tOptions:");
		System.err.println("\t\t-o outfile\t\tOutput file name (default: stdout)");
		System.err.println("\t\t-sh source file\t\tInput Source Shapefile");
		System.err.println("\t\t-b base IRI\t\te.g., http://data.linkedeodata.eu/talking-fields");
		System.err.println("\t\t-u username\t\tDatabase Username");
		System.err.println("\t\t-p password\t\tDatabase Password");
		System.err.println("\t\t-jdbc JDBC URL\t\tThe JDBC URL of the input database");
		System.err.println("\t\t-f format\t\tCan be N3, RDF/XML, TURTLE (default: N3)");
		System.err.println("\tmapping\tinput mapping file (R2RML)");
		System.err.println();
		System.exit(1);
	}
}
