package eu.linkedeodata.geotriples.shapefile;

import java.io.File;

public class TestShapefileParser {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		ShapeFileParser pp =new ShapeFileParser(new File("/Users/admin/Documents/teamkumbarakis/shapefiles/cyprus-latest.shp/buildings.shp"));
		pp.getData("buildings");
		/*(for(ShapefileResultRow r:pp.getData("buildings"))
		{
			System.out.println(r);
		}*/
		
	}

}
