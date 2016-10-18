/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.mmlab.rml.tools;

import net.antidot.semantic.rdf.model.tools.RDFDataValidator;

/**
 *
 * This class customizes the implementation by db2triples
 * 
 * @author mielvandersande
 */
public class CustomRDFDataValidator extends RDFDataValidator{
    	/**
	 * The set of validatable RDF datatypes includes all datatypes 
	 * in the RDF datatype column of the table of natural datatype
	 * mappings, as defined in [XMLSCHEMA2]. 
	 * @param string
	 * @return
	 */
	public static boolean isValidDatatype(String datatype) {
		boolean isValid = true;
		if (!isValidURI(datatype)) return false;
                
                //MVS: Removed constraint since it prevents using types other than XSD
/*		try {
			XSDType.toXSDType(datatype);
		} catch (IllegalArgumentException e) {
			isValid = false;
		}*/
		return isValid;
	}
}
