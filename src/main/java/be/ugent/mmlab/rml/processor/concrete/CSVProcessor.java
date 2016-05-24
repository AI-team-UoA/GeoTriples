package be.ugent.mmlab.rml.processor.concrete;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.csvreader.CsvReader;

import be.ugent.mmlab.rml.core.KeyGenerator;
import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.core.RMLPerformer;
import be.ugent.mmlab.rml.function.Config;
import be.ugent.mmlab.rml.model.LogicalSource;
import be.ugent.mmlab.rml.model.SubjectMap;
import be.ugent.mmlab.rml.model.TermMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.AbstractRMLProcessor;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;

/**
 * 
 * @author mielvandersande, andimou modified by dimis
 */
public class CSVProcessor extends AbstractRMLProcessor {

	private static Log log = LogFactory.getLog(RMLMappingFactory.class);
	private HashMap<String, Object> currentnode;

	private char getDelimiter(LogicalSource ls) {
		String d = RMLEngine.getFileMap().getProperty(ls.getIdentifier() + ".delimiter");
		if (d == null) {
			return ',';
		}
		return d.charAt(0);
	}

	@Override
	public long execute(SesameDataSet dataset, TriplesMap map, RMLPerformer performer, String fileName) {
		// InputStream fis = null;
		long totalmatches = 0;
		try {
			KeyGenerator keygen = new KeyGenerator();
			// char delimiter = getDelimiter(map.getLogicalSource());
			char delimiter = '\t';

			// TODO: add character guessing
			// CsvReader reader = new CsvReader(fis, Charset.defaultCharset());
			log.info("[CSV Processor] filename " + fileName);
			InputStream input = this.isInMemoryInput()?new ByteArrayInputStream(this.getMemoryInput().getBytes(StandardCharsets.UTF_8)):new FileInputStream(fileName); 
			CsvReader reader = new CsvReader(input, Charset.defaultCharset());
			reader.setSafetySwitch(false);
			reader.setDelimiter(delimiter);

			reader.readHeaders();
			// Iterate the rows
			while (reader.readRecord()) {
				// System.out.println(reader.getRawRecord());
				++totalmatches;
				HashMap<String, Object> row = new HashMap<>();

				for (String header : reader.getHeaders()) {
					// System.out.println(reader.get(header));
					if (reader.get(header).startsWith("\"") && reader.get(header).endsWith("\"")) {
						row.put(header, reader.get(header).substring(1, reader.get(header).length()));
					} else {
						row.put(header, reader.get(header));
					}

				}
				row.put(Config.GEOTRIPLES_AUTO_ID, keygen.Generate());
				// let the performer handle the rows
				currentnode = row;
				performer.perform(row, dataset, map);
			}

		} catch (FileNotFoundException ex) {
			log.error(ex);
		} catch (IOException ex) {
			log.error(ex);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error(e);
		}
		return totalmatches;
	}

	@Override
	public List<Object> extractValueFromNode(Object node, String expression) {
		HashMap<String, String> row = (HashMap<String, String>) node;
		// call the right header in the row
		List<Object> list = new ArrayList();
		if (row.containsKey(expression)) {
			list.add(row.get(expression));
		}

		return list;
	}

	@Override
	public void execute_node(SesameDataSet dataset, String expression, TriplesMap parentTriplesMap,
			RMLPerformer performer, Object node, Resource subject) {
		throw new UnsupportedOperationException("[execute_node] Not applicable for CSV sources."); // To
																									// change
																									// body
																									// of
		// generated methods, choose
		// Tools | Templates.
	}

	@Override
	public void execute_node_fromdependency(SesameDataSet dataset, String expression, TriplesMap map,
			RMLPerformer performer, Object node) {
		throw new UnsupportedOperationException("[execute_node_fromdependency] Not applicable for CSV sources.");
	}

	@Override
	public QLTerm getFormulation() {
		return QLTerm.CSV_CLASS;
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
		try {
			throw new Exception("Bug, it shouldn't call this function. (by CSVProcessor)");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}
}
