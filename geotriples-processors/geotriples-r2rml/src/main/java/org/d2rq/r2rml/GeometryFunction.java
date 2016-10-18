package org.d2rq.r2rml;

import java.util.HashMap;
import java.util.Map;

import org.d2rq.vocab.RRX;

import com.hp.hpl.jena.rdf.model.Resource;



public class GeometryFunction extends MappingComponent {
	private ConstantIRI function = null;
	private ColumnNameR2RML parent = null;
	private final Map<Resource,GeometryParametersTerms> objectMaps = new HashMap<Resource,GeometryParametersTerms>();
	
	public ComponentType getType() {
		return ComponentType.FUNCTION;
	}
	
	public void setFunction(ConstantIRI function) {
		this.function = function;
	}
	
	public ConstantIRI getFunction() {
		return function;
	}
	
	public void setParent(ColumnNameR2RML parent) {
		this.parent = parent;
	}
	
	public ColumnNameR2RML getParent() {
		return parent;
	}

	@Override
	public void accept(MappingVisitor visitor) {
		visitor.visitComponent(this);
		visitor.visitTermProperty(RRX.function, function);
		//visitor.visitTermProperty(RR.parent, parent);
		for (Resource objectMap: objectMaps.keySet()) {
			visitor.visitComponentProperty(RRX.argumentMap, objectMap, 
					ComponentType.OBJECT_MAP, ComponentType.REF_OBJECT_MAP);
		}
	}
	public void acceptAs(MappingVisitor visitor, GeometryParametersTerms.Position position,ConstantIRI datatype) {
		visitor.visitComponent(this, position, datatype);
		/*if (position == Position.SUBJECT_MAP) {
			for (ConstantIRI iri: classes) {
				visitor.visitTermProperty(RR.class_, iri);
			}
			for (Resource graphMap: graphMaps) {
				visitor.visitComponentProperty(RR.graphMap, graphMap, ComponentType.GRAPH_MAP);
			}
			for (ConstantShortcut graph: graphs) {
				visitor.visitTermProperty(RR.graph, graph);
			}
		}*/
		/*for(Resource item:objectMaps.keySet())
		{
			GeometryParametersTerms visited=objectMaps.get(item);
			visited.acceptAs(visitor,position);
		}*/
		
	}

	public Map<Resource,GeometryParametersTerms> getObjectMaps() {
		return objectMaps;
	}
}
