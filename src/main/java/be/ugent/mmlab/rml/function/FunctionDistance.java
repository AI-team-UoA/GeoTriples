package be.ugent.mmlab.rml.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.opengis.referencing.FactoryException;
import org.xml.sax.SAXException;

import be.ugent.mmlab.rml.core.MalformedGeometryException;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

import com.vividsolutions.jts.geom.Geometry;


public class FunctionDistance extends GeometryFunction implements Function {
	
	@Override
	public List<? extends Object> execute(
			List<? extends Object> arguments,List<? extends QLTerm> qlterms) throws SAXException, IOException, ParserConfigurationException, FactoryException, MalformedGeometryException {
		List<String> valueList = new ArrayList<>();
		
		Geometry geometry1 = computeGeometry(arguments.get(0), qlterms.get(0));
		Geometry geometry2 = computeGeometry(arguments.get(1), qlterms.get(1));
		//System.out.println(geometry1);
		//System.out.println(geometry2);
		valueList.add(GTransormationFunctions.distance(geometry1, geometry2));
		//System.out.println(valueList.get(0));
		return valueList;
	}


}
