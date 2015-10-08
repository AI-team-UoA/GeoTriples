package eu.linkedeodata.geotriples;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.io.gml2.GMLWriter;

public final class GTransormationFunctions {
	private static WKTWriter wkt_writer = new WKTWriter();
	private static GMLWriter gml_writer = new GMLWriter();
	
	public static String centroidx(Geometry the_geom)
	{
		return String.valueOf(the_geom.getCentroid().getX());
	}
	public static String centroidy(Geometry the_geom)
	{
		return String.valueOf(the_geom.getCentroid().getY());
	}
	public static String length(Geometry the_geom)
	{
		return String.valueOf(the_geom.getLength());
	}
	public static String area(Geometry the_geom)
	{
		return String.valueOf(the_geom.getArea());
	}
	public static String isEmpty(Geometry the_geom)
	{
		return String.valueOf(the_geom.isEmpty());
	}
	public static String isSimple(Geometry the_geom)
	{
		return String.valueOf(the_geom.isSimple());
	}
	public static String is3D(Geometry the_geom)
	{
		return String.valueOf(the_geom.getDimension()==3);
	}
	public static String spatialDimension(Geometry the_geom)
	{
		return String.valueOf(the_geom.getDimension());
	}
	
	public static String dimension(Geometry the_geom)
	{
		return String.valueOf(the_geom.getCoordinates().length);
	}
	public static String coordinateDimension(Geometry the_geom)
	{
		return String.valueOf(the_geom.getCoordinates().length);
	}
	
	
	
	public static String asWKT(Geometry the_geom,CoordinateReferenceSystem coordinatereferencesystem)
	{
		String crs = org.geotools.gml2.bindings.GML2EncodingUtils
				.epsgCode(coordinatereferencesystem);
		if (crs == null) {
			crs = "" + Config.EPSG_CODE + "";
		}
		return String.valueOf("<http://www.opengis.net/def/crs/EPSG/0/"+crs+ "> "+wkt_writer.write(the_geom));
	}
	
	public static String asWKT(Geometry the_geom,String coordinatereferencesystem)
	{
		String crs = coordinatereferencesystem;
		return String.valueOf("<http://www.opengis.net/def/crs/EPSG/0/"+crs+ "> "+wkt_writer.write(the_geom));
	}
	public static String asGML(Geometry the_geom,String coordinatereferencesystem)
	{
		String crs = coordinatereferencesystem;
		gml_writer
		.setSrsName("http://www.opengis.net/def/crs/EPSG/0/"
				+ crs);
		return String.valueOf(gml_writer.write(the_geom).replaceAll("\n", " "));
	}
	public static String asGML(Geometry the_geom,CoordinateReferenceSystem coordinatereferencesystem)
	{
		String crs = org.geotools.gml2.bindings.GML2EncodingUtils
				.epsgCode(coordinatereferencesystem);
		if (crs == null) {
			crs = "" + Config.EPSG_CODE + "";
		}
		gml_writer
		.setSrsName("http://www.opengis.net/def/crs/EPSG/0/"
				+ crs);
		return String.valueOf(gml_writer.write(the_geom).replaceAll("\n", " "));
	}
	public static String hasSerialization(Geometry the_geom,CoordinateReferenceSystem coordinatereferencesystem)
	{
		String crs = org.geotools.gml2.bindings.GML2EncodingUtils
				.epsgCode(coordinatereferencesystem);
		return String.valueOf("<http://www.opengis.net/def/crs/EPSG/0/"+crs+ "> "+wkt_writer.write(the_geom));
	}
	public static String hasSerialization(Geometry the_geom,String coordinatereferencesystem)
	{
		String crs = coordinatereferencesystem;
		return String.valueOf("<http://www.opengis.net/def/crs/EPSG/0/"+crs+ "> "+wkt_writer.write(the_geom));
	}
	
	//strdf WKT transformation function
	public static String strdfWKT(Geometry the_geom, CoordinateReferenceSystem coordinatereferencesystem)
	{
		String crs = org.geotools.gml2.bindings.GML2EncodingUtils
				.epsgCode(coordinatereferencesystem);
		if (crs == null) {
			crs = "" + Config.EPSG_CODE + "";
		}
		return String.valueOf(wkt_writer.write(the_geom) + "; <http://www.opengis.net/def/crs/EPSG/0/"+crs+ ">");
	}
	
	public static String strdfWKT(Geometry the_geom,String coordinatereferencesystem)
	{
		String crs = coordinatereferencesystem;
		return String.valueOf(wkt_writer.write(the_geom) + "; <http://www.opengis.net/def/crs/EPSG/0/"+crs+ ">");
	}
	
	
	
	
	
	
	//GDAL stuff
	
	public static String centroidx(org.gdal.ogr.Geometry the_geom)
	{
		return "";
		//return String.valueOf(the_geom.getCentroid().getX());
	}
	public static String centroidy(org.gdal.ogr.Geometry the_geom)
	{
		return "";
		//return String.valueOf(the_geom.getCentroid().getY());
	}
	public static String length(org.gdal.ogr.Geometry the_geom)
	{
		return "";
		//return String.valueOf(the_geom.getLength());
	}
	public static String area(org.gdal.ogr.Geometry the_geom)
	{
		return "";
		//return String.valueOf(the_geom.getArea());
	}
	public static String isEmpty(org.gdal.ogr.Geometry the_geom)
	{
		return String.valueOf(the_geom.IsEmpty());
	}
	public static String isSimple(org.gdal.ogr.Geometry the_geom)
	{
		return String.valueOf(the_geom.IsSimple());
	}
	public static String is3D(org.gdal.ogr.Geometry the_geom)
	{
		return String.valueOf(the_geom.GetDimension()==3);
	}
	public static String spatialDimension(org.gdal.ogr.Geometry the_geom)
	{
		return String.valueOf(the_geom.GetDimension());
	}
	
	public static String dimension(org.gdal.ogr.Geometry the_geom)
	{
		return String.valueOf(the_geom.GetDimension());
	}
	public static String coordinateDimension(org.gdal.ogr.Geometry the_geom)
	{
		return String.valueOf(the_geom.GetCoordinateDimension());
	}
	
	
	
	public static String asWKT(org.gdal.ogr.Geometry the_geom,CoordinateReferenceSystem coordinatereferencesystem)
	{
		String crs = org.geotools.gml2.bindings.GML2EncodingUtils
				.epsgCode(coordinatereferencesystem);
		if (crs == null) {
			crs = "" + Config.EPSG_CODE + "";
		}
		return String.valueOf("<http://www.opengis.net/def/crs/EPSG/0/"+crs+ "> "+the_geom.ExportToWkt());
	}
	
	public static String asWKT(org.gdal.ogr.Geometry the_geom,String coordinatereferencesystem)
	{
		String crs = coordinatereferencesystem;
		return String.valueOf("<http://www.opengis.net/def/crs/EPSG/0/"+crs+ "> "+the_geom.ExportToWkt());
	}
	public static String asGML(org.gdal.ogr.Geometry the_geom,String coordinatereferencesystem)
	{
		String crs = coordinatereferencesystem;
		gml_writer
		.setSrsName("http://www.opengis.net/def/crs/EPSG/0/"
				+ crs);
		return String.valueOf(the_geom.ExportToGML().replaceAll("\n", " "));
	}
	public static String asGML(org.gdal.ogr.Geometry the_geom,CoordinateReferenceSystem coordinatereferencesystem)
	{
		String crs = org.geotools.gml2.bindings.GML2EncodingUtils
				.epsgCode(coordinatereferencesystem);
		if (crs == null) {
			crs = "" + Config.EPSG_CODE + "";
		}
		gml_writer
		.setSrsName("http://www.opengis.net/def/crs/EPSG/0/"
				+ crs);
		return String.valueOf(the_geom.ExportToGML().replaceAll("\n", " "));
	}
	public static String hasSerialization(org.gdal.ogr.Geometry the_geom,CoordinateReferenceSystem coordinatereferencesystem)
	{
		String crs = org.geotools.gml2.bindings.GML2EncodingUtils
				.epsgCode(coordinatereferencesystem);
		return String.valueOf("<http://www.opengis.net/def/crs/EPSG/0/"+crs+ "> "+the_geom.ExportToWkt());
	}
	public static String hasSerialization(org.gdal.ogr.Geometry the_geom,String coordinatereferencesystem)
	{
		String crs = coordinatereferencesystem;
		return String.valueOf("<http://www.opengis.net/def/crs/EPSG/0/"+crs+ "> "+the_geom.ExportToWkt());
	}
	
	//strdf WKT transformation function
	public static String strdfWKT(org.gdal.ogr.Geometry the_geom, CoordinateReferenceSystem coordinatereferencesystem)
	{
		String crs = org.geotools.gml2.bindings.GML2EncodingUtils
				.epsgCode(coordinatereferencesystem);
		if (crs == null) {
			crs = "" + Config.EPSG_CODE + "";
		}
		return String.valueOf(the_geom.ExportToWkt() + "; <http://www.opengis.net/def/crs/EPSG/0/"+crs+ ">");
	}
	
	public static String strdfWKT(org.gdal.ogr.Geometry the_geom,String coordinatereferencesystem)
	{
		String crs = coordinatereferencesystem;
		return String.valueOf(the_geom.ExportToWkt() + "; <http://www.opengis.net/def/crs/EPSG/0/"+crs+ ">");
	}
}
