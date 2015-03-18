package org.d2rq.vocab;

import java.sql.Types;

import org.d2rq.db.types.DataType;
import org.d2rq.db.types.SQLBoolean;
import org.d2rq.db.types.SQLExactNumeric;
import org.d2rq.r2rml.ConstantIRI;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.linkedeodata.geotriples.GMLLiteral;
import eu.linkedeodata.geotriples.WKTLiteral;

/** SEE WP2 deliverable 1 */
public class STRING_FUNCTIONS {
		

	/**
	 * <p>
	 * The RDF model that holds the vocabulary terms
	 * </p>
	 */
	@SuppressWarnings("unused")
	private static Model m_model = ModelFactory.createDefaultModel();

	/**
	 * <p>
	 * Represents the geometry function dimension.
	 * </p>
	 */
	public static final ConstantIRI asCAPITAL = ConstantIRI.create(RRX
			.getFunctionsURI() + "asCAPITAL");
	public static final ConstantIRI asLOWER = ConstantIRI.create(RRX
			.getFunctionsURI() + "asLOWER");
	public static final ConstantIRI SubString = ConstantIRI.create(RRX
			.getFunctionsURI() + "SubString");
	
	
}
