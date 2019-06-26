package be.ugent.mmlab.rml.processor.concrete;

import be.ugent.mmlab.rml.function.Config;
import be.ugent.mmlab.rml.processor.RMLProcessor;
import be.ugent.mmlab.rml.processor.RMLProcessorFactory;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

/**
 * This factory class creates language-dependent processors processors
 *
 * @author mielvandersande
 */
public class ConcreteRMLProcessorFactory implements RMLProcessorFactory {

	/**
	 * Create the language-dependent processor based on the given language
	 * 
	 * @param term
	 *            Expression language
	 * @return processor able to process the specified language
	 */
	@Override
	public RMLProcessor create(QLTerm term) {
		switch (term) {
		case XPATH_CLASS:
			return new XPathProcessor();
		case CSV_CLASS:
			return new CSVProcessor();
		case TSV_CLASS:
			return new TSVProcessor();
		case JSONPATH_CLASS:
			return new JSONPathProcessor();
		case SHP_CLASS:
			if (Config.useDGALLibrary)
				return new ShapefileProcessorGDAL();
			else
				return new ShapefileProcessor();
		case ROW_CLASS:
			return new RowProcessor();
		// case SQL_CLASS:
		// 	if (Config.useOldDBProcessor)//this old processor asks only one query, the new one ask multiple queries and projects one column at a time
		// 		return new DatabaseProcessor();
		// 	else
		// 		return new DatabaseProcessorWithManyQueries();
		}
		return null;
	}

}
