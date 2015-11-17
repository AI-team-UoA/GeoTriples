package be.ugent.mmlab.rml.processor.concrete;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.d2rq.db.SQLConnection;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import be.ugent.mmlab.rml.core.DependencyRMLPerformer;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.core.RMLPerformer;
import be.ugent.mmlab.rml.model.ObjectMap;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.SubjectMap;
import be.ugent.mmlab.rml.model.TermMap;
import be.ugent.mmlab.rml.model.TermMap.TermMapType;
import be.ugent.mmlab.rml.model.TermType;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.model.reference.ReferenceIdentifier;
import be.ugent.mmlab.rml.processor.AbstractRMLProcessor;
import be.ugent.mmlab.rml.tools.PrintTimeStats;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.tools.R2RMLToolkit;

/**
 * 
 * @author dimis
 */
public class DatabaseProcessorWithManyQueries extends AbstractRMLProcessor {

	private static Log log = LogFactory.getLog(RMLMappingFactory.class);
	private HashMap<String, Object> currentnode;
	protected TriplesMap map;

	public DatabaseProcessorWithManyQueries() {

	}

	@Override
	public long execute(SesameDataSet dataset, TriplesMap map, RMLPerformer performer, String fileName) {
		// InputStream fis = null;
		if (dependencyTriplesMap != null || dependencyProcessor != null) {
			if (dependencyTriplesMap != null) {
				DependencyRMLPerformer dependencyPerformer = ((DependencyRMLPerformer) AbstractRMLProcessor.performersForFunctionInsideJoinCondition
						.get(dependencyTriplesMap));
				execute_node_fromdependency(dataset,
						map.getLogicalSource().getReference()
								.replaceFirst(dependencyPerformer.getOwnmap().getLogicalSource().getReference(), ""),
						map, performer, dependencyPerformer.getCurrentNode());
			} else {
				execute_node_fromdependency(dataset,
						map.getLogicalSource().getReference().replaceFirst(
								dependencyProcessor.getCurrentTriplesMap().getLogicalSource().getReference(), ""),
						map, performer, dependencyProcessor.getCurrentNode());
			}
			return 10;
		}
		final WrappedLong totalmatches = new WrappedLong();
		try {
			this.map = map;
			// TODO: add character guessing
			// CsvReader reader = new CsvReader(fis, Charset.defaultCharset());
			log.info("[Database Processor] url " + fileName);

			DriverManager.registerDriver((Driver) Class.forName("nl.cwi.monetdb.jdbc.MonetDriver").newInstance());
			DriverManager.registerDriver((Driver) Class.forName("org.postgresql.Driver").newInstance());
			/*
			 * DriverManager.registerDriver((Driver)Class.forName(
			 * "com.mysql.jdbc.Driver").newInstance());
			 * DriverManager.registerDriver((Driver)Class.forName(
			 * "org.hsqldb.jdbcDriver").newInstance());
			 */

			// stm.setFetchSize(10000);
			List<Connection> allcons = new ArrayList<>();
			List<Statement> allstm = new ArrayList<>();
			List<ResultSet> allresults = new ArrayList<>();
			Set<String> references = getColumnsReferencesFromTriplesMap(map);
			String effective_query = map.getLogicalSource().getReference();
			if (effective_query.endsWith(";"))
				effective_query = effective_query.substring(0, effective_query.length() - 1);
			for (String reference : references) {
				Connection con = DriverManager.getConnection(fileName);
				Statement stm = con.createStatement();
				allstm.add(stm);
				allcons.add(con);
				String newquery = "SELECT \"effective_query\".\"" + reference + "\" FROM (" + effective_query
						+ ") as effective_query";
				log.info("[Executing query] " + newquery);
				
				long startTime = System.nanoTime();
				ResultSet results = stm.executeQuery(newquery);
				long endTime = System.nanoTime();
				long duration = (endTime - startTime) / 1000000; // divide by
																	// 1000000 to
																	// get
																	// milliseconds.
				PrintTimeStats.printTime("Execute a query", duration);
				
				allresults.add(results);
			}
			double total_duration=0.0;
			try {
				// Iterate the rows
				// Do one step for all separate results
				ResultSet first = allresults.get(0);
				
				while (first.next()) {
					for (int i = 1; i < allresults.size(); ++i) {
						long startTime = System.nanoTime();
						allresults.get(i).next();
						long endTime = System.nanoTime();
						double duration = (endTime - startTime) / 1000000; // divide by
																			// 1000000 to
																			// get
																			// milliseconds.
						PrintTimeStats.printTime("Read line from results of a query", duration);
						total_duration+=duration;
						
					}
					totalmatches.increase();
					HashMap<String, Object> row = new HashMap<>();

					for (ResultSet rs : allresults) {
						row.put(rs.getMetaData().getColumnLabel(1), rs.getObject(1));
					}
					row.put("GeoTriplesID", first.getRow());
					currentnode = row;
					performer.perform(row, dataset, map);
				}
				PrintTimeStats.printTime("Read all lines from all results of the queries posed for a triples map", total_duration);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				for (int i = 1; i < allresults.size(); ++i) {
					allresults.get(i).close();
					allstm.get(0).close();
					allcons.get(0).close();
				}

			}

		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			if (e1 instanceof ClassNotFoundException) {
				log.error("GeoTriples couldn't find the MonetDB jdbc Driver class (nl.cwi.monetdb.jdbc.MonetDriver)");
				System.exit(13);
			}
		}
		return totalmatches.getValue();
	}

	@Override
	public List<Object> extractValueFromNode(Object node, String expression) {
		HashMap<String, Object> row = (HashMap<String, Object>) node;
		// call the right header in the row
		List<Object> list = new ArrayList<Object>();
		if (row.containsKey(expression)) {
			list.add(row.get(expression));
		}

		return list;
	}

	@Override
	public void execute_node(SesameDataSet dataset, String expression, TriplesMap parentTriplesMap,
			RMLPerformer performer, Object node, Resource subject) {
		throw new UnsupportedOperationException("[execute_node] Not applicable for Database sources, yet."); // To
																												// change
																												// body
																												// of
		// generated methods, choose
		// Tools | Templates.
	}

	@Override
	public void execute_node_fromdependency(SesameDataSet dataset, String expression, TriplesMap map,
			RMLPerformer performer, Object node) {
		this.map = map;
		// throw new UnsupportedOperationException(
		// "[execute_node_fromdependency] Not applicable for Shapefile
		// sources.");
		currentnode = (HashMap<String, Object>) node;
		performer.perform(node, dataset, map);
	}

	@Override
	public QLTerm getFormulation() {
		return QLTerm.SQL_CLASS;
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

	private Set<String> getColumnsReferencesFromTriplesMap(TriplesMap tm) {
		Set<String> results = new HashSet<>();
		for (PredicateObjectMap pom : tm.getPredicateObjectMaps()) {
			for (ObjectMap om : pom.getObjectMaps()) {
				if (om.getTermMapType().equals(TermMapType.REFERENCE_VALUED)) {
					ReferenceIdentifier r = om.getReferenceValue();

					// System.out.println(r.toString());
					results.add(r.toString());
				}
			}
		}
		Set<String> cols = R2RMLToolkit.extractColumnNamesFromStringTemplate(tm.getSubjectMap().getStringTemplate());
		for (String c : cols) {
			if (c.equals("GeoTriplesID"))
				continue;
			results.add(c);
		}
		return results;
	}
}
