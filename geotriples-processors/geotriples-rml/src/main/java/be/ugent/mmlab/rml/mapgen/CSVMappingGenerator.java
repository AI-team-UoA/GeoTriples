package be.ugent.mmlab.rml.mapgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import javax.activation.UnsupportedDataTypeException;

import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xmlbeans.XmlException;
import org.geotools.geometry.text.WKTParser;

import com.csvreader.CsvReader;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import be.ugent.mmlab.rml.core.OntologyGenerator;

public class CSVMappingGenerator extends RMLMappingGenerator {
	private final static Logger log = LoggerFactory.getLogger(XMLMappingGenerator.class);
	private HashMap<String, String> triplesMaps = new HashMap<>();

	private File ontologyOutputFile = null;

	OntologyGenerator ontology = null;
	private String pathToShapefile;

	private char separator = ',';

	public CSVMappingGenerator(String datasource_filename, String output_filename, String base_iri,
			String ontology_output_filename, char separator) throws ClassNotFoundException, InstantiationException,
					IllegalAccessException, ClassCastException, FileNotFoundException, XmlException, IOException {
		super(base_iri, output_filename);
		// System.out.println("shapefilefilename=" + shapefilefilename);
		// System.out.println("outputfile=" + outputfile);
		// System.out.println("baseiri" + baseiri);
		this.pathToShapefile = new File(datasource_filename).getAbsolutePath();
		// System.out.println("ontologyOutputFile=" + ontologyOutputFile);
		this.separator = separator;
		if (ontology_output_filename != null) {
			this.ontologyOutputFile = new File(ontology_output_filename);
			ontology = new OntologyGenerator(true, baseURI);
		}
	}

	public void run() throws IOException {
		CsvReader reader = new CsvReader(new FileInputStream(pathToShapefile), Charset.defaultCharset());
		reader.setDelimiter(separator);
		reader.setSafetySwitch(false);

		reader.readHeaders();
		// Iterate the rows

		Path p = Paths.get(pathToShapefile);
		String tmp = p.getFileName().toString();
		String typeName = tmp.substring(0, tmp.lastIndexOf('.'));
		triplesMaps.put(typeName, "");
		triplesMaps.put(typeName, triplesMaps.get(typeName) + printTriplesMap(typeName));
		triplesMaps.put(typeName, triplesMaps.get(typeName) + printLogicalSource(typeName));
		triplesMaps.put(typeName, triplesMaps.get(typeName) + printSubjectMap(baseURI, typeName));

		boolean hasgeometry = false;
		String typeNameGeo = typeName + "_Geometry";
		WKTReader wktReader = new WKTReader();
		String geometry_identifier = null;
		boolean has_record = reader.readRecord();
		for (String header : reader.getHeaders()) {
			String identifier = header;
			if (identifier.equalsIgnoreCase("the_geom")) {
				geometry_identifier = identifier;
				hasgeometry = true;
				continue;
			}
			if (has_record) {
				try {
					if (wktReader.read(reader.get(identifier)) != null) {
						hasgeometry = true;
						geometry_identifier = identifier;
						continue;
					}
				} catch (ParseException e1) {
					System.err.println(reader.get(identifier));
				}
			}

			String datatype = TranslateDataTypeToXSD("String");
			triplesMaps.put(typeName,
					triplesMaps.get(typeName) + printPredicateObjectMap(identifier, identifier, datatype, typeName));

		}

		if (hasgeometry) {
			triplesMaps
					.put(typeName,
							triplesMaps.get(typeName) + printPredicateObjectMap(true, "hasGeometry",
									baseURI + (baseURI.endsWith("/") ? "" : "/") + typeName
											+ "/Geometry/{GeoTriplesID}",
									null, null, "ogc", null, typeName, true, false));
			triplesMaps.put(typeNameGeo, "");
			triplesMaps.put(typeNameGeo, triplesMaps.get(typeNameGeo) + printTriplesMap(typeNameGeo));
			triplesMaps.put(typeNameGeo, triplesMaps.get(typeNameGeo) + printLogicalSource(typeName));
			triplesMaps.put(typeNameGeo,
					triplesMaps.get(typeNameGeo) + printSubjectMap(baseURI, typeName, "ogc", true));
			triplesMaps.put(typeNameGeo,
					triplesMaps.get(typeNameGeo) + printGEOPredicateObjectMaps(geometry_identifier));
		}
		printmapping();
		printontology();
	}

	private void printontology() throws FileNotFoundException {
		if (ontologyOutputFile != null) {
			ontology.writeToOutput(new PrintStream(ontologyOutputFile));
		}

	}

	private void printmapping() throws FileNotFoundException {
		PrintStream out = outputfile == null ? System.out : new PrintStream(outputfile);
		out.println(
				"@prefix rr: <http://www.w3.org/ns/r2rml#>.\n" + "@prefix  rml: <http://semweb.mmlab.be/ns/rml#> .\n"
						+ "@prefix ql: <http://semweb.mmlab.be/ns/ql#> .\n"
						+ "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.\n"
						+ "@base <http://geotriples.eu/base> .\n" + "@prefix rrx: <http://www.w3.org/ns/r2rml-ext#>.\n"
						+ "@prefix rrxf: <http://www.w3.org/ns/r2rml-ext/functions/def/>.\n"
						+ "@prefix ogc: <http://www.opengis.net/ont/geosparql#>.\n"
						+ "@prefix schema: <http://schema.org/>.\n" + "@prefix onto: <" + baseURI + "ontology#>.\n");

		for (String triplesMap : triplesMaps.keySet()) {
			log.debug("TRIPLES MAP: " + triplesMap);
			if (triplesMaps.get(triplesMap).isEmpty() || triplesMaps.get(triplesMap).equals("null")
					|| triplesMaps.get(triplesMap) == null) {
				continue;
			}
			// System.out.println("TRIPLES MAP: " + triplesMap);
			/*
			 * if(triplesMap.equals("/hma:MaskFeature/a:name") ||
			 * triplesMap.equals
			 * ("/hma:maskMembers/hma:MaskFeature/a:extentOf")){
			 * System.out.println(triplesMaps .get(triplesMap)); }
			 */
			out.println(triplesMaps.get(triplesMap).trim().substring(0, triplesMaps.get(triplesMap).trim().length() - 1)
					+ ".\n");

			// log.debug("END TRIPLES MAP: "+triplesMap);
		}
		out.close();
	}

	public static String TranslateDataTypeToXSD(String name) throws UnsupportedDataTypeException {
		if (name.contains(".")) {
			String[] tokens = name.split("[.]");
			name = tokens[tokens.length - 1];
		}
		if (name.equals("String")) {
			return "xsd:string";
		} else if (name.equals("Int") || name.equals("Integer")) {
			return "xsd:integer";
		} else if (name.equals("Bool")) {
			return "xsd:boolean";
		} else if (name.equals("Long")) {
			return "xsd:long";
		} else if (name.equals("Double")) {
			return "xsd:double";
		} else if (name.equals("Date")) { // TODO to be checked !! date xsd??
			return "xsd:datetime";
		} else {
			throw new UnsupportedDataTypeException("Datatype '" + name + "' is not supported!");
		}
	}

	private String printPredicateObjectMap(boolean isTemplate, String predicate, String reference, String type,
			String classname, boolean isSubTypeOfGeometryClass) {
		return printPredicateObjectMap(isTemplate, predicate, reference, type, null, null, null, classname, false,
				isSubTypeOfGeometryClass);
	}

	private String printPredicateObjectMap(String predicate, String reference, String type, String classname) {
		return printPredicateObjectMap(false, predicate, reference, type, null, null, null, classname, false, false);
	}

	private String printPredicateObjectMap(String predicate, String reference, String type, String typeprefix,
			String predicatedprefix, String function, String classname, boolean isgeometrypredicate) {
		return printPredicateObjectMap(false, predicate, reference, type, typeprefix, predicatedprefix, function,
				classname, isgeometrypredicate, false);
	}

	private String printPredicateObjectMap(boolean isTemplate, String predicate, String reference, String type,
			String typeprefix, String predicatedprefix, String function, String classname, boolean isgeometrypredicate,
			boolean isSubtypeOfGeometryClass) {
		predicate = predicate.replace(".", "");
		StringBuilder sb = new StringBuilder();
		sb.append("rr:predicateObjectMap [\n");
		sb.append(
				"\trr:predicateMap [ rr:constant " + ((predicatedprefix == null) ? "onto" : (predicatedprefix)) + ":");
		if (!isgeometrypredicate) {
			predicate = "has" + WordUtils.capitalize(predicate, new char[] { '-' });
		}
		sb.append(predicate + " ];\n");
		sb.append("\trr:objectMap [\n");
		if (type != null) {
			sb.append("\t\trr:datatype " + " " + type + ";\n");
		}
		if (function != null) {
			sb.append("\t\trrx:function rrxf:" + function + ";\n");
			sb.append("\t\trrx:argumentMap ( [ ");
			sb.append("rml:reference \"" + reference + "\"; ] );\n");
		} else { // we have simple reference
			if (ontology != null && !isgeometrypredicate) {
				ontology.createDatatypeProperty(classname, "has" + WordUtils.capitalize(predicate, new char[] { '-' }),
						type);
			}
			sb.append("\t\t" + (isTemplate ? "rr:template" : "rml:reference") + " \"" + reference + "\";\n");
		}
		if (ontology != null) {
			if (isSubtypeOfGeometryClass) {
				ontology.createObjectProperty(classname, predicate, classname + "_Geometry", true);
				if (true) {
					ontology.addFeatureAsSuperClass(classname);
					ontology.createGeometryClass(classname + "_Geometry");
				}
			}
		}
		sb.append("\t];\n");
		sb.append("];\n");
		return sb.toString();
	}

	private String printLogicalSource(String path) {
		StringBuilder sb = new StringBuilder();
		sb.append("rml:logicalSource [\n");
		sb.append("\trml:source \"" + pathToShapefile + "\";\n");
		sb.append("\trml:referenceFormulation ql:"+ (separator==','?"CSV":"TSV") +";\n");
		sb.append("\trml:iterator \"" + path + "\";\n];\n");
		return sb.toString();
	}

	private String printSubjectMap(String baseuri, String classname) {
		return printSubjectMap(baseuri, classname, null, false);
	}

	private String printSubjectMap(String baseuri, String classname, boolean isGeometrySubClass) {
		return printSubjectMap(baseuri, classname, null, isGeometrySubClass);
	}

	private String printSubjectMap(String baseuri, String classname, String classprefix, boolean isGeometrySubClass) {
		classname = classname.replace(".", "");
		if (ontology != null) {
			/*
			 * if (isGeometrySubClass) {
			 * ontology.createGeometryClass(classname); } else {
			 * ontology.createClass(classname); }
			 */
		}
		String base = baseuri + (baseuri.endsWith("/") ? "" : "/");
		StringBuilder sb = new StringBuilder();
		sb.append("rr:subjectMap [\n");
		sb.append("\trr:template \"" + base + classname + (isGeometrySubClass ? "/Geometry/" : "/id/")
				+ "{GeoTriplesID}\";\n");
		if (isGeometrySubClass) {
			sb.append("\trr:class " + "ogc" + ":" + "Geometry" + ";\n");
		} else {
			sb.append("\trr:class " + (classprefix != null ? classprefix : "onto") + ":" + classname + ";\n");
		}
		sb.append("];\n");
		return sb.toString();
	}

	private String printTriplesMap(String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("<#" + name + ">\n");
		return sb.toString();
	}

	private String printGEOPredicateObjectMaps(String column) {
		StringBuilder sb = new StringBuilder();
		sb.append(printPredicateObjectMap("dimension", column, "xsd:integer", null, "ogc", "dimension", "", true));
		sb.append(printPredicateObjectMap("asWKT", column, "ogc:wktLiteral", null, "ogc", "asWKT", "", true));
		// sb.append(printPredicateObjectMap("asGML", "*", "ogc:gmlLiteral",
		// null,
		// "ogc", "asGML", "",true));
		sb.append(printPredicateObjectMap("is3D", column, "xsd:boolean", null, "ogc", "is3D", "", true));
		sb.append(printPredicateObjectMap("isEmpty", column, "xsd:boolean", null, "ogc", "isEmpty", "", true));
		sb.append(printPredicateObjectMap("isSimple", column, "xsd:boolean", null, "ogc", "isSimple", "", true));
		/*
		 * sb.append(printPredicateObjectMap("hasSerialization", "the_geom",
		 * "ogc:wktLiteral", null, "ogc", "hasSerialization", "", true));
		 */
		sb.append(printPredicateObjectMap("coordinateDimension", column, "xsd:integer", null, "ogc",
				"coordinateDimension", "", true));
		sb.append(printPredicateObjectMap("spatialDimension", column, "xsd:integer", null, "ogc", "spatialDimension",
				"", true));
		// TODO write all the GeoSPARQL properties
		return sb.toString();
	}

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, ClassCastException, FileNotFoundException, XmlException, IOException {
		if (args.length < 3) {
			System.err.println("Please give arguments, eg <shapefile> <outputfile> <baseiri> [out-ontologyfile]");
			System.exit(1);
		}
		// XMLMappingGenerator m=new XMLMappingGenerator("TF7.xsd" ,
		// "personal.xml" , "http://ex.com/" , true);
		CSVMappingGenerator m = new CSVMappingGenerator(args[0], args[1], args[2], (args.length > 3) ? args[3] : null,',');
		m.run();
	}
}
