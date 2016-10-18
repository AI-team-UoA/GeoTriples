package eu.linkedeodata.geotriples;

import java.util.HashMap;
import java.util.List;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
/**
 * Generate basic graph patterns also called Disjunctive Normal Form (DNF) groups from a SPARQL query
 * @author Saleem
 *
 */
public class BGPGroupGenerator 

{
	/**
	 * Generate BGP groups from a SPARQL query
	 * @param parsedQuery TupleExpr of the SPARQL query
	 * @return DNFGrps Map of DNF groups
	 * @throws MalformedQueryException 
	 */
	public static HashMap<Integer, List<StatementPattern>>  generateBgpGroups(String strQuery) throws MalformedQueryException
	{
		HashMap<Integer, List<StatementPattern>> bgpGrps = new HashMap<Integer, List<StatementPattern>>();
		int grpNo = 0;
		SPARQLParser parser = new SPARQLParser();
		ParsedQuery parsedQuery = parser.parseQuery(strQuery, null);
		TupleExpr query = parsedQuery.getTupleExpr();
		// collect all basic graph patterns

		for (TupleExpr bgp : BasicGraphPatternExtractor.process(query)) {
			//System.out.println(bgp);
			List<StatementPattern> patterns = StatementPatternCollector.process(bgp);	
			bgpGrps.put(grpNo, patterns );
			grpNo++;
		}

		return bgpGrps;
	}

}