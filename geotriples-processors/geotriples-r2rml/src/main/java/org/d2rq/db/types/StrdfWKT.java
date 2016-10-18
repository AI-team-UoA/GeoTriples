package org.d2rq.db.types;

public class StrdfWKT extends DataType {

	public StrdfWKT(String name) {
		super(name);
	}
	
	@Override
	public String rdfType() {
		return "http://strdf.di.uoa.gr/ontology#WKT";
	}
}
