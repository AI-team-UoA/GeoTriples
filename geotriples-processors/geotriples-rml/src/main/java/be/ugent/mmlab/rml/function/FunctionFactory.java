package be.ugent.mmlab.rml.function;

import java.util.HashMap;

import org.openrdf.model.URI;

public class FunctionFactory {
	public static HashMap<URI, Function> availableFunctions=new HashMap<>();
	public static void registerFunction(URI functionURI,Function function) throws FunctionAlreadyExists {
		if(availableFunctions.containsKey(functionURI)){
			throw new FunctionAlreadyExists("Function "+functionURI + " has been already registered.");
		}
		availableFunctions.put(functionURI, function);
	}
	public static void registerFunction(URI functionURI,Class<? extends Function> function) throws FunctionAlreadyExists, InstantiationException, IllegalAccessException {
		if(availableFunctions.containsKey(functionURI)){
			throw new FunctionAlreadyExists("Function "+functionURI + " has been already registered.");
		}
		availableFunctions.put(functionURI, function.newInstance());
	}
	public static Function get(URI function) throws FunctionNotDefined {
		if(!availableFunctions.containsKey(function))
		{
			throw new FunctionNotDefined(function);
		}
		return availableFunctions.get(function);
	}

}
