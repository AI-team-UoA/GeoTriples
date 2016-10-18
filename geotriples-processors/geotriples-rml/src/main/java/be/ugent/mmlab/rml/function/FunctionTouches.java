package be.ugent.mmlab.rml.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opengis.referencing.FactoryException;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import be.ugent.mmlab.rml.core.MalformedGeometryException;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

public class FunctionTouches extends GeometryFunction implements Function {
	private final static Logger log = LoggerFactory.getLogger(FunctionTouches.class);

	@Override
	public List<? extends Object> execute(List<? extends Object> arguments, List<? extends QLTerm> qlterms)
			throws SAXException, IOException, ParserConfigurationException, FactoryException,
			MalformedGeometryException, ParseException {
		List<String> valueList = new ArrayList<>();
		log.debug("Executing FunctionTouches...");

		Geometry geometry1 = computeGeometry(arguments.get(0), qlterms.get(0));
		Geometry geometry2 = computeGeometry(arguments.get(1), qlterms.get(1));
		if (log.isTraceEnabled()) {
			log.trace("FunctionTouches: geometry0: " + geometry1);
			log.trace("FunctionTouches: geometry0: " + geometry2);
		}
		String result = GTransormationFunctions.touches(geometry1, geometry2);
		log.trace("FunctionTouches: Result: " + result);
		valueList.add(result);
		return valueList;
	}

}
