package org.d2rq.vocab;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/** SEE WP2 deliverable 1*/
public class RRX {
	/** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    private static final String NSXfunctions = "http://www.w3.org/ns/r2rml-ext/functions/def/";
private static final String NSX = "http://www.w3.org/ns/r2rml-ext#";
	
	public static String getURI() { return NSX; }
	public static String getFunctionsURI() { return NSXfunctions; }
    /** <p>Represents a logical geometry table.</p> */
    
    public static final Resource LogicalGeometryTable = m_model.createResource( "http://www.w3.org/ns/r2rml#LogicalGeometryTable" );
    
public static final Resource TransformationMap = m_model.createResource( "http://www.w3.org/ns/r2rml-ext#TransformationMap" );
	
	public static final Resource ArgumentMap = m_model.createResource( "http://www.w3.org/ns/r2rml-ext#ArgumentMap" );
	
	public static final Resource Function = m_model.createResource( "http://www.w3.org/ns/r2rml-ext#Function" );
	
	public static final Property function = m_model.createProperty( "http://www.w3.org/ns/r2rml-ext#function");
	
	public static final Property argumentMap = m_model.createProperty( "http://www.w3.org/ns/r2rml-ext#argumentMap");
	
	public static final Property transformation = m_model.createProperty( "http://www.w3.org/ns/r2rml-ext#transformation");
}
