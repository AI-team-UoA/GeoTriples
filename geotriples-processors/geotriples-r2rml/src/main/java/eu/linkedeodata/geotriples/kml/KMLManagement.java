package eu.linkedeodata.geotriples.kml;

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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.io.gml2.GMLConstants;
import com.vividsolutions.jts.io.gml2.GMLHandler;

import eu.linkedeodata.geotriples.GeneralResultRow;
import eu.linkedeodata.geotriples.KeyGenerator;

/**
 * An example of using the {@link GMLHandler} class to read geometry data out of
 * KML files.
 * 
 * @author mbdavis
 * 
 */
public class KMLManagement {
	public static void main(String[] args) throws Exception {
		String filename = "/Users/Admin/Downloads/states.kml";
		KMLReader rdr = new KMLReader(filename, "R2RMLPrimaryKey");
		rdr.read();
	}
}

class KMLReader {
	private String filename;
	private String primarykey;
	private String crs;
	private List<GeneralResultRow> results = new ArrayList<GeneralResultRow>();

	public KMLReader(String filename, String primarykey, String crs) {
		this.filename = filename;
		this.primarykey = primarykey;
		this.crs = crs;
	}

	public KMLReader(String filename, String primarykey) {
		this(filename, primarykey, "4326"); // Use default coordinate reference
											// system for KML // as google says
	}

	public String getCrs() {
		return crs;
	}

	public void setCrs(String crs) {
		this.crs = crs;
	}

	public List<GeneralResultRow> getResults() {
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
		GeneralResultRow row;
		public final String[] SET_VALUES = new String[] { "name", "address",
				"phonenumber", "visibility", "open", "description", "LookAt",
				"Style", "Region", GMLConstants.GML_MULTI_GEOMETRY,
				GMLConstants.GML_POINT, GMLConstants.GML_MULTI_POINT,
				GMLConstants.GML_POLYGON, GMLConstants.GML_MULTI_POLYGON,
				GMLConstants.GML_LINEARRING, GMLConstants.GML_LINESTRING,
				GMLConstants.GML_MULTI_LINESTRING };
		public final Set<String> LEGALNAMES = new HashSet<String>(
				Arrays.asList(SET_VALUES));

		private GMLHandler currGeomHandler;
		private String lastEltName = null;
		private String lastEltData = "";
		private GeometryFactory fact = new FixingGeometryFactory();

		private boolean placemarkactive = false;
		private Set<String> featurevisits = new HashSet<String>();
		private boolean geometryfound = false;
		private String geometryfound_str = "";

		public KMLHandler() {
			super();
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
				row = new GeneralResultRow(); // new row result;
			}
			featurevisits.add(name);
			if (placemarkactive
					&& !CollectionUtils.intersection(featurevisits, LEGALNAMES)
							.isEmpty()) {
				// if (name.equalsIgnoreCase(GMLConstants.GML_POLYGON)
				// || name.equalsIgnoreCase(GMLConstants.GML_POINT)
				// || name.equalsIgnoreCase(GMLConstants.GML_MULTI_GEOMETRY)) {
				// System.out.println(name);
				if (!geometryfound
						&& (name.equalsIgnoreCase(GMLConstants.GML_MULTI_GEOMETRY)
								|| name.equalsIgnoreCase(GMLConstants.GML_POINT)
								|| name.equalsIgnoreCase(GMLConstants.GML_MULTI_POINT)
								|| name.equalsIgnoreCase(GMLConstants.GML_POLYGON)
								|| name.equalsIgnoreCase(GMLConstants.GML_MULTI_POLYGON)
								|| name.equalsIgnoreCase(GMLConstants.GML_LINEARRING)
								|| name.equalsIgnoreCase(GMLConstants.GML_LINESTRING) || name
									.equalsIgnoreCase(GMLConstants.GML_MULTI_LINESTRING))) {
					geometryfound = true;
					geometryfound_str = name;
					currGeomHandler = new GMLHandler(fact, null);
				}
				if (currGeomHandler != null) {

					currGeomHandler.startElement(uri, name, qName, atts);
				}
				if (currGeomHandler == null) {
					lastEltName = name;
					// System.out.println(name);
				}
			}
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (placemarkactive
					&& !CollectionUtils.intersection(featurevisits, LEGALNAMES)
							.isEmpty()) {
				if (currGeomHandler != null) {
					currGeomHandler.characters(ch, start, length);
				} else {
					String content = new String(ch, start, length).trim();
					if (content.length() > 0) {
						lastEltData += content;
						// System.out.println(lastEltName + "= " + content);
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
		 * SAX handler - handle state information and transitions based on
		 * ending elements.
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
		public void endElement(String uri, String name, String qName)
				throws SAXException {
			// System.out.println("/" + name);
			// System.out.println("the ena name="+name);
			if (placemarkactive
					&& !CollectionUtils.intersection(featurevisits, LEGALNAMES)
							.isEmpty() && currGeomHandler == null
					&& !lastEltData.isEmpty()) {
				// System.out.println(lastEltName + " " + lastEltData);

				row.addPair(lastEltName, lastEltData);

			}
			lastEltData = "";
			if (name.equals("Placemark")) {
				placemarkactive = false;
				try {
					row.addPair(KMLReader.this.primarykey,
							KeyGenerator.Generate());
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
				KMLReader.this.results.add(row);
			}
			featurevisits.remove(name);

			if (currGeomHandler != null) {
				if (name.equals(geometryfound_str)) {
					geometryfound = false;
				}
				currGeomHandler.endElement(uri, name, qName);

				if (currGeomHandler.isGeometryComplete()) {
					Geometry g = currGeomHandler.getGeometry();
					row.addPair("the_geom", g);
					//RowHandler.handleGeometry(row, g, KMLReader.this.crs);
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
class FixingGeometryFactory extends GeometryFactory {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
