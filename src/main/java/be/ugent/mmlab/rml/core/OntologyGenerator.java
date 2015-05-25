package be.ugent.mmlab.rml.core;

import java.io.PrintStream;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.vocabulary.XSD;

public class OntologyGenerator {
	private OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
	private String NS = "http://data.linkedeodata.eu/ontology#";
	private String OGC = "http://www.opengis.net/ont/geosparql#";

	public OntologyGenerator(boolean useGeoSPARQL,String namespace) {
		if(useGeoSPARQL){
			useGeoSPARQL();
		}
		NS=namespace;
	}
	public void useGeoSPARQL(){
		m.read("http://www.opengis.net/ont/geosparql");
	}

	// public static void
	public void createClass(String name) {
		OntClass c = m.createClass(NS + name);
		c.addComment("Automatically generated by GeoTriples (version: "
				+ OntologyGenerator.class.getPackage()
						.getImplementationVersion() + ")", "EN");
		c.addLabel(name, "EN");
	}
	public void addFeatureAsSuperClass(String classname){
		OntClass c = m.getOntClass(NS + classname);
		c.addSuperClass(m.getOntClass(OGC + "Feature"));
	}

	public void createGeometryClass(String name) {
		OntClass c = m.createClass(NS + name);
		c.addComment("Automatically generated by GeoTriples (version: "
				+ OntologyGenerator.class.getPackage()
						.getImplementationVersion() + ")", "EN");
		c.addLabel(name, "EN");
		c.addSuperClass(m.getOntClass(OGC + "Geometry"));
	}

	public void createDatatypeProperty(String classname, String propertyname,
			String datatype) {
		OntClass c = m.getOntClass(NS + classname);
		DatatypeProperty p = m.createDatatypeProperty(NS + propertyname);
		p.addDomain(c);
		// RDFDatatype dt =
		// TypeMapper.getInstance().getSafeTypeByName(datatype);
		Resource dt = selectXSDType(datatype);
		if (dt != null)
			p.addRange(dt);
	}

	public void createObjectProperty(String domainclass, String propertyname,
			String rangeclass,boolean isSubPropertyOfHasGeometry) {
		OntClass dc = m.getOntClass(NS + domainclass);		
		OntClass rc = m.getOntClass(NS + rangeclass);
		if(rc==null){
			createClass(rangeclass);
			rc= m.getOntClass(NS + rangeclass);
		}
		ObjectProperty p = m.createObjectProperty(NS + propertyname);
		p.addDomain(dc);
		p.addRange(rc);
		if(isSubPropertyOfHasGeometry){
			p.addSuperProperty(m.getProperty(OGC + "hasGeometry"));
		}
	}

	public void writeToOutput(PrintStream out) {
		m.write(out,"N-TRIPLES");
	}

	protected Resource selectXSDType(String l) {
		 if (l.equalsIgnoreCase("integer"))
		 return XSD.integer;
		 else if (l.equalsIgnoreCase("int"))
		 return XSD.xint;
		 else if (l.equalsIgnoreCase("unsigned"))
		 return XSD.unsignedInt;
		 else if (l.equalsIgnoreCase("ulong") ||
		 l.equalsIgnoreCase("ulonglong"))
		 return XSD.unsignedLong;
		 else if (l.equalsIgnoreCase("short"))
		 return XSD.xshort;
		 else if (l.equalsIgnoreCase("long") ||
		 l.equalsIgnoreCase("longlong"))
		 return XSD.xlong;
		 else if (l.equalsIgnoreCase("string") || l.equalsIgnoreCase("char"))
		 return XSD.xstring;
		 else if (l.equalsIgnoreCase("float"))
		 return XSD.xfloat;
		 else if (l.equalsIgnoreCase("double")
		 || l.equalsIgnoreCase("longdouble"))
		 return XSD.xdouble;
		 else if (l.equalsIgnoreCase("boolean") || l.equalsIgnoreCase("bool"))
		 return XSD.xboolean;
		 else if (l.equalsIgnoreCase("decimal"))
		 return XSD.decimal;
		 else if (l.equalsIgnoreCase("nonNegativeInteger"))
		 return XSD.nonNegativeInteger;
		 else if (l.equalsIgnoreCase("date"))
		 return XSD.date;
		 else if (l.equalsIgnoreCase("time"))
		 return XSD.time;
		 else if (l.equalsIgnoreCase("datetime"))
		 return XSD.dateTime;
		 else if (l.equalsIgnoreCase("absolutedatetime"))
		 return XSD.dateTime;
		 else
		return new ResourceImpl(l);
		// return null;
	}
	public static void main(String[] args) {
		boolean useGeoSPARQL=true;
		OntologyGenerator a = new OntologyGenerator(useGeoSPARQL,"http://data.linkedeodata.eu/ontology#");
		a.createClass("A");
		a.createClass("B");
		a.createClass("C");
		
		a.createDatatypeProperty("A","name" , XSD.xstring.toString());
		a.createObjectProperty("A","name" , "B",true);
		a.writeToOutput(System.out);
		
	}
}
