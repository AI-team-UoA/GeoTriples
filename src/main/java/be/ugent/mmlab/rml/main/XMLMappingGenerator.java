package be.ugent.mmlab.rml.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

public class XMLMappingGenerator {
	private final static Log log = LogFactory.getLog(XMLMappingGenerator.class);
	private String pathToXML;
	private String xsdFileName;
	private HashMap<String, String> triplesMaps = new HashMap<>();
	private String baseURI = "http://linkedeodata.eu/";
	private boolean allownulltypes = false;
	private File outputfile;
	private char prefix = 'a';
	private String rootelement=null;
	HashMap<String, String> namespaces = new HashMap<String, String>();

	public XMLMappingGenerator(String xsdfilename, String xmlfilename,
			String outputfile, String baseiri, String rootelement, boolean allownulltypesasclasses)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, ClassCastException, FileNotFoundException,
			XmlException, IOException {
		this.baseURI = baseiri;
		if (!this.baseURI.endsWith("/")) {
			this.baseURI += "/";
		}
		this.xsdFileName = xsdfilename;
		this.allownulltypes = allownulltypesasclasses;
		this.pathToXML = new File(xmlfilename).getAbsolutePath();
		this.outputfile = new File(outputfile);
		this.rootelement=rootelement;
	}

	private String getGTName(QName name) {
		if (name.getNamespaceURI() == null) {
			return name.getLocalPart();
		}
		if (name.getNamespaceURI().isEmpty()) {
			return name.getLocalPart();
		}
		String prefix = namespaces.get(name.getNamespaceURI());
		if (prefix != null) {
			return prefix + ":" + name.getLocalPart();
		}
		namespaces.put(name.getNamespaceURI(), String.valueOf(this.prefix));
		return String.valueOf(this.prefix++);

	}

	public void run() throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, ClassCastException, XmlException,
			FileNotFoundException, IOException {

		XmlOptions pp = new XmlOptions();
		pp.put("COMPILE_DOWNLOAD_URLS", "true");
		SchemaTypeSystem sts = XmlBeans.compileXsd(
				new XmlObject[] { XmlObject.Factory.parse(new FileInputStream(
						xsdFileName)) }, XmlBeans.getBuiltinTypeSystem(), pp);
		// System.out.println(sts.getName());
		/*
		 * System.out.println(sts.globalTypes()); for(SchemaType xxx:
		 * sts.globalTypes()){ System.out.println(xxx); }
		 */
		// System.out.println(sts.);
		// SchemaTypeLoader mpla = XmlBeans.loadXsd(new XmlObject[] {
		// XmlObject.Factory
		// .parse(new FileInputStream(xsdFileName)) }, pp );

		// sts.globalElements()[0].
		// System.exit(0);
		SchemaGlobalElement[] globals = sts.globalElements();
		// System.out.println("here2");
		// for (int i = 0; i < globals.length; i++) {
		// SchemaGlobalElement sge = globals[i];
		//
		// System.out.println("Global Element " + i + " Name: "
		// + sge.getName() + " Type: " + sge.getType().getName());
		// }
		// System.exit(0);

		for (int i = 0; i < globals.length; i++) {
			// if (!(globals[i].getName().getLocalPart().equals("Wegdeel") &&
			// globals[i].getName().getNamespaceURI().equals("http://www.opengis.net/gml")))
			// {
			if(rootelement!=null){
				if (!(globals[i].getName().getLocalPart().equals(rootelement))) {
					continue;
				}
			}else{
				if(i!=0){
					continue;
				}
			}
			// if(i!=0)
			// continue;
			SchemaGlobalElement sge = globals[i];
			System.out.println("Global Element " + i + " Name: "
					+ getGTName(sge.getName()) + " Type: "
					+ sge.getType().getName());
			triplesMaps.put("/" + getGTName(sge.getName()), "");
			triplesMaps.put("/" + getGTName(sge.getName()),
					triplesMaps.get("/" + getGTName(sge.getName()))
							+ printTriplesMap(sge.getName().getLocalPart()));
			triplesMaps
					.put("/" + getGTName(sge.getName()),
							triplesMaps.get("/" + getGTName(sge.getName()))
									+ printLogicalSource("/"
											+ getGTName(sge.getName())));
			String classname = (sge.getType().getName() != null) ? sge
					.getType().getName().getLocalPart() : sge.getName()
					.getLocalPart();
			triplesMaps.put(
					"/" + getGTName(sge.getName()),
					triplesMaps.get("/" + getGTName(sge.getName()))
							+ printSubjectMap(baseURI + classname + "/id/",
									classname));
			String targetNamespace = sge.getName().getNamespaceURI();
			String path = "/" + getGTName(sge.getName());

			System.out.println("Namespace: " + targetNamespace);
			SchemaType st = sge.getType();
			// st = sts.globalTypes()[0];
			for (SchemaProperty sp : st.getElementProperties()) {
				System.out.println("\t" + "Element: " + getGTName(sp.getName())
						+ " Type: " + sp.getType().getName());
				boolean isGeometry = false;
				if (sp.getType().getName() != null) {
					isGeometry = checkIfGMLGeometry(sp.getType());
				}

				String newpath = path + "/" + getGTName(sp.getName());
				String typeName = (sp.getType().getName() != null) ? sp
						.getType().getName().getLocalPart() : sp.getName()
						.getLocalPart();
				if (!isGeometry) {
					if (sp.getType().isSimpleType()) {
						if (!triplesMaps.containsKey(path)) {
							triplesMaps.put(path, " ");
						}
						String predicate = (sp.getType().getName() == null) ? newpath
								.replaceFirst(path, "").replaceFirst("/", "")
								.replace("/", "_")
								: sp.getName().getLocalPart();
						String reference = getGTName(sp.getName());

						String typename = (sp.getType().getName() != null) ? sp
								.getType().getName().getLocalPart() : null;
						triplesMaps.put(
								path,
								triplesMaps.get(path)
										+ printPredicateObjectMap(predicate,
												reference, typename));
					} else if (sp.getType().getName() != null || allownulltypes) {
						// this is a class

						// if it is simple type!! I forgot to copy paste all
						// symban from actual visit :P
						// It doesn't produce simple elements, all are going to
						// be classes :P

						if (!triplesMaps.containsKey(newpath)) {
							triplesMaps.put(newpath, " ");
						}
						triplesMaps.put(
								"/" + getGTName(sge.getName()),
								triplesMaps.get("/" + getGTName(sge.getName()))
										+ printRefObjectMap(sp.getName()
												.getLocalPart(), newpath
												.replaceAll("/", "")));
						triplesMaps.put(newpath, triplesMaps.get(newpath)
								+ printTriplesMap(newpath.replaceAll("/", "")));
						triplesMaps.put(newpath, triplesMaps.get(newpath)
								+ printLogicalSource(newpath));
						triplesMaps.put(
								newpath,
								triplesMaps.get(newpath)
										+ printSubjectMap(baseURI + typeName
												+ "/id/", typeName));
						visit(sp, "/" + getGTName(sge.getName()) + "/"
								+ getGTName(sp.getName()),
								"/" + getGTName(sge.getName()) + "/"
										+ getGTName(sp.getName()), 2);
					}
				} else {

					if (!triplesMaps.containsKey(newpath)) {
						triplesMaps.put(newpath, " ");
					}
					triplesMaps.put(
							"/" + getGTName(sge.getName()),
							triplesMaps.get("/" + getGTName(sge.getName()))
									+ printRefObjectMap(sp.getName()
											.getLocalPart(), newpath
											.replaceAll("/", "")));
					triplesMaps.put(newpath, triplesMaps.get(newpath)
							+ printTriplesMap(newpath.replaceAll("/", "")));
					triplesMaps.put(newpath, triplesMaps.get(newpath)
							+ printLogicalSource(newpath));
					triplesMaps.put(
							newpath,
							triplesMaps.get(newpath)
									+ printSubjectMap(baseURI + typeName
											+ "/id/", "Geometry", "ogc"));
					triplesMaps.put(newpath, triplesMaps.get(newpath)
							+ printGEOPredicateObjectMaps());
				}

			}
			for (SchemaProperty spp : st.getAttributeProperties()) {
				String newpath = path + "/@" + getGTName(spp.getName());
				System.out.println("\t" + "ROOTAttribute: "
						+ getGTName(spp.getName()) + " Type: "
						+ spp.getType().getName() + " Path: " + newpath);

				if (spp.getType().isSimpleType()) {
					if (!triplesMaps.containsKey(path)) {
						triplesMaps.put(path, " ");
					}
					String predicate = (spp.getType().getName() == null) ? newpath
							.replaceFirst(path, "").replaceFirst("/", "")
							.replace("/", "_")
							: "@" + spp.getName().getLocalPart();
					String reference = "@" + getGTName(spp.getName());

					String typename = (spp.getType().getName() != null) ? spp
							.getType().getName().getLocalPart() : null;
					triplesMaps.put(
							path,
							triplesMaps.get(path)
									+ printPredicateObjectMap(
											predicate.replace("@", ""),
											reference, typename));
				}
			}
			// break;
		}
		printmapping();
	}

	private void printmapping() throws FileNotFoundException {
		PrintStream out = new PrintStream(outputfile);
		out.println("@prefix rr: <http://www.w3.org/ns/r2rml#>.\n"
				+ "@prefix  rml: <http://semweb.mmlab.be/ns/rml#> .\n"
				+ "@prefix ql: <http://semweb.mmlab.be/ns/ql#> .\n"
				+ "@prefix nse: <http://test.eu/ontology#>.\n"
				+ "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.\n"
				+ "@prefix ex: <http://www.example.com/> .\n"
				+ "@base <http://example.com/base> .\n"
				+ "@prefix rrx: <http://www.w3.org/ns/r2rml-ext#>.\n"
				+ "@prefix rrxf: <http://www.w3.org/ns/r2rml-ext/functions/def/>.\n"
				+ "@prefix ogc: <http://www.opengis.net/ont/geosparql#>.\n"
				+ "@prefix aaa: <http://www.myphotos.org/> .\n"
				+ "@prefix gml: <http://www.opengis.net/gml/> . \n"
				+ "@prefix schema: <http://schema.org/>.\n"
				+ "@prefix wgs84_pos: <http://www.w3.org/2003/01/geo/wgs84_pos#>.\n"
				+ "@prefix onto: <http://eex#>.\n");
		for (String key : namespaces.keySet()) {
			out.println("@prefix " + namespaces.get(key) + ": <" + key + "> .");
		}
		for (String triplesMap : triplesMaps.keySet()) {
			log.debug("TRIPLES MAP: " + triplesMap);

			out.println(triplesMaps
					.get(triplesMap)
					.trim()
					.substring(0,
							triplesMaps.get(triplesMap).trim().length() - 1)
					+ ".\n");

			// log.debug("END TRIPLES MAP: "+triplesMap);
		}
		out.close();
	}

	private void visit(SchemaProperty sp, String path, String pathclass,
			int indent) {
		// System.out.println(pathclass);
		String tabs = "";
		for (int i = 0; i < indent; ++i) {
			tabs += "\t";
		}

		// if (sp.getType().isSimpleType()) {
		// System.out.println("simple type");
		// } else if (sp.getType().getName() == null) {
		// System.out.println("no type");
		// } else {
		// System.out.println("Class Found");
		// }

		for (SchemaProperty spp : sp.getType().getElementProperties()) {
			String newpath = path + "/" + getGTName(spp.getName());
			System.out
					.println(tabs + "Element: " + getGTName(spp.getName())
							+ " Type: " + spp.getType().getName() + " Path: "
							+ newpath);
			boolean hasNotNullType = spp.getType().getName() != null;
			String typeName = (hasNotNullType) ? spp.getType().getName()
					.getLocalPart() : spp.getName().getLocalPart();
			boolean isGeometry = false;
			if (spp.getType().getName() != null) {
				isGeometry = checkIfGMLGeometry(spp.getType());
			}
			if (!isGeometry) {
				if (spp.getType().isSimpleType()) {
					if (!triplesMaps.containsKey(pathclass)) {
						triplesMaps.put(pathclass, " ");
					}
					String predicate = (sp.getType().getName() == null) ? newpath
							.replaceFirst(pathclass, "").replaceFirst("/", "")
							.replace("/", "_")
							: spp.getName().getLocalPart();
					String reference = getGTName(spp.getName());
					String typename = (spp.getType().getName() != null) ? spp
							.getType().getName().getLocalPart() : null;
					triplesMaps.put(
							pathclass,
							triplesMaps.get(pathclass)
									+ printPredicateObjectMap(predicate,
											reference, typename));
				} else if (hasNotNullType || allownulltypes) {
					// this is a class
					if (!triplesMaps.containsKey(pathclass)) {
						triplesMaps.put(pathclass, " ");
					}
					if (!triplesMaps.containsKey(newpath)) {
						triplesMaps.put(newpath, " ");
					}
					triplesMaps.put(
							pathclass,
							triplesMaps.get(pathclass)
									+ printRefObjectMap(spp.getName()
											.getLocalPart(), newpath
											.replaceAll("/", "")));
					triplesMaps.put(newpath, triplesMaps.get(newpath)
							+ printTriplesMap(newpath.replaceAll("/", "")));
					triplesMaps.put(newpath, triplesMaps.get(newpath)
							+ printLogicalSource(newpath));
					triplesMaps.put(
							newpath,
							triplesMaps.get(newpath)
									+ printSubjectMap(baseURI + typeName
											+ "/id/", typeName));
					String currentclasspath = (spp.getType().getName() == null && !allownulltypes) ? pathclass
							: newpath;
					visit(spp, currentclasspath, currentclasspath, indent + 1);
				}
			} else {
				if (!triplesMaps.containsKey(pathclass)) {
					triplesMaps.put(pathclass, " ");
				}
				triplesMaps.put(
						pathclass,
						triplesMaps.get(pathclass)
								+ printRefObjectMap(spp.getName()
										.getLocalPart(), newpath.replaceAll(
										"/", "")));
				triplesMaps.put(newpath, triplesMaps.get(newpath)
						+ printTriplesMap(newpath.replaceAll("/", "")));
				triplesMaps.put(newpath, triplesMaps.get(newpath)
						+ printLogicalSource(newpath));
				triplesMaps.put(
						newpath,
						triplesMaps.get(newpath)
								+ printSubjectMap(baseURI + typeName + "/id/",
										"Geometry", "ogc"));
				triplesMaps.put(newpath, triplesMaps.get(newpath)
						+ printGEOPredicateObjectMaps());
			}

		}
		for (SchemaProperty spp : sp.getType().getAttributeProperties()) {
			String newpath = path + "/@" + getGTName(spp.getName());
			System.out
					.println(tabs + "Attribute: " + getGTName(spp.getName())
							+ " Type: " + spp.getType().getName() + " Path: "
							+ newpath);

			if (spp.getType().isSimpleType()) {
				if (!triplesMaps.containsKey(path)) {
					triplesMaps.put(path, " ");
				}
				String predicate = (sp.getType().getName() == null) ? newpath
						.replaceFirst(path, "").replaceFirst("/", "")
						.replace("/", "_") : "@" + spp.getName().getLocalPart();
				String reference = "@" + getGTName(spp.getName());
				String typename = (spp.getType().getName() != null) ? spp
						.getType().getName().getLocalPart() : null;
				triplesMaps.put(
						path,
						triplesMaps.get(path)
								+ printPredicateObjectMap(
										predicate.replace("@", ""), reference,
										typename));
			}
		}

	}

	private boolean checkIfGMLGeometry(SchemaType type) {
		if (!type.getName().getNamespaceURI()
				.equals("http://www.opengis.net/gml")) {
			return false;
		}
		if (type.getName().getLocalPart().equals("PointPropertyType")) {
			return true;
		}
		if (type.getName().getLocalPart().equals("CurvePropertyType")) {
			return true;
		}
		if (type.getName().getLocalPart().equals("SurfacePropertyType")) {
			return true;
		}
		if (type.getName().getLocalPart().equals("GeometryPropertyType")) {
			return true;
		}
		if (type.getName().getLocalPart().equals("MultiPointPropertyType")) {
			return true;
		}
		if (type.getName().getLocalPart().equals("MultiCurvePropertyType")) {
			return true;
		}
		if (type.getName().getLocalPart().equals("MultiSurfacePropertyType")) {
			return true;
		}
		return false;
	}

	private String printLogicalSource(String path) {
		StringBuilder sb = new StringBuilder();
		sb.append("rml:logicalSource [\n");
		sb.append("\trml:source \"" + pathToXML + "\";\n");
		sb.append("\trml:referenceFormulation ql:XPath;\n");
		sb.append("\trml:iterator \"" + path + "\";\n];\n");
		return sb.toString();
	}

	private String printSubjectMap(String baseuri, String classname) {
		return printSubjectMap(baseuri, classname, null);
	}

	private String printSubjectMap(String baseuri, String classname,
			String classprefix) {
		String base = baseuri + (baseuri.endsWith("/") ? "" : "/");
		StringBuilder sb = new StringBuilder();
		sb.append("rr:subjectMap [\n");
		sb.append("\trr:template \"" + base + "{GeoTriplesID}\";\n");
		sb.append("\trr:class " + (classprefix != null ? classprefix : "onto")
				+ ":" + classname + ";\n");
		sb.append("];\n");
		return sb.toString();
	}

	private String printPredicateObjectMap(String predicate, String reference,
			String type) {
		return printPredicateObjectMap(predicate, reference, type, null, null,
				null);
	}

	private String printPredicateObjectMap(String predicate, String reference,
			String type, String typeprefix, String predicatedprefix,
			String function) {
		StringBuilder sb = new StringBuilder();
		sb.append("rr:predicateObjectMap [\n");
		sb.append("\trr:predicate "
				+ ((predicatedprefix == null) ? "onto" : (predicatedprefix))
				+ ":");
		sb.append(predicate + ";\n");
		sb.append("\trr:objectMap [\n");
		if (type != null) {
			sb.append("\t\trr:datatype "
					+ ((typeprefix == null) ? "xsd" : (typeprefix)) + ":"
					+ type + ";\n");
		}
		if (function != null) {
			sb.append("\t\trrx:function rrxf:" + function + ";\n");
			sb.append("\t\trrx:argumentMap ( [ ");
			sb.append("rml:reference \"" + reference + "\"; ] );\n");
		} else {
			sb.append("\t\trml:reference \"" + reference + "\";\n");
		}
		sb.append("\t];\n");
		sb.append("];\n");
		return sb.toString();
	}

	private String printRefObjectMap(String predicate, String parentTriplesMap) {
		StringBuilder sb = new StringBuilder();
		sb.append("rr:predicateObjectMap [\n");
		sb.append("\trr:predicate onto:has_");
		sb.append(predicate + ";\n");
		sb.append("\trr:objectMap [\n");
		sb.append("\t\trr:parentTriplesMap <#" + parentTriplesMap + ">;\n");
		sb.append("\t\trr:joinCondition [\n");
		sb.append("\t\t\trr:childTriplesMap <#" + parentTriplesMap + ">;\n");
		sb.append("\t\t];\n");
		sb.append("\t];\n");
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
		sb.append(printPredicateObjectMap("dimension", "*", "integer", null,
				"ogc", "dimension"));
		sb.append(printPredicateObjectMap("asWKT", "*", "wktLiteral", "ogc",
				"ogc", "asWKT"));
		sb.append(printPredicateObjectMap("asGML", "*", "gmlLiteral", "ogc",
				"ogc", "asGML"));
		// TODO write all the GeoSPARQL properties
		return sb.toString();
	}

	public static void main(String[] args) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, ClassCastException,
			FileNotFoundException, XmlException, IOException {
		if (args.length < 4) {
			System.err
					.println("Please give arguments, eg <xsdfile> <xmlfile> <outputfile> <baseiri> <rootelement> [<true or false> for generating classes for elements without type name]");
			System.exit(1);
		}
		// XMLMappingGenerator m=new XMLMappingGenerator("TF7.xsd" ,
		// "personal.xml" , "http://ex.com/" , true);
		XMLMappingGenerator m = new XMLMappingGenerator(args[0], args[1],
				args[2], args[3],args[4], (args.length == 5) ? Boolean.valueOf(args[5])
						: false);
		m.run();
	}
}
