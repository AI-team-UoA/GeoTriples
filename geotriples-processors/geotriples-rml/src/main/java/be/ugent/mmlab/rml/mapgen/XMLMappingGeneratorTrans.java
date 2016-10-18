package be.ugent.mmlab.rml.mapgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.namespace.QName;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import be.ugent.mmlab.rml.core.OntologyGenerator;

public class XMLMappingGeneratorTrans extends RMLMappingGenerator{
	private final static Logger log = LoggerFactory.getLogger(XMLMappingGeneratorTrans.class);
	private String pathToXML;
	private String xsdFileName;
	private HashMap<String, String> triplesMaps = new HashMap<>();
	private boolean allownulltypes = false;
	private String rootelement = null;
	HashMap<String, String> namespaces = new HashMap<String, String>();
	private File ontologyOutputFile = null;
	OntologyGenerator ontology = null;
	private String basePath = null;
	private String onlynamespace = null;

	public XMLMappingGeneratorTrans(String xsdfilename, String xmlfilename,
			String outputfile, String baseiri, String rootelement,
			String basepath, String namespaces,
			boolean allownulltypesasclasses, String ontologyOutputFile,
			String onlynamespace) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, ClassCastException,
			FileNotFoundException, XmlException, IOException {
		super(baseiri, outputfile);
//		System.out.println("xsdfilename=" + xsdfilename);
//		System.out.println("xmlfilename=" + xmlfilename);
//		System.out.println("outputfile=" + outputfile);
//		System.out.println("baseiri" + baseiri);
//		System.out.println("root=" + rootelement);
//		System.out.println("basepath=" + basepath);
//		System.out.println("namespaces=" + namespaces);
//		System.out
//				.println("allownulltypesasclasses=" + allownulltypesasclasses);
//		System.out.println("ontologyOutputFile=" + ontologyOutputFile);
//		System.out.println("only global elements for namespace "
//				+ onlynamespace);
		
		this.xsdFileName = xsdfilename;
		this.allownulltypes = allownulltypesasclasses;
		this.pathToXML = new File(xmlfilename).getAbsolutePath();
		if (ontologyOutputFile != null) {
			this.ontologyOutputFile = new File(ontologyOutputFile);
			ontology = new OntologyGenerator(true, baseURI);
		}

		this.onlynamespace = onlynamespace;

		this.rootelement = rootelement;
		this.basePath = basepath;
		if (this.basePath != null)
			if (this.basePath.endsWith("/")) {
				if (this.basePath.length() > 1)
					this.basePath = this.basePath.substring(0,
							this.basePath.length() - 2);
				else
					this.basePath = "";
			}
		if (namespaces != null) {
			String[] nss = namespaces.split(",");
			for (String ns : nss) {
				// String [] tokens=ns.split("(\\s+)");
				String[] tokens = ns.split("\\|");
				String prefix = tokens[0];
				String uri = tokens[1];
				this.namespaces.put(uri.trim(), prefix.trim());
			}
		}
	}

	private String getGTName(QName name) {
		if (name == null) {
			return null;
		}
		if (name.getNamespaceURI() == null) {
			return name.getLocalPart();
		}
		if (name.getNamespaceURI().isEmpty()) {
			return name.getLocalPart();
		}
		if (name.getLocalPart() == null) {
			return null;
		}
		if (name.getLocalPart().isEmpty()) {
			return null;
		}
		String prefix = namespaces.get(name.getNamespaceURI());
		if (prefix != null) {
			return prefix + ":" + name.getLocalPart();
		}
		String newprefix = new String(name.getNamespaceURI());
		// newprefix+="#";
		String newrandomstring = RandomStringUtils.random(5, true, false);
		namespaces.put(newprefix, newrandomstring);
		return newrandomstring + ":" + name.getLocalPart();

	}

	private SchemaTypeSystem sts = null;

	public void run() throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, ClassCastException, XmlException,
			FileNotFoundException, IOException {

		XmlOptions pp = new XmlOptions();
		pp.put("COMPILE_DOWNLOAD_URLS", "true");
		pp.setCompileDownloadUrls();
		// pp.setLoadUseDefaultResolver();

		ArrayList<XmlObject> xmlobjects = new ArrayList<XmlObject>();
		File givenfile = new File(xsdFileName);
		if (givenfile.isDirectory()) {
			File[] files = givenfile.listFiles();
			for (File file : files) {
				if (!file.isFile())
					continue;
				String[] bits = file.getName().split("\\.");
				if (bits.length > 0
						&& bits[bits.length - 1].equalsIgnoreCase("xsd")) {
					System.out.println("Parsing xsd file with name "
							+ file.getName());
					xmlobjects.add(XmlObject.Factory.parse(new FileInputStream(
							file.getAbsolutePath())));
				}
			}
			XmlObject[] array = new XmlObject[xmlobjects.size()];
			array = xmlobjects.toArray(array);
			sts = XmlBeans.compileXsd(array, XmlBeans.getBuiltinTypeSystem(),
					pp);
		} else {
			sts = XmlBeans.compileXsd(new XmlObject[] { XmlObject.Factory
					.parse(new FileInputStream(xsdFileName)) }, XmlBeans
					.getBuiltinTypeSystem(), pp);
		}

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
			System.out.println("Global elementtt: " + globals[i].getName());
			// if(true)
			// continue;

			if (onlynamespace != null) {
				if (!(globals[i].getName().getNamespaceURI()
						.equals(onlynamespace))) {
					continue;
				}
			} else if (rootelement != null) {
				if (!(globals[i].getName().getLocalPart().equals(rootelement))) {
					continue;
				}
				System.out.println("The type is finite: "
						+ globals[i].getType().isFinite());
			}
			if (globals[i].getName().getLocalPart().equals("GeoObject")) {// this
																			// is
																			// only
																			// for
																			// the
																			// netherlands
																			// hma
																			// use
																			// case
				continue;
			}
			// if(i!=0)
			// continue;
			SchemaGlobalElement sge = globals[i];
System.out.println("is abstract type: "+sge.getType().isAbstract());
System.out.println("get outer type: "+sge.getType().getOuterType());
//System.out.println(sge.getType().getOuterType().getName());
			if ((sge.getType().getOuterType() != null && sge.getType().getName()!=null)
					|| sge.getType().isAbstract()) {
				continue;
			}
			if(sge.getName().getLocalPart().equals("RegistratiefGebied")){
				System.out.println("TO BE DELETED");
			}
			if (sge.getType().getName() != null) {
				if (typesHierarhy.contains(sge.getType().getName())) {
					continue;
				}
			}
			else{
				if (elementHierarhy.contains(sge.getName())) {
					continue;
				}
			}
			if(sge.getType().getName()!=null){
				typesHierarhy.add(sge.getType().getName());
			}else{
				elementHierarhy.add(sge.getName());
			}
			
			System.out.println("Global Element " + i + " Name: "
					+ getGTName(sge.getName()) + " Type: "
					+ sge.getType().getName());
			triplesMaps.put("/" + getGTName(sge.getName()), "");
			triplesMaps.put("/" + getGTName(sge.getName()),
					triplesMaps.get("/" + getGTName(sge.getName()))
							+ printTriplesMap(getGTName(sge.getName())
									.replaceAll("/", "")));
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
			SchemaProperty[] elementProperties = st.getElementProperties();
			Queue<SchemaProperty> elementPropertiesList = new LinkedList<SchemaProperty>();
			for (SchemaProperty e : elementProperties) {
				elementPropertiesList.add(e);
			}
			while (elementPropertiesList.size() > 0) {
				final SchemaProperty sp = elementPropertiesList.remove();
				System.out.println("\t" + "Element: " + getGTName(sp.getName())
						+ " Type: " + sp.getType().getName());
				boolean isGeometry = false;
				boolean isGMLGeometry = false;
				if (sp.getType().getName() != null) {
					isGeometry = checkIfGeometry(sp.getType());
					isGMLGeometry = checkIfGMLGeometry(sp.getType());
				}

				String newpath = path + "/" + getGTName(sp.getName());
				String typeName = (sp.getType().getName() != null) ? sp
						.getType().getName().getLocalPart() : sp.getName()
						.getLocalPart();
				if (!isGeometry ||  (!isGMLGeometry)) {
					if (sp.getType().isAbstract()) {
						for (QName nko : sp.acceptedNames()) {
							if (nko.equals(sp.getName())) {
								continue;
							}
							//System.out.println("mplamplampla " + nko);
							//System.out.println(sts.findElement(nko).getType());
							final SchemaGlobalElement finalelement = sts
									.findElement(nko);
							final SchemaType finaltype = sts.findElement(nko)
									.getType();
							elementPropertiesList.add(new SchemaProperty() {

								@Override
								public SchemaType javaBasedOnType() {
									return sp.javaBasedOnType();
								}

								@Override
								public boolean isReadOnly() {
									return sp.isReadOnly();
								}

								@Override
								public boolean isAttribute() {
									return sp.isAttribute();
								}

								@Override
								public int hasNillable() {
									return sp.hasNillable();
								}

								@Override
								public int hasFixed() {
									return sp.hasFixed();
								}

								@Override
								public int hasDefault() {
									return sp.hasDefault();
								}

								@Override
								public SchemaType getType() {
									return finaltype;
								}

								@Override
								public QName getName() {
									return finalelement.getName();
								}

								@Override
								public BigInteger getMinOccurs() {
									return sp.getMinOccurs();
								}

								@Override
								public BigInteger getMaxOccurs() {
									return sp.getMaxOccurs();
								}

								@Override
								public int getJavaTypeCode() {
									return sp.getJavaTypeCode();
								}

								@Override
								public QNameSet getJavaSetterDelimiter() {
									return sp.getJavaSetterDelimiter();
								}

								@Override
								public String getJavaPropertyName() {
									return sp.getJavaPropertyName();
								}

								@Override
								public XmlAnySimpleType getDefaultValue() {
									return sp.getDefaultValue();
								}

								@Override
								public String getDefaultText() {
									return sp.getDefaultText();
								}

								@Override
								public SchemaType getContainerType() {
									return sp.getContainerType();
								}

								@Override
								public boolean extendsJavaSingleton() {
									return sp.extendsJavaSingleton();
								}

								@Override
								public boolean extendsJavaOption() {
									return sp.extendsJavaOption();
								}

								@Override
								public boolean extendsJavaArray() {
									return sp.extendsJavaArray();
								}

								@Override
								public QName[] acceptedNames() {
									return new QName[] { finalelement.getName() };
								}
							});
							// iterator.previous();
						}
						// System.out.println(nko);
						// System.exit(11);
						continue;
					}
					if (sp.getType().isSimpleType()) {
						if (!triplesMaps.containsKey(path)) {
							triplesMaps.put(path, " ");
						}
						String predicate = newpath.replaceFirst(path, "")
								.replaceFirst("/", "").replace("/", "_")
								.replace(":", "-");
						String reference = getGTName(sp.getName());

						String typename = fixType(getGTName(sp.getType().getName()));
						// (sp.getType().getName() != null) ?
						// sp.getType().getName().getLocalPart() : null;
						/*
						 * String typeprefix = (sp.getType().getName() != null)
						 * ? sp .getType().getName().getNamespaceURI() : null;
						 */
						triplesMaps
								.put(path,
										triplesMaps.get(path)
												+ printPredicateObjectMap(
														predicate, reference,
														typename, classname));
					} else if (sp.getType().getName() != null || allownulltypes) {
						// this is a class

						// if it is simple type!! I forgot to copy paste all
						// symban from actual visit :P
						// It doesn't produce simple elements, all are going to
						// be classes :P

						if (!triplesMaps.containsKey(newpath)) {
							triplesMaps.put(newpath, " ");
						}
						System.out.println(sp.getName() + "        here");
						
						String reference = getGTName(sp.getName());
						String baseuri=baseURI + typeName
								+ "/id/";
						triplesMaps.put(
								path,
								triplesMaps.get(path)
										+ printPredicateObjectMap(true,sp.getName()
												.getLocalPart(), baseuri + (baseuri.endsWith("/") ? "{" : "/{")+reference+"/GeoTriplesID}",null,classname));
						
//						triplesMaps.put(
//								"/" + getGTName(sge.getName()),
//								triplesMaps.get("/" + getGTName(sge.getName()))
//										+ printRefObjectMap(sp.getName()
//												.getLocalPart(), newpath
//												.replaceAll("/", ""),
//												classname, typeName));
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
						
						if(isGeometry){
							triplesMaps.put(newpath, triplesMaps.get(newpath)
								+ printGEOPredicateObjectMaps());
						}
					}
				} else {

					if (!triplesMaps.containsKey(newpath)) {
						triplesMaps.put(newpath, " ");
					}
					String baseuri=baseURI + typeName
							+ "/id/";
					String reference = getGTName(sp.getName());
					triplesMaps.put(
							path,
							triplesMaps.get(path)
									+ printPredicateObjectMap(true,sp.getName()
											.getLocalPart(), baseuri + (baseuri.endsWith("/") ? "{" : "/{")+reference+"/GeoTriplesID}",null,classname,true));
//					triplesMaps.put(
//							"/" + getGTName(sge.getName()),
//							triplesMaps.get("/" + getGTName(sge.getName()))
//									+ printRefObjectMap(sp.getName()
//											.getLocalPart(), newpath
//											.replaceAll("/", ""), classname,
//											typeName, true));
					triplesMaps.put(newpath, triplesMaps.get(newpath)
							+ printTriplesMap(newpath.replaceAll("/", "")));
					triplesMaps.put(newpath, triplesMaps.get(newpath)
							+ printLogicalSource(newpath));
					triplesMaps.put(newpath,
					// triplesMaps.get(newpath)
					// + printSubjectMap(baseURI + typeName
					// + "/id/", "Geometry", "ogc"));
							triplesMaps.get(newpath)
									+ printSubjectMap(baseURI + typeName
											+ "/id/", typeName, true));
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
					String predicate = newpath.replaceFirst(path, "")
							.replaceFirst("/", "").replace("/", "_")
							.replace(":", "-");
					String reference = "@" + getGTName(spp.getName());
					String typename = fixType(getGTName(spp.getType().getName()));
					// String typename = (spp.getType().getName() != null) ? spp
					// .getType().getName().getLocalPart() : null;
					String typeprefix = (spp.getType().getName() != null) ? spp
							.getType().getName().getNamespaceURI() : null;
					triplesMaps.put(
							path,
							triplesMaps.get(path)
									+ printPredicateObjectMap(
											predicate.replace("@", ""),
											reference, typename, classname));
				}
			}
			// break;
			
			//delete from map
			if(sge.getType().getName()!=null){
				typesHierarhy.remove(sge.getType().getName());
			}else{
				elementHierarhy.remove(sge.getName());
			}
		}
		printmapping();
		printontology();
	}

	private String fixType(String gtName) {
		if(gtName==null){
			return gtName;
		}
		if(gtName.contains(":")){
			return gtName;
		}
		
		String prefix = "onto";
		
		return prefix+ ":" + gtName;
	}

	private void printontology() throws FileNotFoundException {
		if(ontology!=null)
		ontology.writeToOutput(new PrintStream(ontologyOutputFile));
	}

	private void printmapping() throws FileNotFoundException {
		PrintStream out = outputfile==null?System.out:new PrintStream(outputfile);
		out.println("@prefix rr: <http://www.w3.org/ns/r2rml#>.\n"
				+ "@prefix  rml: <http://semweb.mmlab.be/ns/rml#> .\n"
				+ "@prefix ql: <http://semweb.mmlab.be/ns/ql#> .\n"
				+ "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.\n"
				+ "@base <http://example.com/base> .\n"
				+ "@prefix rrx: <http://www.w3.org/ns/r2rml-ext#>.\n"
				+ "@prefix rrxf: <http://www.w3.org/ns/r2rml-ext/functions/def/>.\n"
				+ "@prefix ogc: <http://www.opengis.net/ont/geosparql#>.\n"
				+ "@prefix schema: <http://schema.org/>.\n"
				+ "@prefix wgs84_pos: <http://www.w3.org/2003/01/geo/wgs84_pos#>.\n"
				+ "@prefix onto: <" + baseURI + "ontology#>.\n");
		for (String key : namespaces.keySet()) {
			out.println("@prefix " + namespaces.get(key) + ": <" + key + "#> .");
		}
		for (String triplesMap : triplesMaps.keySet()) {
			log.debug("TRIPLES MAP: " + triplesMap);
			if (triplesMaps.get(triplesMap).isEmpty()
					|| triplesMaps.get(triplesMap).equals("null")
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

	HashSet<QName> typesHierarhy = new HashSet<>();
	HashSet<QName> elementHierarhy = new HashSet<>();
	
	private void visit(SchemaProperty sp, String path, String pathclass,
			int indent) {
		System.out.println(triplesMaps.size());
		// System.out.println(pathclass);
		/*
		 * if(sp.getType().getBaseType()!=null){
		 * System.out.println("We are in "+sp.getType() );
		 * System.out.println(sp.getType().getBaseType()); try {
		 * System.in.read(); } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); } }
		 */

		if (sp.getType().getName() != null) {
			if (typesHierarhy.contains(sp.getType().getName())) {
				return;
			}
		}
		else{
			if (elementHierarhy.contains(sp.getName())) {
				return;
			}
		}
		if(sp.getType().getName()!=null){
			typesHierarhy.add(sp.getType().getName());
		}else{
			elementHierarhy.add(sp.getName());
		}
		/*System.out.println("The memory is");
		for(QName stype:typesHierarhy){
			System.out.print(stype+" > ");
		}
		System.out.println();*/
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
		boolean hasNotNullTypeFather = sp.getType().getName() != null;
		String fathersTypeName = (hasNotNullTypeFather) ? sp.getType()
				.getName().getLocalPart() : sp.getName().getLocalPart();
		if (sp.getType().getElementProperties().length == 0) {
			String predicate = "XMLElementvalue";
			String reference = "*";
			String typename = "xsd:string";
			triplesMaps.put(
					pathclass,
					triplesMaps.get(pathclass)
							+ printPredicateObjectMap(predicate, reference,
									typename, fathersTypeName));
		} else {
			SchemaProperty[] elementProperties = sp.getType()
					.getElementProperties();
			Queue<SchemaProperty> elementPropertiesList = new LinkedList<SchemaProperty>();
			for (SchemaProperty e : elementProperties) {
				elementPropertiesList.add(e);
			}
			while (elementPropertiesList.size() > 0) {
				final SchemaProperty spp = elementPropertiesList.remove();
				String newpath = path + "/" + getGTName(spp.getName());
				System.out.println(tabs + "Element: "
						+ getGTName(spp.getName()) + " Type: "
						+ spp.getType().getName() + " Path: " + newpath);
				boolean hasNotNullType = spp.getType().getName() != null;
				String typeName = (hasNotNullType) ? spp.getType().getName()
						.getLocalPart() : spp.getName().getLocalPart();

				boolean isGeometry = false;
				boolean isGMLGeometry = false;
				if (spp.getType().getName() != null) {
					isGeometry = checkIfGeometry(spp.getType());
					isGMLGeometry = checkIfGMLGeometry(spp.getType());
				}
				if (!isGeometry ||  (!isGMLGeometry)) {
					if (spp.getType().isAbstract()) {
						for (QName nko : spp.acceptedNames()) {
							if (nko.equals(spp.getName())) {
								continue;
							}
							System.out.println("We are in " + spp.getName());
							System.out.println("mplamplampla " + nko);
							System.out.println(sts.findElement(nko).getType());
							final SchemaGlobalElement finalelement = sts
									.findElement(nko);
							final SchemaType finaltype = sts.findElement(nko)
									.getType();
							elementPropertiesList.add(new SchemaProperty() {

								@Override
								public SchemaType javaBasedOnType() {
									return spp.javaBasedOnType();
								}

								@Override
								public boolean isReadOnly() {
									return spp.isReadOnly();
								}

								@Override
								public boolean isAttribute() {
									return spp.isAttribute();
								}

								@Override
								public int hasNillable() {
									return spp.hasNillable();
								}

								@Override
								public int hasFixed() {
									return spp.hasFixed();
								}

								@Override
								public int hasDefault() {
									return spp.hasDefault();
								}

								@Override
								public SchemaType getType() {
									return finaltype;
								}

								@Override
								public QName getName() {
									return finalelement.getName();
								}

								@Override
								public BigInteger getMinOccurs() {
									return spp.getMinOccurs();
								}

								@Override
								public BigInteger getMaxOccurs() {
									return spp.getMaxOccurs();
								}

								@Override
								public int getJavaTypeCode() {
									return spp.getJavaTypeCode();
								}

								@Override
								public QNameSet getJavaSetterDelimiter() {
									return spp.getJavaSetterDelimiter();
								}

								@Override
								public String getJavaPropertyName() {
									return spp.getJavaPropertyName();
								}

								@Override
								public XmlAnySimpleType getDefaultValue() {
									return spp.getDefaultValue();
								}

								@Override
								public String getDefaultText() {
									return spp.getDefaultText();
								}

								@Override
								public SchemaType getContainerType() {
									return spp.getContainerType();
								}

								@Override
								public boolean extendsJavaSingleton() {
									return spp.extendsJavaSingleton();
								}

								@Override
								public boolean extendsJavaOption() {
									return spp.extendsJavaOption();
								}

								@Override
								public boolean extendsJavaArray() {
									return spp.extendsJavaArray();
								}

								@Override
								public QName[] acceptedNames() {
									return new QName[] { finalelement.getName() };
								}
							});
							// iterator.previous();
						}
						// System.out.println(nko);
						// System.exit(11);
						continue;
					}
					if (spp.getType().isSimpleType()) {
						if (!triplesMaps.containsKey(pathclass)) {
							triplesMaps.put(pathclass, " ");
						}
						/*
						 * String predicate = (sp.getType().getName() == null) ?
						 * newpath .replaceFirst(pathclass,
						 * "").replaceFirst("/", "") .replace("/", "_") :
						 * spp.getName().getLocalPart();
						 */
						String predicate = newpath.replaceFirst(pathclass, "")
								.replaceFirst("/", "").replace("/", "_")
								.replace(":", "-");
						String reference = getGTName(spp.getName());
						String typename = fixType(getGTName(spp.getType().getName()));
						// String typename = (spp.getType().getName() != null) ?
						// spp
						// .getType().getName().getLocalPart() : null;
						String typeprefix = (spp.getType().getName() != null) ? spp
								.getType().getName().getNamespaceURI()
								: null;
						triplesMaps.put(
								pathclass,
								triplesMaps.get(pathclass)
										+ printPredicateObjectMap(predicate,
												reference, typename,
												fathersTypeName));
					} else if (hasNotNullType || allownulltypes) {
						// this is a class
						if (!triplesMaps.containsKey(pathclass)) {
							triplesMaps.put(pathclass, " ");
						}
						if (!triplesMaps.containsKey(newpath)) {
							triplesMaps.put(newpath, " ");
						}
						String baseuri=baseURI + typeName
								+ "/id/";
						String reference = getGTName(spp.getName());
						triplesMaps.put(
								pathclass,
								triplesMaps.get(pathclass)
										+ printPredicateObjectMap(true,spp.getName()
												.getLocalPart(), baseuri + (baseuri.endsWith("/") ? "{" : "/{")+reference+"/GeoTriplesID}",null,fathersTypeName));
						triplesMaps
								.put(newpath,
										((triplesMaps.get(newpath) != null) ? triplesMaps
												.get(newpath) : "")
												+ printTriplesMap(newpath
														.replaceAll("/", "")));
						triplesMaps.put(newpath, triplesMaps.get(newpath)
								+ printLogicalSource(newpath));
						triplesMaps.put(
								newpath,
								triplesMaps.get(newpath)
										+ printSubjectMap(baseURI + typeName
												+ "/id/", typeName));
						String currentclasspath = (spp.getType().getName() == null && !allownulltypes) ? pathclass
								: newpath;
						visit(spp, currentclasspath, currentclasspath,
								indent + 1);
						
						if(isGeometry){
							triplesMaps.put(newpath, triplesMaps.get(newpath)
								+ printGEOPredicateObjectMaps());
						}
					}
				} else {
					if (!triplesMaps.containsKey(pathclass)) {
						triplesMaps.put(pathclass, " ");
					}
					String baseuri=baseURI + typeName
							+ "/id/";
					String reference = getGTName(spp.getName());
					triplesMaps.put(
							pathclass,
							triplesMaps.get(pathclass)
									+ printPredicateObjectMap(true,spp.getName()
											.getLocalPart(), baseuri + (baseuri.endsWith("/") ? "{" : "/{")+reference+"/GeoTriplesID}",null,fathersTypeName,true));
					triplesMaps.put(
							newpath,
							((triplesMaps.get(newpath) != null) ? triplesMaps
									.get(newpath) : "")
									+ printTriplesMap(newpath.replaceAll("/",
											"")));
					triplesMaps.put(newpath, triplesMaps.get(newpath)
							+ printLogicalSource(newpath));
					triplesMaps.put(newpath,
					// triplesMaps.get(newpath)
					// + printSubjectMap(baseURI + typeName + "/id/",
					// "Geometry", "ogc"));
							triplesMaps.get(newpath)
									+ printSubjectMap(baseURI + typeName
											+ "/id/", typeName, true));

					triplesMaps.put(newpath, triplesMaps.get(newpath)
							+ printGEOPredicateObjectMaps());
				}

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
				String predicate = newpath.replaceFirst(path, "")
						.replaceFirst("/", "").replaceAll("/", "_")
						.replace(":", "-");
				String reference = "@" + getGTName(spp.getName());
				String typename = fixType(getGTName(spp.getType().getName()));
				// String typename = (spp.getType().getName() != null) ? spp
				// .getType().getName().getLocalPart() : null;
				String typeprefix = (spp.getType().getName() != null) ? spp
						.getType().getName().getNamespaceURI() : null;
				triplesMaps.put(
						path,
						triplesMaps.get(path)
								+ printPredicateObjectMap(
										predicate.replace("@", ""), reference,
										typename, fathersTypeName));
			}
		}
		if(sp.getType().getName()!=null){
			typesHierarhy.remove(sp.getType().getName());
		}else{
			elementHierarhy.remove(sp.getName());
		}
	}

	private boolean checkIfGMLGeometry(SchemaType type) {
		if (!type.getName().getNamespaceURI()
				.equals("http://www.opengis.net/gml") && !type.getName().getNamespaceURI()
				.equals("http://www.opengis.net/gml/3.1.1") && !!type.getName().getNamespaceURI()
				.equals("http://www.opengis.net/gml/3.2.0")) {
			return false;
		}
		return true;
	}
	private boolean checkIfGeometry(SchemaType type) {
		if (!type.getName().getNamespaceURI()
				.equals("http://www.opengis.net/gml") && !type.getName().getNamespaceURI()
				.equals("http://www.opengis.net/kml/2.2")) {
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
		if (type.getName().getLocalPart().equals("MultiGeometryPropertyType")) {
			return true;
		}
		if (type.getName().getLocalPart().equals("LineStringPropertyType")) {
			return true;
		}
		if (type.getName().getLocalPart().equals("MultiLineStringPropertyType")) {
			return true;
		}
		if (type.getName().getLocalPart().equals("PlacemarkType")) {
			return true;
		}
		
		return false;
	}

	private String printLogicalSource(String path) {
		StringBuilder sb = new StringBuilder();
		sb.append("rml:logicalSource [\n");
		sb.append("\trml:source \"" + pathToXML + "\";\n");
		sb.append("\trml:referenceFormulation ql:XPath;\n");
		sb.append("\trml:iterator \""
				+ (basePath == null ? path : (basePath + path)) + "\";\n];\n");
		return sb.toString();
	}

	private String printSubjectMap(String baseuri, String classname) {
		return printSubjectMap(baseuri, classname, null, false);
	}

	private String printSubjectMap(String baseuri, String classname,
			boolean isGeometrySubClass) {
		return printSubjectMap(baseuri, classname, null, isGeometrySubClass);
	}

	private String printSubjectMap(String baseuri, String classname,
			String classprefix, boolean isGeometrySubClass) {
		classname = classname.replace(".", "");
		if (ontology != null) {
			if (isGeometrySubClass) {
				ontology.createGeometryClass(classname);
			} else {
				ontology.createClass(classname);
			}
		}
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
			String type, String classname) {
		return printPredicateObjectMap(false,predicate, reference, type, null, null,
				null, classname, false);
	}
	private String printPredicateObjectMap(boolean isTemplate,String predicate, String reference,
			String type, String classname) {
		return printPredicateObjectMap(isTemplate,predicate, reference, type, null, null,
				null, classname, false);
	}
	
	private String printPredicateObjectMap(String predicate, String reference,
			String type, String classname,boolean isgeometrypredicate) {
		return printPredicateObjectMap(false,predicate, reference, type, null, null,
				null, classname, isgeometrypredicate);
	}
	
	private String printPredicateObjectMap(boolean isTemplate,String predicate, String reference,
			String type, String classname,boolean isgeometrypredicate) {
		return printPredicateObjectMap(isTemplate,predicate, reference, type, null, null,
				null, classname, isgeometrypredicate);
	}

	private String printPredicateObjectMap(String predicate, String reference,
			String type, String typeprefix, String predicatedprefix,
			String function, String classname, boolean isgeometrypredicate) {
		return printPredicateObjectMap(false, predicate, reference, type, typeprefix, predicatedprefix, function, classname, isgeometrypredicate);
	}
	private String printPredicateObjectMap(boolean isTemplate,String predicate, String reference,
			String type, String typeprefix, String predicatedprefix,
			String function, String classname, boolean isgeometrypredicate) {
		predicate = predicate.replace(".", "");
		StringBuilder sb = new StringBuilder();
		sb.append("rr:predicateObjectMap [\n");
		sb.append("\trr:predicateMap [ rr:constant "
				+ ((predicatedprefix == null) ? "onto" : (predicatedprefix))
				+ ":");
		if (!isgeometrypredicate) {
			predicate = "has"
					+ WordUtils.capitalize(predicate, new char[] { '-' });
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
				ontology.createDatatypeProperty(
						classname,
						"has"
								+ WordUtils.capitalize(predicate,
										new char[] { '-' }), type);
			}
			sb.append("\t\t"+(isTemplate?"rr:template":"rml:reference")+" \"" + reference + "\";\n");
		}
		sb.append("\t];\n");
		sb.append("];\n");
		return sb.toString();
	}

	private String printRefObjectMap(String predicate, String parentTriplesMap,
			String domainclassname, String rangeclassname) {
		return printRefObjectMap(predicate.replace(".", ""), parentTriplesMap,
				domainclassname, rangeclassname, false);
	}

	private String printRefObjectMap(String predicate, String parentTriplesMap,
			String domainclassname, String rangeclassname,
			boolean isSubTypeOfGeometry) {
		StringBuilder sb = new StringBuilder();
		sb.append("rr:predicateObjectMap [\n");
		sb.append("\trr:predicateMap [rr:constant onto:has_");
		sb.append(predicate + "];\n");
		sb.append("\trr:objectMap [\n");
		sb.append("\t\trr:parentTriplesMap <#" + parentTriplesMap + ">;\n");
		sb.append("\t\trr:joinCondition [\n");
		sb.append("\t\t\trr:childTriplesMap <#" + parentTriplesMap + ">;\n");
		sb.append("\t\t];\n");
		sb.append("\t];\n");
		sb.append("];\n");
		if (ontology != null) {
			System.out.println(domainclassname);
			System.out.println(predicate);
			System.out.println(rangeclassname);
			System.out.println(isSubTypeOfGeometry);
			ontology.createObjectProperty(domainclassname, "has_" + predicate,
					rangeclassname, isSubTypeOfGeometry);
			if (isSubTypeOfGeometry) {
				ontology.addFeatureAsSuperClass(domainclassname);
			}
		}
		return sb.toString();
	}

	private String printTriplesMap(String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("<#" + name + ">\n");
		return sb.toString();
	}

	private String printGEOPredicateObjectMaps() {
		StringBuilder sb = new StringBuilder();
		sb.append(printPredicateObjectMap("dimension", "*", "xsd:integer",
				null, "ogc", "dimension", "", true));
		sb.append(printPredicateObjectMap("asWKT", "*", "ogc:wktLiteral", null,
				"ogc", "asWKT", "", true));
		// sb.append(printPredicateObjectMap("asGML", "*", "ogc:gmlLiteral",
		// null,
		// "ogc", "asGML", "",true));
		sb.append(printPredicateObjectMap("is3D", "*", "xsd:boolean", null,
				"ogc", "is3D", "", true));
		sb.append(printPredicateObjectMap("isSimple", "*", "xsd:boolean", null,
				"ogc", "isSimple", "", true));
		sb.append(printPredicateObjectMap("hasSerialization", "*",
				"ogc:wktLiteral", null, "ogc", "hasSerialization", "", true));
		sb.append(printPredicateObjectMap("coordinateDimension", "*",
				"xsd:integer", null, "ogc", "coordinateDimension", "", true));
		sb.append(printPredicateObjectMap("spatialDimension", "*",
				"xsd:integer", null, "ogc", "spatialDimension", "", true));
		// TODO write all the GeoSPARQL properties
		return sb.toString();
	}

	public static void main(String[] args) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, ClassCastException,
			FileNotFoundException, XmlException, IOException {
		if (args.length < 4) {
			System.err
					.println("Please give arguments, eg <xsdfile> <xmlfile> <outputfile> <baseiri> <rootelement> <basepath> <namespaces> [<true or false> for generating classes for elements without type name]");
			System.exit(1);
		}
		// XMLMappingGenerator m=new XMLMappingGenerator("TF7.xsd" ,
		// "personal.xml" , "http://ex.com/" , true);
		XMLMappingGeneratorTrans m = new XMLMappingGeneratorTrans(args[0], args[1],
				args[2], args[3], args[4], args[5], args[6],
				(args.length > 7) ? Boolean.valueOf(args[7]) : false,
				(args.length > 8) ? args[8] : null, (args.length > 9) ? args[9]
						: null);
		m.run();
	}
}
