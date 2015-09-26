package eu.linkedeodata.geotriples;

import java.util.Arrays;

public class GeoTriplesCMD {
	
	public static void main(String [] args) throws Exception {
		long startTime = System.currentTimeMillis();
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
		}else if(mode.equals("obda")){
			String[] ontopargs = Arrays.copyOfRange(args, 1, args.length);
	        org.semanticweb.ontop.cli.Ontop.main(ontopargs);
		}
		else {
			usage();
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Took "+(endTime - startTime)/1000 + " sec");
	}
	
	public static void usage() {
		System.err.println("Usage: geotriples-cmd [mode] [options] source|[mapping]");
		System.err.println("\tmode:");
		System.err.println("\t\tgenerate_mapping");
		System.err.println("\t\tdump_rdf");
		System.err.println();
		System.err.println("Usage for generate_mapping: geotriples-cmd generate_mapping --r2rml [options] source ");
		System.err.println();
		System.err.println("\toptions:");
		System.err.println("\t\t-o outfile\tOutput mapping file name (default: stdout)");
		System.err.println("\t\t-b base IRI\te.g., http://data.linkedeodata.eu/natura");
		System.err.println("\t\t-u username\tDatabase Username (Optional)");
		System.err.println("\t\t-p password\tDatabase Password (Optional)");
		
		/*XSD stuff*/
		System.err.println("\t\t-x xsdfile\t\tThe xsd file (Required for XMLs)");
		System.err.println("\t\t-r xpath\t\tRoot element eg a:Item (Optional)");
		System.err.println("\t\t-rp xpath\t\tXpath path to parent element of root element (see -r) eg /a:AllItems (Optional)");
		System.err.println("\t\t-onlyns namespace\t\tUse root elements only with this namespace eg http://www.opengis.net/gml (Optional)");
		System.err.println("\t\t-ns namespaces\t\tNamespaces used in xsd eg \"kml|http://www.opengis.net/kml/2.2,kml:gml:http://www.opengis.net/gml\" or a property file (Optional)");
		System.err.println("\t\t-ont ontologyoutputfile\t\tOntology output file eg ontology.nt (Optional)");
		
		System.err.println("\tsource\t input Shapefile or XML or GML or KML file path or jdbc URL");
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
		System.err.println("\tArguments:");
		System.err.println("\t\t-rml \t\tUse RML processor (Optional)");
		System.err.println("\tmapping\tinput mapping file (R2RML or RML using -rml)");
		System.err.println();

		System.err.println("Usage for obda: geotriples-cmd obda [commands]");
		
		System.exit(1);
	}
}
