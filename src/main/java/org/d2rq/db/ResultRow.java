package org.d2rq.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.d2rq.db.op.ProjectionSpec;


/**
 * A result row returned by a database query, presented as a
 * map from SELECT clause entries to string values.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class ResultRow {
	public static final ResultRow NO_ATTRIBUTES = 
			new ResultRow(Collections.<ProjectionSpec,String>emptyMap());

	public static ResultRow createOne(ProjectionSpec column, String value) {
		return new ResultRow(Collections.singletonMap(column, value));
	}
	
	public static ResultRow fromResultSet(ResultSet resultSet, 
			List<ProjectionSpec> projectionSpecs, SQLConnection database) 
	throws SQLException {
		Map<ProjectionSpec,String> result = new HashMap<ProjectionSpec,String>();
		ResultSetMetaData metaData = resultSet.getMetaData();
		for (int i = 0; i < projectionSpecs.size(); i++) {
			ProjectionSpec key = projectionSpecs.get(i);
			int jdbcType = metaData == null ? Integer.MIN_VALUE : metaData.getColumnType(i + 1);
			String name = metaData == null ? "UNKNOWN" : metaData.getColumnTypeName(i + 1);
			result.put(key, database.vendor().getDataType(jdbcType, name.toUpperCase(), -1).value(resultSet, i + 1));
		}
		return new ResultRow(result);
	}
	
	private final Map<ProjectionSpec,String> projectionsToValues;
	
	public ResultRow(Map<ProjectionSpec,String> projectionsToValues) {
		this.projectionsToValues = projectionsToValues;
	}
	
	public String get(ProjectionSpec projection) {
		return (String) this.projectionsToValues.get(projection);
	}
	
	//d2.1
	public Object getObject(ProjectionSpec projection) {
		return null;
	}

	public String toString() {
		List<ProjectionSpec> columns = new ArrayList<ProjectionSpec>(this.projectionsToValues.keySet());
		Collections.sort(columns);
		StringBuffer result = new StringBuffer("{");
		Iterator<ProjectionSpec> it = columns.iterator();
		while (it.hasNext()) {
			ProjectionSpec projection = (ProjectionSpec) it.next();
			result.append(projection.toString());
			result.append(" => '");
			result.append(this.projectionsToValues.get(projection));
			result.append("'");
			if (it.hasNext()) {
				result.append(", ");
			}
		}
		result.append("}");
		return result.toString();
	}
}
