package be.ugent.mmlab.rml.processor.concrete;

/**
 * 
 * @author dimis
 */
public class TSVProcessor extends CSVProcessor {
	@Override
	protected char getDelimiter() {
		return '\t';
	}
}
