package eu.linkedeodata.geotriples.writers.dimis;

import java.util.Map;

import com.hp.hpl.jena.n3.N3JenaWriterCommon;

public class WP2N3JenaWriterCommon extends N3JenaWriterCommon {
	static Map<String, String> thiswellKnownPropsMap;
	public WP2N3JenaWriterCommon()
	{
		super();
	}
	public WP2N3JenaWriterCommon(boolean allowTripleQuotedStrngs,Map<String, String> wellKnownPropsMapTrtl) {
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
