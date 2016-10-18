package be.ugent.mmlab.rml.vocabulary;
/***************************************************************************
*
* @author: dimis (dimis@di.uoa.gr)
* 
****************************************************************************/
import java.sql.Types;

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
		/*public static final DataType asWKT = new WKTLiteral("WKTLiteral");
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
*/
		
		public static String getDataTypeOf(String type) {
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
			default:
				break;
			}
			return null;
		}
	}

	/**
	 * <p>
	 * Represents the geometry function dimension.
	 * </p>
	 */
	public static final String asWKT = VocabTrans.RRXF_NAMESPACE + "asWKT";
	public static final String hasSerialization = VocabTrans.RRXF_NAMESPACE + "hasSerialization";
	public static final String asGML = VocabTrans.RRXF_NAMESPACE +"asGML";
	public static final String isSimple = VocabTrans.RRXF_NAMESPACE + "isSimple";
	public static final String isEmpty = VocabTrans.RRXF_NAMESPACE +"isEmpty";
	public static final String is3D = VocabTrans.RRXF_NAMESPACE +"is3D";
	public static final String spatialDimension = VocabTrans.RRXF_NAMESPACE +"spatialDimension";
	public static final String dimension = VocabTrans.RRXF_NAMESPACE +"dimension";
	public static final String coordinateDimension = VocabTrans.RRXF_NAMESPACE +"coordinateDimension";
	public static final String area = VocabTrans.RRXF_NAMESPACE +"area";
	public static final String length = VocabTrans.RRXF_NAMESPACE +"length";
	public static final String centroidx = VocabTrans.RRXF_NAMESPACE +"centroidx";
	public static final String centroidy = VocabTrans.RRXF_NAMESPACE +"centroidy";
	public static String getGeometryFunctionOf(String type) {
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
		default:
			break;
		}
		return null;
	}
}
