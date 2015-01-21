package eu.linkedeodata.geotriples.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * 
 * @author Ioannis Vlachopoulos
 * @author Dimitrianos Savva
 *
 * A simple API that reads an ontology and extracts its classes and properties
 */
public class OntologyLoader {
	
	/**
	 * The file from which the ontology is loaded
	 */
	private String ontologyFilePath = null;
	
	/**
	 * the ontology model as imported from the target ontology file
	 */
	private OntModel model;
	
	public OntologyLoader(String path) {
		this.setOntologyFilePath(path);
	}
	
	/**
	 * load the ontology from the specified file
	 */
	public void load() {
			    
	    model = ModelFactory.createOntologyModel();
	    try {
			model.read(new FileInputStream(ontologyFilePath), "http://data.linkedeodata.eu/natura-2000-de/");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getOntologyFilePath() {
		return ontologyFilePath;
	}

	public void setOntologyFilePath(String ontologyFilePath) {
		this.ontologyFilePath = ontologyFilePath;
	}
	
	public ExtendedIterator<OntClass> getClasses() {
		return model.listClasses();
	}
	
	public ExtendedIterator<DatatypeProperty> getProperties() {
		return model.listDatatypeProperties();
	}
	
}
