package eu.linkedeodata.geotriples.writers.dimis;


public class WP2N3TurtleJenaWriter extends N3WP2Writer{
	public WP2N3TurtleJenaWriter() {
		super(WP2N3JenaWriterPP.getWellKnownPropsMap(),true) ;
//      if ( writer.getPropValue("usePropertySymbols") == null )
//          writer.useWellKnownPropertySymbols = false ;
      // Only allow "a" for rdf:type.
      //dimisremove//writer.wellKnownPropsMap = WP2N3JenaWriterPP.wellKnownPropsMapTurtle ;
      //dimisremove//writer.allowTripleQuotedStrings = true ;
	}
}
