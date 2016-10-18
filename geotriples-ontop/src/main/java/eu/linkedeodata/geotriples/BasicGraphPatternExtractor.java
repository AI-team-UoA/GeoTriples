/*
 * This file is part of RDF Federator.
 * Copyright 2011 Olaf Goerlitz
 * 
 * RDF Federator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * RDF Federator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with RDF Federator.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * RDF Federator uses libraries from the OpenRDF Sesame Project licensed 
 * under the Aduna BSD-style license. 
 */
package eu.linkedeodata.geotriples;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * Extracts all Basic Graph Patterns from a query model.
 * TODO: this is far too complicated - better traverse the tree depth first
 *       and recursively check if the last visited child is a valid BGP.
 * 
 * @author Olaf Goerlitz
 */
public class BasicGraphPatternExtractor extends QueryModelVisitorBase<RuntimeException> {

	private TupleExpr lastBGPNode;

	private List<TupleExpr> bgpList = new ArrayList<TupleExpr>();

	/**
	 * Prevents creation of extractor classes.
	 * The static process() method must be used instead.
	 */
	private BasicGraphPatternExtractor() {}

	public static List<TupleExpr> process(QueryModelNode node) {
		BasicGraphPatternExtractor ex = new BasicGraphPatternExtractor();
		node.visit(ex);
		return ex.bgpList;
	}

	// --------------------------------------------------------------

	/**
	 * Handles binary nodes with potential BGPs as children (e.g. union, left join).
	 */
	@Override
	public void meetBinaryTupleOperator(BinaryTupleOperator node) throws RuntimeException {

		for (TupleExpr expr : new TupleExpr[] { node.getLeftArg(), node.getRightArg() }) {
			expr.visit(this);
			if (lastBGPNode != null) {
				// child is a BGP node but this node is not
				this.bgpList.add(lastBGPNode);
				lastBGPNode = null;
			}
		}
	}

	/**
	 * Handles unary nodes with a potential BGP as child (e.g. projection).
	 */
	@Override
	public void meetUnaryTupleOperator(UnaryTupleOperator node) throws RuntimeException {

		node.getArg().visit(this);

		if (lastBGPNode != null) {
			// child is a BGP node but this node is not
			this.bgpList.add(lastBGPNode);
			lastBGPNode = null;
		}
	}

	/**
	 * Handles statement patterns which are always a valid BGP node.
	 */
	@Override
	public void meet(StatementPattern node) throws RuntimeException {
		this.lastBGPNode = node;
	}

	@Override
	public void meet(Filter filter) throws RuntimeException {

		// visit the filter argument but ignore the filter condition
		filter.getArg().visit(this);
		//System.out.println("Filter is: " +filter.getCondition());
		if (lastBGPNode != null) {
			// child is a BGP as well as the filter
			lastBGPNode = filter;
		}
	}

	@Override
	public void meet(Join join) throws RuntimeException {

		boolean valid = true;

		// visit join arguments and check that all are valid BGPS
		for (TupleExpr expr : new TupleExpr[] { join.getLeftArg(), join.getRightArg() }) {
			expr.visit(this);
			if (lastBGPNode == null) {
				// child is not a BGP -> join is not a BGP
				valid = false;
			} else {
				if (!valid) {
					// last child is a BGP but another child was not
					this.bgpList.add(lastBGPNode);
					lastBGPNode = null;
				}
			}
		}
		if (valid)
			lastBGPNode = join;
	}

	// --------------------------------------------------------------

	//	@Override
	//	public void meet(Filter filter) throws RuntimeException {
	//		
	//		// visit the filter argument but ignore the filter condition
	//		filter.getArg().visit(this);
	//		
	//		// TODO: need to check for valid child node
	//		
	//		// check if the parent node is a valid BGP node (Join or Filter)
	//		// because invalid BGP (parent) nodes are not visited
	//		QueryModelNode parent = filter.getParentNode();
	//		if (parent instanceof Join || parent instanceof Filter) {
	////			this.filters.add(filter.getCondition());
	//			return;
	//		}
	//		// otherwise check if filter is a direct child of a Projection or
	//		// inside a left join expression
	//		// TODO: what if parent is Limit or OrderBy?
	//		if (parent instanceof Projection || parent instanceof MultiProjection
	//				|| parent instanceof LeftJoin || parent instanceof Order || parent instanceof Slice ) {
	////			this.filters.add(filter.getCondition());
	//		}
	////		saveBGP(filter, parent);
	//		this.bgpList.add(filter);
	//	}
	//
	//	@Override
	//	public void meet(Join join) throws RuntimeException {
	//		
	//		boolean valid = true;
	////		List<? extends TupleExpr> joinArgs = join.getArgs();
	//		TupleExpr[] joinArgs = { join.getLeftArg(), join.getRightArg()};
	//		
	//		// First check if Join is a valid BGP - only if all join arguments are valid BGPs
	//		for (TupleExpr expr : joinArgs) {
	//			if (expr instanceof Join || expr instanceof Filter || expr instanceof StatementPattern)
	//				continue;
	//			else
	//				valid = false;
	//		}
	//		
	//		// then process all join arguments but store each valid child BGPs if this join is not a valid BGP
	//		for (TupleExpr expr : joinArgs) {
	//			expr.visit(this);
	//			if (!valid && (expr instanceof Join || expr instanceof Filter || expr instanceof StatementPattern))
	////				saveBGP(expr, join);
	//				this.bgpList.add(expr);
	//		}
	//		
	//		// check if the parent node is a valid BGP node (Join or Filter)
	//		// because invalid BGP (parent) nodes are not visited
	//		QueryModelNode parent = join.getParentNode();
	//		if (valid && !(parent instanceof Join) && !(parent instanceof Filter)) {
	////			saveBGP(join, parent);
	//			this.bgpList.add(join);
	//		}
	//	}

}