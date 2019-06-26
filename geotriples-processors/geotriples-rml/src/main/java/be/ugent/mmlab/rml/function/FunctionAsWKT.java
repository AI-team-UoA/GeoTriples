package be.ugent.mmlab.rml.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.xml.sax.SAXException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

import be.ugent.mmlab.rml.core.MalformedGeometryException;
import eu.linkedeodata.geotriples.PrintTimeStats;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;
import org.locationtech.jts.io.WKTWriter;

public class FunctionAsWKT extends GeometryFunction implements Function {

	@Override
	public List<? extends Object> execute(List<? extends Object> arguments, List<? extends QLTerm> qlterms)
			throws SAXException, IOException, ParserConfigurationException, FactoryException,
			MalformedGeometryException, ParseException {
		List<String> valueList = new ArrayList<>();
		if (qlterms.get(0).equals(QLTerm.ROW_CLASS)){
			Object geom = arguments.get(0);
			if (geom instanceof  String) {
				valueList.add("<http://www.opengis.net/def/crs/EPSG/0/" + Config.EPSG_CODE + "> " + geom);
				return valueList;
			}
			else if (geom instanceof  Geometry){
				WKTWriter wkt = new WKTWriter();
				valueList.add("<http://www.opengis.net/def/crs/EPSG/0/" + Config.EPSG_CODE + "> " +  (wkt.write((Geometry) geom)));
				return valueList;

			}
		}
		else if (qlterms.get(0).equals(QLTerm.SHP_CLASS) && arguments.get(0) instanceof org.gdal.ogr.Geometry) {
			long startTime = System.nanoTime();
			org.gdal.ogr.Geometry gdalgeom=(org.gdal.ogr.Geometry )arguments.get(0);
			long endTime = System.nanoTime();
			long duration = (endTime - startTime)/1000000;  //divide by 1000000 to get milliseconds.
			PrintTimeStats.printTime("Compute Geometry", duration);
			
			startTime = System.nanoTime();
			valueList.add("<http://www.opengis.net/def/crs/EPSG/0/" + Config.EPSG_CODE + "> " + gdalgeom.ExportToWkt());
			endTime = System.nanoTime();
			duration = (endTime - startTime)/1000000;  //divide by 1000000 to get milliseconds.
			PrintTimeStats.printTime("Compute function asWKT", duration);
			
			return valueList;
		}

		long startTime = System.nanoTime();
		Geometry geometry = computeGeometry(arguments.get(0), qlterms.get(0));
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/1000000;  //divide by 1000000 to get milliseconds.
		PrintTimeStats.printTime("Compute Geometry", duration);
		
		startTime = System.nanoTime();
		valueList.add(GTransormationFunctions.asWKT((Geometry) geometry, CRS.decode("EPSG:" + Config.EPSG_CODE)));
		endTime = System.nanoTime();
		duration = (endTime - startTime)/1000000;  //divide by 1000000 to get milliseconds.
		PrintTimeStats.printTime("Compute function asWKT", duration);
		
		return valueList;
	}


	@Override
	public Object execute(Object argument, QLTerm qlterm)
			throws SAXException, IOException, ParserConfigurationException, FactoryException,
			MalformedGeometryException, ParseException {
		if (qlterm.equals(QLTerm.ROW_CLASS)){
			Object geom = argument;
			if (geom instanceof  String) {
				return "<http://www.opengis.net/def/crs/EPSG/0/" + Config.EPSG_CODE + "> " + geom;
			}
			else if (geom instanceof  Geometry){
				WKTWriter wkt = new WKTWriter();
				return "<http://www.opengis.net/def/crs/EPSG/0/" + Config.EPSG_CODE + "> " +  (wkt.write((Geometry) geom));
			}
		}
		else if (qlterm.equals(QLTerm.SHP_CLASS) && argument instanceof org.gdal.ogr.Geometry) {
			org.gdal.ogr.Geometry gdalgeom=(org.gdal.ogr.Geometry )argument;

			return "<http://www.opengis.net/def/crs/EPSG/0/" + Config.EPSG_CODE + "> " + gdalgeom.ExportToWkt();
		}

		Geometry geometry = computeGeometry(argument, qlterm);
		return GTransormationFunctions.asWKT((Geometry) geometry, CRS.decode("EPSG:" + Config.EPSG_CODE));
	}

}
