package eu.linkedeodata.geotriples;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import be.ugent.mmlab.rml.core.NodeRMLPerformer;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.function.FunctionAlreadyExists;
import be.ugent.mmlab.rml.function.FunctionArea;
import be.ugent.mmlab.rml.function.FunctionAsGML;
import be.ugent.mmlab.rml.function.FunctionAsWKT;
import be.ugent.mmlab.rml.function.FunctionCentroidX;
import be.ugent.mmlab.rml.function.FunctionCentroidY;
import be.ugent.mmlab.rml.function.FunctionCoordinateDimension;
import be.ugent.mmlab.rml.function.FunctionDimension;
import be.ugent.mmlab.rml.function.FunctionEQUI;
import be.ugent.mmlab.rml.function.FunctionFactory;
import be.ugent.mmlab.rml.function.FunctionHasSerialization;
import be.ugent.mmlab.rml.function.FunctionIs3D;
import be.ugent.mmlab.rml.function.FunctionIsEmpty;
import be.ugent.mmlab.rml.function.FunctionIsSimple;
import be.ugent.mmlab.rml.function.FunctionLength;
import be.ugent.mmlab.rml.function.FunctionSpatialDimension;
import be.ugent.mmlab.rml.model.RMLMapping;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.RMLProcessor;
import be.ugent.mmlab.rml.processor.RMLProcessorFactory;
import be.ugent.mmlab.rml.processor.concrete.ConcreteRMLProcessorFactory;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLStructureException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLSyntaxException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.R2RMLDataError;

public class QueryOntop {
	public static void registerFunctions() throws FunctionAlreadyExists {
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/equi"),
				new FunctionEQUI()); // don't remove or change this line, it
										// replaces the equi join functionality
										// of R2RML

		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/asWKT"),
				new FunctionAsWKT());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/hasSerialization"),
				new FunctionHasSerialization());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/asGML"),
				new FunctionAsGML());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/isSimple"),
				new FunctionIsSimple());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/isEmpty"),
				new FunctionIsEmpty());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/is3D"),
				new FunctionIs3D());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/spatialDimension"),
				new FunctionSpatialDimension());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/dimension"),
				new FunctionDimension());
		FunctionFactory.registerFunction(
				new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/coordinateDimension"),
				new FunctionCoordinateDimension());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/area"),
				new FunctionArea());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/length"),
				new FunctionLength());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/centroidx"),
				new FunctionCentroidX());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/centroidy"),
				new FunctionCentroidY());

	}

	public static void main(String[] args) throws FunctionAlreadyExists, RepositoryException, RDFParseException,
			InvalidR2RMLStructureException, InvalidR2RMLSyntaxException, R2RMLDataError, IOException {
		Options options = new Options();

		Option input = new Option("i", "input", true, "input RML file");
		input.setRequired(true);
		options.addOption(input);

		Option input_query = new Option("q", "query", true, "SPARQL query file");
		input_query.setRequired(true);
		options.addOption(input_query);

		Option output = new Option("o", "output", true, "output file");
		output.setRequired(true);
		options.addOption(output);

		CommandLineParser parser = new GnuParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("GeoTriples-Ontop", options);
			System.exit(1);
			return;
		}

		String mappingFilePath = cmd.getOptionValue("input");
		String outputFilePath = cmd.getOptionValue("output");
		String queryFilePath = cmd.getOptionValue("query");

		System.out.println(mappingFilePath);
		System.out.println(queryFilePath);
		System.out.println(outputFilePath);

		Repository repo = new SailRepository(new MemoryStore());
		repo.initialize();
		RepositoryConnection connection = repo.getConnection();
		try {
			registerFunctions();

			RMLMapping mapping = RMLMappingFactory.extractRMLMapping(mappingFilePath);

			RMLProcessorFactory factory = new ConcreteRMLProcessorFactory();
			for (TriplesMap m : mapping.getTriplesMaps()) {
				RMLProcessor processor = factory.create(m.getLogicalSource().getReferenceFormulation());

				Collection<Statement> statements = processor.execute(new NullSesameDataset(), m,
						new NodeRMLPerformer(processor), m.getLogicalSource().getIdentifier(), true);

				System.out.println("The triples generated by one iteration:");
				// for(Statement st:statements){
				// System.out.println(st.toString());
				// }
				// System.in.read();
				connection.add(statements);
			}
			Scanner scanner = null;
			String queryString;
			try {
				scanner = new Scanner(new File(queryFilePath));
				queryString = scanner.useDelimiter("\\Z").next();
			} finally {
				if (scanner != null)
					scanner.close();
			}
			
			HashMap<Integer, List<StatementPattern>> bgpGrps =  BGPGroupGenerator.generateBgpGroups(queryString);
			List<Double> meanTPSelectivities = new ArrayList<Double>()  ;
			System.out.println("Basic Graph Patterns (BGPs): " +bgpGrps.size());
			long totalTriplePatterns = 0;
			double meanQrySel =0 ; //mean of the avg triple pattern selectivity of all the triple patterns in a query
			for(int DNFkey:bgpGrps.keySet())  //DNFgrp => bgp
			{
				List<StatementPattern>	 stmts =  bgpGrps.get(DNFkey);
				totalTriplePatterns = totalTriplePatterns + stmts.size();
				for (StatementPattern stmt : stmts) 
				{		
					String sbjVertexLabel, objVertexLabel, predVertexLabel;
					sbjVertexLabel = getSubject(stmt);
					predVertexLabel = getPredicate(stmt);
					objVertexLabel = getObject(stmt);
					String tp = "myquery:"+sbjVertexLabel+"_"+predVertexLabel+"_"+objVertexLabel;
					System.out.println(tp);
				}
			}
			meanQrySel = meanQrySel/totalTriplePatterns;
			System.out.println("\nMean query selectivity (average of of the mean triple pattern selectivities): " + meanQrySel);
			// System.out.println(meanTPSelectivities);
			//double stdv = getStandardDeviation(meanTPSelectivities,meanQrySel);
			//System.out.println("Query Selectivities standard deviation: " + stdv );
			System.out.println("Triple Patterns: " +totalTriplePatterns);
			//System.exit(0);
			
			
			TupleQuery tq = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			System.out.println(tq.getBindings());
			//System.exit(0);
			TupleQueryResult tqr = tq.evaluate();
			try {
				while (tqr.hasNext()) { // iterate over the result solutions
					BindingSet bs = tqr.next();
//					Value p = bs.getValue("p");
//					Value o = bs.getValue("o");
					System.out.println(bs.toString());
				}
			} finally {
				tqr.close(); // always close result to release associated
								// resources
			}
		} catch (MalformedQueryException e) {
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}

	}
	
	public static String getTriplePattern(StatementPattern stmt) {
		String subject = getSubject(stmt);
		String object = getObject(stmt);
		String predicate = getPredicate(stmt);
		String triplePattern = subject + predicate + object ;
		return triplePattern;
	}
	
	/**
	 * Get Predicate from triple pattern
	 * @param stmt Triple pattern
	 * @return tuple Subject tuple
	 */
	public static String getPredicate(StatementPattern stmt) {
		String tuple;
		if (stmt.getPredicateVar().getValue()!=null)
			tuple = " <"+stmt.getPredicateVar().getValue().stringValue()+"> ";
		else
			tuple =" ?"+stmt.getPredicateVar().getName(); 
		return tuple;
	}
	/**
	 * Get object from triple pattern
	 * @param stmt Triple pattern
	 * @return tuple Subject tuple
	 */
	public static String getObject(StatementPattern stmt) {
		String tuple;
		if (stmt.getObjectVar().getValue()!=null && (stmt.getObjectVar().getValue().toString().startsWith("http://") || stmt.getObjectVar().getValue().toString().startsWith("ftp://")))
			tuple = " <"+stmt.getObjectVar().getValue().stringValue()+"> ";
		else if (stmt.getObjectVar().getValue()!=null)
			tuple = " '"+stmt.getObjectVar().getValue().stringValue()+"' ";
		else
			tuple =" ?"+stmt.getObjectVar().getName(); 
		return tuple;
	}
	/**
	 * Get subject from triple pattern
	 * @param stmt Triple pattern
	 * @return tuple Subject tuple
	 */
	public static String getSubject(StatementPattern stmt) {
		String tuple;
		if (stmt.getSubjectVar().getValue()!=null )
			tuple = "<"+stmt.getSubjectVar().getValue().stringValue() + "> ";
		else if (stmt.getSubjectVar().getValue()!=null )
			tuple = "'"+stmt.getSubjectVar().getValue().stringValue() + "' ";
		else
			tuple ="?"+stmt.getSubjectVar().getName(); 
		return tuple;
	}

	/**
	 * Get label for the object vertex of a triple pattern
	 * @param stmt triple pattern 
	 * @return label Vertex label
	 */
	public static String getObjectVertexLabel(StatementPattern stmt) {
		String label ; 
		if (stmt.getObjectVar().getValue()!=null)
			label = stmt.getObjectVar().getValue().stringValue();
		else
			label =stmt.getObjectVar().getName(); 
		return label;

	}
	/**
	 * Get label for the predicate vertex of a triple pattern
	 * @param stmt triple pattern 
	 * @return label Vertex label
	 */
	public static String getPredicateVertexLabel(StatementPattern stmt) {
		String label ; 
		if (stmt.getPredicateVar().getValue()!=null)
			label = stmt.getPredicateVar().getValue().stringValue();
		else
			label =stmt.getPredicateVar().getName(); 
		return label;

	}
	/**
	 * Get label for the subject vertex of a triple pattern
	 * @param stmt triple pattern
	 * @return label Vertex label
	 */
	public static String getSubjectVertexLabel(StatementPattern stmt) {
		String label ; 
		if (stmt.getSubjectVar().getValue()!=null)
			label = stmt.getSubjectVar().getValue().stringValue();
		else
			label =stmt.getSubjectVar().getName(); 
		return label;

	}
}
