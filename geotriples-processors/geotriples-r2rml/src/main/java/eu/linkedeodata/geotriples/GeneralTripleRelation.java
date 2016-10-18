package eu.linkedeodata.geotriples;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.d2rq.algebra.NodeRelation;
import org.d2rq.db.op.DatabaseOp;
import org.d2rq.db.op.LimitOp;
import org.d2rq.nodes.BindingMaker;
import org.d2rq.nodes.NodeMaker;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Var;

/**
 * A collection of virtual triples obtained by applying a {@link DatabaseOp} to a
 * database, and applying {@link NodeMaker}s for subject, predicate and object
 * to each result row. This is a simple extension (or rather restriction) of
 * {@link NodeRelation}.
 *
 * @author Chris Bizer chris@bizer.de
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class GeneralTripleRelation extends GeneralNodeRelation {
	public static final Var SUBJECT = Var.alloc("subject");
	public static final Var PREDICATE = Var.alloc("predicate");
	public static final Var OBJECT = Var.alloc("object");

	public static final Set<Var> SPO = 
		new HashSet<Var>(Arrays.asList(new Var[]{SUBJECT, PREDICATE, OBJECT}));
	
	public static GeneralTripleRelation fromNodeRelation(GeneralNodeRelation relation) {
		if (relation instanceof GeneralTripleRelation) return (GeneralTripleRelation) relation;
		if (!relation.getBindingMaker().variableNames().equals(SPO)) {
			throw new IllegalArgumentException("Not a TripleRelation header: " + 
					relation.getBindingMaker().variableNames());
		}
		return new GeneralTripleRelation(relation.getConnection(), relation.getBaseTabular(), relation.getBindingMaker()); 
	}
	
	public GeneralTripleRelation(GeneralConnection connection, DatabaseOp baseRelation, 
			final NodeMaker subjectMaker, final NodeMaker predicateMaker, final NodeMaker objectMaker) {
		super(connection, baseRelation, new HashMap<Var,NodeMaker>() {/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		{
			put(SUBJECT, subjectMaker);
			put(PREDICATE, predicateMaker);
			put(OBJECT, objectMaker);
		}});
	}

	public GeneralTripleRelation(GeneralConnection connection, DatabaseOp baseRelation, BindingMaker bindingMaker) {
		super(connection, baseRelation, bindingMaker);
	}
	
	public GeneralTripleRelation orderBy(Var variable, boolean ascending) {
		return fromNodeRelation(GeneralNodeRelationUtil.order(this, variable, ascending));
	}

	public GeneralTripleRelation limit(int limit) {
		return fromNodeRelation(GeneralNodeRelationUtil.limit(this, limit));
	}

	public GeneralTripleRelation selectTriple(Triple t) {
		GeneralNodeRelation r = this;
		r = GeneralNodeRelationUtil.select(r, SUBJECT, t.getSubject());
		r = GeneralNodeRelationUtil.select(r, PREDICATE, t.getPredicate());
		r = GeneralNodeRelationUtil.select(r, OBJECT, t.getObject());
		r = GeneralNodeRelationUtil.project(r, SPO);
		if (t.getObject().isConcrete() && !t.getSubject().isConcrete()) {
		    r = new GeneralNodeRelation(getConnection(), 
		    		LimitOp.swapLimits(r.getBaseTabular()), r.getBindingMaker());
		}
		return fromNodeRelation(r);
	}
}