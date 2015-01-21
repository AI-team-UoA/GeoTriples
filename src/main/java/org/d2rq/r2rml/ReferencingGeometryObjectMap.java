package org.d2rq.r2rml;

import java.util.HashSet;
import java.util.Set;

import org.d2rq.vocab.RR;
import org.d2rq.vocab.RRX;

import com.hp.hpl.jena.rdf.model.Resource;

public class ReferencingGeometryObjectMap extends ReferencingObjectMap {

	
	private Set<Resource> geometryFunctions= new HashSet<Resource>();;
	private ConstantIRI datatype = null;
	public ConstantIRI getDatatype() {
		return datatype;
	}

	public void setDatatype(ConstantIRI datatype) {
		this.datatype = datatype;
	}
	
	public ComponentType getType() {
		return ComponentType.REF_OBJECT_MAP;
	}
	
	@Override
	public void accept(MappingVisitor visitor) {
		super.accept(visitor);
		visitor.visitTermProperty(RR.datatype, datatype); //04/06/2014
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
