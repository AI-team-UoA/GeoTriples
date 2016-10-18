package org.d2rq.vocab;

import java.sql.Types;

import org.d2rq.db.types.DataType;
import org.d2rq.db.types.SQLBoolean;
import org.d2rq.db.types.SQLExactNumeric;
import org.d2rq.db.types.StrdfWKT;
import org.d2rq.r2rml.ConstantIRI;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.linkedeodata.geotriples.GMLLiteral;
import eu.linkedeodata.geotriples.WKTLiteral;

/** SEE WP2 deliverable 1 */
public class GEOMETRY_FUNCTIONS {
	public static class GEOMETRY_FUNCTIONS_DATATYPES {
		/**
		 * <p>
		 * The RDF model that holds the vocabulary terms
		 * </p>
		 */

		/**
		 * <p>
		 * Represents the geometry function dimension.
		 * </p>
		 */
		public static final DataType asWKT = new WKTLiteral("WKTLiteral");
		public static final DataType hasSerialization = asWKT;
		public static final DataType asGML = new GMLLiteral("gmlLiteral");
		public static final DataType isSimple = new SQLBoolean("boolenan");
		public static final DataType isEmpty = new SQLBoolean("boolenan");
		public static final DataType is3D = new SQLBoolean("boolenan");
		public static final DataType spatialDimension = new SQLExactNumeric(
				"Int", Types.INTEGER, false);
		public static final DataType dimension = new SQLExactNumeric("Int",
				Types.INTEGER, false);
		public static final DataType coordinateDimension = new SQLExactNumeric(
				"Int", Types.INTEGER, false);
		
		public static final DataType area = new SQLExactNumeric("Double", Types.DOUBLE, false);
		public static final DataType length = new SQLExactNumeric("Double", Types.DOUBLE, false);
		public static final DataType centroidx = new SQLExactNumeric("Double", Types.DOUBLE, false);
		public static final DataType centroidy = new SQLExactNumeric("Double", Types.DOUBLE, false);
		
		//strdf operations
		public static final DataType strdfWKT = new StrdfWKT("WKT");
		

		
		public static DataType getDataTypeOf(String type) {
			switch (type) {
			case "asWKT":
				return asWKT;
			case "asGML":
				return asGML;
			case "isEmpty":
				return isEmpty;
			case "is3D":
				return is3D;
			case "isSimple":
				return isSimple;
			case "hasSerialization":
				return hasSerialization;
			case "spatialDimension":
				return spatialDimension;
			case "dimension":
				return dimension;
			case "coordinateDimension":
				return coordinateDimension;
			case "area":
				return area;
			case "length":
				return length;
			case "centroidx":
				return centroidx;
			case "centroidy":
				return centroidy;
			case "strdfWKT":
				return strdfWKT;
			default:
				break;
			}
			return null;
		}
	}

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
	public static final ConstantIRI asWKT = ConstantIRI.create(RRX
			.getFunctionsURI() + "asWKT");
	public static final ConstantIRI hasSerialization = ConstantIRI.create(RRX
			.getFunctionsURI() + "hasSerialization");
	public static final ConstantIRI asGML = ConstantIRI.create(RRX
			.getFunctionsURI() + "asGML");
	public static final ConstantIRI isSimple = ConstantIRI.create(RRX
			.getFunctionsURI() + "isSimple");
	public static final ConstantIRI isEmpty = ConstantIRI.create(RRX
			.getFunctionsURI() + "isEmpty");
	public static final ConstantIRI is3D = ConstantIRI.create(RRX
			.getFunctionsURI() + "is3D");
	public static final ConstantIRI spatialDimension = ConstantIRI.create(RRX
			.getFunctionsURI() + "spatialDimension");
	public static final ConstantIRI dimension = ConstantIRI.create(RRX
			.getFunctionsURI() + "dimension");
	public static final ConstantIRI coordinateDimension = ConstantIRI
			.create(RRX.getFunctionsURI() + "coordinateDimension");
	public static final ConstantIRI area = ConstantIRI.create(RRX
			.getFunctionsURI() + "area");
	public static final ConstantIRI length = ConstantIRI.create(RRX
			.getFunctionsURI() + "length");
	public static final ConstantIRI centroidx = ConstantIRI.create(RRX
			.getFunctionsURI() + "centroidx");
	public static final ConstantIRI centroidy = ConstantIRI.create(RRX
			.getFunctionsURI() + "centroidy");
	public static final ConstantIRI strdfWKT = ConstantIRI.create(RRX
			.getFunctionsURI() + "strdfWKT");
	public static ConstantIRI getGeometryFunctionOf(String type) {
		switch (type) {
		case "asWKT":
			return asWKT;
		case "asGML":
			return asGML;
		case "isEmpty":
			return isEmpty;
		case "is3D":
			return is3D;
		case "isSimple":
			return isSimple;
		case "hasSerialization":
			return hasSerialization;
		case "spatialDimension":
			return spatialDimension;
		case "dimension":
			return dimension;
		case "coordinateDimension":
			return coordinateDimension;
		case "area":
			return area;
		case "length":
			return length;
		case "centroidx":
			return centroidx;
		case "centroidy":
			return centroidy;
		case "strdfWKT":
			return strdfWKT;
		default:
			break;
		}
		return null;
	}
}
