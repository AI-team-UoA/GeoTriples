package org.d2rq.vocab;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.d2rq.D2RQException;
import org.d2rq.pp.PrettyPrinter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;


/**
 * Lists all the classes and properties in a schemagen-generated
 * vocabulary class, such as the {@link D2RQ} class.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class VocabularySummarizer {
	private final Class<? extends Object> [] vocabularyJavaClasses;
	private final String namespace;
	private final Set<Property> properties;
	private final Set<Resource> classes;
	private final Set<Resource> resources = new HashSet<Resource>();
	
	public VocabularySummarizer(Class<? extends Object> vocabularyJavaClasses[]) {
		this.vocabularyJavaClasses = vocabularyJavaClasses;
		namespace = findNamespace();
		properties = findAllProperties();
		classes = findAllClasses();
		resources.addAll(properties);
		resources.addAll(classes);
	}

	public void addResource(Resource resource) {
		resources.add(resource);
	}
	
	public void addClass(Resource class_) {
		classes.add(class_);
	}
	
	public void addProperty(Property property) {
		properties.add(property);
	}
	
	public Set<Property> getAllProperties() {
		return properties;
	}
	
	private Set<Property> findAllProperties() {
		Set<Property> results = new HashSet<Property>();
		for(int cl=0;cl<vocabularyJavaClasses.length;++cl)
		{
			Class<? extends Object> vocabularyJavaClass=vocabularyJavaClasses[cl];
		for (int i = 0; i < vocabularyJavaClass.getFields().length; i++) {
			Field field = vocabularyJavaClass.getFields()[i];
			if (!Modifier.isStatic(field.getModifiers())) continue;
			if (!Property.class.isAssignableFrom(field.getType())) continue;
			try {
				results.add((Property) field.get(null));
			} catch (IllegalAccessException ex) {
				throw new D2RQException(ex);
			}
		}
		}
		return results;
	}
	
	public Set<Resource> getAllClasses() {
		return classes;
	}
	
	private Set<Resource> findAllClasses() {
		Set<Resource> results = new HashSet<Resource>();
		for(int cl=0;cl<vocabularyJavaClasses.length;++cl)
		{
			Class<? extends Object> vocabularyJavaClass=vocabularyJavaClasses[cl];
		for (int i = 0; i < vocabularyJavaClass.getFields().length; i++) {
			Field field = vocabularyJavaClass.getFields()[i];
			if (!Modifier.isStatic(field.getModifiers())) continue;
			if (!Resource.class.isAssignableFrom(field.getType())) continue;
			if (Property.class.isAssignableFrom(field.getType())) continue;
			try {
				results.add((Resource) field.get(null));
			} catch (IllegalAccessException ex) {
				throw new D2RQException(ex);
			}
		}
		}
		return results;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	private String findNamespace() {
		try {
			for(int i=0;i<vocabularyJavaClasses.length;++i)
			{
				Class<? extends Object> vocabularyJavaClass=vocabularyJavaClasses[i];
				Object o = vocabularyJavaClass.getField("NS").get(vocabularyJavaClass);
				if (o instanceof String) {
					return (String) o;
				}
			}
			return null;
		} catch (NoSuchFieldException ex) {
			return null;
		} catch (IllegalAccessException ex) {
			return null;
		}
	}
	
	public Collection<Resource> getUndefinedClasses(Model model) {
		Set<Resource> result = new HashSet<Resource>();
		StmtIterator it = model.listStatements(null, RDF.type, (RDFNode) null);
		while (it.hasNext()) {
			Statement stmt = it.nextStatement();
			if (stmt.getObject().isURIResource()
					&& stmt.getResource().getURI().startsWith(namespace)
					&& !classes.contains(stmt.getObject())) {
				result.add(stmt.getResource());
			}
		}
		return result;
	}
	
	public Collection<Property> getUndefinedProperties(Model model) {
		Set<Property> result = new HashSet<Property>();
		StmtIterator it = model.listStatements();
		while (it.hasNext()) {
			Statement stmt = it.nextStatement();
			if (stmt.getPredicate().getURI().startsWith(namespace)
					&& !properties.contains(stmt.getPredicate())) {
				result.add(stmt.getPredicate());
			}
		}
		return result;
	}
	
	public Collection<Resource> getUndefinedResources(Model model) {
		Set<Resource> result = new HashSet<Resource>();
		StmtIterator it = model.listStatements();
		while (it.hasNext()) {
			Statement stmt = it.nextStatement();
			if (stmt.getSubject().isURIResource()
					&& stmt.getSubject().getURI().startsWith(namespace)
					&& !resources.contains(stmt.getSubject())) {
				result.add(stmt.getSubject());
			}
			if (stmt.getPredicate().equals(RDF.type)) continue;
			if (stmt.getObject().isURIResource()
					&& stmt.getResource().getURI().startsWith(namespace)
					&& !resources.contains(stmt.getResource())) {
				result.add(stmt.getResource());
			}
		}
		return result;
	}
	
	public void assertNoUndefinedTerms(Model model, 
			int undefinedPropertyErrorCode, int undefinedClassErrorCode) {
		Collection<Property> unknownProperties = getUndefinedProperties(model);
		if (!unknownProperties.isEmpty()) {
			throw new D2RQException(
					"Unknown property " + PrettyPrinter.toString(
							unknownProperties.iterator().next()) + ", maybe a typo?",
					undefinedPropertyErrorCode);
		}
		Collection<Resource> unknownClasses = getUndefinedClasses(model);
		if (!unknownClasses.isEmpty()) {
			throw new D2RQException(
					"Unknown class " + PrettyPrinter.toString(
							unknownClasses.iterator().next()) + ", maybe a typo?",
					undefinedClassErrorCode);
		}
	}
	
	public boolean usesVocabulary(Model model) {
		StmtIterator it = model.listStatements();
		while (it.hasNext()) {
			Statement stmt = it.nextStatement();
			if (stmt.getPredicate().getURI().startsWith(namespace)) {
				return true;
			}
			if (stmt.getPredicate().equals(RDF.type) && stmt.getResource().getURI().startsWith(namespace)) {
				return true;
			}
		}
		return false;
	}
	
	public Model triplesInvolvingVocabulary(Model model) {
		Model result = ModelFactory.createDefaultModel();
		result.getNsPrefixMap().putAll(model.getNsPrefixMap());
		StmtIterator it = model.listStatements();
		while (it.hasNext()) {
			Statement stmt = it.next();
			if (properties.contains(stmt.getPredicate())
					|| (stmt.getPredicate().equals(RDF.type) && classes.contains(stmt.getObject()))) {
				result.add(stmt);
			}
		}
		return result;
	}
}
