package org.d2rq.values;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.d2rq.db.ResultRow;
import org.d2rq.db.expr.Expression;
import org.d2rq.db.op.DatabaseOp;
import org.d2rq.db.op.OrderOp.OrderSpec;
import org.d2rq.db.op.ProjectionSpec;
import org.d2rq.db.renamer.Renamer;
import org.d2rq.db.vendor.Vendor;
import org.d2rq.nodes.NodeSetFilter;
import org.d2rq.nodes.TypedNodeMaker.NodeType;
import org.d2rq.r2rml.ConstantIRI;
import org.d2rq.vocab.GEOMETRY_FUNCTIONS;
import org.d2rq.vocab.STRING_FUNCTIONS;
import org.gdal.ogr.ogr;
import org.gdal.ogr.ogrJNI;

import com.vividsolutions.jts.geom.Geometry;

import be.ugent.mmlab.rml.function.Config;
import be.ugent.mmlab.rml.tools.PrintTimeStats;
import eu.linkedeodata.geotriples.GTransormationFunctions;
import eu.linkedeodata.geotriples.GeneralConnection;

/**
 * A {@link TransformationValueMaker} that takes its values from a ValueMaker.
 * 
 * @author Dimis (dimis@di.uoa.gr)
 */
public class TransformationValueMaker implements ValueMaker {
	private ConstantIRI gfunction;
	private GeneralConnection connection;
	protected List<ValueMaker> valueMakers;
	private List<Object> values = new ArrayList<Object>();

	public TransformationValueMaker(NodeType nodeType, List<ValueMaker> valueMakers, ConstantIRI function,
			GeneralConnection con) {
		this.valueMakers = valueMakers;
		gfunction = function;
		connection = con;
	}

	public String makeValue(ResultRow row) {
		values.clear(); // this is a bug fix, we were getting the same geometry
						// for all triples!
		for (int i = 0; i < valueMakers.size(); ++i) {
			values.add(valueMakers.get(i).makeValueObject(row));
		}
		String crs = null;
		if (Config.useDGALLibrary) {
			crs = Config.EPSG_CODE;
		} else {
			crs = connection.getCRS();
		}
		String function = gfunction.toString();
		try {
			if (Config.useDGALLibrary) {
				if (function.equals(GEOMETRY_FUNCTIONS.asWKT.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.asWKT((org.gdal.ogr.Geometry) values.get(0), crs);
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function asWKT", duration);
					return result;
				} else if (function.equals(GEOMETRY_FUNCTIONS.isSimple.toString())) {
					
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.isSimple((org.gdal.ogr.Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function isSimple", duration);
					return result;
				} else if (function.equals(GEOMETRY_FUNCTIONS.hasSerialization.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.hasSerialization((org.gdal.ogr.Geometry) values.get(0), crs);
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function hasSerialization", duration);
					return result;
				} else if (function.equals(GEOMETRY_FUNCTIONS.asGML.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.asGML((org.gdal.ogr.Geometry) values.get(0), crs);
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function asGML", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.isEmpty.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.isEmpty((org.gdal.ogr.Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function isEmpty", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.is3D.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.is3D((org.gdal.ogr.Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function is3D", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.spatialDimension.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.spatialDimension((org.gdal.ogr.Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function spatialDimension", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.dimension.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.dimension((org.gdal.ogr.Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function dimension", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.coordinateDimension.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.coordinateDimension((org.gdal.ogr.Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function coordinateDimension", duration);
					return result;
				} else if (function.equals(GEOMETRY_FUNCTIONS.area.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.area((org.gdal.ogr.Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function area", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.length.toString())) {
					return GTransormationFunctions.length((org.gdal.ogr.Geometry) values.get(0));
				} else if (function.equals(GEOMETRY_FUNCTIONS.centroidx.toString())) {
					return GTransormationFunctions.centroidx((org.gdal.ogr.Geometry) values.get(0));
				} else if (function.equals(GEOMETRY_FUNCTIONS.centroidy.toString())) {
					return GTransormationFunctions.centroidy((org.gdal.ogr.Geometry) values.get(0));
				} else if (function.equals(STRING_FUNCTIONS.asCAPITAL.toString())) {
					return values.get(0).toString().toUpperCase();
				} else if (function.equals(STRING_FUNCTIONS.asLOWER.toString())) {
					return values.get(0).toString().toLowerCase();
				} else if (function.equals(STRING_FUNCTIONS.SubString.toString())) {
					return values.get(0).toString().substring(0, Integer.valueOf(values.get(1).toString()));
				}
			} else {
				if (function.equals(GEOMETRY_FUNCTIONS.asWKT.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.asWKT((Geometry) values.get(0), crs);
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function asWKT", duration);
					return result;
				} else if (function.equals(GEOMETRY_FUNCTIONS.isSimple.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.isSimple((Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function isSimple", duration);
					return result;
				} else if (function.equals(GEOMETRY_FUNCTIONS.hasSerialization.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.hasSerialization((Geometry) values.get(0), crs);
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function hasSerialization", duration);
					return result;
				
				} else if (function.equals(GEOMETRY_FUNCTIONS.asGML.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.asGML((Geometry) values.get(0), crs);
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function asGML", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.isEmpty.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.isEmpty((Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function isEmpty", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.is3D.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.is3D((Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function is3D", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.spatialDimension.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.spatialDimension((Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function spatialDimension", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.dimension.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.dimension((Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function dimension", duration);
					return result;
				} else if (function.equals(GEOMETRY_FUNCTIONS.coordinateDimension.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.coordinateDimension((Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function coordinateDimension", duration);
					return result;
				} else if (function.equals(GEOMETRY_FUNCTIONS.area.toString())) {
					return GTransormationFunctions.area((Geometry) values.get(0));
				} else if (function.equals(GEOMETRY_FUNCTIONS.length.toString())) {
					return GTransormationFunctions.length((Geometry) values.get(0));
				} else if (function.equals(GEOMETRY_FUNCTIONS.centroidx.toString())) {
					return GTransormationFunctions.centroidx((Geometry) values.get(0));
				} else if (function.equals(GEOMETRY_FUNCTIONS.centroidy.toString())) {
					return GTransormationFunctions.centroidy((Geometry) values.get(0));
				} else if (function.equals(GEOMETRY_FUNCTIONS.strdfWKT.toString())) {
					return GTransormationFunctions.strdfWKT((Geometry) values.get(0), crs);
				} else if (function.equals(STRING_FUNCTIONS.asCAPITAL.toString())) {
					return values.get(0).toString().toUpperCase();
				} else if (function.equals(STRING_FUNCTIONS.asLOWER.toString())) {
					return values.get(0).toString().toLowerCase();
				} else if (function.equals(STRING_FUNCTIONS.SubString.toString())) {
					return values.get(0).toString().substring(0, Integer.valueOf(values.get(1).toString()));
				}
			}

		} catch (ArrayIndexOutOfBoundsException e) {
			try {
				throw new Exception("mple Not enough arguments given for function <" + function + ">");
			} catch (Exception e1) {
				System.out.println(e.getMessage());
				e1.printStackTrace();
			}
		}
		/*
		 * else { return this.nodeType.makeNode(((String)value).toLowerCase());
		 * }
		 */

		try {
			throw new Exception("mple Not supported Transformation function <" + function + ">");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public Object makeValueObject(ResultRow row) {
		for (int i = 0; i < valueMakers.size(); ++i) {
			values.add(valueMakers.get(i).makeValueObject(row));
		}
		String crs = null;
		if (Config.useDGALLibrary) {
			crs = Config.EPSG_CODE;
		} else {
			crs = connection.getCRS();
		}
		String function = gfunction.toString();
		try {
			if (Config.useDGALLibrary) {
				if (function.equals(GEOMETRY_FUNCTIONS.asWKT.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.asWKT((org.gdal.ogr.Geometry) values.get(0), crs);
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function asWKT", duration);
					return result;
				} else if (function.equals(GEOMETRY_FUNCTIONS.isSimple.toString())) {
					
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.isSimple((org.gdal.ogr.Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function isSimple", duration);
					return result;
				} else if (function.equals(GEOMETRY_FUNCTIONS.hasSerialization.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.hasSerialization((org.gdal.ogr.Geometry) values.get(0), crs);
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function hasSerialization", duration);
					return result;
				} else if (function.equals(GEOMETRY_FUNCTIONS.asGML.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.asGML((org.gdal.ogr.Geometry) values.get(0), crs);
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function asGML", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.isEmpty.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.isEmpty((org.gdal.ogr.Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function isEmpty", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.is3D.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.is3D((org.gdal.ogr.Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function is3D", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.spatialDimension.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.spatialDimension((org.gdal.ogr.Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function spatialDimension", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.dimension.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.dimension((org.gdal.ogr.Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function dimension", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.coordinateDimension.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.coordinateDimension((org.gdal.ogr.Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function coordinateDimension", duration);
					return result;
				} else if (function.equals(GEOMETRY_FUNCTIONS.area.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.area((org.gdal.ogr.Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function area", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.length.toString())) {
					return GTransormationFunctions.length((org.gdal.ogr.Geometry) values.get(0));
				} else if (function.equals(GEOMETRY_FUNCTIONS.centroidx.toString())) {
					return GTransormationFunctions.centroidx((org.gdal.ogr.Geometry) values.get(0));
				} else if (function.equals(GEOMETRY_FUNCTIONS.centroidy.toString())) {
					return GTransormationFunctions.centroidy((org.gdal.ogr.Geometry) values.get(0));
				} else if (function.equals(STRING_FUNCTIONS.asCAPITAL.toString())) {
					return values.get(0).toString().toUpperCase();
				} else if (function.equals(STRING_FUNCTIONS.asLOWER.toString())) {
					return values.get(0).toString().toLowerCase();
				} else if (function.equals(STRING_FUNCTIONS.SubString.toString())) {
					return values.get(0).toString().substring(0, Integer.valueOf(values.get(1).toString()));
				}
			} else {
				if (function.equals(GEOMETRY_FUNCTIONS.asWKT.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.asWKT((Geometry) values.get(0), crs);
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function asWKT", duration);
					return result;
				} else if (function.equals(GEOMETRY_FUNCTIONS.isSimple.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.isSimple((Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function isSimple", duration);
					return result;
				} else if (function.equals(GEOMETRY_FUNCTIONS.hasSerialization.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.hasSerialization((Geometry) values.get(0), crs);
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function hasSerialization", duration);
					return result;
				
				} else if (function.equals(GEOMETRY_FUNCTIONS.asGML.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.asGML((Geometry) values.get(0), crs);
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function asGML", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.isEmpty.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.isEmpty((Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function isEmpty", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.is3D.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.is3D((Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function is3D", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.spatialDimension.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.spatialDimension((Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function spatialDimension", duration);
					return result;
					
				} else if (function.equals(GEOMETRY_FUNCTIONS.dimension.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.dimension((Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function dimension", duration);
					return result;
				} else if (function.equals(GEOMETRY_FUNCTIONS.coordinateDimension.toString())) {
					long startTime = System.nanoTime();
					String result = GTransormationFunctions.coordinateDimension((Geometry) values.get(0));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
					PrintTimeStats.printTime("Compute function coordinateDimension", duration);
					return result;
				} else if (function.equals(GEOMETRY_FUNCTIONS.area.toString())) {
					return GTransormationFunctions.area((Geometry) values.get(0));
				} else if (function.equals(GEOMETRY_FUNCTIONS.length.toString())) {
					return GTransormationFunctions.length((Geometry) values.get(0));
				} else if (function.equals(GEOMETRY_FUNCTIONS.centroidx.toString())) {
					return GTransormationFunctions.centroidx((Geometry) values.get(0));
				} else if (function.equals(GEOMETRY_FUNCTIONS.centroidy.toString())) {
					return GTransormationFunctions.centroidy((Geometry) values.get(0));
				} else if (function.equals(GEOMETRY_FUNCTIONS.strdfWKT.toString())) {
					return GTransormationFunctions.strdfWKT((Geometry) values.get(0), crs);
				} else if (function.equals(STRING_FUNCTIONS.asCAPITAL.toString())) {
					return values.get(0).toString().toUpperCase();
				} else if (function.equals(STRING_FUNCTIONS.asLOWER.toString())) {
					return values.get(0).toString().toLowerCase();
				} else if (function.equals(STRING_FUNCTIONS.SubString.toString())) {
					return values.get(0).toString().substring(0, Integer.valueOf(values.get(1).toString()));
				}
			}

		} catch (ArrayIndexOutOfBoundsException e) {
			try {
				throw new Exception("Not enough arguments given for function <" + function + ">");
			} catch (Exception e1) {
				System.out.println(e.getMessage());
				e1.printStackTrace();
			}
		}
		/*
		 * else { return this.nodeType.makeNode(((String)value).toLowerCase());
		 * }
		 */

		try {
			throw new Exception("mple Not supported Transformation function <" + function + ">");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public void describeSelf(NodeSetFilter c) {

	}

	public boolean matches(String value) {
		return false;
	}

	public Expression valueExpression(String value, DatabaseOp tabular, Vendor vendor) {
		return null;
	}

	public Set<ProjectionSpec> projectionSpecs() {
		Set<ProjectionSpec> result = new HashSet<ProjectionSpec>();
		for (ValueMaker vm : valueMakers) {
			result.addAll(vm.projectionSpecs());
		}
		return result;
	}

	public ValueMaker rename(Renamer renamer) {
		return null;
	}

	public List<OrderSpec> orderSpecs(boolean ascending) {
		return null;
	}

	public String toString() {
		return this.toString();
	}
}
