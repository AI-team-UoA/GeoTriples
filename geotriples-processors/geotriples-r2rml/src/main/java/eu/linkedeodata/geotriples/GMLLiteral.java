package eu.linkedeodata.geotriples;

import org.d2rq.db.types.DataType;

public class GMLLiteral extends DataType {

	public GMLLiteral(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String rdfType() {
		return "http://opengis.net/ont/geosparql#gmlLiteral";
	}

}
