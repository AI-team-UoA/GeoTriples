package be.ugent.mmlab.rml.processor.concrete;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.FsUrlStreamHandlerFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.referencing.FactoryException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.ugent.mmlab.rml.core.DependencyRMLPerformer;
import be.ugent.mmlab.rml.core.KeyGenerator;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.core.RMLPerformer;
import be.ugent.mmlab.rml.function.Config;
import be.ugent.mmlab.rml.model.SubjectMap;
import be.ugent.mmlab.rml.model.TermMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.AbstractRMLProcessor;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;
import eu.linkedeodata.geotriples.PrintTimeStats;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;

/**
 * 
 * @author dimis
 */
public class ShapefileProcessor extends AbstractRMLProcessor {

	private static Logger log = LoggerFactory.getLogger(RMLMappingFactory.class);
	private HashMap<String, Object> currentnode;
	protected TriplesMap map;

	static {
		URL.setURLStreamHandlerFactory(new FsUrlStreamHandlerFactory());
	}

	public ShapefileProcessor() {

	}

	@Override
	public Collection<Statement> execute(SesameDataSet dataset, TriplesMap map, RMLPerformer performer, String fileName,
			Boolean RETURN_ALL_STATEMENTS) {
		// InputStream fis = null;

		List<Statement> statements = new LinkedList<>();

		if (dependencyTriplesMap != null || dependencyProcessor != null) {
			if (dependencyTriplesMap != null) {
				DependencyRMLPerformer dependencyPerformer = ((DependencyRMLPerformer) AbstractRMLProcessor.performersForFunctionInsideJoinCondition
						.get(dependencyTriplesMap));
				return execute_node_fromdependency(dataset,
						map.getLogicalSource().getReference()
								.replaceFirst(dependencyPerformer.getOwnmap().getLogicalSource().getReference(), ""),
						map, performer, dependencyPerformer.getCurrentNode());
			} else {
				return execute_node_fromdependency(dataset,
						map.getLogicalSource().getReference().replaceFirst(
								dependencyProcessor.getCurrentTriplesMap().getLogicalSource().getReference(), ""),
						map, performer, dependencyProcessor.getCurrentNode());
			}
			// return 10;
		}
		final WrappedLong totalmatches = new WrappedLong();
		try {
			this.map = map;
			// TODO: add character guessing
			// CsvReader reader = new CsvReader(fis, Charset.defaultCharset());
			log.info("[Shapefile Processor] filename " + fileName);

			Map<String, URL> connect = new HashMap<String, URL>();
			if (!fileName.startsWith("hdfs://")) {
				connect.put("url", new File(fileName).toURI().toURL());
			} else {
				connect.put("url", new URL(fileName));
			}
			DataStore dataStore = DataStoreFinder.getDataStore(connect);
			FeatureSource<?, ?> featureSource = dataStore.getFeatureSource(map.getLogicalSource().getReference());

			String epsg = org.geotools.gml2.bindings.GML2EncodingUtils
					.epsgCode(featureSource.getSchema().getCoordinateReferenceSystem());

			if (epsg != null) {
				Config.EPSG_CODE = epsg;
			} else {
				try {
					int code = CRS.lookupEpsgCode(featureSource.getSchema().getCoordinateReferenceSystem(), true);
					// System.out.println("the code is: "+code);
					Config.EPSG_CODE = String.valueOf(code);
				} catch (FactoryException e1) {
					// TODO Auto-generated catch block
					// e1.printStackTrace();
				}
			}

			FeatureCollection<?, ?> collection = featureSource.getFeatures();

			long startTime = System.nanoTime();
			FeatureIterator<?> iterator = collection.features();
			long endTime = System.nanoTime();
			long duration = (endTime - startTime) / 1000000; // divide by
																// 1000000 to
																// get
																// milliseconds.
			PrintTimeStats.printTime("Get the iterator of shapefile", duration);

			try {

				KeyGenerator keygen = new KeyGenerator();
				// Iterate the rows
				double total_thematic_duration = 0.0;
				double total_duration_geom = 0.0;
				while (iterator.hasNext()) {
					totalmatches.increase();
					HashMap<String, Object> row = new HashMap<>();

					startTime = System.nanoTime();
					Feature feature = iterator.next();
					for (Property p : feature.getProperties()) {
						if (!p.getName().getLocalPart().equalsIgnoreCase("the_geom")) {
							row.put(p.getName().getLocalPart(), p.getValue());
						}

					}
					endTime = System.nanoTime();
					duration = (endTime - startTime); // divide by
														// 1000000 to
														// get
														// milliseconds.
					total_thematic_duration += duration;
					PrintTimeStats.printTime("Read Thematic from file", duration);

					startTime = System.nanoTime();
					GeometryAttribute geomp = feature.getDefaultGeometryProperty();
					row.put(geomp.getName().getLocalPart(), geomp.getValue());
					endTime = System.nanoTime();
					duration = (endTime - startTime) / 1000000; // divide by
																// 1000000 to
																// get
																// milliseconds.
					PrintTimeStats.printTime("Read Geometry from file", duration);
					total_duration_geom += duration;

					row.put(Config.GEOTRIPLES_AUTO_ID, keygen.Generate());
					/*
					 * GeometryAttribute sourceGeometryAttribute = feature
					 * .getDefaultGeometryProperty(); row.put("the_geom",
					 * (Geometry)sourceGeometryAttribute.getValue());
					 */
					currentnode = row;
					if (RETURN_ALL_STATEMENTS == true) { // this is temporary
						statements.addAll(performer.perform(row, dataset, map));
					} else {
						performer.perform(row, dataset, map);
					}
				}
				PrintTimeStats.printTime("Read Thematic data (total) from file", total_thematic_duration);
				PrintTimeStats.printTime("Read Geometries (total) from file", total_duration_geom);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				iterator.close();
				dataStore.dispose();
			}

		} catch (FileNotFoundException ex) {
			log.error(ex.toString());
		} catch (IOException ex) {
			log.error(ex.toString());
		}
		return statements;
		// return totalmatches.getValue();
	}

	@Override
	public List<Object> extractValueFromNode(Object node, String expression) {
		HashMap<String, Object> row = (HashMap<String, Object>) node;
		// call the right header in the row
		List<Object> list = new ArrayList<>();
		if (row.containsKey(expression)) {
			list.add(row.get(expression));
		}

		return list;
	}

	@Override
	public Collection<Statement> execute_node(SesameDataSet dataset, String expression, TriplesMap parentTriplesMap,
			RMLPerformer performer, Object node, Resource subject) {
		throw new UnsupportedOperationException("[execute_node] Not applicable for Shapefile sources."); // To
																											// change
																											// body
																											// of
		// generated methods, choose
		// Tools | Templates.
	}

	@Override
	public Collection<Statement> execute_node_fromdependency(SesameDataSet dataset, String expression, TriplesMap map,
			RMLPerformer performer, Object node) {
		this.map = map;
		// throw new UnsupportedOperationException(
		// "[execute_node_fromdependency] Not applicable for Shapefile
		// sources.");
		currentnode = (HashMap<String, Object>) node;
		return performer.perform(node, dataset, map);
	}

	@Override
	public QLTerm getFormulation() {
		return QLTerm.SHP_CLASS;
	}

	@Override
	public List<Object> processTermMap(TermMap map, TriplesMap triplesMap, Resource subject, URI predicate,
			SesameDataSet dataset, boolean ignoreOwnerBecauseWeAreInJoin) {
		return processTermMap(map, currentnode, triplesMap, subject, predicate, dataset, ignoreOwnerBecauseWeAreInJoin);

	}

	@Override
	public Resource processSubjectMap(SesameDataSet dataset, SubjectMap subjectMap) {
		return processSubjectMap(dataset, subjectMap, currentnode);
	}

	@Override
	public Object getCurrentNode() {
		return currentnode;
	}

	@Override
	public TriplesMap getCurrentTriplesMap() {
		// try {
		// throw new Exception("Bug, it shouldn't use this function from
		// ShapefileProcessor");
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// System.exit(0);
		// }
		// return null;
		return map;
	}

	public static byte[] toByteArray(double value) {
		byte[] bytes = new byte[8];
		ByteBuffer.wrap(bytes).putDouble(value);
		return bytes;
	}

	public static void main(String[] args) {
		double d = 4799826.0986166214570499999999999;
		// System.out.printf("%.18f\n", d);
		// byte[] array=toByteArray(d);
		/*
		 * for(int i=0;i<8;++i){ System.out.printf("%d ",array[i]); }
		 */
		// System.out.println(Long.toBinaryString(Double.doubleToRawLongBits(d)));

	}
}
