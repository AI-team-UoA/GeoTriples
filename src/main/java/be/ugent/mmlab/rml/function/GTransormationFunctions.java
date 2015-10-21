package be.ugent.mmlab.rml.function;

/***************************************************************************
*
* @author: dimis (dimis@di.uoa.gr)
* 
****************************************************************************/
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.io.gml2.GMLWriter;

public final class GTransormationFunctions {
	private static WKTWriter wkt_writer = new WKTWriter();
	private static GMLWriter gml_writer = new GMLWriter();

	public static String centroidx(Geometry the_geom) {
		return String.valueOf(the_geom.getCentroid().getX());
	}

	public static String centroidy(Geometry the_geom) {
		return String.valueOf(the_geom.getCentroid().getY());
	}

	public static String length(Geometry the_geom) {
		return String.valueOf(the_geom.getLength());
	}

	public static String area(Geometry the_geom) {
		return String.valueOf(the_geom.getArea());
	}

	public static String isEmpty(Geometry the_geom) {
		return String.valueOf(the_geom.isEmpty());
	}

	public static String isSimple(Geometry the_geom) {
		return String.valueOf(the_geom.isSimple());
	}

	public static String is3D(Geometry the_geom) {
		return String.valueOf(the_geom.getDimension() == 3);
	}

	public static String spatialDimension(Geometry the_geom) {
		return String.valueOf(the_geom.getDimension());
	}

	public static String dimension(Geometry the_geom) {
		// System.out.println("mpike kai tha parei timi");
		// String result=String.valueOf(the_geom.getCoordinates().length);
		// System.out.println("to result einai "+result);
		// return result;
		return String.valueOf(the_geom.getDimension());
	}

	public static String coordinateDimension(Geometry the_geom) {
		return String.valueOf(the_geom.getCoordinates().length);
	}

	public static String asWKT(Geometry the_geom, CoordinateReferenceSystem coordinatereferencesystem) {
		String crs = org.geotools.gml2.bindings.GML2EncodingUtils.epsgCode(coordinatereferencesystem);
		if (crs == null) {
			crs = "" + Config.EPSG_CODE + "";
		}
		return String.valueOf("<http://www.opengis.net/def/crs/EPSG/0/" + crs + "> " + wkt_writer.write(the_geom));
	}

	public static String asWKT(Geometry the_geom, String coordinatereferencesystem) {
		String crs = coordinatereferencesystem;
		return String.valueOf("<http://www.opengis.net/def/crs/EPSG/0/" + crs + "> " + wkt_writer.write(the_geom));
	}

	public static String asGML(Geometry the_geom, String coordinatereferencesystem) {
		String crs = coordinatereferencesystem;
		gml_writer.setSrsName("http://www.opengis.net/def/crs/EPSG/0/" + crs);
		return String.valueOf(gml_writer.write(the_geom).replaceAll("\n", " "));
	}

	public static String asGML(Geometry the_geom, CoordinateReferenceSystem coordinatereferencesystem) {
		String crs = org.geotools.gml2.bindings.GML2EncodingUtils.epsgCode(coordinatereferencesystem);
		if (crs == null) {
			crs = "" + Config.EPSG_CODE + "";
		}
		gml_writer.setSrsName("http://www.opengis.net/def/crs/EPSG/0/" + crs);
		return String.valueOf(gml_writer.write(the_geom).replaceAll("\n", " "));
	}

	public static String hasSerialization(Geometry the_geom, CoordinateReferenceSystem coordinatereferencesystem) {
		String crs = org.geotools.gml2.bindings.GML2EncodingUtils.epsgCode(coordinatereferencesystem);
		return String.valueOf("<http://www.opengis.net/def/crs/EPSG/0/" + crs + "> " + wkt_writer.write(the_geom));
	}

	public static String hasSerialization(Geometry the_geom, String coordinatereferencesystem) {
		String crs = coordinatereferencesystem;
		return String.valueOf("<http://www.opengis.net/def/crs/EPSG/0/" + crs + "> " + wkt_writer.write(the_geom));
	}

	public static String contains(Geometry the_geom1, Geometry the_geom2) {

		return ((Boolean) the_geom1.contains(the_geom2)).toString();
	}

	public static String distance(Geometry the_geom1, Geometry the_geom2) {

		return ((Double) (the_geom1.distance(the_geom2))).toString();
	}

	public static String intersects(Geometry the_geom1, Geometry the_geom2) {

		return ((Boolean) the_geom1.intersects(the_geom2)).toString();
	}

	public static String greaterThan(Double arg1, Double arg2) {

		return ((Boolean) (arg1 >= arg2)).toString();
	}

	// new
	public static String disjoint(Geometry the_geom1, Geometry the_geom2) {

		return ((Boolean) the_geom1.disjoint(the_geom2)).toString();
	}

	public static String equals(Geometry the_geom1, Geometry the_geom2) {

		return ((Boolean) the_geom1.equals(the_geom2)).toString();
	}

	public static String touches(Geometry the_geom1, Geometry the_geom2) {

		return ((Boolean) the_geom1.touches(the_geom2)).toString();
	}

	public static String crosses(Geometry the_geom1, Geometry the_geom2) {

		return ((Boolean) the_geom1.crosses(the_geom2)).toString();
	}

	public static String within(Geometry the_geom1, Geometry the_geom2) {

		return ((Boolean) the_geom1.within(the_geom2)).toString();
	}
	
	public static String overlaps(Geometry the_geom1, Geometry the_geom2) {

		return ((Boolean) the_geom1.overlaps(the_geom2)).toString();
	}

}
