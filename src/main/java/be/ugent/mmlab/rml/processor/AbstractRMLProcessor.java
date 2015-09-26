package be.ugent.mmlab.rml.processor;

import static be.ugent.mmlab.rml.model.TermType.BLANK_NODE;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.core.R2RMLEngine;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLStructureException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLSyntaxException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.R2RMLDataError;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.tools.R2RMLToolkit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.GraphIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import be.ugent.mmlab.rml.core.ArgumentPosition;
import be.ugent.mmlab.rml.core.DependencyRMLPerformer;
import be.ugent.mmlab.rml.core.JoinRMLPerformer;
import be.ugent.mmlab.rml.core.JoinReferenceRMLPerformer;
import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.RMLPerformer;
import be.ugent.mmlab.rml.core.SimpleReferencePerformer;
import be.ugent.mmlab.rml.function.Function;
import be.ugent.mmlab.rml.function.FunctionFactory;
import be.ugent.mmlab.rml.function.FunctionNotDefined;
import be.ugent.mmlab.rml.model.GraphMap;
import be.ugent.mmlab.rml.model.JoinCondition;
import be.ugent.mmlab.rml.model.LogicalSource;
import be.ugent.mmlab.rml.model.ObjectMap;
import be.ugent.mmlab.rml.model.PredicateMap;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.ReferencingObjectMap;
import be.ugent.mmlab.rml.model.StdObjectMap;
import be.ugent.mmlab.rml.model.StdTriplesMap;
import be.ugent.mmlab.rml.model.SubjectMap;
import be.ugent.mmlab.rml.model.TermMap;
import be.ugent.mmlab.rml.model.TermMap.TermMapType;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.reference.ReferenceIdentifierImpl;
import be.ugent.mmlab.rml.processor.concrete.ConcreteRMLProcessorFactory;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;
import be.ugent.mmlab.rml.vocabulary.VocabTrans;

/**
 * This class contains all generic functionality for executing an iteration and
 * processing the mapping
 * 
 * @author mielvandersande, andimou, dimis
 */
public abstract class AbstractRMLProcessor implements RMLProcessor {
	protected class WrappedLong{
		Long value;
		public WrappedLong() {
			this.value=0l;
		}
		public void increase(){
			++this.value;
		}
		public Long getValue(){
			return this.value;
		}
	}
	/**
	 * Gets the globally defined identifier-to-path map
	 * 
	 * @param ls
	 *            the current LogicalSource
	 * @return the location of the file or table
	 */
	// Log
	private static Log log = LogFactory.getLog(R2RMLEngine.class);

	/*
	 * protected String getIdentifier(LogicalSource ls) { return
	 * RMLEngine.getFileMap().getProperty(ls.getIdentifier()); }
	 */
	protected static HashMap<TriplesMap, DependencyRMLPerformer> performersForFunctionInsideJoinCondition = new HashMap<>();

	protected TriplesMap dependencyTriplesMap = null;
	protected RMLProcessor dependencyProcessor = null;

	/**
	 * gets the expression specified in the logical source
	 * 
	 * @param ls
	 * @return
	 */
	protected String getReference(LogicalSource ls) {
		return ls.getReference();
	}

	@Override
	public void setDependencyTriplesMap(TriplesMap dependencyTriplesMap) {
		this.dependencyTriplesMap = dependencyTriplesMap;
	}

	@Override
	public void setDependencyProcessor(RMLProcessor dependencyProcessor) {
		this.dependencyProcessor = dependencyProcessor;
	}

	/**
	 * 
	 * Process the subject map
	 * 
	 * @param dataset
	 * @param subjectMap
	 * @param node
	 * @return the created subject
	 */
	@Override
	public Resource processSubjectMap(SesameDataSet dataset,
			SubjectMap subjectMap, Object node) {
		// Get the uri
		List<Object> values = processTermMap(subjectMap, node, null, null,
				null, null, false);
		// log.info("Abstract RML Processor Graph Map" +
		// subjectMap.getGraphMaps().toString());
		if (values.isEmpty())
			if (subjectMap.getTermType() != BLANK_NODE)
				return null;

		Object value = null;
		if (subjectMap.getTermType() != BLANK_NODE) {
			// Since it is the subject, more than one value is not allowed.
			// Only return the first one. Throw exception if not?
			value = values.get(0);

			if ((value == null) || (value.equals("")))
				return null;
		}

		Resource subject = null;

		// doublicate code from ObjectMap - they should be handled together
		switch (subjectMap.getTermType()) {
		case IRI:
			if (value != null && !value.equals("")) {
				if (value.toString().startsWith("www."))
					value = "http://" + value;
				subject = new URIImpl(value.toString());
			}
			break;
		case BLANK_NODE:
			subject = new BNodeImpl(
					org.apache.commons.lang.RandomStringUtils
							.randomAlphanumeric(10));
			break;
		default:
			subject = new URIImpl(value.toString());
		}
		// subject = new URIImpl(value);
		return subject;
	}

	@Override
	public void processSubjectTypeMap(SesameDataSet dataset, Resource subject,
			SubjectMap subjectMap, Object node) {

		// Add the type triples
		Set<org.openrdf.model.URI> classIRIs = subjectMap.getClassIRIs();
		if (subject != null)
			for (org.openrdf.model.URI classIRI : classIRIs) {
				dataset.add(subject, RDF.TYPE, classIRI);
			}
	}

	/**
	 * Process any Term Map
	 * 
	 * @param map
	 *            current term map
	 * @param node
	 *            current node in iteration
	 * @return the resulting value
	 */

	@Override
	public List<Object> processTermMap(TermMap map, Object node,
			TriplesMap triplesMap, Resource subject, URI predicate,
			SesameDataSet dataset,
			boolean ignoreOwnerBecauseWeAreInJoinInFirstLevel) {
		List<Object> value = new ArrayList<>();
		// extra addition
		TriplesMap tm = map.getTriplesMap();

		RMLProcessor processor = null;
		String fileName = null;
		if (tm != null && !ignoreOwnerBecauseWeAreInJoinInFirstLevel
				&& performersForFunctionInsideJoinCondition.size() == 0) {
			// Create the processor based on the owner triples map
			RMLProcessorFactory factory = new ConcreteRMLProcessorFactory();
			QLTerm queryLanguage = tm.getLogicalSource()
					.getReferenceFormulation();

			File file = new File(tm.getLogicalSource().getIdentifier());
			if (RMLEngine.getSourceProperties())
				fileName = RMLEngine.getFileMap().getProperty(file.toString());
			else if (!file.exists())
				fileName = getClass().getResource(
						tm.getLogicalSource().getIdentifier()).getFile();
			else
				fileName = tm.getLogicalSource().getIdentifier();

			processor = factory.create(queryLanguage);
		}
		switch (map.getTermMapType()) {
		case REFERENCE_VALUED:
			// Get the expression and extract the value
			ReferenceIdentifierImpl identifier = (ReferenceIdentifierImpl) map
					.getReferenceValue();
			if (tm == null || (ignoreOwnerBecauseWeAreInJoinInFirstLevel)) {
				return extractValueFromNode(node, identifier.toString().trim());
			} else {
				// we are in join, and processing a function argument!
				if (performersForFunctionInsideJoinCondition.size() > 0) {

					return performersForFunctionInsideJoinCondition.get(tm)
							.perform(identifier.toString().trim());
				} else {
					RMLPerformer performer = new JoinReferenceRMLPerformer(
							processor, subject, predicate, identifier
									.toString().trim());
					processor.execute(dataset, tm, performer, fileName);
					return new ArrayList<>();
				}
			}
		case CONSTANT_VALUED:
			// Extract the value directly from the mapping
			value.add(map.getConstantValue().stringValue().trim());
			return value;

		case TEMPLATE_VALUED:
			// Resolve the template
			String template = map.getStringTemplate();
			Set<String> tokens = R2RMLToolkit
					.extractColumnNamesFromStringTemplate(template);

			for (String expression : tokens) {
				List<Object> replacements = extractValueFromNode(node,
						expression);

				for (int i = 0; i < replacements.size(); i++) {
					if (value.size() < (i + 1)) {
						value.add(template);
					}
					String replacement = null;
					if (replacements.get(i) != null)
						replacement = replacements.get(i).toString().trim();

					// if (replacement == null || replacement.isEmpty()) {
					if (replacement == null || replacement.equals("")) {
						// if the replacement value is null or empty, the
						// reulting uri would be invalid, skip this.
						// The placeholders remain which removes them in the
						// end.
						continue;
					}

					String temp = value.get(i).toString().trim();

					if (expression.contains("[")) {
						expression = expression.replaceAll("\\[", "")
								.replaceAll("\\]", "");
						temp = temp.replaceAll("\\[", "").replaceAll("\\]", "");
					}
					// JSONPath expression cause problems when replacing, remove
					// the $ first
					if (expression.contains("$")) {
						expression = expression.replaceAll("\\$", "");
						temp = temp.replaceAll("\\$", "");
					}
					// try {
					// StringEscapeUtils.escapeJava(expression);
					String quote = Pattern.quote(expression);
					String rplcmnt = replacement;// URLEncoder.encode(replacement,"UTF-8");
					temp = temp.replaceAll("\\{" + quote + "\\}",
							Matcher.quoteReplacement(rplcmnt));
					// } catch (UnsupportedEncodingException ex) {
					// Logger.getLogger(AbstractRMLProcessor.class.getName()).log(Level.SEVERE,
					// null, ex);
					// }
					// temp = temp.replaceAll("\\{" + expression + "\\}",
					// replacement);
					value.set(i, temp.toString());

				}
			}

			// Check if there are any placeholders left in the templates and
			// remove uris that are not
			List<Object> validValues = new ArrayList<>();
			for (Object uri : value) {
				if (R2RMLToolkit.extractColumnNamesFromStringTemplate(
						uri.toString()).isEmpty()) {
					validValues.add(uri);
				}
			}

			return validValues;
		case TRANSFORMATION_VALUED:
			// Extract the value directly from the mapping
			List<Object> argumentsString = new ArrayList<Object>();
			List<QLTerm> argumentsQLTerms = new ArrayList<QLTerm>();
			for (TermMap argument : map.getArgumentMap()) {
				List<Object> temp = processTermMap(argument, node, triplesMap,
						subject, predicate, dataset, false);
				argumentsString.addAll(temp);
				for (int i = 0; i < temp.size(); ++i) {
					argumentsQLTerms
							.add((argument.getTriplesMap() == null) ? getFormulation()
									: argument.getTriplesMap()
											.getLogicalSource()
											.getReferenceFormulation());
				}

			}
			//System.out.println(map.getFunction().toString());
			Function function = null;
			try {
				function = FunctionFactory.get(map.getFunction());
			} catch (FunctionNotDefined e1) {
				e1.printStackTrace();
				System.exit(13);
			}
			try {
				value.addAll(function
						.execute(argumentsString, argumentsQLTerms));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// return value;
				 e.printStackTrace(); //uncomment for debug
			}
			return value;

		default:
			return value;
		}

		// return value;
	}

	/**
	 * Process a predicate object map
	 * 
	 * @param dataset
	 * @param subject
	 *            the subject from the triple
	 * @param pom
	 *            the predicate object map
	 * @param node
	 *            the current node
	 */
	@Override
	public void processPredicateObjectMap(SesameDataSet dataset,
			Resource subject, PredicateObjectMap pom, Object node,
			TriplesMap map) {

		Set<PredicateMap> predicateMaps = pom.getPredicateMaps();
		// Go over each predicate map
		for (PredicateMap predicateMap : predicateMaps) {
			// Get the predicate
			List<URI> predicates = processPredicateMap(predicateMap, node);

			for (URI predicate : predicates) {
				// Process the joins first
				Set<ReferencingObjectMap> referencingObjectMaps = pom
						.getReferencingObjectMaps();
				for (ReferencingObjectMap referencingObjectMap : referencingObjectMaps) {
					Set<JoinCondition> joinConditions = referencingObjectMap
							.getJoinConditions();

					TriplesMap parentTriplesMap = referencingObjectMap
							.getParentTriplesMap();

					// Create the processor based on the parent triples map to
					// perform the join
					RMLProcessorFactory factory = new ConcreteRMLProcessorFactory();
					QLTerm queryLanguage = parentTriplesMap.getLogicalSource()
							.getReferenceFormulation();

					String fileName;
					File file = new File(parentTriplesMap.getLogicalSource()
							.getIdentifier());
					if (RMLEngine.getSourceProperties())
						fileName = RMLEngine.getFileMap().getProperty(
								file.toString());
					else if (!file.exists())
						fileName = getClass().getResource(
								parentTriplesMap.getLogicalSource()
										.getIdentifier()).getFile();
					else
						fileName = parentTriplesMap.getLogicalSource()
								.getIdentifier();

					RMLProcessor processor = factory.create(queryLanguage);

					RMLPerformer performer;
					// different Logical Source and no Conditions
					if (joinConditions.isEmpty()
							& !parentTriplesMap
									.getLogicalSource()
									.getIdentifier()
									.equals(map.getLogicalSource()
											.getIdentifier())) {
						performer = new JoinRMLPerformer(processor, subject,
								predicate);
						processor.execute(dataset, parentTriplesMap, performer,
								fileName);
					}
					// same Logical Source and no Conditions
					else if (joinConditions.isEmpty()
							& parentTriplesMap
									.getLogicalSource()
									.getIdentifier()
									.equals(map.getLogicalSource()
											.getIdentifier())) {
						performer = new SimpleReferencePerformer(processor,
								subject, predicate);
						if ((parentTriplesMap.getLogicalSource()
								.getReferenceFormulation().toString()
								.equals("CSV"))
								|| (parentTriplesMap.getLogicalSource()
										.getReference().equals(map
										.getLogicalSource().getReference()))) {
							performer.perform(node, dataset, parentTriplesMap);
						} else {
							int end = map.getLogicalSource().getReference()
									.length();
							/*log.info("RML:AbstractRMLProcessor "
									+ parentTriplesMap.getLogicalSource()
											.getReference().toString());*/
							String expression = "";
							switch (parentTriplesMap.getLogicalSource()
									.getReferenceFormulation().toString()) {
							case "XPath":
								expression = parentTriplesMap
										.getLogicalSource().getReference()
										.toString().substring(end);
								break;
							case "JSONPath":
								expression = parentTriplesMap
										.getLogicalSource().getReference()
										.toString().substring(end + 1);
								break;
							}
							processor.execute_node(dataset, expression,
									parentTriplesMap, performer, node, null);
						}
					}
					// Conditions
					else {
						// Build a join map where
						// key: the parent expression
						// value: the value extracted from the child
						// HashMap<String, String> joinMap = new HashMap<>();
						//
						// for (JoinCondition joinCondition : joinConditions) {
						// List<String> childValues = extractValueFromNode(node,
						// joinCondition.getChild());
						//
						// //Allow multiple values as child - fits with RML's
						// definition of multiple Object Maps
						// for(String childValue : childValues){
						// joinMap.put(joinCondition.getParent(), childValue);
						// if(joinMap.size() == joinConditions.size()){
						// performer = new
						// ConditionalJoinRMLPerformer(processor, joinMap,
						// subject, predicate);
						// processor.execute(dataset, parentTriplesMap,
						// performer, fileName);
						// }
						// }
						// }
						List<Integer> maxargs = new ArrayList<>();
						List<URI> functions = new ArrayList<>();
						List<List<Object>> arguments = new ArrayList<>();
						List<List<QLTerm>> argumentsQLTerms = new ArrayList<>();

						Map<TriplesMap, List<TermMap>> clusters = new HashMap<TriplesMap, List<TermMap>>();
						Map<TriplesMap, List<ArgumentPosition>> clustersPositions = new HashMap<TriplesMap, List<ArgumentPosition>>();
						int iterator = -1;

						DefaultDirectedGraph<TriplesMap, DefaultEdge> condgraph = new DefaultDirectedGraph<>(
								DefaultEdge.class);
						for (JoinCondition joinCondition : joinConditions) { // find
																				// all
																				// structural
																				// relations
							if (!joinCondition.isStructural()) {
								continue;
							}

							addCondition(
									(joinCondition.getParentTriplesMap() != null) ? joinCondition
											.getParentTriplesMap()
											: StdTriplesMap
													.getCurrentTriplesMap(),
									joinCondition.getChildTriplesMap(),
									condgraph);
						}
						List<TermMap> postponed = new ArrayList<>();
						List<ArgumentPosition> postponedArgumentPositions = new ArrayList<>();
						for (JoinCondition joinCondition : joinConditions) {
							if (joinCondition.isStructural()) {
								continue;
							}
							++iterator;
							int position = 0;

							if (joinCondition.getFunction() == null) {
								// System.out.println(joinCondition.getFunction());
								List<TermMap> args = new ArrayList<>();
								try {
									args.add(new StdObjectMap(
											null,
											null,
											null,
											null,
											null,
											null,
											null,
											ReferenceIdentifierImpl
													.buildFromR2RMLConfigFile(joinCondition
															.getChild()), null,
											null, null));
									args.add(new StdObjectMap(
											null,
											null,
											null,
											null,
											null,
											null,
											null,
											ReferenceIdentifierImpl
													.buildFromR2RMLConfigFile(joinCondition
															.getParent()),
											null, null, parentTriplesMap));
								} catch (R2RMLDataError
										| InvalidR2RMLStructureException
										| InvalidR2RMLSyntaxException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									System.exit(0);
								}
								joinCondition.setArgumentMap(args);
								joinCondition.setFunction(new URIImpl(
										VocabTrans.RRXF_NAMESPACE + "equi"));
							}

							for (TermMap tm : joinCondition.getArgumentMap()) {
								if (tm.getTermMapType().equals(
										TermMapType.TRANSFORMATION_VALUED)) {

									tm.setTriplesMap(null);

								}
								TriplesMap owner = tm.getTriplesMap();

								if (clusters.get(owner) == null) {
									clusters.put(owner,
											new ArrayList<TermMap>());
									clustersPositions.put(owner,
											new ArrayList<ArgumentPosition>());
								}
								clusters.get(owner).add(tm);
								clustersPositions.get(owner)
										.add(new ArgumentPosition(iterator,
												position));
								++position;
							}
							maxargs.add(joinCondition.getArgumentMap().size());
							functions.add(joinCondition.getFunction());
							arguments.add(new ArrayList<Object>());
							argumentsQLTerms.add(new ArrayList<QLTerm>());

							for (int i = 0; i < maxargs.get(iterator); ++i) {
								arguments.get(iterator).add(null);
								argumentsQLTerms.get(iterator).add(null);
							}
						}

						HashSet<TriplesMap> seen = new HashSet<>();
						ArrayList<TriplesMap> clusters_sorted = new ArrayList<>();
						EdgeReversedGraph xx = new EdgeReversedGraph(condgraph);
						/*
						 * ConnectivityInspector<String, DefaultEdge> inspector2
						 * = new ConnectivityInspector<String, DefaultEdge>(xx);
						 * List<Set<String>> connectedSets2 =
						 * inspector2.connectedSets(); for(Set<String>
						 * c:connectedSets2){ System.out.println(c);
						 */
						GraphIterator<TriplesMap, DefaultEdge> graphiter = new TopologicalOrderIterator<TriplesMap, DefaultEdge>(
								xx);
						while (graphiter.hasNext()) {
							TriplesMap next = graphiter.next();
							// if(c.contains(next))
							// {
							seen.add(next);
							if (StdTriplesMap.getCurrentTriplesMap() == next) {
								continue;
							}
							//System.out.println(next.getName());
							clusters_sorted.add(next);
							// } //
						}

						// }

						for (TriplesMap trm : clusters.keySet()) {
							if (seen.contains(trm)) {
								continue;
							}
							seen.add(trm);
							clusters_sorted.add(trm); // put the last
														// independent
														// processors
						}

						RMLProcessor oldprocessor1 = null;
						RMLPerformer oldperformer1 = null;
						TriplesMap trmlast = null;
						String fileName1 = null;
						DependencyRMLPerformer performer1 = null;
						RMLProcessor processor1 = null;
						boolean foundparent = false;
						for (TriplesMap t : clusters_sorted) {
							if (parentTriplesMap.equals(t)) {
								foundparent = true;
							}
						}
						if (!foundparent) {

							processor1 = processor;
							performer1 = new DependencyRMLPerformer(
									processor1,
									subject,
									predicate,
									parentTriplesMap,
									(clusters.containsKey(parentTriplesMap)) ? clusters
											.get(parentTriplesMap)
											: new ArrayList<TermMap>(),
									clustersPositions
											.containsKey(parentTriplesMap) ? clustersPositions
											.get(parentTriplesMap)
											: new ArrayList<ArgumentPosition>(),
									(DependencyRMLPerformer) oldperformer1,
									oldprocessor1, processor, this, arguments,
									argumentsQLTerms, functions,
									parentTriplesMap, fileName,
									referencingObjectMap.isReversedParent());
							performersForFunctionInsideJoinCondition.put(
									parentTriplesMap, performer1);

							trmlast = parentTriplesMap;

							postponed = clusters.get(null);
							postponedArgumentPositions = clustersPositions
									.get(null);
							TriplesMap owner = parentTriplesMap;
							if (postponed != null) {
								for (int i = 0; i < postponed.size(); ++i) {
									TermMap tm = postponed.get(i);
									if (!tm.getTermMapType().equals(
											TermMapType.TRANSFORMATION_VALUED)) {
										continue;
									}
									if (clusters.get(owner) == null) {
										clusters.put(owner,
												new ArrayList<TermMap>());
										clustersPositions
												.put(owner,
														new ArrayList<ArgumentPosition>());
									}
									clusters.get(owner).add(tm);
									clustersPositions.get(owner).add(
											postponedArgumentPositions.get(i));
									postponed.remove(i);
									postponedArgumentPositions.remove(i);
									tm.setTriplesMap(parentTriplesMap); // to be
									// processed
									// on
									// the
									// last
									// iteration,
									// where
									// is
									// the
									// parent
									// triples
									// map
									addRecursivelyTheNewClusters(tm, clusters,
											clustersPositions, parentTriplesMap);
								}
							}
							clusters_sorted.add(0, parentTriplesMap);
							seen.add(parentTriplesMap);
						} else {
							postponed = clusters.get(null);
							postponedArgumentPositions = clustersPositions
									.get(null);
							if (postponed != null) {
								TriplesMap owner = clusters_sorted.get(0);
								for (int i = 0; i < postponed.size(); ++i) {
									TermMap tm = postponed.get(i);
									if (!tm.getTermMapType().equals(
											TermMapType.TRANSFORMATION_VALUED)) {
										continue;
									}
									if (clusters.get(owner) == null) {
										clusters.put(owner,
												new ArrayList<TermMap>());
										clustersPositions
												.put(owner,
														new ArrayList<ArgumentPosition>());
									}
									clusters.get(owner).add(tm);
									clustersPositions.get(owner).add(
											postponedArgumentPositions.get(i));
									postponed.remove(i);
									postponedArgumentPositions.remove(i);
									--i;
									tm.setTriplesMap(clusters_sorted.get(0)); // to
																				// be
									// processed
									// on
									// the
									// last
									// iteration,
									// where
									// is
									// the
									// parent
									// triples
									// map
									addRecursivelyTheNewClusters(tm, clusters,
											clustersPositions,
											clusters_sorted.get(0));
								}
							}
						}
						for (TriplesMap trm : clusters.keySet()) {
							if (seen.contains(trm)) {
								continue;
							}
							seen.add(trm);
							if (StdTriplesMap.getCurrentTriplesMap() == trm) { // should
																				// not
																				// happen
								continue;
							}
							clusters_sorted.add(trm); // put the last
														// independent
														// processors
						}

						fileName1 = fileName;

						for (TriplesMap trm : clusters_sorted) { // clusters.keySet())
																	// {
							if (trm == null) { // || trm==parentTriplesMap){
								continue;
							}
							Set<DefaultEdge> dependencies = null;
							if (condgraph.containsVertex(trm)) {
								dependencies = condgraph.incomingEdgesOf(trm);
							}

							trmlast = trm;
							List<TermMap> termmaps = clusters.get(trm);

							QLTerm queryLanguage1 = trm.getLogicalSource()
									.getReferenceFormulation();

							File file1 = new File(trm.getLogicalSource()
									.getIdentifier());
							if (RMLEngine.getSourceProperties())
								fileName1 = RMLEngine.getFileMap().getProperty(
										file1.toString());
							else if (!file1.exists())
								fileName1 = getClass().getResource(
										trm.getLogicalSource().getIdentifier())
										.getFile();
							else
								fileName1 = trm.getLogicalSource()
										.getIdentifier();
							if (trm == parentTriplesMap) {
								processor1 = processor;
							} else {
								processor1 = factory.create(queryLanguage1);
							}
							if (dependencies != null) {
								if (!dependencies.isEmpty()) {
									TriplesMap source = null;
									for (DefaultEdge e : dependencies) {
										source = condgraph.getEdgeSource(e);
										break; // only one outgoing edge!!
									}
									if (source == StdTriplesMap
											.getCurrentTriplesMap()) {
										processor1.setDependencyProcessor(this);
									} else
										processor1
												.setDependencyTriplesMap(source);
								}
							}
							performer1 = new DependencyRMLPerformer(processor1,
									subject, predicate, parentTriplesMap,
									termmaps, clustersPositions.get(trm),
									(DependencyRMLPerformer) oldperformer1,
									oldprocessor1, processor, this, arguments,
									argumentsQLTerms, functions, trm,
									fileName1,
									referencingObjectMap.isReversedParent());
							performersForFunctionInsideJoinCondition.put(trm,
									performer1);
							oldperformer1 = performer1;
							oldprocessor1 = processor1;
						}
						if (clusters.containsKey(null)) {
							List<TermMap> nullcluster = clusters.get(null);
							List<ArgumentPosition> nullpositions = clustersPositions
									.get(null);
							for (int i = 0; i < nullpositions.size(); ++i) {
								ArgumentPosition pos = nullpositions.get(i);
								arguments.get(pos.getArgumentList()).remove(
										pos.getActualPosition());
								arguments.get(pos.getArgumentList()).add(
										pos.getActualPosition(),
										this.processTermMap(nullcluster.get(i),
												node, null, null, null, null,
												false | true).get(0));
								argumentsQLTerms.get(pos.getArgumentList())
										.remove(pos.getActualPosition());
								argumentsQLTerms.get(pos.getArgumentList())
										.add(pos.getActualPosition(),
												getFormulation());
							}

						}

						oldprocessor1.execute(dataset, trmlast, oldperformer1,
								fileName1);
						performersForFunctionInsideJoinCondition.clear(); // for
																			// next
																			// use
					}

				}

				// process the objectmaps
				Set<ObjectMap> objectMaps = pom.getObjectMaps();
				for (ObjectMap objectMap : objectMaps) {
					// Get the one or more objects returned by the object map
					List<Value> objects = processObjectMap(objectMap, node,
							map, subject, predicate, dataset);
					for (Value object : objects) {
						if (object.stringValue() != null) {
							Set<GraphMap> graphs = pom.getGraphMaps();
							if (graphs.isEmpty())
								dataset.add(subject, predicate, object);
							else
								for (GraphMap graph : graphs) {
									Resource graphResource = new URIImpl(graph
											.getConstantValue().toString());
									dataset.add(subject, predicate, object,
											graphResource);
								}

						}
					}
				}
			}

		}
	}

	private void addRecursivelyTheNewClusters(TermMap tm,
			Map<TriplesMap, List<TermMap>> clusters,
			Map<TriplesMap, List<ArgumentPosition>> clustersPositions,
			TriplesMap parentTriplesMap) {
		for (TermMap insidetm : tm.getArgumentMap()) {
			if (insidetm.getTermMapType().equals(
					TermMapType.TRANSFORMATION_VALUED)) {
				addRecursivelyTheNewClusters(insidetm, clusters,
						clustersPositions, parentTriplesMap);
				tm.setTriplesMap(parentTriplesMap);
			} else {
				TriplesMap owner = insidetm.getTriplesMap();

				if (clusters.get(owner) == null) {
					clusters.put(owner, new ArrayList<TermMap>());
					clustersPositions.put(owner,
							new ArrayList<ArgumentPosition>());
				}
			}
		}

	}

	/**
	 * process a predicate map
	 * 
	 * @param predicateMap
	 * @param node
	 * @return the uri of the extracted predicate
	 */
	protected List<URI> processPredicateMap(PredicateMap predicateMap,
			Object node) {
		// Get the value
		List<Object> values = processTermMap(predicateMap, node, null, null,
				null, null, false);

		List<URI> uris = new ArrayList<>();
		for (Object value : values) {
			if (value.toString().startsWith("www."))
				value = "http://" + value;
			uris.add(new URIImpl(value.toString()));
		}
		// return the uri
		return uris;
	}

	/**
	 * process an object map
	 * 
	 * @param objectMap
	 * @param node
	 * @return
	 */
	public List<Value> processObjectMap(ObjectMap objectMap, Object node,
			TriplesMap triplesMap, Resource subject, URI predicate,
			SesameDataSet dataset) {
		// A Term map returns one or more values (in case expression matches
		// more)

		List<Object> values = processTermMap(objectMap, node, triplesMap,
				subject, predicate, dataset, false);

		List<Value> valueList = new ArrayList<>();
		for (Object value : values) {
			switch (objectMap.getTermType()) {
			case IRI:
				if (value != null && !value.equals("")) {
					if (value.toString().startsWith("www."))
						value = "http://" + value;
					valueList.add(new URIImpl(value.toString()));
				}
				break;
			case BLANK_NODE:
				valueList.add(new BNodeImpl(value.toString()));
				break;
			case LITERAL:
				if (objectMap.getLanguageTag() != null && !value.equals("")) {
					valueList.add(new LiteralImpl(value.toString(), objectMap
							.getLanguageTag()));
				} else if (value != null && !value.equals("")
						&& objectMap.getDataType() != null) {
					valueList.add(new LiteralImpl(value.toString(), objectMap
							.getDataType()));
				} else if (value != null && !value.equals("")) {
					valueList.add(new LiteralImpl(value.toString().trim()));
				}
			}

		}
		return valueList;
	}

	@Override
	public QLTerm getFormulation() {
		return null;
	}

	private static void manage(DefaultEdge incomingedge, TriplesMap from,
			DefaultDirectedGraph<TriplesMap, DefaultEdge> xx) {
		TriplesMap incomingFromVertex = xx.getEdgeSource(incomingedge);
		TriplesMap incomingTargetVertex = xx.getEdgeTarget(incomingedge);
		if (sorted(incomingFromVertex, from)) {
			// put it here
			xx.removeEdge(incomingedge);
			xx.addEdge(incomingFromVertex, from);
			xx.addEdge(from, incomingTargetVertex);
		} else {
			Set<DefaultEdge> incoming = xx.incomingEdgesOf(incomingFromVertex);
			DefaultEdge incedge = null;
			for (DefaultEdge e : incoming) {
				incedge = e;
				break;
			}
			if (incedge == null) {
				xx.addEdge(from, incomingFromVertex);
			} else {
				manage(incedge, from, xx);
			}
		}
		// Set<DefaultEdge> incoming = xx.incomingEdgesOf(xx
		// .getEdgeSource(incomingedge));
		// it should be only one incoming
	}

	private static boolean addCondition(TriplesMap from, TriplesMap target,
			DefaultDirectedGraph<TriplesMap, DefaultEdge> xx) {

		if (xx.containsEdge(from, target)) {
			return false;
		}
		xx.addVertex(from);
		xx.addVertex(target);

		Set<DefaultEdge> incoming = xx.incomingEdgesOf(target);
		DefaultEdge incedge = null;
		for (DefaultEdge e : incoming) {
			incedge = e;
			break;
		}
		if (incedge == null) {
			xx.addEdge(from, target);
		} else {
			manage(incedge, from, xx);
		}
		return true;
	}

	private static boolean sorted(TriplesMap incomingFromVertex, TriplesMap from) {
		if (from.getLogicalSource()
				.getReference()
				.startsWith(
						incomingFromVertex.getLogicalSource().getReference())) {
			return true;
		}
		return false;
	}
	
	
}
