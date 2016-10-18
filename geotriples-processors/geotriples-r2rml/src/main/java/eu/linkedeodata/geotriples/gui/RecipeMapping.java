package eu.linkedeodata.geotriples.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeMapping {
	private String classThematic;
	private String classGeometry;
	private boolean existThematicTable=false;
	private boolean existGeometryTable=false;
	
	private Map<String,List<ColumnReceipt>> receiptThematic=new HashMap<String,List<ColumnReceipt>>();
	private Map<String,List<ColumnReceipt>> receiptGeometry=new HashMap<String,List<ColumnReceipt>>();
	
	public void setReceiptThematic(java.util.List<ColumnReceipt> columnsfromshapefile)
	{
		existThematicTable=true;
		for(Object column:columnsfromshapefile)
		{
			if(!receiptThematic.containsKey(((ColumnReceipt)column).getColumnName()))
			{
				receiptThematic.put(((ColumnReceipt)column).getColumnName(), new ArrayList<ColumnReceipt>());
			}
			receiptThematic.get(((ColumnReceipt)column).getColumnName()).add((ColumnReceipt) column);
		}
		
	}
	public Map<String,List<ColumnReceipt>> getReceiptThematic()
	{
		return receiptThematic;
	}
	public void setReceiptGeometry(java.util.List<ColumnReceipt> columnsfromshapefilegeometry)
	{
		existGeometryTable=true;
		for(Object column:columnsfromshapefilegeometry)
		{
			if(!receiptGeometry.containsKey(((ColumnReceipt)column).getColumnName()))
			{
				receiptGeometry.put(((ColumnReceipt)column).getColumnName(), new ArrayList<ColumnReceipt>());
			}
			receiptGeometry.get(((ColumnReceipt)column).getColumnName()).add((ColumnReceipt) column);
		}
	}
	public Map<String,List<ColumnReceipt>> getReceiptGeometry()
	{
		return receiptGeometry;
	}
	public String getClassThematic() {
		return classThematic;
	}
	public void setClassThematic(String classThematic) {
		existThematicTable=true;
		this.classThematic = classThematic;
	}
	public String getClassGeometry() {
		return classGeometry;
	}
	public void setClassGeometry(String classGeometry) {
		existGeometryTable=true;
		this.classGeometry = classGeometry;
	}
	public boolean existThematicTable() {
		return existThematicTable;
	}
	public boolean existGeometryTable() {
		return existGeometryTable;
	}
	
}
