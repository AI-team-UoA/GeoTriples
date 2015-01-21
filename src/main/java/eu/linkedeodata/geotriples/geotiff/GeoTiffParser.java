/**
 * @author Dimitrianos Savva National and Kapodistrian University of Athens
 * @author Giannis Vlachopoulos National and Kapodistrian University of Athens
 */
package eu.linkedeodata.geotriples.geotiff;

import java.io.File;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.UnsupportedDataTypeException;

import org.d2rq.db.schema.ColumnDef;
import org.d2rq.db.schema.ForeignKey;
import org.d2rq.db.schema.Identifier;
import org.d2rq.db.schema.Key;
import org.d2rq.db.schema.TableDef;
import org.d2rq.db.schema.TableName;
import org.d2rq.db.types.DataType;
import org.d2rq.db.types.SQLApproximateNumeric;
import org.d2rq.db.types.SQLBoolean;
import org.d2rq.db.types.SQLCharacterString;
import org.d2rq.db.types.SQLExactNumeric;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;

import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.io.gml2.GMLWriter;

public class GeoTiffParser {
	private File geoTiffFile;
	private String primarykey;
	private Map<String, List<GeoTiffResultRow>> cacheList = new HashMap<String, List<GeoTiffResultRow>>();

	public GeoTiffParser(File kmlfile, String primkey) {
		this.geoTiffFile = kmlfile;
		this.primarykey = primkey;
	}

	public GeoTiffParser(File geoTiffFile) {
		this(geoTiffFile, "R2RMLPrimaryKey");
	}

	public void setGeoTiffFile(File geoTiffFile) {
		this.geoTiffFile = geoTiffFile;
	}

	public File getGeoTiffFile() {
		return this.geoTiffFile;
	}

	@SuppressWarnings({ "unused", "rawtypes" })
	private List<GeoTiffResultRow> getData(FeatureSource featureSource)
			throws Exception {
		/* Must not be null in any case! */
		if (featureSource == null) {
			Exception mple = new Exception("featureSource is null");
			mple.printStackTrace();
			throw mple;
		}
		String crs = org.geotools.gml2.bindings.GML2EncodingUtils
				.epsgCode(featureSource.getSchema()
						.getCoordinateReferenceSystem());
		// System.out.println(crs.getCRSIdentifier(featureSource.getSchema().getCoordinateReferenceSystem()));

		FeatureCollection collection = featureSource.getFeatures();
		FeatureIterator iterator = collection.features();
		List<GeoTiffResultRow> resultlist = new ArrayList<GeoTiffResultRow>();
		WKTWriter wkt_writer = new WKTWriter();
		GMLWriter gml_writer = new GMLWriter();
		try {
			while (iterator.hasNext()) {
				Feature feature = iterator.next();
				// System.out.println(sourceGeometryAttribute);
				GeoTiffResultRow newrow = new GeoTiffResultRow(); // new row
				for (Property p : feature.getProperties()) {
					// System.out.println(p.getType());
					newrow.addPair(p.getName().getLocalPart(), p.getValue());
					// p.getType().
					// System.out.println(p.getName() + " " + p.getValue());
				}
				newrow.addPair(primarykey, KeyGenerator.Generate()); // Add
																		// primary
																		// key

				GeometryAttribute sourceGeometryAttribute = feature
						.getDefaultGeometryProperty();

				Object initgeometry = null;
				if (sourceGeometryAttribute.getValue().toString()
						.startsWith("POINT")) {
					Point geometry = (com.vividsolutions.jts.geom.Point) sourceGeometryAttribute
							.getValue();
					newrow.addPair("isEmpty", geometry.isEmpty());
					newrow.addPair("isSimple", geometry.isSimple());
					newrow.addPair("dimension",
							geometry.getCoordinates().length);
					newrow.addPair("coordinateDimension",
							geometry.getCoordinates().length);
					newrow.addPair("spatialDimension", geometry.getDimension()); // spatialdimension
																					// <=
																					// dimension
					// System.out.println(geometry.getCoordinate().x + " "
					// +geometry.getCoordinate().z);
					// System.out.println(geometry.get .getSRID());
					// CRS.
					if (crs == null) {
						System.err.println("No SRID specified. Aborting...");
						System.exit(-1);
					}

					newrow.addPair("asWKT",
							"<http://www.opengis.net/def/crs/EPSG/0/" + crs
									+ ">" + wkt_writer.write(geometry));
					newrow.addPair("hasSerialization",
							"<http://www.opengis.net/def/crs/EPSG/0/" + crs
									+ ">" + wkt_writer.write(geometry));
					// newrow.addPair("hasSerialization",
					// wkt_writer.write(geometry));
					gml_writer.setSrsName(crs);
					newrow.addPair("asGML", gml_writer.write(geometry)
							.replaceAll("\n", " "));
					newrow.addPair("is3D", geometry.getDimension() == 3);
				} else {
					GeometryCollection geometry = (GeometryCollection) sourceGeometryAttribute
							.getValue();
					newrow.addPair("isEmpty", geometry.isEmpty());
					newrow.addPair("isSimple", geometry.isSimple());
					newrow.addPair("dimension",
							geometry.getCoordinates().length);
					newrow.addPair("coordinateDimension",
							geometry.getCoordinates().length);
					newrow.addPair("spatialDimension", geometry.getDimension()); // spatialdimension
																					// <=
																					// dimension
					// System.out.println(geometry.getCoordinate().x + " "
					// +geometry.getCoordinate().z);
					// System.out.println(geometry.get .getSRID());
					// CRS.
					if (crs == null) {
						System.err.println("No SRID specified. Aborting...");
						System.exit(-1);
					}
					// geometry.getNumPoints();
					// TODO spatialDimension??????
					// TODO coordinateDimension??????
					// Geometry geometry1=
					// (Geometry)sourceGeometryAttribute.getValue();
					// geometry1.transform(arg0, arg1)

					// sourceGeometryAttribute.ge

					newrow.addPair("asWKT",
							"<http://www.opengis.net/def/crs/EPSG/0/" + crs
									+ ">" + wkt_writer.write(geometry));
					newrow.addPair("hasSerialization",
							"<http://www.opengis.net/def/crs/EPSG/0/" + crs
									+ ">" + wkt_writer.write(geometry));
					// newrow.addPair("hasSerialization",
					// wkt_writer.write(geometry));
					gml_writer
							.setSrsName("http://www.opengis.net/def/crs/EPSG/0/"
									+ crs);
					newrow.addPair("asGML", gml_writer.write(geometry)
							.replaceAll("\n", " "));
					newrow.addPair("is3D", geometry.getDimension() == 3);
				}
				// Geometry sourceGeometryAttribute=
				// (Geometry)feature.getDefaultGeometryProperty();
				// System.out.println(sourceGeometryAttribute.getValue().getClass().toString());

				resultlist.add(newrow);
			}
		} finally {
			iterator.close();
		}

		return resultlist;
	}

	public List<GeoTiffResultRow> getData(String tablename) throws Exception {
		if (cacheList.containsKey(tablename)) {
			return cacheList.get(tablename);
		} else if (cacheList.containsKey(tablename.replaceAll("_geometry", ""))) {
			return cacheList.get(tablename.replaceAll("_geometry", ""));
		}
		List<GeoTiffResultRow> result = null;
		try {
			GeoTiffReader2 rdr = new GeoTiffReader2(geoTiffFile.getAbsolutePath(),this.primarykey);
			rdr.read();
			result=rdr.getResults();
			cacheList.put(tablename.replaceAll("_geometry", ""), result);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		}
		cacheList.put(tablename.replaceAll("_geometry", ""), result);
		return result;
	}

	public List<TableDef> getTablesDefs() throws Exception {
		
		
		List<ColumnDef> columns = new ArrayList<ColumnDef>();
		TableDef onlytable = null;
		@SuppressWarnings("unused")
		Key primarykey = null;
		Set<Key> primkeys = new HashSet<Key>();
		List<TableDef> tables = new ArrayList<TableDef>();
		Identifier[] identifiers= {Identifier.createDelimited("name"),
				Identifier.createDelimited("visibility"),
				Identifier.createDelimited("open"),
				Identifier.createDelimited("phoneNumber"),
				Identifier.createDelimited("description"),
				Identifier.createDelimited("LookAt"),
				Identifier.createDelimited("Style"),
				Identifier.createDelimited("Region"),
				Identifier.createDelimited("Geometry")};
		/*Identifier name = Identifier.createDelimited("name");
		Identifier visibility = Identifier.createDelimited("visibility");
		Identifier open = Identifier.createDelimited("open");
		Identifier phonenumber = Identifier.createDelimited("phoneNumber");
		Identifier description = Identifier.createDelimited("description");
		Identifier lookat = Identifier.createDelimited("LookAt");
		Identifier style = Identifier.createDelimited("Style");
		Identifier region = Identifier.createDelimited("Region");
		Identifier geometry = Identifier.createDelimited("Geometry");
		 */
		
		DataType[] datatypes ={
		TranslateDataTypeToSQLType("string"),
		TranslateDataTypeToSQLType("bool"),
		TranslateDataTypeToSQLType("bool"),
		TranslateDataTypeToSQLType("string"),
		TranslateDataTypeToSQLType("string"),
		TranslateDataTypeToSQLType("point"),
		TranslateDataTypeToSQLType("string"),
		TranslateDataTypeToSQLType("string"),
		TranslateDataTypeToSQLType("multipolygon" +
				"")};
		
		/*
		DataType dname = TranslateDataTypeToSQLType("string");
		DataType dvisibility = TranslateDataTypeToSQLType("bool");
		DataType dopen = TranslateDataTypeToSQLType("bool");
		DataType dphonenumber = TranslateDataTypeToSQLType("string");
		DataType ddescriprion = TranslateDataTypeToSQLType("string");
		DataType dlookat = TranslateDataTypeToSQLType("point");
		DataType dstyle = TranslateDataTypeToSQLType("string");
		DataType dregion = TranslateDataTypeToSQLType("string");
		DataType dgeometry = TranslateDataTypeToSQLType("multipolygon" +
				"");
		*/
		for(int i=0;i<identifiers.length;++i)
		{
			columns.add(new ColumnDef(identifiers[i], datatypes[i],true));
		}
		TableName tablename = TableName.create(null, null,
				Identifier.create(true,"tablename"));

		// primkeys.add(primarykey);
		onlytable = new TableDef(tablename, columns, null, primkeys,
				new HashSet<ForeignKey>());
		tables.add(onlytable);
		return tables;

	}

	private DataType TranslateDataTypeToSQLType(String name)
			throws UnsupportedDataTypeException {
		if (name.contains(".")) {
			String[] tokens = name.split("[.]");
			name = tokens[tokens.length - 1];
		}
		if (name.equalsIgnoreCase("String")) {
			return new SQLCharacterString("String", true);
		} else if (name.equalsIgnoreCase("Int") || name.equalsIgnoreCase("Integer")) {
			return new SQLExactNumeric("Int", Types.INTEGER, false);
		} else if (name.equalsIgnoreCase("Bool")) {
			return new SQLBoolean("Boolean");
		} else if (name.equalsIgnoreCase("MultiPolygon")) {
			return new SQLCharacterString("Geometry", true);
		} else if (name.equalsIgnoreCase("Point")) {
			return new SQLCharacterString("Geometry", true);
		} else if (name.equalsIgnoreCase("LinearString")) {
			return new SQLCharacterString("Geometry", true);
		} else if (name.equalsIgnoreCase("MultiLineString")) {
			return new SQLCharacterString("Geometry", true);
		} else if (name.equalsIgnoreCase("Long")) {
			return new SQLExactNumeric("Int", Types.BIGINT, false);
		} else if (name.equalsIgnoreCase("Double")) {
			return new SQLApproximateNumeric("Double");
		} else {
			throw new UnsupportedDataTypeException(name
					+ " datatype is not supported");
		}
	}

	public List<GeoTiffResultRow> getData(String tablename, int primkey) {
		if (cacheList.containsKey(tablename)) {
			List<GeoTiffResultRow> tempresult = cacheList.get(tablename);
			List<GeoTiffResultRow> newresult = new ArrayList<GeoTiffResultRow>();
			for (GeoTiffResultRow row : tempresult) {
				if (((int) (row.getData(this.primarykey))) == primkey) {
					newresult.add(row);
				}
			}
			return newresult;
		} else if (cacheList.containsKey(tablename.replaceAll("_geometry", ""))) {
			List<GeoTiffResultRow> tempresult = cacheList.get(tablename.replaceAll(
					"_geometry", ""));
			List<GeoTiffResultRow> newresult = new ArrayList<GeoTiffResultRow>();
			for (GeoTiffResultRow row : tempresult) {
				if (((int) (row.getData(this.primarykey))) == primkey) {
					newresult.add(row);
				}
			}
			return newresult;
		}
		List<GeoTiffResultRow> result = null;
		try {
			GeoTiffReader2 rdr = new GeoTiffReader2(geoTiffFile.getAbsolutePath(),this.primarykey);
			rdr.read();
			result=rdr.getResults();
			cacheList.put(tablename.replaceAll("_geometry", ""), result);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		}
		return getData(tablename, primkey);
	}

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		File file = new File("/Users/Admin/Downloads/states.kml");
	}

}
