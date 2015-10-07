package be.ugent.mmlab.rml.function;

import java.util.HashMap;

/***************************************************************************
*
* @author: dimis (dimis@di.uoa.gr)
* 
****************************************************************************/
public class Config {
	public static String EPSG_CODE = "4326";
	public static boolean useGML3=true;
	public static boolean useGML2=false;
	public static boolean useKML_22=false;
	public static boolean useDGALLibrary=false;
	public static boolean useOldDBProcessor=false;
	public static final HashMap<String, String> user_namespaces=new HashMap<String, String>();
	public static final String GEOTRIPLES_AUTO_ID = "GeoTriplesID";
	public static void setGML3() {
		useGML3=true;
		useGML2=false;
		useKML_22=false;
	}
	public static void setGML2() {
		useGML3=false;
		useGML2=true;
		useKML_22=false;
	}
	public static void setKML() {
		useGML3=false;
		useGML2=false;
		useKML_22=true;
	}
	public static void setGDAL() {
		useDGALLibrary=true;
	}
	public static void setOldDBProcessor() {
		useOldDBProcessor=true;
	}
}
