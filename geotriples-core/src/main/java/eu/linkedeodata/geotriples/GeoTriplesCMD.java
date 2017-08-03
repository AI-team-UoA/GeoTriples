package eu.linkedeodata.geotriples;

import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class GeoTriplesCMD {

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		if (args.length == 0) {
			usage();
		}
		String mode = args[0];
		String[] yourArray = Arrays.copyOfRange(args, 1, args.length);
		if (mode.equals("generate_mapping")) {
			new generate_mapping().process(yourArray);
		} else if (mode.equals("dump_rdf")) {
			try {
				new dump_rdf().process(yourArray);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (mode.equals("obda")) {
			String[] ontopargs = Arrays.copyOfRange(args, 1, args.length);
			Class<?> ontop = null;
			try {
				// org.semanticweb.ontop.cli.Ontop.main(ontopargs);
				ontop=Class.forName("org.semanticweb.ontop.cli.Ontop");
				Class<?>[] argTypes = new Class[] { String[].class };
				Method main = ontop.getMethod("main", argTypes);
				main.invoke(null, (Object)ontopargs);
			} catch (ClassNotFoundException e) {
				System.err.println("Missing libraries for ontop-spatial??");
				System.err.println("Is maven configured to add jars in ontop-spatial-lib/ direcotry??");
				e.printStackTrace();
			} catch (NoSuchMethodException x) {
			    x.printStackTrace();
			} catch (IllegalAccessException x) {
			    x.printStackTrace();
			} catch (InvocationTargetException x) {
			    x.printStackTrace();
			}
			return;
		} else {
			usage();
		}
		long endTime = System.currentTimeMillis();
//		System.out.println("Took " + (endTime - startTime) / 1000 + " sec");
		System.out.println("Took " + (endTime - startTime)  + " msec");
		//FileOutputStream logfile=new FileOutputStream(new File())
	}

	public static void usage() {
		System.err.println(".___________________________________________________________.");
		System.err.println("|\\._______________________________________________________./|");
		System.err.println("|\\|                       GeoTriples                      |/|");
		System.err.println("|\\|  a tool for transforming EO/geospatial data into RDF  |/|");
		System.err.println(" \\._______________________________________________________./ ");

		System.err.println();
		System.err.println("Usage: geotriples-cmd [mode] [options] <source>|[mapping]");
		System.err.println("\tModes:");
		System.err.println("\tgenerate_mapping -- Generate a mapping file for a:");
		System.err.println("\t\t\t\t1. Relational Database (MonetDB, Postgresql, Mysql etc.)");
		System.err.println("\t\t\t\t   Support for geometries only with MonetDB and Postgresql");
		System.err.println("\t\t\t\t2. Shapefile");
		System.err.println("\t\t\t\t3. XML (GML, KML etc), accompanied with an XSD schema definition");

		System.err.println("\tdump_rdf\t -- Convert the input to an RDF graph, using a mapping file. Input can be");
		System.err.println("\t\t\t\t1. Relational Database (MonetDB, Postgresql, Mysql etc.)");
		System.err.println("\t\t\t\t2. Shapefile");
		System.err.println("\t\t\t\t3. XML (GML, KML, etc)");
		System.err.println("\t\t\t\t4. JSON (GeoJSON)");
		System.err.println("\t\t\t\t5. CSV");

		System.err.println("\tobda\t\t -- Ontology Based Data Access");
		System.err.println();
		
		
		
		System.err.println("Information");
		System.err.println("-----------");
		System.err.println("A. Usage for generate_mapping: geotriples-cmd generate_mapping [options] <source> ");
		System.err.println("\t<source>\tinput Shapefile or XML or GML or KML file path or jdbc URL");
		System.err.println();
		System.err.println("\tOptions:");
		System.err.println("\tDatabase options:");
		System.err.println("\t\t-u username\t\tDatabase Username (Optional)");
		System.err.println("\t\t-p password\t\tDatabase Password (Optional)");
		/* Shapefile mapping options*/
		System.err.println("\tShapefile options:");
		System.err.println("\t\t-s epsgcode\t\tUse this epsg code for geometries. Useful when shapefile doesn't have the CRS defined. (Optional)");
		/* XML mapping options*/
		System.err.println("\tXML/GML/KML options:");
		System.err.println("\t\t-x xsdfile\t\tThe xsd file (Required for XMLs)");
		System.err.println("\t\t-r rootelement\t\tRoot element eg a:Item (Optional)");
		System.err.println(
				"\t\t-rp xpath\t\tXpath path to parent element of root element (see -r) eg /a:AllItems (Optional)");
		System.err.println(
				"\t\t-onlyns namespace\tUse root elements only with this namespace eg http://www.opengis.net/gml (Optional)");
		System.err.println(
				"\t\t-ns namespaces\t\tNamespaces used in -rp argument eg \"kml|http://www.opengis.net/kml/2.2,kml:gml:http://www.opengis.net/gml\" or a property file (Optional)");
		System.err.println("\t\t-ont ontologyfile\tOntology output file eg ontology.nt (Optional)");
		
		System.err.println("\tGeneral options:");
		System.err.println("\t\t-o outfile\t\tOutput mapping file name (default: stdout)");
		System.err.println("\t\t-b base IRI\\tte.g., http://data.linkedeodata.eu/natura");
		System.err.println("\t-geov vocabulary\t\tTarget geospatial vocabulary (GeoSPARQL/stRDF)");
		System.err.println("\tExamples:");
		System.err.println("\tShapefile (R2RML):\tgeotriples-cmd generate_mapping -o mapping.ttl -b http://example.com shapefile.shp");
		System.err.println("\tShapefile   (RML):\tgeotriples-cmd generate_mapping -o mapping.ttl -b http://example.com shapefile.shp");
		System.err.println("\tXML         (RML):\tgeotriples-cmd generate_mapping -o mapping.ttl -b http://example.com xmlfile.xml");
		System.err.println("\tJSON        (RML):\tgeotriples-cmd generate_mapping -o mapping.ttl -b http://example.com jsonfile.json");
		System.err.println("\tDatabase  (R2RML):\tgeotriples-cmd generate_mapping -o mapping.ttl -b http://example.com -u dbuser -p dbpassword jdbc:postgresql://localhost:5432/dbname");
		
		System.err.println();
		
		System.err.println("B. Usage for dump_rdf: geotriples-cmd dump_rdf [options] mapping");
		System.err.println("\tmapping\t\tmappingfile (R2RML or RML using -rml)");
		System.err.println();
		System.err.println("\tOptions:");
		System.err.println("\tDatabase options:");
		System.err.println("\t\t-u username\t\tDatabase Username (Optional)");
		System.err.println("\t\t-p password\t\tDatabase Password (Optional)");
		System.err.println("\t\t-jdbc JDBC URL\t\tThe JDBC URL of the input database");
		
		/* Shapefile mapping options*/
		System.err.println("\tShapefile options:");
		System.err.println("\t\t-sh source file\t\tInput Source Shapefile");
		/* XML mapping options*/
		System.err.println("\tXML/GML/KML options:");
		System.err.println("\t\t-i inputfile\t\tInput XML/GML/KML file if used with the -readyeop or -readykml arguments");
		System.err.println("\t\t-readyeop\t\tUse the embended EOP mapping of GeoTriples. You must provide the input file with -i argument");
		System.err.println("\t\t-readykml\t\tUse the embended KML mapping of GeoTriples. You must provide the input file with -i argument");
		
		System.err.println("\tGeneral options:");
		System.err.println("\t\t-o outfile\t\tOutput file name (default: stdout)");
		System.err.println("\t\t-b base IRI\t\te.g., http://data.linkedeodata.eu/talking-fields");
		System.err.println("\t\t-f format\t\tCan be N3, RDF/XML, TURTLE (default: N3)");
		System.err.println("\t\t-rml\t\t\tUse RML processor (Default is R2RML processor)");

		System.err.println("\tExamples:");
		System.err.println("\tShapefile (R2RML):\tgeotriples-cmd dump_rdf -o out.nt -b http://example.com -sh shapefile.shp mapping.ttl");
		System.err.println("\tShapefile   (RML):\tgeotriples-cmd dump_rdf -o out.ttl -f TURTLE -b http://example.com mapping.ttl");
		System.err.println("\tXML         (RML):\tgeotriples-cmd dump_rdf -o out.nt -b http://example.com mapping.ttl");
		System.err.println("\tJSON        (RML):\tgeotriples-cmd dump_rdf -o out.xml -f RDFXML -b http://example.com mapping.ttl");
		System.err.println("\tDatabase  (R2RML):\tgeotriples-cmd dump_rdf -o out.nt -b http://example.com -u dbuser -p dbpassword -jdbc jdbc:postgresql://localhost:5432/dbname mapping.ttl");
		
		
		System.err.println();

		System.err.println("C. Usage for obda: geotriples-cmd obda [commands]");
		System.err.println("\tCommands:");
		System.err.println("\t\tbootstrap");
		System.err.println("\t\tmaterialize");
		System.err.println("\t\tquery");
		System.err.println("\t\tmapping");
		System.err.println("\tFor help, type geotriples-cmd obda help <command>");
		
		System.err.println();
		System.err.println("Developers: dimis@di.uoa.gr, johnvl@di.uoa.gr, Kostis.Kyzirakos@cwi.nl");
		System.exit(1);
	}
}
