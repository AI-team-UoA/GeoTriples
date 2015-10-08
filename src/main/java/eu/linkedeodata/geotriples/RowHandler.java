package eu.linkedeodata.geotriples;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.io.gml2.GMLWriter;

public class RowHandler {
	public static void handleGeometry(GeneralResultRow row,Geometry g,String crs)
	{
		if (crs == null) {
			System.err.println("No Coordinate Reference System specified. Aborting...");
			System.exit(-1);
		}
		WKTWriter wkt_writer = new WKTWriter();
		GMLWriter gml_writer = new GMLWriter();
		if (g.getClass().equals(
				com.vividsolutions.jts.geom.Point.class)) {
			Point geometry = (com.vividsolutions.jts.geom.Point) g;
			row.addPair("isEmpty", geometry.isEmpty());
			row.addPair("isSimple", geometry.isSimple());
			row.addPair("dimension",
					geometry.getCoordinates().length);
			row.addPair("coordinateDimension",
					geometry.getCoordinates().length);
			row.addPair("spatialDimension", geometry.getDimension()); // spatialdimension
			row.addPair("asWKT",
					"<http://www.opengis.net/def/crs/EPSG/0/" + crs
							+ ">" + wkt_writer.write(geometry));
			/*row.addPair("hasSerialization",
					"<http://www.opengis.net/def/crs/EPSG/0/" + crs
							+ ">" + wkt_writer.write(geometry));*/
			// newrow.addPair("hasSerialization",
			// wkt_writer.write(geometry));
			gml_writer.setSrsName(crs);
			row.addPair("asGML", gml_writer.write(geometry)
					.replaceAll("\n", " "));
			row.addPair("is3D", geometry.getDimension() == 3);
		} else {
			Geometry geometry = (Geometry) g;
			row.addPair("isEmpty", geometry.isEmpty());
			row.addPair("isSimple", geometry.isSimple());
			row.addPair("dimension",
					geometry.getCoordinates().length);
			row.addPair("coordinateDimension",
					geometry.getCoordinates().length);
			row.addPair("spatialDimension", geometry.getDimension()); // spatialdimension
			row.addPair("asWKT",
					"<http://www.opengis.net/def/crs/EPSG/0/" + crs
							+ ">" + wkt_writer.write(geometry));
			/*row.addPair("hasSerialization",
					"<http://www.opengis.net/def/crs/EPSG/0/" + crs
							+ ">" + wkt_writer.write(geometry));*/
			// newrow.addPair("hasSerialization",
			// wkt_writer.write(geometry));
			gml_writer
					.setSrsName("http://www.opengis.net/def/crs/EPSG/0/"
							+ crs);
			row.addPair("asGML", gml_writer.write(geometry)
					.replaceAll("\n", " "));
			row.addPair("is3D", geometry.getDimension() == 3);
		}
	}
}
