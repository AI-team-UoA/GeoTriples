package be.ugent.mmlab.rml.processor.concrete;

import be.ugent.mmlab.rml.core.KeyGenerator;
import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.core.RMLPerformer;
import be.ugent.mmlab.rml.function.Config;
import be.ugent.mmlab.rml.model.LogicalSource;
import be.ugent.mmlab.rml.model.TermMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.AbstractRMLProcessor;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

import com.csvreader.CsvReader;
import com.vividsolutions.jts.geom.Geometry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

/**
 * 
 * @author dimis
 */
public class ShapefileProcessor extends AbstractRMLProcessor {

	private static Log log = LogFactory.getLog(RMLMappingFactory.class);
	private HashMap<String, Object> currentnode;

	@Override
	public void execute(SesameDataSet dataset, TriplesMap map,
			RMLPerformer performer, String fileName) {
		// InputStream fis = null;
		try {
			// TODO: add character guessing
			// CsvReader reader = new CsvReader(fis, Charset.defaultCharset());
			log.info("[Shapefile Processor] filename " + fileName);
			
			Map<String, URL> connect = new HashMap<String, URL>();
			connect.put("url", new File(fileName).toURI().toURL());
			DataStore dataStore = DataStoreFinder.getDataStore(connect);
			FeatureSource<?, ?> featureSource = dataStore.getFeatureSource(new File(fileName).getName().split("\\.")[0]);
			
			FeatureCollection<?, ?> collection = featureSource.getFeatures();
			FeatureIterator<?> iterator = collection.features();
			try {
				KeyGenerator keygen=new KeyGenerator();
				// Iterate the rows
				while (iterator.hasNext()) {
					HashMap<String, Object> row = new HashMap<>();
					Feature feature = iterator.next();
					for (Property p : feature.getProperties()) {
						row.put(p.getName().getLocalPart(), p.getValue());

					}
					row.put(Config.GEOTRIPLES_AUTO_ID, keygen.Generate());
					/*GeometryAttribute sourceGeometryAttribute = feature
							.getDefaultGeometryProperty();
					row.put("the_geom", (Geometry)sourceGeometryAttribute.getValue());*/
					currentnode = row;
					performer.perform(row, dataset, map);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				iterator.close();
				dataStore.dispose();
			}


		} catch (FileNotFoundException ex) {
			log.error(ex);
		} catch (IOException ex) {
			log.error(ex);
		}
	}

	@Override
	public List<Object> extractValueFromNode(Object node, String expression) {
		HashMap<String, Object> row = (HashMap<String, Object>) node;
		// call the right header in the row
		List<Object> list = new ArrayList();
		if (row.containsKey(expression)) {
			list.add(row.get(expression));
		}

		return list;
	}

	@Override
	public void execute_node(SesameDataSet dataset, String expression,
			TriplesMap parentTriplesMap, RMLPerformer performer, Object node,
			Resource subject) {
		throw new UnsupportedOperationException(
				"Not applicable for CSV sources."); // To change body of
													// generated methods, choose
													// Tools | Templates.
	}

	@Override
	public QLTerm getFormulation() {
		return QLTerm.SHP_CLASS;
	}

	@Override
	public List<Object> processTermMap(TermMap map, TriplesMap triplesMap,
			Resource subject, URI predicate, SesameDataSet dataset,
			boolean ignoreOwnerBecauseWeAreInJoin) {
		return processTermMap(map, currentnode, triplesMap, subject, predicate,
				dataset, ignoreOwnerBecauseWeAreInJoin);

	}
}