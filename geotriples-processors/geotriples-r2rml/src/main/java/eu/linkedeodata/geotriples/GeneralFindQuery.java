package eu.linkedeodata.geotriples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.d2rq.D2RQOptions;
import org.d2rq.algebra.TripleRelation;
import org.d2rq.db.op.LimitOp;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterConcat;

import eu.linkedeodata.geotriples.GeneralURIMakerRule.GeneralURIMakerRuleChecker;


/**
 * A find query on a collection of {@link TripleRelation}s. Results are 
 * delivered as a {@link QueryIter} over three-variable s/p/o bindings.
 * Will combine queries on multiple
 * relations into one SQL statement where possible.
 * An option for limiting the number of triples returned from each
 * {@link TripleRelation} is available.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class GeneralFindQuery {
	private final Triple triplePattern;
	private final Collection<GeneralTripleRelation> tripleRelations;
	private final int limitPerRelation;
	private final ExecutionContext context;
	
	public GeneralFindQuery(Triple triplePattern, Collection<GeneralTripleRelation> tripleRelations,
			ExecutionContext context) {
		this(triplePattern, tripleRelations, LimitOp.NO_LIMIT, context);
	}	

	public GeneralFindQuery(Triple triplePattern, Collection<GeneralTripleRelation> tripleRelations, int limit,
			ExecutionContext context) {
		this.triplePattern = triplePattern;
		this.tripleRelations = tripleRelations;
		this.limitPerRelation = limit;
		this.context = context;
	}	

	private List<GeneralTripleRelation> selectedTripleRelations() {
		GeneralURIMakerRule rule = new GeneralURIMakerRule();
		List<GeneralTripleRelation> sortedTripleRelations = rule.sortRDFRelations(tripleRelations);
		GeneralURIMakerRuleChecker subjectChecker = rule.createRuleChecker(triplePattern.getSubject());
		GeneralURIMakerRuleChecker predicateChecker = rule.createRuleChecker(triplePattern.getPredicate());
		GeneralURIMakerRuleChecker objectChecker = rule.createRuleChecker(triplePattern.getObject());
		List<GeneralTripleRelation> result = new ArrayList<GeneralTripleRelation>();
		for (GeneralTripleRelation tripleRelation: sortedTripleRelations) {
			GeneralTripleRelation selectedTripleRelation = tripleRelation.selectTriple(triplePattern);
			if (selectedTripleRelation != null
					&& subjectChecker.canMatch(tripleRelation.nodeMaker(GeneralTripleRelation.SUBJECT))
					&& predicateChecker.canMatch(tripleRelation.nodeMaker(GeneralTripleRelation.PREDICATE))
					&& objectChecker.canMatch(tripleRelation.nodeMaker(GeneralTripleRelation.OBJECT))) {
				subjectChecker.addPotentialMatch(tripleRelation.nodeMaker(GeneralTripleRelation.SUBJECT));
				predicateChecker.addPotentialMatch(tripleRelation.nodeMaker(GeneralTripleRelation.PREDICATE));
				objectChecker.addPotentialMatch(tripleRelation.nodeMaker(GeneralTripleRelation.OBJECT));
				if ("true".equals(context.getContext().get(D2RQOptions.TRIM_JOINS, "false"))) {
					selectedTripleRelation = GeneralTripleRelation.fromNodeRelation(GeneralJoinOptimizer.optimize(selectedTripleRelation));
				}
				if (limitPerRelation != LimitOp.NO_LIMIT) {
					selectedTripleRelation = selectedTripleRelation.limit(limitPerRelation);
				}
				result.add(selectedTripleRelation);
			}
		}
		return result;
	}
	
	public QueryIter iterator() {
		QueryIterConcat qIter = new QueryIterConcat(context);
		if ("true".equals(context.getContext().getAsString(D2RQOptions.MULTIPLEX_QUERIES, "false")))
		{
			System.out.println("this is true!!! ShapefileFindQuery");
		}
		/*if ("true".equals(context.getContext().getAsString(D2RQOptions.MULTIPLEX_QUERIES, "false"))) {
			for (ShapefileCompatibleRelationGroup group: 
				ShapefileCompatibleRelationGroup.groupNodeRelations(selectedTripleRelations())) {
				qIter.add(QueryIterTableSQL.create(group.getShapefileConnection(),
						group.baseRelation(), group.bindingMakers(), context));
			}
		} else {*/
			for (GeneralTripleRelation relation: selectedTripleRelations()) {
				qIter.add(GeneralQueryIterTableSQL.create(relation, context));
			}
		//}
		return qIter;
		
		//dimis implement this!!!		return null;
	}
}
