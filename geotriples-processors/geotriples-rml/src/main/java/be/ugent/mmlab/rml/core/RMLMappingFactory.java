/**
 * *************************************************************************
 *
 * RML : RML Mapping Factory abstract class
 *
 * Factory responsible of RML Mapping generation.
 *
 * based on R2RMLMappingFactory in db2triples
 *
 ***************************************************************************
 */
package be.ugent.mmlab.rml.core;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

import be.ugent.mmlab.rml.dataset.CustomSesameDataset;
import be.ugent.mmlab.rml.model.GraphMap;
import be.ugent.mmlab.rml.model.JoinCondition;
import be.ugent.mmlab.rml.model.LogicalSource;
import be.ugent.mmlab.rml.model.ObjectMap;
import be.ugent.mmlab.rml.model.PredicateMap;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.RMLMapping;
import be.ugent.mmlab.rml.model.ReferencingObjectMap;
import be.ugent.mmlab.rml.model.StdGraphMap;
import be.ugent.mmlab.rml.model.StdJoinCondition;
import be.ugent.mmlab.rml.model.StdLogicalSource;
import be.ugent.mmlab.rml.model.StdObjectMap;
import be.ugent.mmlab.rml.model.StdPredicateMap;
import be.ugent.mmlab.rml.model.StdPredicateObjectMap;
import be.ugent.mmlab.rml.model.StdReferencingObjectMap;
import be.ugent.mmlab.rml.model.StdSubjectMap;
import be.ugent.mmlab.rml.model.StdTriplesMap;
import be.ugent.mmlab.rml.model.SubjectMap;
import be.ugent.mmlab.rml.model.TermMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.reference.ReferenceIdentifier;
import be.ugent.mmlab.rml.model.reference.ReferenceIdentifierImpl;
import be.ugent.mmlab.rml.vocabulary.Vocab;
import be.ugent.mmlab.rml.vocabulary.Vocab.R2RMLTerm;
import be.ugent.mmlab.rml.vocabulary.Vocab.RMLTerm;
import be.ugent.mmlab.rml.vocabulary.VocabTrans;
import be.ugent.mmlab.rml.vocabulary.VocabTrans.RRXTerm;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLStructureException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLSyntaxException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.R2RMLDataError;

public abstract class RMLMappingFactory {

	// Logger
	private static Logger log = LoggerFactory.getLogger(RMLMappingFactory.class);
	// Value factory
	private static ValueFactory vf = new ValueFactoryImpl();

	/**
	 * Extract RML Mapping object from a RML file written with Turtle syntax.
	 * 
	 * Important : The R2RML vocabulary also includes the following R2RML
	 * classes, which represent various R2RML mapping constructs. Using these
	 * classes is optional in a mapping graph. The applicable class of a
	 * resource can always be inferred from its properties. Consequently, in
	 * order to identify each triple type, a rule will be used to extract the
	 * applicable class of a resource.
	 * 
	 * @param fileToRMLFile
	 * @return
	 * @throws InvalidR2RMLSyntaxException
	 * @throws InvalidR2RMLStructureException
	 * @throws R2RMLDataError
	 * @throws IOException
	 * @throws RDFParseException
	 * @throws RepositoryException
	 */
	public static RMLMapping extractRMLMapping(Object fileToRMLFile)
			throws InvalidR2RMLStructureException, InvalidR2RMLSyntaxException,
			R2RMLDataError, RepositoryException, RDFParseException, IOException {
		// Load RDF data from R2RML Mapping document
		CustomSesameDataset r2rmlMappingGraph = new CustomSesameDataset();
		if(fileToRMLFile instanceof InputStream){
			r2rmlMappingGraph.loadDataFromInputStream((InputStream)fileToRMLFile, RDFFormat.TURTLE,new URIImpl("http://mplamplou"));
		}
		else if(fileToRMLFile instanceof String)
		r2rmlMappingGraph.loadDataFromFile((String)fileToRMLFile, RDFFormat.TURTLE);
		else{
			r2rmlMappingGraph.loadDataFromURL(fileToRMLFile.toString());
		}
		
		log.debug("[RMLMappingFactory:extractRMLMapping] Number of R2RML triples in file "
				+ fileToRMLFile + " : " + r2rmlMappingGraph.getSize());
		// Transform RDF with replacement shortcuts
		replaceShortcuts(r2rmlMappingGraph);
		// Run few tests to help user in its RDF syntax
		launchPreChecks(r2rmlMappingGraph);
		// Construct R2RML Mapping object
		Map<Resource, TriplesMap> triplesMapResources = extractTripleMapResources(r2rmlMappingGraph);

		
		log.debug("[RMLMappingFactory:extractRMLMapping] Number of RML triples with "
				+ " type "
				+ R2RMLTerm.TRIPLES_MAP_CLASS
				+ " in file "
				+ fileToRMLFile + " : " + triplesMapResources.size());
		// Fill each triplesMap object
		for (Resource triplesMapResource : triplesMapResources.keySet()) // Extract
																			// each
																			// triplesMap
		{
			extractTriplesMap(r2rmlMappingGraph, triplesMapResource,
					triplesMapResources);
		}
		// Generate RMLMapping object
		RMLMapping result = new RMLMapping(triplesMapResources.values());
		return result;
	}

	/**
	 * Constant-valued term maps can be expressed more concisely using the
	 * constant shortcut properties rr:subject, rr:predicate, rr:object and
	 * rr:graph. Occurrances of these properties must be treated exactly as if
	 * the following triples were present in the mapping graph instead.
	 * 
	 * @param r2rmlMappingGraph
	 */
	protected static void replaceShortcuts(CustomSesameDataset r2rmlMappingGraph) {
		Map<URI, URI> shortcutPredicates = new HashMap<URI, URI>();
		shortcutPredicates.put(
				vf.createURI(Vocab.R2RML_NAMESPACE + R2RMLTerm.SUBJECT),
				vf.createURI(Vocab.R2RML_NAMESPACE + R2RMLTerm.SUBJECT_MAP));
		shortcutPredicates.put(
				vf.createURI(Vocab.R2RML_NAMESPACE + R2RMLTerm.PREDICATE),
				vf.createURI(Vocab.R2RML_NAMESPACE + R2RMLTerm.PREDICATE_MAP));
		shortcutPredicates.put(
				vf.createURI(Vocab.R2RML_NAMESPACE + R2RMLTerm.OBJECT),
				vf.createURI(Vocab.R2RML_NAMESPACE + R2RMLTerm.OBJECT_MAP));
		shortcutPredicates.put(
				vf.createURI(Vocab.R2RML_NAMESPACE + R2RMLTerm.GRAPH),
				vf.createURI(Vocab.R2RML_NAMESPACE + R2RMLTerm.GRAPH_MAP));
		for (URI u : shortcutPredicates.keySet()) {
			List<Statement> shortcutTriples = r2rmlMappingGraph.tuplePattern(
					null, u, null);
			log.debug("[RMLMappingFactory:replaceShortcuts] Number of R2RML shortcuts found "
					+ "for "
					+ u.getLocalName()
					+ " : "
					+ shortcutTriples.size());
			int progress=0;
			for (Statement shortcutTriple : shortcutTriples) {
				log.debug("Processing "+ (++progress) + "shorcut");
				r2rmlMappingGraph.remove(shortcutTriple.getSubject(),
						shortcutTriple.getPredicate(),
						shortcutTriple.getObject());
				BNode blankMap = vf.createBNode();

				URI pMap = vf.createURI(shortcutPredicates.get(u).toString());
				URI pConstant = vf.createURI(Vocab.R2RML_NAMESPACE
						+ R2RMLTerm.CONSTANT);
				r2rmlMappingGraph.add(shortcutTriple.getSubject(), pMap,
						blankMap);
				r2rmlMappingGraph.add(blankMap, pConstant,
						shortcutTriple.getObject());
			}
		}
		//r2rmlMappingGraph.dumpRDF("processed_mapping.ttl", RDFFormat.TURTLE);
	}

	/**
	 * Construct TriplesMap objects rule. A triples map is represented by a
	 * resource that references the following other resources : - It must have
	 * exactly one subject map * using the rr:subjectMap property.
	 * 
	 * @param r2rmlMappingGraph
	 * @return
	 * @throws InvalidR2RMLStructureException
	 */
	protected static Map<Resource, TriplesMap> extractTripleMapResources(
			CustomSesameDataset r2rmlMappingGraph)
			throws InvalidR2RMLStructureException {
		// A triples map is represented by a resource that references the
		// following other resources :
		// - It must have exactly one subject map
		Map<Resource, TriplesMap> triplesMapResources = new HashMap<Resource, TriplesMap>();
		URI p = r2rmlMappingGraph.URIref(Vocab.R2RML_NAMESPACE
				+ Vocab.R2RMLTerm.SUBJECT_MAP);
		List<Statement> statements = r2rmlMappingGraph.tuplePattern(null, p,
				null);
		if (statements.isEmpty()) {
			log.warn("[RMLMappingFactory:extractRMLMapping] No subject statement found. Exit...");
		} /*
		 * throw new InvalidR2RMLStructureException(
		 * "[RMLMappingFactory:extractRMLMapping]" +
		 * " One subject statement is required.");
		 */else // No subject map, Many shortcuts subjects
		{
			for (Statement s : statements) {
				List<Statement> otherStatements = r2rmlMappingGraph
						.tuplePattern(s.getSubject(), p, null);
				if (otherStatements.size() > 1) {
					throw new InvalidR2RMLStructureException(
							"[RMLMappingFactory:extractRMLMapping] "
									+ s.getSubject() + " has many subjectMap "
									+ "(or subject) but only one is required.");
				} else // First initialization of triples map : stored to link
						// them
				// with referencing objects
				{
					triplesMapResources.put(s.getSubject(), new StdTriplesMap(
							null, null, null, s.getSubject().stringValue()));
				}
			}
		}
		return triplesMapResources;
	}

	protected static void launchPreChecks(CustomSesameDataset r2rmlMappingGraph)
			throws InvalidR2RMLStructureException {
		// Pre-check 1 : test if a triplesMap with predicateObject map exists
		// without subject map
		URI p = r2rmlMappingGraph.URIref(Vocab.R2RML_NAMESPACE
				+ R2RMLTerm.PREDICATE_OBJECT_MAP);
		List<Statement> statements = r2rmlMappingGraph.tuplePattern(null, p,
				null);
		for (Statement s : statements) {
			p = r2rmlMappingGraph.URIref(Vocab.R2RML_NAMESPACE
					+ R2RMLTerm.SUBJECT_MAP);
			List<Statement> otherStatements = r2rmlMappingGraph.tuplePattern(
					s.getSubject(), p, null);
			if (otherStatements.isEmpty()) {
				throw new InvalidR2RMLStructureException(
						"[RMLMappingFactory:launchPreChecks] You have a triples map without subject map : "
								+ s.getSubject().stringValue() + ".");
			}
		}
	}

	/**
	 * Extract triplesMap contents.
	 * 
	 * @param triplesMap
	 * @param r2rmlMappingGraph
	 * @param triplesMapSubject
	 * @param triplesMapResources
	 * @param storedTriplesMaps
	 * @throws InvalidR2RMLStructureException
	 * @throws InvalidR2RMLSyntaxException
	 * @throws R2RMLDataError
	 */
	protected static void extractTriplesMap(CustomSesameDataset r2rmlMappingGraph,
			Resource triplesMapSubject,
			Map<Resource, TriplesMap> triplesMapResources)
			throws InvalidR2RMLStructureException, InvalidR2RMLSyntaxException,
			R2RMLDataError {

		if (log.isDebugEnabled()) {
			log.debug("[RMLMappingFactory:extractTriplesMap] Extract TriplesMap subject : "
					+ triplesMapSubject.stringValue());
		}

		TriplesMap result = triplesMapResources.get(triplesMapSubject);

		// Extract TriplesMap properties
		// MVS: create LogicalSource
		LogicalSource logicalSource = extractLogicalSource(r2rmlMappingGraph,
				triplesMapSubject);

		// Extract subject
		// Create a graph maps storage to save all met graph uri during parsing.
		Set<GraphMap> graphMaps = new HashSet<GraphMap>();
		// log.debug("[RMLMappingFactory:extractTriplesMap] Current number of created graphMaps : "
		// + graphMaps.size());
		SubjectMap subjectMap = extractSubjectMap(r2rmlMappingGraph,
				triplesMapSubject, graphMaps, result);
		// log.debug("[RMLMappingFactory:extractTriplesMap] Current number of created graphMaps : "
		// + graphMaps.size());
		// Extract predicate-object maps
		Set<PredicateObjectMap> predicateObjectMaps = extractPredicateObjectMaps(
				r2rmlMappingGraph, triplesMapSubject, graphMaps, result,
				triplesMapResources);
		log.debug("[RMLMappingFactory:extractTriplesMap] Current number of created graphMaps : "
				+ graphMaps.size());
		// Fill triplesMap
		for (PredicateObjectMap predicateObjectMap : predicateObjectMaps) {
			result.addPredicateObjectMap(predicateObjectMap);
		}
		result.setLogicalSource(logicalSource);
		result.setSubjectMap(subjectMap);
		log.debug("[RMLMappingFactory:extractTriplesMap] Extract of TriplesMap subject : "
				+ triplesMapSubject.stringValue() + " done.");
	}

	/*
	 * Still needs changing!!!!
	 */
	protected static Set<PredicateObjectMap> extractPredicateObjectMaps(
			CustomSesameDataset r2rmlMappingGraph, Resource triplesMapSubject,
			Set<GraphMap> graphMaps, TriplesMap result,
			Map<Resource, TriplesMap> triplesMapResources)
			throws InvalidR2RMLStructureException, R2RMLDataError,
			InvalidR2RMLSyntaxException {
		log.debug("[RMLMappingFactory:extractPredicateObjectMaps] Extract predicate-object maps...");
		// Extract predicate-object maps
		URI p = r2rmlMappingGraph.URIref(Vocab.R2RML_NAMESPACE
				+ R2RMLTerm.PREDICATE_OBJECT_MAP);

		List<Statement> statements = r2rmlMappingGraph.tuplePattern(
				triplesMapSubject, p, null);

		Set<PredicateObjectMap> predicateObjectMaps = new HashSet<PredicateObjectMap>();
		try {
			for (Statement statement : statements) {
				PredicateObjectMap predicateObjectMap = extractPredicateObjectMap(
						r2rmlMappingGraph, (Resource) statement.getObject(),
						graphMaps, triplesMapResources);
				// Add own tripleMap to predicateObjectMap
				predicateObjectMap.setOwnTriplesMap(result);
				predicateObjectMaps.add(predicateObjectMap);
			}
		} catch (ClassCastException e) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractPredicateObjectMaps] "
							+ "A resource was expected in object of predicateObjectMap of "
							+ triplesMapSubject.stringValue());
		}
		log.debug("[RMLMappingFactory:extractPredicateObjectMaps] Number of extracted predicate-object maps : "
				+ predicateObjectMaps.size());
		return predicateObjectMaps;
	}

	/*
	 * Still needs changing
	 */

	protected static PredicateObjectMap extractPredicateObjectMap(
			CustomSesameDataset r2rmlMappingGraph, Resource predicateObject,
			Set<GraphMap> savedGraphMaps,
			Map<Resource, TriplesMap> triplesMapResources)
			throws InvalidR2RMLStructureException, R2RMLDataError,
			InvalidR2RMLSyntaxException {
		log.debug("[RMLMappingFactory:extractPredicateObjectMap] Extract predicate-object map..");
		// Extract predicate maps
		URI p = r2rmlMappingGraph.URIref(Vocab.R2RML_NAMESPACE
				+ R2RMLTerm.PREDICATE_MAP);

		List<Statement> statements = r2rmlMappingGraph.tuplePattern(
				predicateObject, p, null);

		if (statements.size() < 1) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractSubjectMap] "
							+ predicateObject.stringValue()
							+ " has no predicate map defined : one or more is required.");
		}

		Set<PredicateMap> predicateMaps = new HashSet<PredicateMap>();
		try {
			for (Statement statement : statements) {
				log.debug("[RMLMappingFactory] saved Graphs " + savedGraphMaps);
				PredicateMap predicateMap = extractPredicateMap(
						r2rmlMappingGraph, (Resource) statement.getObject(),
						savedGraphMaps);
				predicateMaps.add(predicateMap);
			}
		} catch (ClassCastException e) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractPredicateObjectMaps] "
							+ "A resource was expected in object of predicateMap of "
							+ predicateObject.stringValue());
		}
		// Extract object maps
		URI o = r2rmlMappingGraph.URIref(Vocab.R2RML_NAMESPACE
				+ R2RMLTerm.OBJECT_MAP);
		statements = r2rmlMappingGraph.tuplePattern(predicateObject, o, null);
		if (statements.size() < 1) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractPredicateObjectMap] "
							+ predicateObject.stringValue()
							+ " has no object map defined : one or more is required.");
		}
		Set<ObjectMap> objectMaps = new HashSet<ObjectMap>();
		Set<ReferencingObjectMap> refObjectMaps = new HashSet<ReferencingObjectMap>();
		try {
			for (Statement statement : statements) {
				log.debug("[RMLMappingFactory:extractPredicateObjectMap] Try to extract object map..");
				ReferencingObjectMap refObjectMap = extractReferencingObjectMap(
						r2rmlMappingGraph, (Resource) statement.getObject(),
						savedGraphMaps, triplesMapResources);
				if (refObjectMap != null) {
					refObjectMaps.add(refObjectMap);
					// Not a simple object map, skip to next.
					continue;
				}
				ObjectMap objectMap = extractObjectMap(r2rmlMappingGraph,
						(Resource) statement.getObject(), savedGraphMaps,triplesMapResources);
				if (objectMap != null) {
					objectMaps.add(objectMap);
				}
			}
		} catch (ClassCastException e) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractPredicateObjectMaps] "
							+ "A resource was expected in object of objectMap of "
							+ predicateObject.stringValue());
		}
		PredicateObjectMap predicateObjectMap = new StdPredicateObjectMap(
				predicateMaps, objectMaps, refObjectMaps);

		// Add graphMaps
		Set<GraphMap> graphMaps = new HashSet<GraphMap>();
		Set<Value> graphMapValues = extractValuesFromResource(
				r2rmlMappingGraph, predicateObject, R2RMLTerm.GRAPH_MAP);

		if (graphMapValues != null) {
			graphMaps = extractGraphMapValues(r2rmlMappingGraph,
					graphMapValues, savedGraphMaps);
			log.info("[RMLMappingFactory] graph Maps returned " + graphMaps);
		}
		/*
		 * Set<GraphMap> graphMaps = new HashSet<GraphMap>();
		 * log.debug("[RMLMappingFactory] GraphMaps " + graphMaps); if
		 * (graphMapValues != null) { for (Value graphMap : graphMapValues) {
		 * log.info("[RMLMappingFactory] graph map + " + graphMap); // Create
		 * associated graphMap if it has not already created boolean found =
		 * false; GraphMap graphMapFound = null; /* for (GraphMap savedGraphMap
		 * : savedGraphMaps) if (savedGraphMap.getGraph().equals(graphMap)) {
		 * found = true; graphMapFound = savedGraphMap; }
		 */
		/*
		 * if (found) { log.info("[RMLMappingFactory] graph map + " + graphMap);
		 * graphMaps.add(graphMapFound); } else { GraphMap newGraphMap =
		 * extractGraphMap(r2rmlMappingGraph, (Resource) graphMap);
		 * savedGraphMaps.add(newGraphMap); graphMaps.add(newGraphMap);
		 * log.info("[RMLMappingFactory] new graph map + " + newGraphMap); } } }
		 */
		predicateObjectMap.setGraphMaps(graphMaps);
		log.debug("[RMLMappingFactory:extractPredicateObjectMap] Extract predicate-object map done.");
		return predicateObjectMap;
	}

	/*
	 * Still needs changing
	 */

	protected static ReferencingObjectMap extractReferencingObjectMap(
			CustomSesameDataset r2rmlMappingGraph, Resource object,
			Set<GraphMap> graphMaps,
			Map<Resource, TriplesMap> triplesMapResources)
			throws InvalidR2RMLStructureException, InvalidR2RMLSyntaxException, R2RMLDataError {
		log.debug("[RMLMappingFactory:extractReferencingObjectMap] Extract referencing object map..");
		URI parentTriplesMap = (URI) extractValueFromTermMap(r2rmlMappingGraph,
				object, R2RMLTerm.PARENT_TRIPLES_MAP);
		Set<JoinCondition> joinConditions = extractJoinConditions(
				r2rmlMappingGraph, object, graphMaps, triplesMapResources);
		
		
		if (parentTriplesMap == null && !joinConditions.isEmpty()) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractReferencingObjectMap] "
							+ object.stringValue()
							+ " has no parentTriplesMap map defined whereas one or more joinConditions exist"
							+ " : exactly one parentTripleMap is required.");
		}
		if (parentTriplesMap == null && joinConditions.isEmpty()) {
			log.debug("[RMLMappingFactory:extractReferencingObjectMap] This object map is not a referencing object map.");
			return null;
		}
		// Extract parent
		boolean contains = false;
		TriplesMap parent = null;
		for (Resource triplesMapResource : triplesMapResources.keySet()) {
			if (triplesMapResource.stringValue().equals(
					parentTriplesMap.stringValue())) {
				contains = true;
				parent = triplesMapResources.get(triplesMapResource);
				log.debug("[RMLMappingFactory:extractReferencingObjectMap] Parent triples map found : "
						+ triplesMapResource.stringValue());
				break;
			}
		}
		if (!contains) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractReferencingObjectMap] "
							+ object.stringValue()
							+ " reference to parent triples maps is broken : "
							+ parentTriplesMap.stringValue() + " not found.");
		}
		// Link between this reerencing object and its triplesMap parent will be
		// performed
		// at the end f treatment.
		ReferencingObjectMap refObjectMap = new StdReferencingObjectMap(null,
				parent, joinConditions);
		log.debug("[RMLMappingFactory:extractReferencingObjectMap] Extract referencing object map done.");
		return refObjectMap;
	}

	/*
	 * Still needs changing
	 */

	protected static Set<JoinCondition> extractJoinConditions(
			CustomSesameDataset r2rmlMappingGraph, Resource object ,Set<GraphMap> graphMaps , Map<Resource, TriplesMap> triplesMapResources)
			throws InvalidR2RMLStructureException, InvalidR2RMLSyntaxException, R2RMLDataError {
		log.debug("[RMLMappingFactory:extractJoinConditions] Extract join conditions..");
		Set<JoinCondition> result = new HashSet<JoinCondition>();
		// Extract predicate-object maps
		URI p = r2rmlMappingGraph.URIref(Vocab.R2RML_NAMESPACE
				+ R2RMLTerm.JOIN_CONDITION);
		List<Statement> statements = r2rmlMappingGraph.tuplePattern(object, p,
				null);
		try {
			for (Statement statement : statements) {
				Resource jc = (Resource) statement.getObject();
				URI function= (URI) extractValueFromTermMap(r2rmlMappingGraph, jc,
						RRXTerm.FUNCTION);
				//start argumentMap
				Resource argumentMap = (Resource) extractValueFromTermMap(
						r2rmlMappingGraph, jc, RRXTerm.ARGUMENTMAP);
				List<TermMap> arguments = new ArrayList<>();
				if (argumentMap != null) {
					List<Statement> first = null;
					// System.out.println(firstnode);
					// System.out.println(firstnode.getObject()); //we got the termmap

					Resource it = (Resource) (argumentMap);
					while (!RDF.NIL.equals(it)) {
						first = r2rmlMappingGraph.tuplePattern(it, RDF.FIRST, null);
						TermMap tm = extractObjectMap(r2rmlMappingGraph,
								(Resource) first.get(0).getObject(), graphMaps,triplesMapResources);
						arguments.add(tm);
						List<Statement> next = r2rmlMappingGraph.tuplePattern(it,
								RDF.REST, null);
						it = (Resource) next.get(0).getObject();
					}

					Set<Value> valls = extractValuesFromResource(r2rmlMappingGraph,
							argumentMap);
					//System.out.println(valls);
				}
				//end argumentMap
				
				
				
				
				String child = extractLiteralFromTermMap(r2rmlMappingGraph, jc,
						R2RMLTerm.CHILD);
				String parent = extractLiteralFromTermMap(r2rmlMappingGraph,
						jc, R2RMLTerm.PARENT);
				URI parentTriplesMap = (URI) extractValueFromTermMap(r2rmlMappingGraph,
						jc, R2RMLTerm.PARENT_TRIPLES_MAP);
				URI childTriplesMap = (URI) extractValueFromTermMap(r2rmlMappingGraph,
						jc, R2RMLTerm.CHILD_TRIPLES_MAP);
				if ((parent == null || child == null) && function==null && childTriplesMap==null) {
					throw new InvalidR2RMLStructureException(
							"[RMLMappingFactory:extractReferencingObjectMap] "
									+ object.stringValue()
									+ " must have exactly two properties child and parent or (function and argumentsMap) or (parentTriplesMap and childTriplesMap). ");
				}
				TriplesMap parentTriplesMapTM=triplesMapResources.get(parentTriplesMap);
				TriplesMap childTriplesMapTM=triplesMapResources.get(childTriplesMap);
				result.add(new StdJoinCondition(child, parent,function,arguments,parentTriplesMapTM,childTriplesMapTM));
			}
		} catch (ClassCastException e) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractJoinConditions] "
							+ "A resource was expected in object of predicateMap of "
							+ object.stringValue());
		}
		log.debug("[RMLMappingFactory:extractJoinConditions] Extract join conditions done.");
		return result;
	}

	/*
	 * Still needs changing
	 */

	protected static ObjectMap extractObjectMap(
			CustomSesameDataset r2rmlMappingGraph, Resource object,
			Set<GraphMap> graphMaps , Map<Resource, TriplesMap> triplesMapResources) throws InvalidR2RMLStructureException,
			R2RMLDataError, InvalidR2RMLSyntaxException {
		log.debug("[RMLMappingFactory:extractObjectMap] Extract object map..");
		// Extract object maps properties
		Value constantValue = extractValueFromTermMap(r2rmlMappingGraph,
				object, R2RMLTerm.CONSTANT);
		String stringTemplate = extractLiteralFromTermMap(r2rmlMappingGraph,
				object, R2RMLTerm.TEMPLATE);
		String languageTag = extractLiteralFromTermMap(r2rmlMappingGraph,
				object, R2RMLTerm.LANGUAGE);
		URI termType = (URI) extractValueFromTermMap(r2rmlMappingGraph, object,
				R2RMLTerm.TERM_TYPE);
		URI dataType = (URI) extractValueFromTermMap(r2rmlMappingGraph, object,
				R2RMLTerm.DATATYPE);
		String inverseExpression = extractLiteralFromTermMap(r2rmlMappingGraph,
				object, R2RMLTerm.INVERSE_EXPRESSION);
		URI function = (URI) extractValueFromTermMap(r2rmlMappingGraph, object,
				RRXTerm.FUNCTION);
		Resource argumentMap = (Resource) extractValueFromTermMap(
				r2rmlMappingGraph, object, RRXTerm.ARGUMENTMAP);
		
		//System.out.println(function);
		
		URI triplesMap = (URI) extractValueFromTermMap(r2rmlMappingGraph,
				object, RRXTerm.TRIPLES_MAP);
		TriplesMap owner=null;
		if(triplesMap!=null){
			for (Resource triplesMapResource : triplesMapResources.keySet()) {
				if (triplesMapResource.stringValue().equals(
						triplesMap.stringValue())) {
					owner=triplesMapResources.get(triplesMapResource);
					break;
				}
			}
		}
		
		
		List<TermMap> arguments = new ArrayList<>();
		if (argumentMap != null) {
			List<Statement> first = null;
			// System.out.println(firstnode);
			// System.out.println(firstnode.getObject()); //we got the termmap

			Resource it = (Resource) (argumentMap);
			while (!RDF.NIL.equals(it)) {
				first = r2rmlMappingGraph.tuplePattern(it, RDF.FIRST, null);
				TermMap tm = extractObjectMap(r2rmlMappingGraph,
						(Resource) first.get(0).getObject(), graphMaps,triplesMapResources);
				arguments.add(tm);
				List<Statement> next = r2rmlMappingGraph.tuplePattern(it,
						RDF.REST, null);
				it = (Resource) next.get(0).getObject();
			}

			//Set<Value> valls = extractValuesFromResource(r2rmlMappingGraph,
				//	argumentMap);
			//System.out.println(valls);
		}

		// MVS: Decide on ReferenceIdentifier
		ReferenceIdentifier referenceValue = extractReferenceIdentifier(
				r2rmlMappingGraph, object);

		StdObjectMap result = new StdObjectMap(null, constantValue, dataType,
				languageTag, stringTemplate, termType, inverseExpression,
				referenceValue, function, arguments,owner);
		log.debug("[RMLMappingFactory:extractObjectMap] Extract object map done.");
		// return null;
		return result;
	}

	protected static ReferenceIdentifier extractReferenceIdentifier(
			CustomSesameDataset r2rmlMappingGraph, Resource resource)
			throws InvalidR2RMLStructureException {
		// MVS: look for a reference or column, prefer rr:column
		String columnValueStr = extractLiteralFromTermMap(r2rmlMappingGraph,
				resource, R2RMLTerm.COLUMN);
		String referenceValueStr = extractLiteralFromTermMap(r2rmlMappingGraph,
				resource, RMLTerm.REFERENCE);

		if (columnValueStr != null && referenceValueStr != null) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractReferenceIdentifier] "
							+ resource + " has a reference and column defined.");
		}

		// MVS: use the generic ReferenceIdentifier to represent rr:column or
		// rml:reference
		if (columnValueStr != null) {
			return ReferenceIdentifierImpl
					.buildFromR2RMLConfigFile(columnValueStr);
		}

		return ReferenceIdentifierImpl
				.buildFromR2RMLConfigFile(referenceValueStr);
	}

	protected static PredicateMap extractPredicateMap(
			CustomSesameDataset r2rmlMappingGraph, Resource object,
			Set<GraphMap> graphMaps) throws InvalidR2RMLStructureException,
			R2RMLDataError, InvalidR2RMLSyntaxException {
		log.debug("[RMLMappingFactory:extractPredicateMap] Extract predicate map..");
		// Extract object maps properties
		Value constantValue = extractValueFromTermMap(r2rmlMappingGraph,
				object, R2RMLTerm.CONSTANT);
		String stringTemplate = extractLiteralFromTermMap(r2rmlMappingGraph,
				object, R2RMLTerm.TEMPLATE);
		URI termType = (URI) extractValueFromTermMap(r2rmlMappingGraph, object,
				R2RMLTerm.TERM_TYPE);

		String inverseExpression = extractLiteralFromTermMap(r2rmlMappingGraph,
				object, R2RMLTerm.INVERSE_EXPRESSION);

		// MVS: Decide on ReferenceIdentifier
		ReferenceIdentifier referenceValue = extractReferenceIdentifier(
				r2rmlMappingGraph, object);

		PredicateMap result = new StdPredicateMap(null, constantValue,
				stringTemplate, inverseExpression, referenceValue, termType);
		log.debug("[RMLMappingFactory:extractPredicateMap] Extract predicate map done.");
		return result;
	}

	/**
	 * Extract subjectMap contents
	 * 
	 * @param r2rmlMappingGraph
	 * @param triplesMapSubject
	 * @return
	 * @throws InvalidR2RMLStructureException
	 * @throws InvalidR2RMLSyntaxException
	 * @throws R2RMLDataError
	 */
	protected static SubjectMap extractSubjectMap(
			CustomSesameDataset r2rmlMappingGraph, Resource triplesMapSubject,
			Set<GraphMap> savedGraphMaps, TriplesMap ownTriplesMap)
			throws InvalidR2RMLStructureException, R2RMLDataError,
			InvalidR2RMLSyntaxException {
		log.debug("[RMLMappingFactory:extractPredicateObjectMaps] Extract subject map...");
		// Extract subject map
		URI p = r2rmlMappingGraph.URIref(Vocab.R2RML_NAMESPACE
				+ R2RMLTerm.SUBJECT_MAP);
		List<Statement> statements = r2rmlMappingGraph.tuplePattern(
				triplesMapSubject, p, null);

		if (statements.isEmpty()) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractSubjectMap] "
							+ triplesMapSubject
							+ " has no subject map defined.");
		}
		if (statements.size() > 1) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractSubjectMap] "
							+ triplesMapSubject
							+ " has too many subject map defined.");
		}

		Resource subjectMap = (Resource) statements.get(0).getObject();
		log.debug("[RMLMappingFactory:extractTriplesMap] Found subject map : "
				+ subjectMap.stringValue());

		Value constantValue = extractValueFromTermMap(r2rmlMappingGraph,
				subjectMap, R2RMLTerm.CONSTANT);
		String stringTemplate = extractLiteralFromTermMap(r2rmlMappingGraph,
				subjectMap, R2RMLTerm.TEMPLATE);
		URI termType = (URI) extractValueFromTermMap(r2rmlMappingGraph,
				subjectMap, R2RMLTerm.TERM_TYPE);
		String inverseExpression = extractLiteralFromTermMap(r2rmlMappingGraph,
				subjectMap, R2RMLTerm.INVERSE_EXPRESSION);

		// MVS: Decide on ReferenceIdentifier
		ReferenceIdentifier referenceValue = extractReferenceIdentifier(
				r2rmlMappingGraph, subjectMap);
		// AD: The values of the rr:class property must be IRIs.
		// AD: Would that mean that it can not be a reference to an extract of
		// the input or a template?
		Set<URI> classIRIs = extractURIsFromTermMap(r2rmlMappingGraph,
				subjectMap, R2RMLTerm.CLASS);

		Set<GraphMap> graphMaps = new HashSet<GraphMap>();
		Set<Value> graphMapValues = extractValuesFromResource(
				r2rmlMappingGraph, subjectMap, R2RMLTerm.GRAPH_MAP);

		if (graphMapValues != null) {
			graphMaps = extractGraphMapValues(r2rmlMappingGraph,
					graphMapValues, savedGraphMaps);
			log.info("[RMLMappingFactory] graph Maps returned " + graphMaps);
		}
		/*
		 * Set<Value> graphMapValues = extractValuesFromResource(
		 * r2rmlMappingGraph, subjectMap, R2RMLTerm.GRAPH_MAP); Set<GraphMap>
		 * graphMaps = new HashSet<GraphMap>(); if (graphMapValues != null) {
		 * for (Value graphMap : graphMapValues) { // Create associated graphMap
		 * if it has not already created boolean found = false; GraphMap
		 * graphMapFound = null; /* for (GraphMap savedGraphMap :
		 * savedGraphMaps) if (savedGraphMap.getGraph().equals(graphMap)) {
		 * found = true; graphMapFound = savedGraphMap; }
		 */
		/*
		 * if (found) { graphMaps.add(graphMapFound); } else { GraphMap
		 * newGraphMap = extractGraphMap(r2rmlMappingGraph, (Resource)
		 * graphMap); savedGraphMaps.add(newGraphMap);
		 * graphMaps.add(newGraphMap); } } }
		 */
		SubjectMap result = new StdSubjectMap(ownTriplesMap, constantValue,
				stringTemplate, termType, inverseExpression, referenceValue,
				classIRIs, graphMaps);
		log.debug("[RMLMappingFactory:extractSubjectMap] Subject map extracted.");
		return result;
	}

	protected static Set<GraphMap> extractGraphMapValues(
			CustomSesameDataset r2rmlMappingGraph, Set<Value> graphMapValues,
			Set<GraphMap> savedGraphMaps) throws InvalidR2RMLStructureException {

		Set<GraphMap> graphMaps = new HashSet<GraphMap>();

		for (Value graphMap : graphMapValues) {
			// Create associated graphMap if it has not already created
			boolean found = false;
			GraphMap graphMapFound = null;
			/*
			 * for (GraphMap savedGraphMap : savedGraphMaps) if
			 * (savedGraphMap.getGraph().equals(graphMap)) { found = true;
			 * graphMapFound = savedGraphMap; }
			 */
			if (found) {
				graphMaps.add(graphMapFound);
			} else {
				GraphMap newGraphMap = null;
				try {
					newGraphMap = extractGraphMap(r2rmlMappingGraph,
							(Resource) graphMap);
				} catch (R2RMLDataError ex) {
					LoggerFactory.getLogger(RMLMappingFactory.class.getName()).error(ex.toString());
				} catch (InvalidR2RMLSyntaxException ex) {
					LoggerFactory.getLogger(RMLMappingFactory.class.getName()).error(ex.toString());
				}
				savedGraphMaps.add(newGraphMap);
				graphMaps.add(newGraphMap);
			}
		}

		return graphMaps;
	}

	/*
	 * Still needs to be modified!!
	 */
	protected static GraphMap extractGraphMap(CustomSesameDataset r2rmlMappingGraph,
			Resource graphMap) throws InvalidR2RMLStructureException,
			R2RMLDataError, InvalidR2RMLSyntaxException {
		log.debug("[RMLMappingFactory:extractPredicateObjectMaps] Extract graph map...");

		Value constantValue = extractValueFromTermMap(r2rmlMappingGraph,
				graphMap, R2RMLTerm.CONSTANT);
		String stringTemplate = extractLiteralFromTermMap(r2rmlMappingGraph,
				graphMap, R2RMLTerm.TEMPLATE);
		String inverseExpression = extractLiteralFromTermMap(r2rmlMappingGraph,
				graphMap, R2RMLTerm.INVERSE_EXPRESSION);

		// MVS: Decide on ReferenceIdentifier
		ReferenceIdentifier referenceValue = extractReferenceIdentifier(
				r2rmlMappingGraph, graphMap);

		URI termType = (URI) extractValueFromTermMap(r2rmlMappingGraph,
				graphMap, R2RMLTerm.TERM_TYPE);

		GraphMap result = new StdGraphMap(constantValue, stringTemplate,
				inverseExpression, referenceValue, termType);
		log.debug("[RMLMappingFactory:extractPredicateObjectMaps] Graph map extracted.");
		return result;
	}

	/**
	 * Extract content literal from a term type resource.
	 * 
	 * @param r2rmlMappingGraph
	 * @param termType
	 * @param term
	 * @return
	 * @throws InvalidR2RMLStructureException
	 */
	protected static String extractLiteralFromTermMap(
			CustomSesameDataset r2rmlMappingGraph, Resource termType, Enum term)
			throws InvalidR2RMLStructureException {

		URI p = getTermURI(r2rmlMappingGraph, term);

		List<Statement> statements = r2rmlMappingGraph.tuplePattern(termType,
				p, null);
		if (statements.isEmpty()) {
			return null;
		}
		if (statements.size() > 1) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractValueFromTermMap] " + termType
							+ " has too many " + term + " predicate defined.");
		}
		String result = statements.get(0).getObject().stringValue();
		if (log.isDebugEnabled()) {
			log.debug("[RMLMappingFactory:extractLiteralFromTermMap] Extracted "
					+ term + " : " + result);
		}
		return result;
	}

	/**
	 * Extract content value from a term type resource.
	 * 
	 * @return
	 * @throws InvalidR2RMLStructureException
	 */
	protected static Value extractValueFromTermMap(
			CustomSesameDataset r2rmlMappingGraph, Resource termType, Enum term)
			throws InvalidR2RMLStructureException {

		URI p = getTermURI(r2rmlMappingGraph, term);
		List<Statement> statements = r2rmlMappingGraph.tuplePattern(termType,
				p, null);
		if (statements.isEmpty()) {
			return null;
		}

		if (statements.size() > 1) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractValueFromTermMap] " + termType
							+ " has too many " + term + " predicate defined.");
		}
		Value result = statements.get(0).getObject();
		log.debug("[RMLMappingFactory:extractValueFromTermMap] Extracted "
				+ term + " : " + result.stringValue());
		return result;
	}

	/**
	 * Extract content values from a term type resource.
	 * 
	 * @return
	 * @throws InvalidR2RMLStructureException
	 */
	protected static Set<Value> extractValuesFromResource(
			CustomSesameDataset r2rmlMappingGraph, Resource termType, Enum term)
			throws InvalidR2RMLStructureException {
		URI p = getTermURI(r2rmlMappingGraph, term);

		List<Statement> statements = r2rmlMappingGraph.tuplePattern(termType,
				p, null);
		if (statements.isEmpty()) {
			return null;
		}
		Set<Value> values = new HashSet<Value>();
		for (Statement statement : statements) {
			Value value = statement.getObject();
			log.debug("[RMLMappingFactory:extractURIsFromTermMap] Extracted "
					+ term + " : " + value.stringValue());
			values.add(value);
		}
		return values;
	}

	protected static Set<Value> extractValuesFromResource(
			CustomSesameDataset r2rmlMappingGraph, Resource termType)
			throws InvalidR2RMLStructureException {

		List<Statement> statements = r2rmlMappingGraph.tuplePattern(termType,
				null, null);
		if (statements.isEmpty()) {
			return null;
		}
		Set<Value> values = new HashSet<Value>();
		for (Statement statement : statements) {
			Value value = statement.getObject();

			values.add(value);
		}
		return values;
	}

	/**
	 * Extract content URIs from a term type resource.
	 * 
	 * @return
	 * @throws InvalidR2RMLStructureException
	 */
	protected static Set<URI> extractURIsFromTermMap(
			CustomSesameDataset r2rmlMappingGraph, Resource termType, R2RMLTerm term)
			throws InvalidR2RMLStructureException {
		URI p = getTermURI(r2rmlMappingGraph, term);

		List<Statement> statements = r2rmlMappingGraph.tuplePattern(termType,
				p, null);
		if (statements.isEmpty()) {
			return null;
		}
		Set<URI> uris = new HashSet<URI>();
		for (Statement statement : statements) {
			URI uri = (URI) statement.getObject();
			log.debug("[RMLMappingFactory:extractURIsFromTermMap] Extracted "
					+ term + " : " + uri.stringValue());
			uris.add(uri);
		}
		return uris;
	}

	private static URI getTermURI(CustomSesameDataset r2rmlMappingGraph, Enum term)
			throws InvalidR2RMLStructureException {
		String namespace = Vocab.R2RML_NAMESPACE;
		//System.out.println(term.getClass());
		if (term instanceof Vocab.RMLTerm) {
			namespace = Vocab.RML_NAMESPACE;

		} else if (term instanceof VocabTrans.RRXTerm) {
			namespace = VocabTrans.RRX_NAMESPACE;
		} else if (!(term instanceof R2RMLTerm)) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractValueFromTermMap] " + term
							+ " is not valid.");
		}

		return r2rmlMappingGraph.URIref(namespace + term);
	}

	/**
	 * Extract logicalSource contents.
	 * 
	 * @param r2rmlMappingGraph
	 * @param triplesMapSubject
	 * @return
	 * @throws InvalidR2RMLStructureException
	 * @throws InvalidR2RMLSyntaxException
	 * @throws R2RMLDataError
	 */
	protected static LogicalSource extractLogicalSource(
			CustomSesameDataset rmlMappingGraph, Resource triplesMapSubject)
			throws InvalidR2RMLStructureException, InvalidR2RMLSyntaxException,
			R2RMLDataError {

		Vocab.QLTerm referenceFormulation = null;

		// Extract logical table blank node
		// favor logical table over source
		URI pTable = rmlMappingGraph.URIref(Vocab.R2RML_NAMESPACE
				+ Vocab.R2RMLTerm.LOGICAL_TABLE);

		URI pSource = rmlMappingGraph.URIref(Vocab.RML_NAMESPACE
				+ Vocab.RMLTerm.LOGICAL_SOURCE);

		List<Statement> sTable = rmlMappingGraph.tuplePattern(
				triplesMapSubject, pTable, null);

		List<Statement> sSource = rmlMappingGraph.tuplePattern(
				triplesMapSubject, pSource, null);

		if (!sTable.isEmpty() && !sSource.isEmpty()) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractLogicalSource] "
							+ triplesMapSubject
							+ " has both a source and table defined.");
		}

		if (!sTable.isEmpty()) {
			extractLogicalTable();
		}

		// TODO: decide between source and table
		List<Statement> statements = sSource;

		if (statements.isEmpty()) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractLogicalSource] "
							+ triplesMapSubject
							+ " has no logical source defined.");
		}
		if (statements.size() > 1) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractLogicalSource] "
							+ triplesMapSubject
							+ " has too many logical source defined.");
		}

		Resource blankLogicalSource = (Resource) statements.get(0).getObject();

		if (referenceFormulation == null)
			referenceFormulation = getReferenceFormulation(rmlMappingGraph,
					blankLogicalSource);

		if (referenceFormulation == null) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractLogicalSource] "
							+ triplesMapSubject
							+ " has an unknown query language.");
		}

		// Check SQL base table or view
		URI pName = rmlMappingGraph.URIref(Vocab.RML_NAMESPACE
				+ Vocab.RMLTerm.SOURCE);

		List<Statement> statementsName = rmlMappingGraph.tuplePattern(
				blankLogicalSource, pName, null);

		URI pView = rmlMappingGraph.URIref(Vocab.RML_NAMESPACE
				+ Vocab.RMLTerm.ITERATOR);
		List<Statement> statementsView = rmlMappingGraph.tuplePattern(
				blankLogicalSource, pView, null);

		LogicalSource logicalSource = null;

		if (!statementsName.isEmpty()) {
			if (statementsName.size() > 1) {
				throw new InvalidR2RMLStructureException(
						"[RMLMappingFactory:extractLogicalSource] "
								+ triplesMapSubject
								+ " has too many logical source name defined.");
			}
			/*
			 * MVS: This check is only valid in case of logicalTable/R2RMLView
			 */
			/*
			 * if (!statementsView.isEmpty()) { throw new
			 * InvalidR2RMLStructureException(
			 * "[RMLMappingFactory:extractLogicalTable] " + triplesMapSubject +
			 * " can't have a logical table and sql query defined" +
			 * " at the same time."); }
			 */
			// Table name defined

			// Extract the file identifier
			String file = statementsName.get(0).getObject().stringValue();

			// Extract the iterator to create the iterator. Some formats have
			// null, like CSV or SQL
			String iterator = null;
			if (!statementsView.isEmpty()) {
				iterator = statementsView.get(0).getObject().stringValue();
			}

			// MVS: find a good way to distinct SQL and others
			logicalSource = new StdLogicalSource(iterator, file,
					referenceFormulation);

		} else {
			// Logical table defined by R2RML View
			// TODO: adapt support for this
			/*
			 * if (statementsView.size() > 1) { throw new
			 * InvalidR2RMLStructureException(
			 * "[RMLMappingFactory:extractLogicalTable] " + triplesMapSubject +
			 * " has too many logical table defined."); } if
			 * (statementsView.isEmpty()) { throw new
			 * InvalidR2RMLStructureException(
			 * "[RMLMappingFactory:extractLogicalTable] " + triplesMapSubject +
			 * " has no logical table defined."); }
			 */
			// TODO: add support for referenceFormulation version
			/*
			 * URI pVersion = rmlMappingGraph .URIref(Vocab.RML_NAMESPACE +
			 * Vocab.RMLTerm.VERSION);
			 */
			// HOW DO R2RMLViews and their versions fit in to the more generic
			// logicalSource???
			// Check SQL versions
			/*
			 * URI pVersion = rmlMappingGraph .URIref(Vocab.R2RML_NAMESPACE +
			 * Vocab.R2RMLTerm.SQL_VERSION);
			 * 
			 * List<Statement> statementsVersion = rmlMappingGraph.tuplePattern(
			 * statementsView.get(0).getSubject(), pVersion, null); String
			 * sqlQuery = statementsView.get(0).getObject().stringValue(); if
			 * (statementsVersion.isEmpty()) {
			 * 
			 * //MVS: change this to more generic structure //logicalSource =
			 * new StdR2RMLView(sqlQuery); logicalSource = new
			 * StdLogicalSource(sqlQuery); }
			 */
			/*
			 * Set<R2RMLView.SQLVersion> versions = new
			 * HashSet<R2RMLView.SQLVersion>(); for (Statement statementVersion
			 * : statementsVersion) {
			 * 
			 * R2RMLView.SQLVersion sqlVersion = R2RMLView.SQLVersion
			 * .getSQLVersion(statementVersion.getObject() .stringValue());
			 * versions.add(sqlVersion); } if (versions.isEmpty()) { // SQL 2008
			 * by default if (log.isDebugEnabled()) {
			 * log.debug("[RMLMappingFactory:extractLogicalTable] " +
			 * triplesMapSubject +
			 * " has no SQL version defined : SQL 2008 by default"); } }
			 * logicalSource = new StdR2RMLView(sqlQuery, versions);
			 */
		}
		log.debug("[RMLMappingFactory:extractLogicalSource] Logical source extracted : "
				+ logicalSource);
		return logicalSource;
	}

	private static Vocab.QLTerm getReferenceFormulation(
			CustomSesameDataset rmlMappingGraph, Resource subject)
			throws InvalidR2RMLStructureException {
		URI pReferenceFormulation = rmlMappingGraph.URIref(Vocab.RML_NAMESPACE
				+ Vocab.RMLTerm.REFERENCE_FORMULATION);
		List<Statement> statements = rmlMappingGraph.tuplePattern(subject,
				pReferenceFormulation, null);
		if (statements.size() > 1) {
			throw new InvalidR2RMLStructureException(
					"[RMLMappingFactory:extractLogicalSource] " + subject
							+ " has too many query language defined.");
		}
		if (statements.isEmpty()) {
			return Vocab.QLTerm.SQL_CLASS;
		}
		Resource object = (Resource) statements.get(0).getObject();

		return Vocab.getQLTerms(object.stringValue());
	}

	protected static void extractLogicalTable() {
		// TODO: Original R2RML Logic move here
	}
}
