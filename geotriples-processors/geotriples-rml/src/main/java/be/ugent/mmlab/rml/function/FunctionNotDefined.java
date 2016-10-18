package be.ugent.mmlab.rml.function;

import org.openrdf.model.URI;

public class FunctionNotDefined extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3034963235618628380L;

	public FunctionNotDefined(URI function) {
		super("Function " + function + "is not registered to GeoTriples. Use FactionFactory.registerFunction to register custom functions");
	}
}
