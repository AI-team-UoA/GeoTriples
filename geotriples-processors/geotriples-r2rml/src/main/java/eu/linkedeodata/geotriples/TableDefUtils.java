package eu.linkedeodata.geotriples;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import org.d2rq.db.types.SQLDate;
import org.d2rq.db.types.SQLExactNumeric;
/**
 * Tools for transformations over TableDef.
 * @author Dimitrianos Savva NKUA (d.savva@di.uoa.gr)
 * @author Giannis Vlachopoulos NKUA (johnvl@di.uoa.gr)
 */
public class TableDefUtils {
	public static TableDef addPrimaryKeyDef(TableDef table,String primarykeyname)
	{
		Identifier primarykeyid=Identifier.createDelimited(primarykeyname);
		Key primarykey=Key.create(primarykeyid);
		Set<Key> primkeys=new  HashSet<Key>();
		primkeys.add(primarykey);
		List<ColumnDef> columns=new ArrayList<ColumnDef>(table.getColumns());
		columns.add(new ColumnDef(primarykeyid, new SQLExactNumeric("Int", Types.BIGINT, true), false));
		TableName tablename=table.getName();
		TableDef newtable = new TableDef(tablename ,columns, null, primkeys, new  HashSet<ForeignKey>());
		return newtable;
	}
	public static TableDef addPrimaryKeyDef(TableDef table)
	{
		return addPrimaryKeyDef(table, "gid");
	}
	public static List<TableDef> addPrimaryKeyDef(List<TableDef> tables)
	{
		List<TableDef> newtables=new ArrayList<TableDef>();
		for(TableDef table:tables)
		{
			newtables.add(addPrimaryKeyDef(table, "gid"));
		}
		return newtables;
	}
	
	public static TableDef generateVirtualGeometryTable(TableDef originalTable)throws UnsupportedDataTypeException {
		TableName tablename=TableName.create(null, null, Identifier.createDelimited(originalTable.getName().getTable().getName() + "_geometry"));
		//TableName tablename=TableName.create(null, null, Identifier.createDelimited(originalTable.getName().getTable().getName() ));

		List<ColumnDef> columns = new ArrayList<ColumnDef>();
		//columns.add(originalTable.getColumnDef(Identifier.createDelimited("the_geom")));
		columns.add(new ColumnDef(Identifier.createDelimited("the_geom"),
				TableDefUtils.TranslateDataTypeToSQLType("Geometry"), false));
		Set<Key> primkeys=new  HashSet<Key>();
		
		/*DataType dType = new WKTLiteral("wktLiteral");
		columns.add(new ColumnDef(Identifier.createDelimited("asWKT"), dType, false));
		columns.add(new ColumnDef(Identifier.createDelimited("hasSerialization"), dType, false));
		dType = new GMLLiteral("gmlLiteral");
		columns.add(new ColumnDef(Identifier.createDelimited("asGML"), dType, false));
		dType = new SQLBoolean("Bool");
		columns.add(new ColumnDef(Identifier.createDelimited("isSimple"), dType, false));
		columns.add(new ColumnDef(Identifier.createDelimited("isEmpty"), dType, false));
		columns.add(new ColumnDef(Identifier.createDelimited("is3D"), dType, false));
		dType = new SQLExactNumeric("Integer", Types.INTEGER, true);
		columns.add(new ColumnDef(Identifier.createDelimited("spatialDimension"), dType, false));
		columns.add(new ColumnDef(Identifier.createDelimited("dimension"), dType, false));
		columns.add(new ColumnDef(Identifier.createDelimited("coordinateDimension"), dType, false));
		
		TableDef gTable = new TableDef(tablename, columns, null, primkeys, new HashSet<ForeignKey>());
		return TableDefUtils.addPrimaryKeyDef(gTable);*/
		TableDef gTable = new TableDef(tablename, columns, null, primkeys, new HashSet<ForeignKey>());
		return TableDefUtils.addPrimaryKeyDef(gTable);
		
	}
	public static DataType TranslateDataTypeToSQLType(String name)
			throws UnsupportedDataTypeException {
		if (name.contains(".")) {
			String[] tokens = name.split("[.]");
			name = tokens[tokens.length - 1];
		}
		if (name.equals("String")) {
			return new SQLCharacterString("String", true);
		} else if (name.equals("Bool")) {
			return new SQLBoolean("String");
		} else if (name.equals("Int") || name.equals("Integer")) {
			return new SQLExactNumeric("Int", Types.INTEGER, false);
		} else if (name.equals("Bool")) {
			return new SQLBoolean("Boolean");
		} else if (name.equals("Geometry")) {
			return new SQLCharacterString("Geometry", true);
		}else if (name.equals("MultiPolygon")) {
			return new SQLCharacterString("Geometry", true);
		}else if (name.equals("MultiLineString")) {
			return new SQLCharacterString("Geometry", true);
		} else if (name.equals("Point")) {
			return new SQLCharacterString("Geometry", true);
		} else if (name.equals("Long")) {
			return new SQLExactNumeric("Int", Types.BIGINT, false);
		} else if (name.equals("Double")) {
			return new SQLApproximateNumeric("Double");
		} else if (name.equals("Date")) {  //TODO to be checked !! date xsd??
			return new SQLDate("Date");
		} else {
			throw new UnsupportedDataTypeException("Datatype '" + name + "' is not supported!");
		}
	}
}
