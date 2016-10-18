package be.ugent.mmlab.rml.processor.concrete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.Geometry;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

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
public class ShapefileProcessorGDAL extends AbstractRMLProcessor {

	private static Logger log = LoggerFactory.getLogger(RMLMappingFactory.class);
	private HashMap<String, Object> currentnode;
	protected TriplesMap map;

	public ShapefileProcessorGDAL() {

	}

	@Override
	public Collection<Statement> execute(SesameDataSet dataset, TriplesMap map, RMLPerformer performer, String fileName,Boolean RETURN_ALL_STATEMENTS) {
		// InputStream fis = null;
		List<Statement> statements=new LinkedList<>();
		
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
			//return 10;
		}
		final WrappedLong totalmatches = new WrappedLong();
		
			this.map = map;
			log.info("[Shapefile Processor] filename " + fileName);
			log.info("[Shapefile Processor] Using GDAL library.");
			ogr.DontUseExceptions();

			/*
			 * --------------------------------------------------------------------
			 */
			/* Register format(s). */
			/*
			 * --------------------------------------------------------------------
			 */
			if (ogr.GetDriverCount() == 0)
				ogr.RegisterAll();
			String pszDataSource = null;
			Vector papszLayers = new Vector();
			DataSource poDS;
			pszDataSource = fileName;
			poDS = ogr.Open(pszDataSource, false);

			/*
			 * -----------------------------------------------------------------
			 * ---
			 */
			/* Report failure */
			/*
			 * -----------------------------------------------------------------
			 * ---
			 */
			if (poDS == null) {
				System.err.println("FAILURE:\n" + "Unable to open datasource ` " + pszDataSource
						+ "' with the following drivers.");

				for (int iDriver = 0; iDriver < ogr.GetDriverCount(); iDriver++) {
					System.err.println("  . " + ogr.GetDriver(iDriver).GetName());
				}

				System.exit(1);
			}

		int nLayerCount = 0;
		Layer[] papoLayers = null;
		if (papszLayers.size() == 0) {
			nLayerCount = poDS.GetLayerCount();
			papoLayers = new Layer[nLayerCount];

			for (int iLayer = 0; iLayer < nLayerCount; iLayer++) {
				Layer poLayer = poDS.GetLayer(iLayer);

				if (poLayer == null) {
					System.err.println("FAILURE: Couldn't fetch advertised layer " + iLayer + "!");
					System.exit(1);
				}

				papoLayers[iLayer] = poLayer;
			}
		}
		for (int iLayer = 0; iLayer < nLayerCount; iLayer++) {
			Layer poLayer = papoLayers[iLayer];
			if (!poLayer.GetName().equals(map.getLogicalSource().getReference())) {
				continue;
			}
			Config.EPSG_CODE = poLayer.GetSpatialRef().GetAttrValue("AUTHORITY", 1);

			// System.out.println(poLayer.GetSpatialRef().GetAttrValue("AUTHORITY",
			// 1));
			KeyGenerator keygen = new KeyGenerator();
			Feature feature;
			double total_thematic_duration=0.0;
			double total_duration_geom=0.0;
			for (int f = 0; f < poLayer.GetFeatureCount(); ++f) {
				// while ((feature = poLayer.GetNextFeature()) != null) {
				long startTime = System.nanoTime();
				feature = poLayer.GetNextFeature();
				long endTime = System.nanoTime();
				long duration = (endTime - startTime)/1000000;  //divide by 1000000 to get milliseconds.
				total_thematic_duration+=duration;
				
				totalmatches.increase();
				
				
				HashMap<String, Object> row = new HashMap<>();
				int fieldssize = feature.GetFieldCount();
				
				
				startTime = System.nanoTime();
				for (int i = 0; i < fieldssize; ++i) {
					String type = feature.GetFieldDefnRef(i).GetFieldTypeName(feature.GetFieldType(i));
					if (type.equals("String")) {
						row.put(feature.GetFieldDefnRef(i).GetName(), feature.GetFieldAsString(i));
						// System.out.println(feature.GetFieldAsString(i));
					} else if (type.equals("StringList")) {
						// System.out.println(feature.GetFieldAsStringList(i));
						// for (String s : feature.GetFieldAsStringList(i))
						// {
						// System.out.print(s);
						// System.out.println();
						// }
					} else if (type.equals("Integer")) {
						row.put(feature.GetFieldDefnRef(i).GetName(), feature.GetFieldAsInteger(i));
						// System.out.println(feature.GetFieldAsInteger(i));
					} else if (type.equals("IntegerList")) {
						// for (Integer s :
						// feature.GetFieldAsIntegerList(i)) {
						// System.out.print(s);
						// System.out.println();
						// }
					} else if (type.equals("Real")) {
						row.put(feature.GetFieldDefnRef(i).GetName(), feature.GetFieldAsDouble(i));
						// System.out.println(feature.GetFieldAsDouble(i));
					} else if (type.equals("RealList")) {
						// System.out.println(feature.GetFieldAsDoubleList(i));
					} /*
						 * else if (type.equals("(unknown)")) {
						 * System.out.println(feature.GetGeometryRef()); }
						 */

					// System.out.println(feature.GetFieldAsString(i));
					// System.out.println(feature.GetFieldAsInteger(i));

				}
				endTime = System.nanoTime();
				duration = (endTime - startTime)/1000000;  //divide by 1000000 to get milliseconds.
				PrintTimeStats.printTime("Read Thematic from file", duration);
				total_thematic_duration+=duration;
				
				startTime = System.nanoTime();
				Geometry geom = feature.GetGeometryRef();
				endTime = System.nanoTime();
				duration = (endTime - startTime)/1000000;  //divide by 1000000 to get milliseconds.
				total_duration_geom+=duration;
				PrintTimeStats.printTime("Read Geometry from file", duration);
				
				row.put("the_geom", geom);
				try {
					row.put(Config.GEOTRIPLES_AUTO_ID, keygen.Generate());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// System.out.println(geom.ExportToWkt());
				currentnode = row;
				
				if(RETURN_ALL_STATEMENTS==true){
					statements.addAll(performer.perform(row, dataset, map));					
				}else{
					performer.perform(row, dataset, map);
				}
				
				feature.delete();
				feature = null;
			}
			
			PrintTimeStats.printTime("Read Thematic data (total) from file", total_thematic_duration);
			PrintTimeStats.printTime("Read Geometries (total) from file", total_duration_geom);
			

		}

		return statements;
		//return totalmatches.getValue();
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

	public static void main(String[] args) {
		double d = 4799826.0986166212;
		System.out.println(d);

	}
}
