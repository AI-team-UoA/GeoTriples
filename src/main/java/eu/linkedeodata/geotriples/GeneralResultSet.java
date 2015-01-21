package eu.linkedeodata.geotriples;

import java.util.List;

public class GeneralResultSet {
	private int current = -1;
	private List<GeneralResultRow> results;

	public GeneralResultSet(List<GeneralResultRow> results) {
		this.results = results;
	}

	public boolean next() {
		current++;
		if (current < results.size())
			return true;
		else
			return false;
	}

	public GeneralResultRow getCurrent() {
		if (current >= results.size())
			return null;
		return results.get(current);
	}

	public GeneralResultRow getCurrent(List<String> columns) {
		if (current >= results.size())
			return null;
		boolean isnull = false;
		for (String column : columns) {
			if (results.get(current).getData(column) == null) {
				isnull = true;
				break;
			}
		}
		while (isnull) {
			++current;
			isnull = false;
			for (String column : columns) {
				if (results.get(current).getData(column) == null) {
					isnull = true;
					break;
				}
			}
		}
		
		if (current >= results.size())
			return null;
		return results.get(current);
	}

	public Object getObject(String columnname) {
		return results.get(current).getData(columnname);
	}

}
