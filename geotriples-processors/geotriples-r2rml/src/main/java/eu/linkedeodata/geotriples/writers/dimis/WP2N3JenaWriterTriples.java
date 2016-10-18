package eu.linkedeodata.geotriples.writers.dimis;

import java.util.Map;

import com.hp.hpl.jena.n3.N3JenaWriterTriples;

public class WP2N3JenaWriterTriples extends N3JenaWriterTriples {
	static Map<String, String> thiswellKnownPropsMap;
	public WP2N3JenaWriterTriples()
	{
		super();
	}
	public WP2N3JenaWriterTriples(boolean allowTripleQuotedStrngs,Map<String, String> wellKnownPropsMapTrtl) {
		super();
		allowTripleQuotedStrings=allowTripleQuotedStrngs;
		wellKnownPropsMap = wellKnownPropsMapTrtl;
		thiswellKnownPropsMap=wellKnownPropsMap;
	}
	public static Map<String, String> getWellKnownPropsMap()
	{
		return thiswellKnownPropsMap;
	}
}
