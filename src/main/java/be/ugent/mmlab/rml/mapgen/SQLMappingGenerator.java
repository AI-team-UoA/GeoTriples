package be.ugent.mmlab.rml.mapgen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.d2rq.db.SQLConnection;
import org.d2rq.db.op.TableOp;
import org.d2rq.db.schema.ColumnName;
import org.d2rq.db.schema.Identifier;
import org.d2rq.db.schema.Key;
import org.d2rq.db.schema.TableDef;
import org.d2rq.db.schema.TableName;
import org.d2rq.db.types.DataType;

import com.hp.hpl.jena.datatypes.TypeMapper;

import be.ugent.mmlab.rml.core.OntologyGenerator;

public class SQLMappingGenerator {
	private final static Log log = LogFactory.getLog(XMLMappingGenerator.class);
	private HashMap<String, String> triplesMaps = new HashMap<>();
	private String baseURI = "http://linkedeodata.eu/";

	private File outputfile;
	private File ontologyOutputFile = null;

	OntologyGenerator ontology = null;
	private String jdbcURL;
	private String password;
	private String username;
	private String table = null;

	public SQLMappingGenerator(String jdbcURL, String outputfile, String baseiri, String username, String password,
			String ontologyOutputFile, String table) throws ClassNotFoundException, InstantiationException,
					IllegalAccessException, ClassCastException, FileNotFoundException, XmlException, IOException {
		log.warn(
				"Join conditions have not been implemented yet. If you need JoinConditions please use the same command without -rml argument.");
//		System.out.println("jdbc URL=" + jdbcURL);
//		System.out.println("outputfile=" + outputfile);
//		System.out.println("baseiri" + baseiri);
//		System.out.println("username " + username);
//		System.out.println("password " + password);
//		System.out.println("only table " + table);
		this.jdbcURL = jdbcURL;
		System.out.println("ontologyOutputFile=" + ontologyOutputFile);
		this.baseURI = baseiri;
		if (!this.baseURI.endsWith("/")) {
			this.baseURI += "/";
		}
		this.username = username;
		this.password = password;
		this.table = table;
		this.outputfile = new File(outputfile);
		if (ontologyOutputFile != null) {
			this.ontologyOutputFile = new File(ontologyOutputFile);
			ontology = new OntologyGenerator(true, baseURI);
		}
	}

	public void run() throws IOException {

		String jdbcDriver = "";
		if (jdbcURL.contains("monetdb")) {
			jdbcDriver = "org.postgresql.Driver";
		} else if (jdbcURL.contains("postgres")) {
			jdbcDriver = "nl.cwi.monetdb.jdbc.MonetDriver";
		}
		SQLConnection connection = new SQLConnection(jdbcURL, jdbcDriver, username, password);
		Collection<TableName> tablenames = null;
		try {
			tablenames = connection.getTableNames();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (TableName tablename : tablenames) {

			String typeName = tablename.getTable().getName();
			if (table != null) {
				if (!typeName.equalsIgnoreCase(table)) {
					continue;
				}
			}
			TableOp tabledef = connection.getTable(tablename);
			Key key = findBestKey(tabledef.getTableDefinition());
			if (key == null) {
				key = makeKey(tabledef.getTableDefinition());
			}
			String primarykey = key.getColumns().get(0).getName();
			for (int i = 1; i < key.getColumns().size(); ++i) {
				String c = key.getColumns().get(i).getName();
				primarykey += ("/" + c);
			}
			triplesMaps.put(typeName, "");
			triplesMaps.put(typeName, triplesMaps.get(typeName) + printTriplesMap(typeName));
			triplesMaps.put(typeName,
					triplesMaps.get(typeName) + printLogicalSource("SELECT * FROM " + typeName + ";"));
			triplesMaps.put(typeName, triplesMaps.get(typeName) + printSubjectMap(baseURI, typeName, primarykey));

			String typeNameGeo = typeName + "_Geometry";
			List<ColumnName> columnnames = tabledef.getColumns();
			for (ColumnName columnname : columnnames) {
				DataType columntype = tabledef.getColumnType(columnname);
				String identifier = columnname.getColumn().getName();
				if (identifier.equals("geom")) {
					triplesMaps.put(typeName,
							triplesMaps.get(typeName) + printPredicateObjectMap(true,
									"hasGeometry", baseURI + (baseURI.endsWith("/") ? "" : "/") + typeName
											+ "/Geometry/{" + primarykey + "}",
									null, null, "ogc", null, typeName, true, false));
					triplesMaps.put(typeNameGeo, "");
					triplesMaps.put(typeNameGeo, triplesMaps.get(typeNameGeo) + printTriplesMap(typeNameGeo));
					triplesMaps.put(typeNameGeo,
							triplesMaps.get(typeNameGeo) + printLogicalSource(
									"SELECT *, st_dimension(geom) as \\\"dimension\\\", st_dimension(geom) as \\\"coordinateDimension\\\", st_dimension(geom) as \\\"spatialDimension\\\",  st_issimple(geom) as \\\"isSimple\\\", st_isempty(geom) as \\\"isEmpty\\\", CASE WHEN st_dimension(geom)=3 THEN 'true' ELSE 'false' END as \\\"is3D\\\", CONCAT('<http://www.opengis.net/def/crs/EPSG/0/4326> ' , REPLACE(CAST(geom AS TEXT), '\\\"', '')) as \\\"asWKT\\\" FROM "
											+ typeName + ";"));
					triplesMaps.put(typeNameGeo,
							triplesMaps.get(typeNameGeo) + printSubjectMap(baseURI, typeName, null, true, primarykey));
					triplesMaps.put(typeNameGeo, triplesMaps.get(typeNameGeo) + printGEOPredicateObjectMaps());
				} else {
					if (!key.getColumns().contains(Identifier.createDelimited(identifier))) {
						String datatype = "<"
								+ TypeMapper.getInstance().getSafeTypeByName(columntype.rdfType()).getURI() + ">";
						triplesMaps.put(typeName, triplesMaps.get(typeName)
								+ printPredicateObjectMap(identifier, identifier, datatype, typeName));
					}
				}

			}
			// triplesMaps.put(typeName,
			// triplesMaps.get(typeName) + printPredicateObjectMap(true,
			// "hasGeometry",
			// baseURI + (baseURI.endsWith("/") ? "" : "/") + typeNameGeo +
			// "/{GeoTriplesID}", null,
			// typeName, true));

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
		PrintStream out = new PrintStream(outputfile);
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
		sb.append("\trml:source \"" + jdbcURL + "?user=" + username + "&password=" + password + "\";\n");
		sb.append("\trml:referenceFormulation ql:SQL;\n");
		sb.append("\trml:iterator \"" + path + "\";\n];\n");
		return sb.toString();
	}

	private String printSubjectMap(String baseuri, String classname, String primarykey) {
		return printSubjectMap(baseuri, classname, null, false, primarykey);
	}

	private String printSubjectMap(String baseuri, String classname, boolean isGeometrySubClass) {
		return printSubjectMap(baseuri, classname, null, isGeometrySubClass, null);
	}

	private String printSubjectMap(String baseuri, String classname, String classprefix, boolean isGeometrySubClass,
			String primarykey) {
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
		sb.append("\trr:template \"" + base + classname + (isGeometrySubClass ? "/Geometry/" : "/id/") + "{"
				+ ((primarykey == null) ? "GeoTriplesID" : primarykey) + "}\";\n");
		sb.append("\trr:class " + (classprefix != null ? classprefix : "onto") + ":" + classname + ";\n");
		sb.append("];\n");
		return sb.toString();
	}

	private String printTriplesMap(String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("<#" + name + ">\n");
		return sb.toString();
	}

	private String printGEOPredicateObjectMaps() {
		StringBuilder sb = new StringBuilder();
		sb.append(printPredicateObjectMap("dimension", "dimension", "xsd:integer", null, "ogc", null, "", true));
		sb.append(printPredicateObjectMap("asWKT", "asWKT", "ogc:wktLiteral", null, "ogc", null, "", true));
		// sb.append(printPredicateObjectMap("asGML", "*", "ogc:gmlLiteral",
		// null,
		// "ogc", "asGML", "",true));
		sb.append(printPredicateObjectMap("is3D", "is3D", "xsd:boolean", null, "ogc", null, "", true));
		sb.append(printPredicateObjectMap("isEmpty", "isEmpty", "xsd:boolean", null, "ogc", null, "", true));
		sb.append(printPredicateObjectMap("isSimple", "isSimple", "xsd:boolean", null, "ogc", null, "", true));
		/*
		 * sb.append(printPredicateObjectMap("hasSerialization",
		 * "hasSerialization", "ogc:wktLiteral", null, "ogc", null, "", true));
		 */
		sb.append(printPredicateObjectMap("coordinateDimension", "coordinateDimension", "xsd:integer", null, "ogc",
				null, "", true));
		sb.append(printPredicateObjectMap("spatialDimension", "spatialDimension", "xsd:integer", null, "ogc", null, "",
				true));
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
		SQLMappingGenerator m = new SQLMappingGenerator(args[0], args[1], args[2], args[3], args[4],
				(args.length > 5) ? args[5] : null, (args.length > 6) ? args[6] : null);
		m.run();
	}

	private Key findBestKey(TableDef table) {
		if (table.getPrimaryKey() != null) {
			return table.getPrimaryKey();
		}
		for (Key uniqueKey : table.getUniqueKeys()) {
			return uniqueKey;
		}
		return null;
	}

	private Key makeKey(TableDef table) {
		ArrayList<Identifier> temp = new ArrayList<Identifier>();
		for (Identifier partKey : table.getColumnNames()) {
			if (!partKey.getName().equalsIgnoreCase("geom")) {
				temp.add(partKey);
			}
		}
		return Key.createFromIdentifiers(temp);
	}
}
