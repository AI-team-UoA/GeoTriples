package be.ugent.mmlab.rml.function;

import java.util.HashMap;

import org.openrdf.model.URI;

public class FunctionFactory {
	public static HashMap<URI, Function> availableFunctions=new HashMap<>();
	public static void registerFunction(URI functionURI,Function function) {
		availableFunctions.put(functionURI, function);
	}
	public static Function get(URI function) {
		return availableFunctions.get(function);
	}

}
