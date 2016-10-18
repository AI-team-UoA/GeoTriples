package org.d2rq.r2rml;

import java.util.HashSet;
import java.util.Set;

import org.d2rq.vocab.RRX;

import com.hp.hpl.jena.rdf.model.Resource;

public class ReferencingGeometryAttributes extends ReferencingObjectMap {
	private Resource parentTriplesMap = null;
	private Set<Resource> joinConditions = new HashSet<Resource>();
	
	private Set<Resource> geometryFunctions= new HashSet<Resource>();;
	
	public ComponentType getType() {
		return ComponentType.REF_OBJECT_MAP;
	}
	
	public void setParentTriplesMap(Resource parentTriplesMap) {
		this.parentTriplesMap = parentTriplesMap;
	}
	
	public Resource getParentTriplesMap() {
		return parentTriplesMap;
	}
	
	public Set<Resource> getJoinConditions() {
		return joinConditions;
	}
	
	@Override
	public void accept(MappingVisitor visitor) {
		super.accept(visitor);
		for (Resource function: geometryFunctions) {
			//visitor.visitComponentProperty(RRX.transformation, function, ComponentType.FUNCTION);
			visitor.visitComponentProperty(RRX.transformation, function, ComponentType.FUNCTION);
		}
	}

	public Set<Resource> getGeometryFunctions() {
		return geometryFunctions;
	}

	public void setGeometryFunctions(Set<Resource> geometryFunctions) {
		this.geometryFunctions = geometryFunctions;
	}
}
