package be.ugent.mmlab.rml.function;

public class FunctionAlreadyExists extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2643810499378545795L;
	public FunctionAlreadyExists() {
		super();
	}
	public FunctionAlreadyExists(String message) {
		super(message);
	}
}
