package eu.linkedeodata.db;

import eu.linkedeodata.core.DBTest;

public class FieldBoundariesSchallermayer extends DBTest {

	public FieldBoundariesSchallermayer() {
		super("fieldboundariesschallermayer", "postgres", "postgres", true, 
				"http://data.linkedeodata.eu/talking-fields", "resources/test/db/FieldBoundariesSchallermayer.ttl",
				"resources/test/db/FieldBoundariesSchallermayer.nt");
	}
}
