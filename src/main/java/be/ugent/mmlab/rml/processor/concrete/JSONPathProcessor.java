/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.mmlab.rml.processor.concrete;

import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.core.RMLPerformer;
import be.ugent.mmlab.rml.model.TermMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.AbstractRMLProcessor;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

import com.jayway.jsonpath.JsonPath;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

/**
 * 
 * @author mielvandersande, andimou modified by dimis
 */
public class JSONPathProcessor extends AbstractRMLProcessor {

	private static Log log = LogFactory.getLog(RMLMappingFactory.class);
	private Object currentnode;

	@Override
	public void execute(SesameDataSet dataset, TriplesMap map,
			RMLPerformer performer, String fileName) {

		try {
			String reference = getReference(map.getLogicalSource());
			// This is a none streaming solution. A streaming parser requires
			// own implementation, possibly based on
			// https://code.google.com/p/json-simple/wiki/DecodingExamples
			JsonPath path = JsonPath.compile(reference);
			Object val = path.read(new FileInputStream(fileName));

			execute(dataset, map, performer, val);

		} catch (FileNotFoundException ex) {
			Logger.getLogger(JSONPathProcessor.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(JSONPathProcessor.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	@Override
	public List<Object> extractValueFromNode(Object node, String expression) {

		try {
			Object val = JsonPath.read(node, expression);
			List<Object> list = new ArrayList<>();
			if (val instanceof JSONArray) {
				JSONArray arr = (JSONArray) val;
				return Arrays.asList(arr.toArray(new Object[0]));
			}
			list.add((String) val.toString());
			return list;
		} catch (com.jayway.jsonpath.InvalidPathException ex) {
			return new ArrayList<>();
		} catch (Exception ex) {
			log.debug("[JSONPathProcessor:extractValueFromNode]. Error: " + ex);
			return null;
		}

	}

	@Override
	public void execute_node(SesameDataSet dataset, String expression,
			TriplesMap parentTriplesMap, RMLPerformer performer, Object node,
			Resource subject) {

		Object val = JsonPath.read(node, expression);

		execute(dataset, parentTriplesMap, performer, val);

		// TODO: check if it's complete for sub-mappings
	}

	private void execute(SesameDataSet dataset, TriplesMap parentTriplesMap,
			RMLPerformer performer, Object node) {
		if (node instanceof JSONObject) {
			currentnode = node;
			performer.perform(node, dataset, parentTriplesMap);
		} else {
			List<Object> nodes;

			if (node instanceof JSONArray) {
				JSONArray arr = (JSONArray) node;
				nodes = arr.subList(0, arr.size());
			} else {
				try {
					nodes = (List<Object>) node;
				} catch (ClassCastException cce) {
					nodes = new ArrayList<Object>();
				}
			}

			// iterate over all the objects
			for (Object object : nodes) {
				currentnode = node;
				performer.perform(object, dataset, parentTriplesMap);
			}
		}
	}

	@Override
	public QLTerm getFormulation() {
		return QLTerm.JSONPATH_CLASS;
	}

	@Override
	public List<Object> processTermMap(TermMap map, TriplesMap triplesMap,
			Resource subject, URI predicate, SesameDataSet dataset,
			boolean ignoreOwnerBecauseWeAreInJoin) {
		return processTermMap(map, currentnode, triplesMap, subject, predicate,
				dataset, ignoreOwnerBecauseWeAreInJoin);

	}
}
