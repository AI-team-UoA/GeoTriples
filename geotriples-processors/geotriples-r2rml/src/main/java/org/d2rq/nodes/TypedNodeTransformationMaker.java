package org.d2rq.nodes;

import java.util.List;

import org.d2rq.db.ResultRow;
import org.d2rq.r2rml.ConstantIRI;
import org.d2rq.values.ValueMaker;
import org.d2rq.vocab.GEOMETRY_FUNCTIONS;

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
public class TypedNodeTransformationMaker extends TypedNodeMaker {
	
	private ConstantIRI gfunction;
	private GeneralConnection connection;
	
	public TypedNodeTransformationMaker(NodeType nodeType, ValueMaker valueMaker,ConstantIRI function,GeneralConnection con) {
		super(nodeType, valueMaker);
		gfunction=function;
		connection=con;
	}
	@Override
	public Node makeNode(ResultRow tuple) {
		Object value = this.valueMaker.makeValueObject(tuple);
		if (value == null) {
			return null;
		}
		String function=gfunction.toString();
		if (function.equals(GEOMETRY_FUNCTIONS.asWKT.toString())) {
			return this.nodeType.makeNode(GTransormationFunctions.asWKT((Geometry)value,connection.getCRS()));
		}
		else if(function.equals(GEOMETRY_FUNCTIONS.isSimple.toString())) {
			return this.nodeType.makeNode(GTransormationFunctions.isSimple((Geometry)value));
		}
		else if(function.equals(GEOMETRY_FUNCTIONS.hasSerialization.toString())) {
			return this.nodeType.makeNode(GTransormationFunctions.hasSerialization((Geometry)value,connection.getCRS()));
		}
		else if(function.equals(GEOMETRY_FUNCTIONS.asGML.toString())) {
			return this.nodeType.makeNode(GTransormationFunctions.asGML((Geometry)value,connection.getCRS()));
		}
		else if(function.equals(GEOMETRY_FUNCTIONS.isEmpty.toString())) {
			return this.nodeType.makeNode(GTransormationFunctions.isEmpty((Geometry)value));
		}
		else if(function.equals(GEOMETRY_FUNCTIONS.is3D.toString())) {
			return this.nodeType.makeNode(GTransormationFunctions.is3D((Geometry)value));
		}
		else if(function.equals(GEOMETRY_FUNCTIONS.spatialDimension.toString())) {
			return this.nodeType.makeNode(GTransormationFunctions.spatialDimension((Geometry)value));
		}
		else if(function.equals(GEOMETRY_FUNCTIONS.dimension.toString())) {
			return this.nodeType.makeNode(GTransormationFunctions.dimension((Geometry)value));
		}
		else if(function.equals(GEOMETRY_FUNCTIONS.coordinateDimension.toString())) {
			return this.nodeType.makeNode(GTransormationFunctions.coordinateDimension((Geometry)value));
		}
		else if(function.equals(GEOMETRY_FUNCTIONS.area.toString())) {
			return this.nodeType.makeNode(GTransormationFunctions.area((Geometry)value));
		}
		else if(function.equals(GEOMETRY_FUNCTIONS.length.toString())) {
			return this.nodeType.makeNode(GTransormationFunctions.length((Geometry)value));
		}
		else if(function.equals(GEOMETRY_FUNCTIONS.centroidx.toString())) {
			return this.nodeType.makeNode(GTransormationFunctions.centroidx((Geometry)value));
		}
		else if(function.equals(GEOMETRY_FUNCTIONS.centroidy.toString())) {
			return this.nodeType.makeNode(GTransormationFunctions.centroidy((Geometry)value));
		}
		/*else
		{
			return this.nodeType.makeNode(((String)value).toLowerCase());
		}*/
		else{
			try {
				throw new Exception("mple Not supported Transformation function <" + function +">");
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			return null;
		}
	}
	
@Override
	public void accept(NodeMakerVisitor visitor) {
	super.accept(visitor);
		//visitor.visit(this); //DIMIS FIX ME NOW!!!!!!!
		System.out.print("visit?");
	}
}
