package eu.linkedeodata.shp;

import eu.linkedeodata.core.SHPTest;

public class FieldBoundariesSimple extends SHPTest {

	public FieldBoundariesSimple() {
		super("resources/test/shp/FieldBoundariesSchallermayer.shp", 
				"resources/test/shp/FieldBoundaries-simple.ttl", 
				"http://data.linkedeodata.eu/field-boundaries",
				"resources/test/shp/FieldBoundariesSchallermayer.nt");
	}
}
