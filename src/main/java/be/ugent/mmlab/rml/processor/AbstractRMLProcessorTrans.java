package be.ugent.mmlab.rml.processor;

/***************************************************************************
 *
 * @author: dimis (dimis@di.uoa.gr)
 * 
 ****************************************************************************/
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.tools.R2RMLToolkit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.xml.sax.SAXException;

import be.ugent.mmlab.rml.core.ConditionalJoinRMLPerformer;
import be.ugent.mmlab.rml.core.JoinRMLPerformer;
import be.ugent.mmlab.rml.core.MalformedGeometryException;
import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.RMLPerformer;
import be.ugent.mmlab.rml.core.SimpleReferencePerformer;
import be.ugent.mmlab.rml.model.ArgumentMap;
import be.ugent.mmlab.rml.model.GraphMap;
import be.ugent.mmlab.rml.model.JoinCondition;
import be.ugent.mmlab.rml.model.ObjectMap;
import be.ugent.mmlab.rml.model.PredicateMap;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.PredicateObjectMapTrans;
import be.ugent.mmlab.rml.model.ReferencingObjectMap;
import be.ugent.mmlab.rml.model.TermMap;
import be.ugent.mmlab.rml.model.Transformation;
import be.ugent.mmlab.rml.model.TransformationObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.reference.ReferenceIdentifierImpl;
import be.ugent.mmlab.rml.model.transformation.Config;
import be.ugent.mmlab.rml.model.transformation.GTransormationFunctions;
import be.ugent.mmlab.rml.processor.concrete.ConcreteRMLProcessorFactoryTrans;
import be.ugent.mmlab.rml.vocabulary.GEOMETRY_FUNCTIONS;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.gml2.GMLReader;

/**
 * This class contains all generic functionality for executing an iteration and
 * processing the mapping
 * 
 * @author mielvandersande, andimou
 */
public abstract class AbstractRMLProcessorTrans extends AbstractRMLProcessor {

	/**
	 * Gets the globally defined identifier-to-path map
	 * 
	 * @param ls
	 *            the current LogicalSource
	 * @return the location of the file or table
	 */
	// Log
	private static Log log = LogFactory.getLog(AbstractRMLProcessorTrans.class);

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
			Resource subject, PredicateObjectMap pomOrig, Object node,
			TriplesMap map) {
		PredicateObjectMapTrans pom = (PredicateObjectMapTrans) pomOrig;
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
					RMLProcessorFactory factory = new ConcreteRMLProcessorFactoryTrans();
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
							log.info("RML:AbstractRMLProcessor "
									+ parentTriplesMap.getLogicalSource()
											.getReference().toString());
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
						HashMap<String, String> joinMap = new HashMap<>();

						for (JoinCondition joinCondition : joinConditions) {
							List<String> childValues = extractValueFromNode(
									node, joinCondition.getChild());

							// Allow multiple values as child - fits with RML's
							// definition of multiple Object Maps
							for (String childValue : childValues) {
								joinMap.put(joinCondition.getParent(),
										childValue);
								if (joinMap.size() == joinConditions.size()) {
									performer = new ConditionalJoinRMLPerformer(
											processor, joinMap, subject,
											predicate);
									processor.execute(dataset,
											parentTriplesMap, performer,
											fileName);
								}
							}
						}
					}

				}

				// process the transformations
				Set<TransformationObjectMap> transformationObjectMaps = pom
						.getTransformationObjectMaps();
				for (TransformationObjectMap transformationObjectMap : transformationObjectMaps) {
					Set<Transformation> transformations = transformationObjectMap
							.getTransformationFunction();

					// Create the processor based on the parent triples map to
					// perform the join
					RMLProcessorFactory factory = new ConcreteRMLProcessorFactoryTrans();
					QLTerm queryLanguage = map.getLogicalSource()
							.getReferenceFormulation();

					RMLProcessor processor = factory.create(queryLanguage);

					RMLPerformer performer;
					// different Logical Source and no Conditions
					if (!transformationObjectMaps.isEmpty()) {
						// Build a join map where
						// key: the parent expression
						// value: the value extracted from the child
						HashMap<String, String> joinMap = new HashMap<>();

						for (Transformation transformation : transformations) {
							List<Value> objects = null;
							try {
								objects = processArgumentMap(transformation,
										node, map.getLogicalSource()
												.getReferenceFormulation());
							} catch (NoSuchAuthorityCodeException e) {
								log.error("Bad CRS given");
								e.printStackTrace();
								System.exit(-1);
							} catch (FactoryException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								System.exit(-1);
							} catch (SAXException e) {
								log.error("Maybe: GML geometry not recognized");
								e.printStackTrace();
								System.exit(-1);
							} catch (IOException e) {
								log.error("Maybe: GeoJSON geometry not recognized");
								e.printStackTrace();
								System.exit(-1);
							} catch (ParserConfigurationException e) {
								log.error("Maybe: GML geometry not recognized");
								e.printStackTrace();
								System.exit(-1);
							} catch (MalformedGeometryException e) {
								log.error("Geometry not recognized");
								e.printStackTrace();
								System.exit(-1);
							}
							for (Value object : objects) {
								if (object.stringValue() != null) {
									Set<GraphMap> graphs = pom.getGraphMaps();
									if (graphs.isEmpty())
										dataset.add(subject, predicate, object);
									else
										for (GraphMap graph : graphs) {
											Resource graphResource = new URIImpl(
													graph.getConstantValue()
															.toString());
											dataset.add(subject, predicate,
													object, graphResource);
										}

								}
							}
							// Allow multiple values as child - fits with RML's
							// definition of multiple Object Maps
							/*
							 * for(String argument : arguments){
							 * joinMap.put(joinCondition.getParent(), argument);
							 * if(joinMap.size() == joinConditions.size()){
							 * performer = new
							 * ConditionalJoinRMLPerformer(processor, joinMap,
							 * subject, predicate); processor.execute(dataset,
							 * parentTriplesMap, performer, fileName); } }
							 */
							// processor.
						}
					}

				}
				if (transformationObjectMaps.size() > 0) {
					continue;
				}
				// process the objectmaps
				Set<ObjectMap> objectMaps = pom.getObjectMaps();

				for (ObjectMap objectMap : objectMaps) {
					// Get the one or more objects returned by the object map
					List<Value> objects = processObjectMap(objectMap, node);
					// System.out.println(objects);
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

	/**
	 * process an object map
	 * 
	 * @param argumentMap
	 * @param node
	 * @return
	 * @throws FactoryException
	 * @throws NoSuchAuthorityCodeException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws MalformedGeometryException 
	 */
	public List<Value> processArgumentMap(Transformation transformation,
			Object node, QLTerm termkind) throws NoSuchAuthorityCodeException,
			FactoryException, SAXException, IOException,
			ParserConfigurationException, MalformedGeometryException {
		ArgumentMap argumentMap = transformation.getArgumentMap();
		String transformationFunction = transformation.getFunction();
		// A Term map returns one or more values (in case expression matches
		// more)
		List<String> values = processTermMap(argumentMap, node);

		List<Value> valueList = new ArrayList<>();
		URI datatype = argumentMap.getTransformationObjectMap().getDataType();
		if (transformationFunction.equals(GEOMETRY_FUNCTIONS.asWKT.toString())) {
			Geometry geometry = computeGeometry(values.get(0), termkind);
			valueList.add(new LiteralImpl(GTransormationFunctions.asWKT(
					(Geometry) geometry, CRS.decode("EPSG:4326")), datatype));
		} else if (transformationFunction.equals(GEOMETRY_FUNCTIONS.isSimple
				.toString())) {
			Geometry geometry = computeGeometry(values.get(0), termkind);
			valueList.add(new LiteralImpl(GTransormationFunctions
					.isSimple((Geometry) geometry), datatype));
		} else if (transformationFunction
				.equals(GEOMETRY_FUNCTIONS.hasSerialization.toString())) {
			Geometry geometry = computeGeometry(values.get(0), termkind);
			valueList.add(new LiteralImpl(GTransormationFunctions
					.hasSerialization((Geometry) geometry,
							CRS.decode("EPSG:4326")), datatype));
		} else if (transformationFunction.equals(GEOMETRY_FUNCTIONS.asGML
				.toString())) {
			Geometry geometry = computeGeometry(values.get(0), termkind);
			valueList.add(new LiteralImpl(GTransormationFunctions.asGML(
					(Geometry) geometry, CRS.decode("EPSG:4326")), datatype));
		} else if (transformationFunction.equals(GEOMETRY_FUNCTIONS.isEmpty
				.toString())) {
			Geometry geometry = computeGeometry(values.get(0), termkind);
			valueList.add(new LiteralImpl(GTransormationFunctions
					.isEmpty((Geometry) geometry), datatype));
		} else if (transformationFunction.equals(GEOMETRY_FUNCTIONS.is3D
				.toString())) {
			Geometry geometry = computeGeometry(values.get(0), termkind);
			valueList.add(new LiteralImpl(GTransormationFunctions
					.is3D((Geometry) geometry), datatype));
		} else if (transformationFunction
				.equals(GEOMETRY_FUNCTIONS.spatialDimension.toString())) {
			Geometry geometry = computeGeometry(values.get(0), termkind);
			valueList.add(new LiteralImpl(GTransormationFunctions
					.spatialDimension((Geometry) geometry), datatype));
		} else if (transformationFunction.equals(GEOMETRY_FUNCTIONS.dimension
				.toString())) {
			Geometry geometry = computeGeometry(values.get(0), termkind);
			valueList.add(new LiteralImpl(GTransormationFunctions
					.dimension((Geometry) geometry), datatype));
		} else if (transformationFunction
				.equals(GEOMETRY_FUNCTIONS.coordinateDimension.toString())) {
			Geometry geometry = computeGeometry(values.get(0), termkind);
			valueList.add(new LiteralImpl(GTransormationFunctions
					.coordinateDimension((Geometry) geometry), datatype));
		} else if (transformationFunction.equals(GEOMETRY_FUNCTIONS.area
				.toString())) {
			Geometry geometry = computeGeometry(values.get(0), termkind);
			valueList.add(new LiteralImpl(GTransormationFunctions
					.area((Geometry) geometry), datatype));
		} else if (transformationFunction.equals(GEOMETRY_FUNCTIONS.length
				.toString())) {
			Geometry geometry = computeGeometry(values.get(0), termkind);
			valueList.add(new LiteralImpl(GTransormationFunctions
					.length((Geometry) geometry), datatype));
		} else if (transformationFunction.equals(GEOMETRY_FUNCTIONS.centroidx
				.toString())) {
			Geometry geometry = computeGeometry(values.get(0), termkind);
			valueList.add(new LiteralImpl(GTransormationFunctions
					.centroidx((Geometry) geometry), datatype));
		} else if (transformationFunction.equals(GEOMETRY_FUNCTIONS.centroidy
				.toString())) {
			Geometry geometry = computeGeometry(values.get(0), termkind);
			valueList.add(new LiteralImpl(GTransormationFunctions
					.centroidy((Geometry) geometry), datatype));
		}
		/*
		 * else { return this.nodeType.makeNode(((String)value).toLowerCase());
		 * }
		 */
		else {
			try {
				throw new Exception(
						"mple Not supported Transformation function <"
								+ transformationFunction + ">");
			} catch (Exception e) {
				log.info(e.getMessage());
				e.printStackTrace();
			}
			return null;
		}
		/*
		 * for (String value : values) { switch (argumentMap.getTermType()) {
		 * case IRI: if (value != null && !value.equals("")){
		 * if(value.startsWith("www.")) value = "http://" + value;
		 * valueList.add(new URIImpl(value));} break; case BLANK_NODE:
		 * valueList.add(new BNodeImpl(value)); break; case LITERAL: if
		 * (argumentMap.getLanguageTag() != null && !value.equals("")) {
		 * valueList.add(new LiteralImpl(value, argumentMap.getLanguageTag()));
		 * } else if (value != null && !value.equals("") &&
		 * argumentMap.getDataType() != null) { valueList.add(new
		 * LiteralImpl(value, argumentMap.getDataType())); } else if (value !=
		 * null && !value.equals("")) { valueList.add(new
		 * LiteralImpl(value.trim())); } }
		 * 
		 * }
		 */
		return valueList;
	}

	@Override
	public List<String> processTermMap(TermMap map, Object node) {
		List<String> value = new ArrayList<>();
		switch (map.getTermMapType()) {
		case REFERENCE_VALUED:
			// Get the expression and extract the value
			ReferenceIdentifierImpl identifier = (ReferenceIdentifierImpl) map
					.getReferenceValue();
			return extractValueFromNode(node, identifier.toString().trim());
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
				List<String> replacements = extractValueFromNode(node,
						expression);

				for (int i = 0; i < replacements.size(); i++) {
					if (value.size() < (i + 1)) {
						value.add(template);
					}
					String replacement = null;
					if (replacements.get(i) != null)
						replacement = replacements.get(i).trim();

					// if (replacement == null || replacement.isEmpty()) {
					if (replacement == null || replacement.equals("")) {
						// if the replacement value is null or empty, the
						// reulting uri would be invalid, skip this.
						// The placeholders remain which removes them in the
						// end.
						continue;
					}

					String temp = value.get(i).trim();

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
			List<String> validValues = new ArrayList<>();
			for (String uri : value) {
				if (R2RMLToolkit.extractColumnNamesFromStringTemplate(uri)
						.isEmpty()) {
					validValues.add(uri);
				}
			}

			return validValues;

		default:
			return value;
		}

		// return value;
	}

	private Geometry computeGeometry(String value, QLTerm term)
			throws SAXException, IOException, ParserConfigurationException,
			NoSuchAuthorityCodeException, FactoryException, MalformedGeometryException {
		Geometry geometry = null;
		WKTReader wktReader = new WKTReader();
		try {
			geometry = wktReader.read(value);
			return geometry;
		} catch (ParseException e1) {
			//just continue
		}

		switch (term) {
		case XPATH_CLASS:
			GMLReader gmlreader = new GMLReader();
			geometry = gmlreader.read(value, null);
			return geometry;
		case CSV_CLASS:
			throw new UnsupportedOperationException(
					"Reading geometries form CSV implementation is missing");
		case JSONPATH_CLASS:
			GeometryJSON g = new GeometryJSON();

			InputStream istream = new ByteArrayInputStream(
					value.getBytes(StandardCharsets.UTF_8));
			
				geometry = g.read(istream);
			
			return geometry;
		default:
			throw new MalformedGeometryException("GeoTriples cannot recognize this type of geometry");
		}
	}
}
