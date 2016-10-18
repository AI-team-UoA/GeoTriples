package eu.linkedeodata.geotriples;

import org.d2rq.db.types.DataType;

public class WKTLiteral extends DataType {

	public WKTLiteral(String name) {
		super(name);
	}
	
	@Override
	public String rdfType() {
		return "http://www.opengis.net/ont/geosparql#wktLiteral";
	}

}
