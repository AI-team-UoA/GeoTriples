package be.ugent.mmlab.rml.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.opengis.referencing.FactoryException;
import org.xml.sax.SAXException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

import be.ugent.mmlab.rml.core.MalformedGeometryException;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;


public class FunctionCoordinateDimension extends GeometryFunction implements Function {
	
	@Override
	public List<? extends Object> execute(
			List<? extends Object> arguments,List<? extends QLTerm> qlterms) throws SAXException, IOException, ParserConfigurationException, FactoryException, MalformedGeometryException, ParseException {
		List<String> valueList = new ArrayList<>();
		
		if(qlterms.get(0).equals(QLTerm.SHP_CLASS) && arguments.get(0) instanceof org.gdal.ogr.Geometry ){
			org.gdal.ogr.Geometry gdalgeom=(org.gdal.ogr.Geometry )arguments.get(0);
			valueList.add(String.valueOf(gdalgeom.GetCoordinateDimension()));
			return valueList;
		}
		
		Geometry geometry = computeGeometry(arguments.get(0), qlterms.get(0));
		valueList.add(GTransormationFunctions.coordinateDimension(
				(Geometry) geometry));
		return valueList;
	}


	@Override
	public Object execute(Object argument, QLTerm qlterm) throws SAXException, IOException, ParserConfigurationException, FactoryException, MalformedGeometryException, ParseException {

		if(qlterm.equals(QLTerm.SHP_CLASS) && argument instanceof org.gdal.ogr.Geometry ){
			org.gdal.ogr.Geometry gdalgeom=(org.gdal.ogr.Geometry )argument;
			return String.valueOf(gdalgeom.GetCoordinateDimension());
		}

		Geometry geometry = computeGeometry(argument, qlterm);
		return GTransormationFunctions.coordinateDimension(
				(Geometry) geometry);

	}


}
