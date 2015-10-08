/**
 * @author Dimitrianos Savva National and Kapodistrian University of Athens
 * @author Giannis Vlachopoulos National and Kapodistrian University of Athens
 */
package eu.linkedeodata.geotriples.shapefile;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.d2rq.db.schema.ColumnDef;
import org.d2rq.db.schema.ForeignKey;
import org.d2rq.db.schema.Identifier;
import org.d2rq.db.schema.Key;
import org.d2rq.db.schema.TableDef;
import org.d2rq.db.schema.TableName;
import org.d2rq.db.types.DataType;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;

import eu.linkedeodata.geotriples.GeneralParser;
import eu.linkedeodata.geotriples.GeneralResultRow;
import eu.linkedeodata.geotriples.KeyGenerator;
import eu.linkedeodata.geotriples.TableDefUtils;

public class ShapeFileParserGDAL extends ShapeFileParser implements GeneralParser {

	public String getCrsString() {
		return crsstring;
	}

	private Map<String, List<GeneralResultRow>> cacheList = new HashMap<String, List<GeneralResultRow>>();

	public ShapeFileParserGDAL(File shapefile, String primkey) {
		super(shapefile, primkey);
	}

	public ShapeFileParserGDAL(File shapefile) {
		this(shapefile, "gid");
	}

	public void setFile(File shapefile) {
		this.shapefile = shapefile;
	}

	public File getFile() {
		return this.shapefile;
	}

	private List<GeneralResultRow> getData(Layer poLayer) throws Exception {

		be.ugent.mmlab.rml.function.Config.EPSG_CODE = poLayer.GetSpatialRef().GetAttrValue("AUTHORITY", 1);

		// System.out.println(poLayer.GetSpatialRef().GetAttrValue("AUTHORITY",
		// 1));
		KeyGenerator keygen = new KeyGenerator();
		org.gdal.ogr.Feature feature;
		List<GeneralResultRow> resultlist = new ArrayList<GeneralResultRow>();
		for (int f = 0; f < poLayer.GetFeatureCount(); ++f) {
			// while ((feature = poLayer.GetNextFeature()) != null) {
			feature = poLayer.GetNextFeature();
			GeneralResultRow newrow = new GeneralResultRow(); // new row
			int fieldssize = feature.GetFieldCount();
			for (int i = 0; i < fieldssize; ++i) {
				String type = feature.GetFieldDefnRef(i).GetFieldTypeName(feature.GetFieldType(i));
				if (type.equals("String")) {
					newrow.addPair(feature.GetFieldDefnRef(i).GetName(), feature.GetFieldAsString(i));
					// System.out.println(feature.GetFieldAsString(i));
				} else if (type.equals("StringList")) {
					// System.out.println(feature.GetFieldAsStringList(i));
					// for (String s : feature.GetFieldAsStringList(i))
					// {
					// System.out.print(s);
					// System.out.println();
					// }
				} else if (type.equals("Integer")) {
					newrow.addPair(feature.GetFieldDefnRef(i).GetName(), feature.GetFieldAsInteger(i));
					// System.out.println(feature.GetFieldAsInteger(i));
				} else if (type.equals("IntegerList")) {
					// for (Integer s :
					// feature.GetFieldAsIntegerList(i)) {
					// System.out.print(s);
					// System.out.println();
					// }
				} else if (type.equals("Real")) {
					newrow.addPair(feature.GetFieldDefnRef(i).GetName(), feature.GetFieldAsDouble(i));
					// System.out.println(feature.GetFieldAsDouble(i));
				} else if (type.equals("RealList")) {
					// System.out.println(feature.GetFieldAsDoubleList(i));
				} /*
					 * else if (type.equals("(unknown)")) {
					 * System.out.println(feature.GetGeometryRef()); }
					 */

				// System.out.println(feature.GetFieldAsString(i));
				// System.out.println(feature.GetFieldAsInteger(i));

			}
			org.gdal.ogr.Geometry geom = feature.GetGeometryRef();
			newrow.addPair("the_geom", geom);
			try {
				newrow.addPair(primarykey, keygen.Generate());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println(geom.ExportToWkt());

			//feature.delete();
			//feature = null;
			resultlist.add(newrow);
		}
		return resultlist;
	}

	public List<GeneralResultRow> getData(String tablename) throws Exception {
		if (cacheList.containsKey(tablename)) {
			return cacheList.get(tablename);
		} else if (cacheList.containsKey(tablename.replaceAll("_geometry", ""))) {
			return cacheList.get(tablename.replaceAll("_geometry", ""));
		}

		ogr.DontUseExceptions();

		/*
		 * --------------------------------------------------------------------
		 */
		/* Register format(s). */
		/*
		 * --------------------------------------------------------------------
		 */
		if (ogr.GetDriverCount() == 0)
			ogr.RegisterAll();
		String pszDataSource = null;
		Vector papszLayers = new Vector();
		DataSource poDS;
		pszDataSource = shapefile.getAbsolutePath();
		poDS = ogr.Open(pszDataSource, false);

		/*
		 * ----------------------------------------------------------------- ---
		 */
		/* Report failure */
		/*
		 * ----------------------------------------------------------------- ---
		 */
		if (poDS == null) {
			System.err.println(
					"FAILURE:\n" + "Unable to open datasource ` " + pszDataSource + "' with the following drivers.");

			for (int iDriver = 0; iDriver < ogr.GetDriverCount(); iDriver++) {
				System.err.println("  . " + ogr.GetDriver(iDriver).GetName());
			}

			System.exit(1);
		}

		int nLayerCount = 0;
		Layer[] papoLayers = null;
		if (papszLayers.size() == 0) {
			nLayerCount = poDS.GetLayerCount();
			papoLayers = new Layer[nLayerCount];

			for (int iLayer = 0; iLayer < nLayerCount; iLayer++) {
				Layer poLayer = poDS.GetLayer(iLayer);

				if (poLayer == null) {
					System.err.println("FAILURE: Couldn't fetch advertised layer " + iLayer + "!");
					System.exit(1);
				}

				papoLayers[iLayer] = poLayer;
			}
		}
		List<GeneralResultRow> result = null;
		for (int iLayer = 0; iLayer < nLayerCount; iLayer++) {
			Layer poLayer = papoLayers[iLayer];
			if (!poLayer.GetName().equals(tablename.replaceAll("_geometry", ""))) {
				continue;
			} else {
				result = getData(poLayer);
				break;
			}
		}
		cacheList.put(tablename.replaceAll("_geometry", ""), result);
		return result;
	}

	public List<TableDef> getTablesDefs() throws Exception {
		DataStore dataStore = null;
		List<ColumnDef> columns = null;
		TableDef onlytable = null;
		Set<Key> primkeys = new HashSet<Key>();
		List<TableDef> tables = new ArrayList<TableDef>();
		try {
			Map<String, URL> connect = new HashMap<String, URL>();
			connect.put("url", shapefile.toURI().toURL());
			dataStore = DataStoreFinder.getDataStore(connect);
			String[] typeNames = dataStore.getTypeNames();
			for (int i = 0; i < typeNames.length; ++i) {
				String typeName = typeNames[i];
				FeatureSource<?, ?> featureSource = dataStore.getFeatureSource(typeName);

				FeatureType ft = featureSource.getSchema();
				columns = new ArrayList<ColumnDef>();
				for (PropertyDescriptor property : ft.getDescriptors()) {

					Identifier identifier = Identifier.createDelimited(property.getName().getLocalPart());
					DataType datatype = TableDefUtils
							.TranslateDataTypeToSQLType((property.getType().getBinding().getName()));
					ColumnDef col = new ColumnDef(identifier, datatype, property.isNillable());
					columns.add(col);
				}
				// Identifier identifier = Identifier.createDelimited("gid");
				// ColumnDef col = new ColumnDef(identifier,
				// TranslateDataTypeToSQLType("Int"), false);
				// columns.add(col);
				// primkeys.add(Key.create(identifier));
				TableName tablename = TableName.create(null, null,
						Identifier.create(true, dataStore.getTypeNames()[0]));
				onlytable = new TableDef(tablename, columns, null, primkeys, new HashSet<ForeignKey>());
				tables.add(onlytable);
			}

		} catch (Throwable e) {
			throw new Exception(e.getMessage());
		} finally {
			dataStore.dispose();

		}
		return tables;

	}

	public List<GeneralResultRow> getData(String tablename, int primkey) {
		if (cacheList.containsKey(tablename)) {
			List<GeneralResultRow> tempresult = cacheList.get(tablename);
			List<GeneralResultRow> newresult = new ArrayList<GeneralResultRow>();
			for (GeneralResultRow row : tempresult) {
				if (((int) (row.getData(this.primarykey))) == primkey) {
					newresult.add(row);
				}
			}
			return newresult;
		} else if (cacheList.containsKey(tablename.replaceAll("_geometry", ""))) {
			List<GeneralResultRow> tempresult = cacheList.get(tablename.replaceAll("_geometry", ""));
			List<GeneralResultRow> newresult = new ArrayList<GeneralResultRow>();
			for (GeneralResultRow row : tempresult) {
				if (((int) (row.getData(this.primarykey))) == primkey) {
					newresult.add(row);
				}
			}
			return newresult;
		}

		ogr.DontUseExceptions();

		/*
		 * --------------------------------------------------------------------
		 */
		/* Register format(s). */
		/*
		 * --------------------------------------------------------------------
		 */
		if (ogr.GetDriverCount() == 0)
			ogr.RegisterAll();
		String pszDataSource = null;
		Vector papszLayers = new Vector();
		DataSource poDS;

		pszDataSource = shapefile.getAbsolutePath();

		poDS = ogr.Open(pszDataSource, false);

		/*
		 * ----------------------------------------------------------------- ---
		 */
		/* Report failure */
		/*
		 * ----------------------------------------------------------------- ---
		 */
		if (poDS == null) {
			System.err.println(
					"FAILURE:\n" + "Unable to open datasource ` " + pszDataSource + "' with the following drivers.");

			for (int iDriver = 0; iDriver < ogr.GetDriverCount(); iDriver++) {
				System.err.println("  . " + ogr.GetDriver(iDriver).GetName());
			}

			System.exit(1);
		}

		int nLayerCount = 0;
		Layer[] papoLayers = null;
		if (papszLayers.size() == 0) {
			nLayerCount = poDS.GetLayerCount();
			papoLayers = new Layer[nLayerCount];

			for (int iLayer = 0; iLayer < nLayerCount; iLayer++) {
				Layer poLayer = poDS.GetLayer(iLayer);

				if (poLayer == null) {
					System.err.println("FAILURE: Couldn't fetch advertised layer " + iLayer + "!");
					System.exit(1);
				}

				papoLayers[iLayer] = poLayer;
			}
		}
		List<GeneralResultRow> result = null;
		for (int iLayer = 0; iLayer < nLayerCount; iLayer++) {
			Layer poLayer = papoLayers[iLayer];
			if (!poLayer.GetName().equals(tablename.replaceAll("_geometry", ""))) {
				continue;
			} else {
				try {
					result = getData(poLayer);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				cacheList.put(tablename.replaceAll("_geometry", ""), result);
				break;
			}
		}

		return getData(tablename, primkey);
	}

}
