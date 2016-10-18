/**
 * @author Dimitrianos Savva National and Kapodistrian University of Athens
 * @author Giannis Vlachopoulos National and Kapodistrian University of Athens
 */
package eu.linkedeodata.geotriples.kml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.UnsupportedDataTypeException;
import javax.xml.parsers.ParserConfigurationException;

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
import org.geotools.kml.KMLConfiguration;
import org.geotools.xml.Parser;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;

import eu.linkedeodata.geotriples.GeneralParser;
import eu.linkedeodata.geotriples.GeneralResultRow;

@SuppressWarnings("unused")
public class KMLParser implements GeneralParser{
	private File file;
	private String primarykey;
	private Map<String, List<GeneralResultRow>> cacheList = new HashMap<String, List<GeneralResultRow>>();
	
	public KMLParser(File file, String primkey) {
		this.file = file;
		this.primarykey = primkey;
	}

	public KMLParser(File file) {
		this(file, "R2RMLPrimaryKey");
	}

	public void setGeneralFile(File Generalfile) {
		this.file = Generalfile;
	}

	public List<GeneralResultRow> getData(String tablename) throws Exception {
		if (cacheList.containsKey(tablename)) {
			return cacheList.get(tablename);
		} else if (cacheList.containsKey(tablename.replaceAll("_geometry", ""))) {
			return cacheList.get(tablename.replaceAll("_geometry", ""));
		}
		List<GeneralResultRow> result = null;
		try {
			KMLReader rdr = new KMLReader(file.getAbsolutePath(),this.primarykey);
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
		Set<Key> primkeys = new HashSet<Key>();
		List<TableDef> tables = new ArrayList<TableDef>();
		Identifier[] identifiers= {Identifier.createDelimited("name"),
				Identifier.createDelimited("visibility"),
				Identifier.createDelimited("open"),
				Identifier.createDelimited("phoneNumber"),
				Identifier.createDelimited("description"),
				Identifier.createDelimited("LookAt"),
				Identifier.createDelimited("Style"),
				Identifier.createDelimited("Region")};
		
		DataType[] datatypes ={
		TranslateDataTypeToSQLType("string"),
		TranslateDataTypeToSQLType("bool"),
		TranslateDataTypeToSQLType("bool"),
		TranslateDataTypeToSQLType("string"),
		TranslateDataTypeToSQLType("string"),
		TranslateDataTypeToSQLType("point"),
		TranslateDataTypeToSQLType("string"),
		TranslateDataTypeToSQLType("string")};

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
			List<GeneralResultRow> tempresult = cacheList.get(tablename.replaceAll(
					"_geometry", ""));
			List<GeneralResultRow> newresult = new ArrayList<GeneralResultRow>();
			for (GeneralResultRow row : tempresult) {
				if (((int) (row.getData(this.primarykey))) == primkey) {
					newresult.add(row);
				}
			}
			return newresult;
		}
		List<GeneralResultRow> result = null;
		try {
			KMLReader rdr = new KMLReader(file.getAbsolutePath(),this.primarykey);
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

		File file = new File("/Users/Admin/Downloads/states.General");
		FileInputStream fis = null;

		try {
			fis = new FileInputStream(file);

			System.out.println("Total file size to read (in bytes) : "
					+ fis.available());

			Parser parser = new Parser(new KMLConfiguration());
			SimpleFeature f = (SimpleFeature) parser.parse(fis);
			Collection<?> placemarks = (Collection<?>) f.getAttribute("Feature");

			for (Object mark : placemarks) {

				SimpleFeature feature = (SimpleFeature) mark;
				for (Property p : feature.getProperties()) {
					System.out.println(p.getName() + " "
							+ p.getType().getBinding().getSimpleName());
					if (p.getName().getLocalPart().contains("Region")) {
						System.out.println(p.getValue());
					}
					/*
					 * if(p.getName().getLocalPart().contains("Geometry")) {
					 * GeometryCollection gg=(GeometryCollection)p;
					 * System.out.println(gg.toText()); }
					 */
				}
				for(Object a : feature.getAttributes())
				{
					System.out.println(a);
				}
				//GeometryCollection g = (GeometryCollection) feature.getAttribute("MultiGeometry");
				
				Object g = (Object) feature.getAttribute("Geometry");
				//Object g = (Object) feature.getDefaultGeometry();
				System.out.println(g.getClass());
				//System.out.println(g.getGeometryN(0));
				//System.out.println(g.getGeometryN(1));

				
				System.out.println(g);
				Geometry k=(Geometry)g;
				System.out.println(k.getNumGeometries());
				for (int i = 0; i < k.getNumGeometries(); i++) {
					System.out.println(k.getGeometryN(i));
				}

				System.out.println("----------------");
				// Set<Entry<Object, Object>> entrySet =
				// feature.getUserData().entrySet();
				// System.out.printf("props:{} \n attributes: {}",
				// feature.getProperties(), feature.getAttributes());
				// System.out.printf("{}: {}", feature.getAttribute("name"),
				// feature.getAttribute("description"));
			}
			// SimpleFeature f = (SimpleFeature) parser.parse( fis );
			// Collection placemarks = (Collection<SimpleFeature>)
			// f.getAttribute("Feature");

		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	@Override
	public void setFile(File shapefile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public File getFile() {
		return this.file;
	}

}
