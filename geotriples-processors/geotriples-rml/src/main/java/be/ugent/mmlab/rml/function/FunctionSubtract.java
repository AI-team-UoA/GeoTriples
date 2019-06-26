package be.ugent.mmlab.rml.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opengis.referencing.FactoryException;
import org.xml.sax.SAXException;

import be.ugent.mmlab.rml.core.MalformedGeometryException;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

public class FunctionSubtract extends AbstractFunction implements Function {
	private final static Logger log = LoggerFactory.getLogger(FunctionSubtract.class);

	@Override
	public List<? extends Object> execute(List<? extends Object> arguments, List<? extends QLTerm> qlterms)
			throws SAXException, IOException, ParserConfigurationException, FactoryException,
			MalformedGeometryException {
		List<String> valueList = new ArrayList<>();
		log.debug("Executing FunctionSubtract...");

		if (log.isTraceEnabled()) {
			log.trace("FunctionSubtract: value0: " + arguments.get(0));
			log.trace("FunctionSubtract: value1: " + arguments.get(1));
		}
		String result = String
				.valueOf(Double.valueOf(arguments.get(0).toString()) - Double.valueOf(arguments.get(1).toString()));
		valueList.add(result);
		log.trace("FunctionSubtract: Result: " + result);

		return valueList;
	}


	@Override
	public Object execute(Object argument, QLTerm qlterm)
			throws SAXException, IOException, ParserConfigurationException, FactoryException,MalformedGeometryException {

		return null;
	}

}
