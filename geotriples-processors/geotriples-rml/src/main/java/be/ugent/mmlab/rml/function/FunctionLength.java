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


public class FunctionLength extends GeometryFunction implements Function {
	
	@Override
	public List<? extends Object> execute(
			List<? extends Object> arguments,List<? extends QLTerm> qlterms) throws SAXException, IOException, ParserConfigurationException, FactoryException, MalformedGeometryException, ParseException {
		List<String> valueList = new ArrayList<>();
		
		Geometry geometry = computeGeometry(arguments.get(0), qlterms.get(0));
		valueList.add(GTransormationFunctions.length(
				(Geometry) geometry));
		return valueList;
	}


	@Override
	public Object execute(Object argument, QLTerm qlterm) throws SAXException, IOException, ParserConfigurationException, FactoryException, MalformedGeometryException, ParseException {

		Geometry geometry = computeGeometry(argument, qlterm);
		return GTransormationFunctions.length((Geometry) geometry);
	}


}
