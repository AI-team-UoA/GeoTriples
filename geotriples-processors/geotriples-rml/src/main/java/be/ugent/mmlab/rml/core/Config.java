package be.ugent.mmlab.rml.core;

import java.util.HashMap;

/**
 * here lie the default parameters of the application. For instance, which epsg code should be used if non is specified explicitly?
 */
public class Config {
	public static final String GEOTRIPLES_AUTO_ID = "GeoTriplesID";
    public static int EPSG_CODE = 4326;
	public static boolean GEOMETRY = false;
	public static String VOCABULARY = "GeoSPARQL";
	
	
	public static HashMap<String,String> variables=new HashMap<>();
}
