package eu.linkedeodata.geotriples;

import java.util.HashMap;

import org.d2rq.db.schema.TableName;


import eu.linkedeodata.geotriples.gui.ColumnReceipt;

/**
 * here lie the default parameters of the application. For instance, which epsg code should be used if non is specified explicitly?
 */
public class Config {
	public static int EPSG_CODE = 4326;
	public static boolean GEOMETRY = false;
	public static String VOCABULARY = "GeoSPARQL";
	
	/**
	 * as given by the gui so that the mapping generator can decide which tables and columns to include/exclude and which predicates to assign per column
	 */
	public static java.util.Map<TableName, java.util.List<ColumnReceipt>> tablesAndColumns;
	
	/**
	 * as given by the gui so that the mapping generator can decide which what class the instances of each table have
	 */
	public static java.util.Map<TableName, String> tablesAndClasses;
	public static HashMap<String,String> variables=new HashMap<>();
}
