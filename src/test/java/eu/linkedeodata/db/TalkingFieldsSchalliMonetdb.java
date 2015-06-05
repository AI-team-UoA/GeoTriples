package eu.linkedeodata.db;

import eu.linkedeodata.core.DBTest;

public class TalkingFieldsSchalliMonetdb extends DBTest {
	public TalkingFieldsSchalliMonetdb() {
		super("talkingfieldsschalli", "monetdb", "monetdb", false, 
				"http://data.linkedeodata.eu/talking-fields", "resources/test/db/TalkingFields-Schalli-monetdb.ttl",
				"resources/test/db/TalkingFields-Schalli-monetdb.nt");
	}
}
