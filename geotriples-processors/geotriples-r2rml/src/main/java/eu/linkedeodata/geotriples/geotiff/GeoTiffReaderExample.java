package eu.linkedeodata.geotriples.geotiff;

//package com.vividsolutions.jtsexample.io.gml2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.CoordinateSequences;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.io.gml2.GMLConstants;
import com.vividsolutions.jts.io.gml2.GMLHandler;
import com.vividsolutions.jts.io.gml2.GMLWriter;

/**
 * An example of using the {@link GMLHandler} class to read geometry data out of
 * KML files.
 * 
 * @author mbdavis
 * 
 */
public class GeoTiffReaderExample {
	public static void main(String[] args) throws Exception {
		String filename = "/Users/Admin/Downloads/states.kml";
		GeoTiffReader2 rdr = new GeoTiffReader2(filename,"R2RMLPrimaryKey");
		rdr.read();
	}
}

class GeoTiffReader2 {
	private String filename;
	private String primarykey;
	private List<GeoTiffResultRow> results=new ArrayList<GeoTiffResultRow>();
	public GeoTiffReader2(String filename,String primarykey) {
		this.filename = filename;
		this.primarykey=primarykey;
	}
	public List<GeoTiffResultRow> getResults()
	{
		return results;
	}
	public void read() throws IOException, SAXException {
		XMLReader xr;
		xr = new org.apache.xerces.parsers.SAXParser();
		KMLHandler kmlHandler = new KMLHandler();
		xr.setContentHandler(kmlHandler);
		xr.setErrorHandler(kmlHandler);

		Reader r = new BufferedReader(new FileReader(filename));
		LineNumberReader myReader = new LineNumberReader(r);
		xr.parse(new InputSource(myReader));

		// List geoms = kmlHandler.getGeometries();
	}


private class KMLHandler extends DefaultHandler {
	GeoTiffResultRow row;
	public final String[] SET_VALUES = new String[] { "name", "address",
			"phonenumber", "visibility", "open", "description", "LookAt",
			"Style", "Region", "Geometry" , "MultiGeometry"};
	public final Set<String> LEGALNAMES = new HashSet<String>(
			Arrays.asList(SET_VALUES));
	@SuppressWarnings("rawtypes")
	private List geoms = new ArrayList();;

	private GMLHandler currGeomHandler;
	private String lastEltName = null;
	private String lastEltData = "";
	private GeometryFactory fact = new FixingGeometryFactory();

	private boolean placemarkactive = false;
	private Set<String> visits = new HashSet<String>();

	public KMLHandler() {
		super();
	}

	@SuppressWarnings({ "unused", "rawtypes" })
	public List getGeometries() {
		return geoms;
	}

	/**
	 * SAX handler. Handle state and state transitions based on an element
	 * starting.
	 * 
	 * @param uri
	 *            Description of the Parameter
	 * @param name
	 *            Description of the Parameter
	 * @param qName
	 *            Description of the Parameter
	 * @param atts
	 *            Description of the Parameter
	 * @exception SAXException
	 *                Description of the Exception
	 */
	public void startElement(String uri, String name, String qName,
			Attributes atts) throws SAXException {
		
		if (name.equals("Placemark")) {
			placemarkactive = true;
			row=new GeoTiffResultRow(); //new row result;
		}
		visits.add(name);
		if (placemarkactive && !CollectionUtils.intersection(visits, LEGALNAMES).isEmpty()) {
			//if (name.equalsIgnoreCase(GMLConstants.GML_POLYGON)
				//	|| name.equalsIgnoreCase(GMLConstants.GML_POINT) 
					//|| name.equalsIgnoreCase(GMLConstants.GML_MULTI_GEOMETRY)) {
			if (name.equalsIgnoreCase(GMLConstants.GML_MULTI_GEOMETRY)) {
				currGeomHandler = new GMLHandler(fact, null);
			}
			if (currGeomHandler != null)
				currGeomHandler.startElement(uri, name, qName, atts);
			if (currGeomHandler == null) {
				lastEltName = name;
				// System.out.println(name);
			}
		}
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (placemarkactive
				&& !CollectionUtils.intersection(visits, LEGALNAMES).isEmpty()) {
			if (currGeomHandler != null) {
				currGeomHandler.characters(ch, start, length);
			} else {
				String content = new String(ch, start, length).trim();
				if (content.length() > 0) {
					lastEltData+=content;
					//System.out.println(lastEltName + "= " + content);
				}
			}
		}
	}

	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		if (currGeomHandler != null)
			currGeomHandler.ignorableWhitespace(ch, start, length);
	}

	/**
	 * SAX handler - handle state information and transitions based on ending
	 * elements.
	 * 
	 * @param uri
	 *            Description of the Parameter
	 * @param name
	 *            Description of the Parameter
	 * @param qName
	 *            Description of the Parameter
	 * @exception SAXException
	 *                Description of the Exception
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	public void endElement(String uri, String name, String qName)
			throws SAXException {
		// System.out.println("/" + name);
		//System.out.println("the ena name="+name);
		if (placemarkactive && !CollectionUtils.intersection(visits, LEGALNAMES).isEmpty()
				&& currGeomHandler==null && !lastEltData.isEmpty()) {
			//System.out.println(lastEltName + " " + lastEltData);
			
			row.addPair(lastEltName, lastEltData);
			
		}
		lastEltData="";
		if (name.equals("Placemark")) {
			placemarkactive = false;
			try {
				row.addPair(GeoTiffReader2.this.primarykey, KeyGenerator.Generate());
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
			GeoTiffReader2.this.results.add(row);
		}
		visits.remove(name);
		

		if (currGeomHandler != null) {
			currGeomHandler.endElement(uri, name, qName);

			if (currGeomHandler.isGeometryComplete()) {
				Geometry g = currGeomHandler.getGeometry();
				
				
				
				WKTWriter wkt_writer=new WKTWriter();
				GMLWriter gml_writer = new GMLWriter();
				if (g.getClass().equals(com.vividsolutions.jts.geom.Point.class)) {
					Point geometry = (com.vividsolutions.jts.geom.Point) g;
					row.addPair("isEmpty", geometry.isEmpty());
					row.addPair("isSimple", geometry.isSimple());
					row.addPair("dimension",
							geometry.getCoordinates().length);
					row.addPair("coordinateDimension",
							geometry.getCoordinates().length);
					row.addPair("spatialDimension", geometry.getDimension()); // spatialdimension
																					// <=
																					// dimension
					// System.out.println(geometry.getCoordinate().x + " "
					// +geometry.getCoordinate().z);
					// System.out.println(geometry.get .getSRID());
					// CRS.
					String crs="2311";
					if (crs == null) {
						System.err.println("No SRID specified. Aborting...");
						System.exit(-1);
					}

					row.addPair("asWKT",
							"<http://www.opengis.net/def/crs/EPSG/0/" + crs
									+ ">" + wkt_writer.write(geometry));
					row.addPair("hasSerialization",
							"<http://www.opengis.net/def/crs/EPSG/0/" + crs
									+ ">" + wkt_writer.write(geometry));
					// newrow.addPair("hasSerialization",
					// wkt_writer.write(geometry));
					gml_writer.setSrsName(crs);
					row.addPair("asGML", gml_writer.write(geometry)
							.replaceAll("\n", " "));
					row.addPair("is3D", geometry.getDimension() == 3);
				} else {
					GeometryCollection geometry = (GeometryCollection) g;
					row.addPair("isEmpty", geometry.isEmpty());
					row.addPair("isSimple", geometry.isSimple());
					row.addPair("dimension",
							geometry.getCoordinates().length);
					row.addPair("coordinateDimension",
							geometry.getCoordinates().length);
					row.addPair("spatialDimension", geometry.getDimension()); // spatialdimension
																					// <=
																					// dimension
					// System.out.println(geometry.getCoordinate().x + " "
					// +geometry.getCoordinate().z);
					// System.out.println(geometry.get .getSRID());
					// CRS.
					String crs="2323";
					if (crs == null) {
						System.err.println("No SRID specified. Aborting...");
						System.exit(-1);
					}
					// geometry.getNumPoints();
					// TODO spatialDimension??????
					// TODO coordinateDimension??????
					// Geometry geometry1=
					// (Geometry)sourceGeometryAttribute.getValue();
					// geometry1.transform(arg0, arg1)

					// sourceGeometryAttribute.ge

					row.addPair("asWKT",
							"<http://www.opengis.net/def/crs/EPSG/0/" + crs
									+ ">" + wkt_writer.write(geometry));
					row.addPair("hasSerialization",
							"<http://www.opengis.net/def/crs/EPSG/0/" + crs
									+ ">" + wkt_writer.write(geometry));
					// newrow.addPair("hasSerialization",
					// wkt_writer.write(geometry));
					gml_writer
							.setSrsName("http://www.opengis.net/def/crs/EPSG/0/"
									+ crs);
					row.addPair("asGML", gml_writer.write(geometry)
							.replaceAll("\n", " "));
					row.addPair("is3D", geometry.getDimension() == 3);
				}
				
				
				
				
				
				
				
				
				
				//System.out.println(g);
				
				//System.out.println(ww.write(g));
				geoms.add(g);

				// reset to indicate no longer parsing geometry
				currGeomHandler = null;
			}
		}

	}
}
}

/**
 * A GeometryFactory extension which fixes structurally bad coordinate sequences
 * used to create LinearRings.
 * 
 * @author mbdavis
 * 
 */
@SuppressWarnings("serial")
class FixingGeometryFactory extends GeometryFactory {
	public LinearRing createLinearRing(CoordinateSequence cs) {
		if (cs.getCoordinate(0).equals(cs.getCoordinate(cs.size() - 1)))
			return super.createLinearRing(cs);

		// add a new coordinate to close the ring
		CoordinateSequenceFactory csFact = getCoordinateSequenceFactory();
		CoordinateSequence csNew = csFact.create(cs.size() + 1,
				cs.getDimension());
		CoordinateSequences.copy(cs, 0, csNew, 0, cs.size());
		CoordinateSequences.copyCoord(csNew, 0, csNew, csNew.size() - 1);
		return super.createLinearRing(csNew);
	}

}
