package eu.linkedeodata.geotriples;

import java.io.File;
import java.util.List;

import org.d2rq.db.schema.TableDef;

public interface GeneralParser {

	public void setFile(File shapefile);

	public File getFile();

	public List<GeneralResultRow> getData(String tablename) throws Exception;
	

	public List<TableDef> getTablesDefs() throws Exception ;
	public List<GeneralResultRow> getData(String tablename, int primkey);
	

}
