package be.ugent.mmlab.rml.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;

import be.ugent.mmlab.rml.core.MalformedGeometryException;
import be.ugent.mmlab.rml.tools.PrintTimeStats;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

public class FunctionAsWKT extends GeometryFunction implements Function {

	@Override
	public List<? extends Object> execute(List<? extends Object> arguments, List<? extends QLTerm> qlterms)
			throws SAXException, IOException, ParserConfigurationException, FactoryException,
			MalformedGeometryException {
		List<String> valueList = new ArrayList<>();

		if (qlterms.get(0).equals(QLTerm.SHP_CLASS) && arguments.get(0) instanceof org.gdal.ogr.Geometry) {
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

}
