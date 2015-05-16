package be.ugent.mmlab.rml.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

public class XMLMappingGenerator {
	private final static Log log = LogFactory.getLog(XMLMappingGenerator.class);
	private String pathToXML;
	private String xsdFileName;
	private HashMap<String, String> triplesMaps=new HashMap<>();
	private String baseURI="http://linkedeodata.eu/";
	private boolean allownulltypes=false;
	private File outputfile;
	public XMLMappingGenerator(String xsdfilename,String xmlfilename,String outputfile,String baseiri,boolean allownulltypesasclasses) throws ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException, FileNotFoundException, XmlException, IOException {
		this.baseURI=baseiri;
		if(!this.baseURI.endsWith("/")){
			this.baseURI+="/";
		}
		this.xsdFileName=xsdfilename;
		this.allownulltypes=allownulltypesasclasses;
		this.pathToXML=new File(xmlfilename).getAbsolutePath();
		this.outputfile=new File(outputfile);
	}
	public void run() throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, ClassCastException,
			XmlException, FileNotFoundException, IOException {

		SchemaTypeSystem sts = XmlBeans
				.compileXsd(new XmlObject[] { XmlObject.Factory
						.parse(new FileInputStream(xsdFileName)) }, XmlBeans
						.getBuiltinTypeSystem(), null);

		SchemaGlobalElement[] globals = sts.globalElements();
		for (int i = 0; i < globals.length; i++) {
			SchemaGlobalElement sge = globals[i];
			log.debug("Global Element " + i + " Name: "
					+ sge.getName() + " Type: " + sge.getType().getName());
			triplesMaps.put("/"+sge.getName().getLocalPart(), "");
			triplesMaps.put("/"+sge.getName().getLocalPart(), triplesMaps.get("/"+sge.getName().getLocalPart())+printTriplesMap(sge.getName().getLocalPart()));
			triplesMaps.put("/"+sge.getName().getLocalPart(), triplesMaps.get("/"+sge.getName().getLocalPart())+printLogicalSource("/" + sge.getName()));
			String classname=(sge.getType().getName()!=null)?sge.getType().getName().getLocalPart():sge.getName().getLocalPart();
			triplesMaps.put("/"+sge.getName().getLocalPart(), triplesMaps.get("/"+sge.getName().getLocalPart())+printSubjectMap(baseURI+classname+"/id/", classname));
			String targetNamespace = sge.getName().getNamespaceURI();
			log.debug("Namespace: " + targetNamespace);
			SchemaType st = sge.getType();
			for (SchemaProperty sp : st.getElementProperties()) {
				log.debug("\t" + "Element: " + sp.getName()
						+ " Type: " + sp.getType().getName());
				visit(sp, "/" + sge.getName() + "/" + sp.getName(),(sp.getType().getName()!=null)?"/" + sge.getName() + "/" + sp.getName():"/" + sge.getName() , 2);
			}
			break;
		}
		PrintStream out=new PrintStream(outputfile);
		for(String triplesMap:triplesMaps.keySet()){
			log.debug("TRIPLES MAP: "+triplesMap);
			
			out.println(triplesMaps.get(triplesMap).trim().substring(0, triplesMaps.get(triplesMap).trim().length()-1)+".\n");
			
			//log.debug("END TRIPLES MAP: "+triplesMap);
		}
		out.close();
	}
	
	private void visit(SchemaProperty sp, String path,String pathclass, int indent) {
		//log.debug(pathclass);
		String tabs = "";
		for (int i = 0; i < indent; ++i) {
			tabs += "\t";
		}

//		if (sp.getType().isSimpleType()) {
//			log.debug("simple type");
//		} else if (sp.getType().getName() == null) {
//			log.debug("no type");
//		} else {
//			log.debug("Class Found");
//		}

		for (SchemaProperty spp : sp.getType().getElementProperties()) {
			String newpath=path+"/"+spp.getName();
			log.debug(tabs + "Element: " + spp.getName() + " Type: "
					+ spp.getType().getName() + " Path: " + newpath);
			boolean hasNotNullType=spp.getType().getName()!=null;
			String typeName=(hasNotNullType)?spp.getType().getName().getLocalPart():spp.getName().getLocalPart();
			
			if(spp.getType().isSimpleType()){
				if(!triplesMaps.containsKey(pathclass))
				{
					triplesMaps.put(pathclass, " ");
				}
				String predicate=(sp.getType().getName()==null)?newpath.replaceFirst(pathclass, "").replaceFirst("/", "").replace("/", "_"):spp.getName().getLocalPart();
				String typename=(spp.getType().getName()!=null)?spp.getType().getName().getLocalPart():null;
				triplesMaps.put(pathclass, triplesMaps.get(pathclass)+printPredicateObjectMap(predicate, predicate.replace("_", "/"), typename));
			}
			else if(hasNotNullType || allownulltypes){
				//this is a class
				if(!triplesMaps.containsKey(pathclass))
				{
					triplesMaps.put(pathclass," ");
				}
				if(!triplesMaps.containsKey(newpath))
				{
					triplesMaps.put(newpath, " ");
				}
				triplesMaps.put(pathclass, triplesMaps.get(pathclass)+printRefObjectMap(spp.getName().getLocalPart(),newpath.replaceAll("/", "")));
				triplesMaps.put(newpath, triplesMaps.get(newpath)+printTriplesMap(newpath.replaceAll("/", "")));
				triplesMaps.put(newpath, triplesMaps.get(newpath)+printLogicalSource(newpath));
				triplesMaps.put(newpath, triplesMaps.get(newpath)+printSubjectMap(baseURI+typeName+"/id/",typeName));
				
			}
			String currentclasspath = (spp.getType().getName()==null && !allownulltypes)?pathclass:newpath;
			visit(spp, newpath,currentclasspath, indent + 1);
		}
		for (SchemaProperty spp : sp.getType().getAttributeProperties()) {
			String newpath=path+"/@"+spp.getName();
			log.debug(tabs + "Attribute: " + spp.getName() + " Type: "
					+ spp.getType().getName() + " Path: " + newpath);
			if(spp.getType().isSimpleType()){
				if(!triplesMaps.containsKey(pathclass))
				{
					triplesMaps.put(pathclass, " ");
				}
				String predicate=(sp.getType().getName()==null)?newpath.replaceFirst(pathclass, "").replaceFirst("/", "").replace("/", "_"):"@"+spp.getName().getLocalPart();
				String typename=(spp.getType().getName()!=null)?spp.getType().getName().getLocalPart():null;
				triplesMaps.put(pathclass, triplesMaps.get(pathclass)+printPredicateObjectMap(predicate.replace("@", ""), predicate.replace("_", "/"), typename));
			}
		}
	}
	private String printLogicalSource(String path){
		StringBuilder sb=new StringBuilder();
		sb.append("rml:logicalSource [\n"); 
		sb.append("\trml:source \"" + pathToXML + "\";\n");
		sb.append("\trml:referenceFormulation ql:XPath;\n");
		sb.append("\trml:iterator \""+path+"\";\n];\n");
		return sb.toString();
	}
	private String printSubjectMap(String baseuri,String classname){
		String base=baseuri+(baseuri.endsWith("/")?"":"/");
		StringBuilder sb =new  StringBuilder();
		sb.append("rr:subjectMap [\n");
		sb.append("\trr:template \""+base+"{GeoTriplesID}\";\n");
		sb.append("\trr:class onto:"+classname+";\n");
		sb.append("];\n");
		return sb.toString();
	}
	private String printPredicateObjectMap(String predicate,String reference,String type){
		StringBuilder sb=new StringBuilder();
		sb.append("rr:predicateObjectMap [\n");
		sb.append("\trr:predicate onto:");
		sb.append(predicate + ";\n");
		sb.append("\trr:objectMap [\n");
		if(type!=null){
			sb.append("\t\trr:datatype xsd:"+type+";\n");
		}
		sb.append("\t\trml:reference \""+reference+"\";\n");
		sb.append("\t];\n");
		sb.append("];\n");
		return sb.toString();
	}
	private String printRefObjectMap(String predicate,String parentTriplesMap){
		StringBuilder sb=new StringBuilder();
		sb.append("rr:predicateObjectMap [\n");
		sb.append("\trr:predicate onto:has_");
		sb.append(predicate + ";\n");
		sb.append("\trr:objectMap [\n");
		sb.append("\t\trr:parentTriplesMap <#"+parentTriplesMap+">;\n");
		sb.append("\t\trr:joinCondition [\n");
		sb.append("\t\t\trr:childTriplesMap <#"+parentTriplesMap+">;\n");
		sb.append("\t\t];\n");
		sb.append("\t];\n");
		sb.append("];\n");
		return sb.toString();
	}
	private String printTriplesMap(String name){
		StringBuilder sb=new StringBuilder();
		sb.append("<#"+name+">\n");
		return sb.toString();
	}
	
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException, FileNotFoundException, XmlException, IOException {
		if(args.length<3){
			System.err.println("Please give arguments, eg <xsdfile> <xmlfile> <outputfile> <baseiri> [<true or false> for generating classes for elements without type name]");
			System.exit(1);
		}
		//XMLMappingGenerator m=new XMLMappingGenerator("TF7.xsd" , "personal.xml" , "http://ex.com/" , true);
		XMLMappingGenerator m=new XMLMappingGenerator(args[0] , args[1] , args[2], args[3] , (args.length==4)?Boolean.valueOf(args[4]):false);
		m.run();
	}
}
