/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.mmlab.rml.processor.concrete;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.JsonPath;

import be.ugent.mmlab.rml.core.DependencyRMLPerformer;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.core.RMLPerformer;
import be.ugent.mmlab.rml.model.SubjectMap;
import be.ugent.mmlab.rml.model.TermMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.AbstractRMLProcessor;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * 
 * @author mielvandersande, andimou modified by dimis
 */
public class JSONPathProcessor extends AbstractRMLProcessor {

	private static Logger log = LoggerFactory.getLogger(RMLMappingFactory.class);
	private Object currentnode;
	private TriplesMap map;

	@Override
	public Collection<Statement> execute(SesameDataSet dataset, TriplesMap map,
			RMLPerformer performer, String fileName,Boolean RETURN_ALL_STATEMENTS) {
		List<Statement> statements=new LinkedList<>();
		
		if(dependencyTriplesMap!=null || dependencyProcessor!=null){
			if(dependencyTriplesMap!=null){
			DependencyRMLPerformer dependencyPerformer=((DependencyRMLPerformer)AbstractRMLProcessor.performersForFunctionInsideJoinCondition.get(dependencyTriplesMap));
				return execute_node_fromdependency(dataset, map.getLogicalSource().getReference().replaceFirst(dependencyPerformer.getOwnmap().getLogicalSource().getReference(), ""), map, performer, dependencyPerformer.getCurrentNode());
			
			}else
			{
				return 	execute_node_fromdependency(dataset, map.getLogicalSource().getReference().replaceFirst(dependencyProcessor.getCurrentTriplesMap().getLogicalSource().getReference(), ""), map, performer, dependencyProcessor.getCurrentNode());
			}
			//return 10;
		}
		long totalmatches=0l;
		try {
			String reference = getReference(map.getLogicalSource());
			// This is a none streaming solution. A streaming parser requires
			// own implementation, possibly based on
			// https://code.google.com/p/json-simple/wiki/DecodingExamples
			JsonPath path = JsonPath.compile(reference);
			Object val = path.read(new FileInputStream(fileName));
			
			if(RETURN_ALL_STATEMENTS==true){
				statements.addAll(execute(dataset, map, performer, val));					
			}else{
				execute(dataset, map, performer, val);
			}
			
			//totalmatches=execute(dataset, map, performer, val);

		} catch (FileNotFoundException ex) {
			LoggerFactory.getLogger(JSONPathProcessor.class.getName()).error(ex.toString());
		} catch (IOException ex) {
			LoggerFactory.getLogger(JSONPathProcessor.class.getName()).error(ex.toString());
		}
		return statements;
		//return totalmatches;
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
			if(val!=null)
				list.add((String) val.toString());
			else
				list.add("null");
			return list;
		} catch (com.jayway.jsonpath.InvalidPathException ex) {
			return new ArrayList<>();
		} catch (Exception ex) {
			log.debug("[JSONPathProcessor:extractValueFromNode]. Error: " + ex);
			return null;
		}

	}

	@Override
	public Collection<Statement> execute_node(SesameDataSet dataset, String expression,
			TriplesMap parentTriplesMap, RMLPerformer performer, Object node,
			Resource subject) {

		Object val = JsonPath.read(node, expression);

		return execute(dataset, parentTriplesMap, performer, val);

		// TODO: check if it's complete for sub-mappings
	}
	
	@Override
	public Collection<Statement> execute_node_fromdependency(SesameDataSet dataset, String expression,TriplesMap map,
			 RMLPerformer performer, Object node
			){
		this.map = map;
		Object val = JsonPath.read(node, expression);

		return execute(dataset,map, performer, val);
	}

	private Collection<Statement> execute(SesameDataSet dataset, TriplesMap parentTriplesMap,
			RMLPerformer performer, Object node) {
		List<Statement> statements=new LinkedList<>();
		
		if (node instanceof JSONObject) {
			currentnode = node;
			performer.perform(node, dataset, parentTriplesMap);
		} else { 
			long totalmatches=0l;
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
				currentnode = object;
				statements.addAll(performer.perform(object, dataset, parentTriplesMap));
			}
		}
		return statements;
		//return 10;
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
	@Override
	public Resource processSubjectMap(SesameDataSet dataset,
			SubjectMap subjectMap) {
		return processSubjectMap(dataset, subjectMap,currentnode);
	} 
	@Override
	public Object getCurrentNode(){
		return currentnode;
	}
	@Override
	public TriplesMap getCurrentTriplesMap(){
		return map;
	}
}
