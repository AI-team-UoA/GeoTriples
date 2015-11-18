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

import org.d2rq.db.schema.ColumnDef;
import org.d2rq.db.schema.ForeignKey;
import org.d2rq.db.schema.Identifier;
import org.d2rq.db.schema.Key;
import org.d2rq.db.schema.TableDef;
import org.d2rq.db.schema.TableName;
import org.d2rq.db.types.DataType;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import be.ugent.mmlab.rml.tools.PrintTimeStats;
import eu.linkedeodata.geotriples.Config;
import eu.linkedeodata.geotriples.GeneralParser;
import eu.linkedeodata.geotriples.GeneralResultRow;
import eu.linkedeodata.geotriples.KeyGenerator;
import eu.linkedeodata.geotriples.RowHandler;
import eu.linkedeodata.geotriples.TableDefUtils;

public class ShapeFileParser implements GeneralParser {
	protected File shapefile;
	protected String primarykey;
	protected CoordinateReferenceSystem crs;
	protected String crsstring;
	
	public String getCrsString(){
		return  crsstring;
	}

	private Map<String, List<GeneralResultRow>> cacheList = new HashMap<String, List<GeneralResultRow>>();

	public ShapeFileParser(File shapefile, String primkey) {
		this.shapefile = shapefile;
		this.primarykey = primkey;
		this.crs=null;
	}

	public ShapeFileParser(File shapefile) {
		this(shapefile, "gid");
	}

	public void setFile(File shapefile) {
		this.shapefile = shapefile;
	}

	public File getFile() {
		return this.shapefile;
	}

	private List<GeneralResultRow> getData(FeatureSource<?, ?> featureSource)
			throws Exception {
		/* Must not be null in any case! */
		if (featureSource == null) {
			Exception mple = new Exception("featureSource is null");
			mple.printStackTrace();
			throw mple;
		}
		this.crs=featureSource.getSchema()
				.getCoordinateReferenceSystem();
		String crs = org.geotools.gml2.bindings.GML2EncodingUtils
				.epsgCode(this.crs);
		if (crs == null) {
			crs = "" + Config.EPSG_CODE + "";
		}else{
			try {
				int code=CRS.lookupEpsgCode(featureSource.getSchema().getCoordinateReferenceSystem(), true);
				//System.out.println("the code is: "+code);
				crs = String.valueOf(code);
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
			}
		}
		this.crsstring=crs;
		
		FeatureCollection<?, ?> collection = featureSource.getFeatures();
		FeatureIterator<?> iterator = collection.features();
		List<GeneralResultRow> resultlist = new ArrayList<GeneralResultRow>();
		try {
			double total_thematic_duration=0.0;
			double total_duration_geom=0.0;
			while (iterator.hasNext()) {
				
				long startTime = System.nanoTime();
				Feature feature = iterator.next();
				GeneralResultRow newrow = new GeneralResultRow(); // new row
				for (Property p : feature.getProperties()) {
					newrow.addPair(p.getName().getLocalPart(), p.getValue());
				}
				long endTime = System.nanoTime();
				double duration = (endTime - startTime) / 1000000; // divide by
															// 1000000 to
															// get
															// milliseconds.
				total_thematic_duration += duration;
				PrintTimeStats.printTime("Read Thematic from file", duration);
				
				newrow.addPair(primarykey, KeyGenerator.Generate()); // Add
																		// primary
																		// key
				startTime = System.nanoTime();
				GeometryAttribute sourceGeometryAttribute = feature
						.getDefaultGeometryProperty();
				endTime = System.nanoTime();
				duration = (endTime - startTime) ; // divide by
																	// 1000000 to
																	// get
																	// milliseconds.
				total_duration_geom+= duration;
				PrintTimeStats.printTime("Read Geometry from file", duration);
				

				//RowHandler.handleGeometry(newrow, (Geometry)sourceGeometryAttribute.getValue(), crs);
				resultlist.add(newrow);
			}
			PrintTimeStats.printTime("Read Thematic data (total) from file", total_thematic_duration);
			PrintTimeStats.printTime("Read Geometries (total) from file", total_duration_geom);
		} finally {
			iterator.close();
		}

		return resultlist;
	}

	public List<GeneralResultRow> getData(String tablename) throws Exception {
		if (cacheList.containsKey(tablename)) {
			return cacheList.get(tablename);
		} else if (cacheList.containsKey(tablename.replaceAll("_geometry", ""))) {
			return cacheList.get(tablename.replaceAll("_geometry", ""));
		}
		DataStore dataStore = null;
		List<GeneralResultRow> result = null;
		try {
			Map<String, URL> connect = new HashMap<String, URL>();
			connect.put("url", shapefile.toURI().toURL());
			dataStore = DataStoreFinder.getDataStore(connect);
			FeatureSource<?, ?> featureSource = dataStore.getFeatureSource(tablename
					.replaceAll("_geometry", ""));
			if (featureSource != null) {
				result = getData(featureSource);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			dataStore.dispose();
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
				FeatureSource<?, ?> featureSource = dataStore
						.getFeatureSource(typeName);

				FeatureType ft = featureSource.getSchema();
				columns = new ArrayList<ColumnDef>();
				for (PropertyDescriptor property : ft.getDescriptors()) {

					Identifier identifier = Identifier.createDelimited(property
							.getName().getLocalPart());
					DataType datatype = TableDefUtils.TranslateDataTypeToSQLType((property
							.getType().getBinding().getName()));
					ColumnDef col = new ColumnDef(identifier, datatype,
							property.isNillable());
					columns.add(col);
				}
				//Identifier identifier = Identifier.createDelimited("gid");
				//ColumnDef col = new ColumnDef(identifier, TranslateDataTypeToSQLType("Int"), false);
				//columns.add(col);
				//primkeys.add(Key.create(identifier));
				TableName tablename = TableName.create(null, null,
						Identifier.create(true, dataStore.getTypeNames()[0]));
				onlytable = new TableDef(tablename, columns, null, primkeys,
						new HashSet<ForeignKey>());
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
			List<GeneralResultRow> tempresult=cacheList.get(tablename);
			List<GeneralResultRow> newresult=new ArrayList<GeneralResultRow>();
			for(GeneralResultRow row:tempresult)
			{
				if(((int)(row.getData(this.primarykey))) == primkey)
				{
					newresult.add(row);
				}
			}
			return newresult;
		} else if (cacheList.containsKey(tablename.replaceAll("_geometry", ""))) {
			List<GeneralResultRow> tempresult= cacheList.get(tablename.replaceAll("_geometry", ""));
			List<GeneralResultRow> newresult=new ArrayList<GeneralResultRow>();
			for(GeneralResultRow row:tempresult)
			{
				if(((int)(row.getData(this.primarykey))) == primkey)
				{
					newresult.add(row);
				}
			}
			return newresult;
		}
		DataStore dataStore = null;
		List<GeneralResultRow> result = null;
		try {
			Map<String, URL> connect = new HashMap<String, URL>();
			connect.put("url", shapefile.toURI().toURL());
			dataStore = DataStoreFinder.getDataStore(connect);
			FeatureSource<?, ?> featureSource = dataStore.getFeatureSource(tablename
					.replaceAll("_geometry", ""));
			if (featureSource != null) {
				result = getData(featureSource);
				cacheList.put(tablename.replaceAll("_geometry", ""), result);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			dataStore.dispose();
		}
		return getData(tablename, primkey);
	}

}
