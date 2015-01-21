package org.d2rq.db.schema;

import org.d2rq.db.types.DataType;
import org.d2rq.db.types.SQLGeometry;

import eu.linkedeodata.geotriples.Config;



public class ColumnDef {
	private final Identifier name;
	private DataType dataType;
	private final boolean isNullable;

	public ColumnDef(Identifier name, DataType dataType, boolean isNullable) {
		this.name = name;
		if (dataType == null) {
			if (Config.GEOMETRY) {
				dataType = new SQLGeometry("Geometry");
				Config.GEOMETRY = false;
			}
		}
		this.dataType = dataType;
		this.isNullable = isNullable;
	}

	public Identifier getName() {
		return name;
	}

	public DataType getDataType() {
		return dataType;
	}

	public boolean isNullable() {
		return isNullable;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof ColumnDef)) return false;
		ColumnDef other = (ColumnDef) o;
		return name.equals(other.name) && dataType.equals(other.dataType) && isNullable == other.isNullable;
	}
	
	public int hashCode() {
		if (dataType == null) {
			dataType = new SQLGeometry("Geometry");
		}
		return name.hashCode() ^ dataType.hashCode() ^ (isNullable ? 555 : 556);
	}
}
