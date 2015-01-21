package eu.linkedeodata.geotriples.geotiff;

import java.util.HashMap;
import java.util.Map;

import org.d2rq.db.ResultRow;
import org.d2rq.db.op.ProjectionSpec;

public class GeoTiffResultRow extends ResultRow{

	Map<String,Object> result = new HashMap<String,Object>();
	
	public GeoTiffResultRow()
	{
		super(null);
	}
	public void addPair(String column,Object data)
	{
		result.put(column, data);
	}
	public Object getData(String column)
	{
		return result.get(column);
	}
	@Override
	public String toString()
	{
		StringBuilder stb=new StringBuilder();
		for(String key:result.keySet())
		{
			stb.append(result.get(key)+"\n");
		}
		stb.append("telos\n");
		return stb.toString();
	}
	@Override
	public String get(ProjectionSpec p)
	{
		return String.valueOf(result.get(p.getColumn().getColumn().getName()));
	}
}
