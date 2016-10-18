package be.ugent.mmlab.rml.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.opengis.referencing.FactoryException;
import org.xml.sax.SAXException;

import be.ugent.mmlab.rml.core.MalformedGeometryException;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;


public class FunctionGreaterThan extends AbstractFunction implements Function {
	
	@Override
	public List<? extends Object> execute(
			List<? extends Object> arguments,List<? extends QLTerm> qlterms) throws SAXException, IOException, ParserConfigurationException, FactoryException, MalformedGeometryException {
		List<String> valueList = new ArrayList<>();
//		System.out.println(arguments.get(0));
//		System.out.println(arguments.get(1));
//		System.out.println("result ");
		valueList.add(GTransormationFunctions.greaterThan(
				Double.valueOf(arguments.get(0).toString()),Double.valueOf(arguments.get(1).toString())));
//		System.out.println("GreaterThan("+ arguments.get(0) +","+arguments.get(1)+") ="+valueList.get(0));
		return valueList;
	}


}

               
               
               
               
      
               