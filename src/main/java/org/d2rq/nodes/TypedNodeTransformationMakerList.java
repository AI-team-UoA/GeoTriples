package org.d2rq.nodes;

import java.util.ArrayList;
import java.util.List;

import org.d2rq.db.ResultRow;
import org.d2rq.nodes.TypedNodeMaker.NodeType;
import org.d2rq.r2rml.ConstantIRI;
import org.d2rq.values.ValueMaker;
import org.d2rq.vocab.GEOMETRY_FUNCTIONS;
import org.d2rq.vocab.STRING_FUNCTIONS;

import com.hp.hpl.jena.graph.Node;
import com.vividsolutions.jts.geom.Geometry;

import eu.linkedeodata.geotriples.GTransormationFunctions;
import eu.linkedeodata.geotriples.GeneralConnection;


/**
 * A {@link NodeMaker} that produces nodes from an underlying
 * {@link ValueMaker} according to a {@link NodeType}.
 * 
 * TODO: isUnique() should probably not be stored here, but derived from unique key information in the underlying table(s). d2rq:containsDuplicates should be treated as asserting a unique key.
 * 
 * @author Dimitrianos Savva (dimis@di.uoa.gr)
 */
public class TypedNodeTransformationMakerList extends TypedNodeMaker {
		
	public TypedNodeTransformationMakerList(NodeType nodeType, ValueMaker valueMaker) {
		super(nodeType, valueMaker);
	}
	@Override
	public Node makeNode(ResultRow tuple) {
		String value = this.valueMaker.makeValue(tuple);
		if (value == null) {
			return null;
		}
		return this.nodeType.makeNode(value);
	}
	
@Override
	public void accept(NodeMakerVisitor visitor) {
	super.accept(visitor);
		//visitor.visit(this); //DIMIS FIX ME NOW!!!!!!!
		System.out.print("visit?");
	}
}
