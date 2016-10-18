/**
 * @author Dimitrianos Savva National and Kapodistrian University of Athens
 * @author Giannis Vlachopoulos National and Kapodistrian University of Athens
 */
package eu.linkedeodata.geotriples.kml;

import org.d2rq.D2RQException;

import eu.linkedeodata.geotriples.GeneralConnection;
import eu.linkedeodata.geotriples.GeneralSystemLoader;
import eu.linkedeodata.geotriples.gui.RecipeMapping;

public class KMLSystemLoader extends GeneralSystemLoader {
	
	public KMLSystemLoader() {
		super();
	}

	public KMLSystemLoader(RecipeMapping receipt, String guimapping) {
		super(receipt, guimapping);
	}
	
	public GeneralConnection getConnection() {
		if (connection == null) {
			if (sourceURL == null) {
				throw new D2RQException("No kml file provided");
			}
			connection = new KMLConnection(sourceURL);
		}
		return connection;
	}
}
