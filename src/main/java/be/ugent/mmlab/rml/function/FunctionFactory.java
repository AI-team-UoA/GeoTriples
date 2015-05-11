package be.ugent.mmlab.rml.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.event.ListSelectionEvent;

import jena.qtest;

import org.openrdf.model.URI;

import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

public class FunctionFactory {
	public static HashMap<URI, Function> availableFunctions=new HashMap<>();
	public static void registerFunction(URI functionURI,Function function) {
		availableFunctions.put(functionURI, function);
	}
	public static Function get(URI function) throws FunctionNotDefined {
		if(!availableFunctions.containsKey(function))
		{
			throw new FunctionNotDefined(function);
		}
		return availableFunctions.get(function);
	}

}
