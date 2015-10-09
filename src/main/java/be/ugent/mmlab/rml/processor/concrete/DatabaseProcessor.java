package be.ugent.mmlab.rml.processor.concrete;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.d2rq.db.SQLConnection;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import be.ugent.mmlab.rml.core.DependencyRMLPerformer;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.core.RMLPerformer;
import be.ugent.mmlab.rml.model.SubjectMap;
import be.ugent.mmlab.rml.model.TermMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.AbstractRMLProcessor;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;

/**
 * 
 * @author dimis
 */
public class DatabaseProcessor extends AbstractRMLProcessor {

	private static Log log = LogFactory.getLog(RMLMappingFactory.class);
	private ResultSet currentnode;
	protected TriplesMap map;

	public DatabaseProcessor() {
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
			
			DriverManager.registerDriver((Driver)Class.forName("nl.cwi.monetdb.jdbc.MonetDriver").newInstance());
			DriverManager.registerDriver((Driver)Class.forName("org.postgresql.Driver").newInstance());
			/*DriverManager.registerDriver((Driver)Class.forName("com.mysql.jdbc.Driver").newInstance());
			DriverManager.registerDriver((Driver)Class.forName("org.hsqldb.jdbcDriver").newInstance());*/
			Connection con = DriverManager.getConnection(fileName);
			Statement stm = con.createStatement();
			//stm.setFetchSize(10000);
			ResultSet results = stm.executeQuery(map.getLogicalSource().getReference());
			
			
			try {
				// Iterate the rows
				while (results.next()) {
					totalmatches.increase();
					currentnode = results;
					performer.perform(results, dataset, map);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				results.close();
				stm.close();
				con.close();
			}

		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			if(e1 instanceof ClassNotFoundException){
				log.error("GeoTriples couldn't find the MonetDB jdbc Driver class (nl.cwi.monetdb.jdbc.MonetDriver)");
				System.exit(13);
			}
		}
		return totalmatches.getValue();
	}

	@Override
	public List<Object> extractValueFromNode(Object node, String expression) {
		ResultSet row = (ResultSet) node;
		// call the right header in the row
		List<Object> list = new ArrayList<Object>();
		try {
			if (expression.equals("GeoTriplesID")) {
				list.add(row.getRow());
			} else {
				Object value = row.getObject(expression);
				list.add(value);
			}
		} catch (SQLException e) {
			if (log.isDebugEnabled()) {
				e.printStackTrace();
			}
			log.warn("There is no column \"" + expression + "\" in the result set");
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
		currentnode = (ResultSet) node;
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
}
