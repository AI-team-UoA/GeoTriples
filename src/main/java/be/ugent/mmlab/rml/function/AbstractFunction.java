package be.ugent.mmlab.rml.function;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.geojson.geom.GeometryJSON;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.xml.sax.SAXException;

import be.ugent.mmlab.rml.core.MalformedGeometryException;
import be.ugent.mmlab.rml.tools.CacheFifo;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.gml2.GMLReader;

public abstract class AbstractFunction {
	private final static Log log = LogFactory.getLog(AbstractFunction.class);
	protected Geometry computeGeometry(String value, QLTerm term)
			throws SAXException, IOException, ParserConfigurationException,
			NoSuchAuthorityCodeException, FactoryException,
			MalformedGeometryException {
		Geometry geometry = null;
		WKTReader wktReader = new WKTReader();
		try {
			geometry = wktReader.read(value);
			if (geometry != null)
				return geometry;
		} catch (ParseException e1) {
			// just continue
		}

		switch (term) {
		case XPATH_CLASS:

			// I wrap the geometric value read with these tags so that the GML3
			// parser perceives it as an autonomous dataset
			// value =
			// "<gml:FeatureCollection xmlns:gml=\"http://www.opengis.net/gml\" ><gml:featureMember><a>"
			// + value;
			// value = value +
			// "</a></gml:featureMember></gml:FeatureCollection>";
			// System.out.println(value);

			// System.out.println(value);

			if (Config.useGML3) {
				org.geotools.xml.Configuration configuration = new org.geotools.gml3.GMLConfiguration();
				org.geotools.xml.Parser parser = new org.geotools.xml.Parser(
						configuration);

				StringBuffer sbuffer = new StringBuffer(
						"<gml:FeatureCollection xmlns:gml=\"http://www.opengis.net/gml\" ><gml:featureMember><a>");
				// StringBuffer sbuffer = new
				// StringBuffer("<gml:featureMember xmlns:gml=\"http://www.opengis.net/gml\">");
				sbuffer.append(value);
				sbuffer.append("</a></gml:featureMember></gml:FeatureCollection>");
				// sbuffer.append("</gml:featureMember>");
				InputStream in = new ByteArrayInputStream(sbuffer.toString()
						.getBytes());
				Object oo = parser.parse(in);
				// System.out.println(oo.getClass());
				// System.out.println(oo);
				// System.out.println("------------1");
				/*
				 * HashMap<?, ?> kk =(HashMap<?, ?>) parser.parse(in);
				 * for(Object o:kk.keySet()){ System.out.println(kk.get(o)); }
				 */

				org.geotools.feature.DefaultFeatureCollection dfc = (org.geotools.feature.DefaultFeatureCollection) oo;
				SimpleFeature aa = dfc.features().next();
				// System.out.println(aa.getDefaultGeometry());
				geometry = (Geometry) aa.getDefaultGeometry();
				// System.out.println("------------2");
				/*
				 * StringBuffer sbuffer = new StringBuffer(
				 * "<gml:FeatureCollection xmlns:gml=\"http://www.opengis.net/gml\" ><gml:featureMember><a>"
				 * ); sbuffer.append(value);
				 * sbuffer.append("</a></gml:featureMember></gml:FeatureCollection>"
				 * );
				 * 
				 * org.geotools.GML gml = new org.geotools.GML(Version.GML3);
				 * SimpleFeatureIterator iter = gml.decodeFeatureIterator(in);
				 * SimpleFeature feature = iter.next(); geometry = (Geometry)
				 * feature.getDefaultGeometryProperty().;
				 */
			} else if (Config.useGML2) {
				value = convert2GML2(value);
				GMLReader gmlreader = new GMLReader();
				geometry = gmlreader.read(value, null);

			} else if (Config.useKML_22) {
				org.geotools.xml.Configuration configuration = new org.geotools.kml.v22.KMLConfiguration();
				org.geotools.xml.Parser parser = new org.geotools.xml.Parser(
						configuration);

				StringBuffer sbuffer = new StringBuffer(
						"<kml:Folder xmlns:kml=\"http://www.opengis.net/kml/2.2\"> <kml:Placemark>");
				// StringBuffer sbuffer = new
				// StringBuffer("<gml:featureMember xmlns:gml=\"http://www.opengis.net/gml\">");
				sbuffer.append(value.replaceAll("<(?=\\w+)", "<kml:").replace(
						"</", "</kml:"));
				sbuffer.append("</kml:Placemark></kml:Folder>");
				// sbuffer.append("</gml:featureMember>");
				InputStream in = new ByteArrayInputStream(sbuffer.toString()
						.getBytes());
				Object oo = parser.parse(in);

				SimpleFeature dfc = (SimpleFeature) oo;
				ArrayList<?> feature = (ArrayList<?>) dfc
						.getAttribute("Feature");
				SimpleFeature sf = (SimpleFeature) feature.get(0);

				// System.out.println(aa.getDefaultGeometry());
				geometry = (Geometry) sf.getAttribute("Geometry");
			}

			// ElementInstance instance =null;
			// Node node;
			// FeatureTypeCache ftCache;
			// BindingWalkerFactory bwFactory;
			// GML3ParsingUtils.parseFeature(instance, node, value, ftCache,
			// bwFactory)
			// DataUtilities.crea createFeature(featureType, value);
			// SimpleFeatureBuilder.
			// SimpleFeature f=new SimpleFeatureImpl(values, featureType, id)
			if (geometry == null) {
				if (log.isDebugEnabled()) {
					try {
						throw new Exception(
								"Could not parse the geometry! Value is: "
										+ value);
					} catch (Exception ee) {
						ee.printStackTrace();
					}
				} else {
					if (Config.useKML_22) {
						log.warn("Could not parse the geometry! Is there a placeless placemark in KML file?");
					} else {
						log.warn("Could not parse the geometry! Please report this incident to dimis");
					}

				}
			}
			return geometry;
		case CSV_CLASS:
			throw new UnsupportedOperationException(
					"Reading geometries form CSV implementation is missing");
		case JSONPATH_CLASS:
			GeometryJSON g = new GeometryJSON();

			InputStream istream = new ByteArrayInputStream(
					value.getBytes(StandardCharsets.UTF_8));

			geometry = g.read(istream);

			return geometry;
		case SHP_CLASS:
			throw new UnsupportedOperationException(
					"Reading geometries form Shapefile (in String format) implementation is missing");
		default:
			throw new MalformedGeometryException(
					"GeoTriples cannot recognize this type of geometry");
		}
	}
	static CacheFifo<Object,Geometry> cache=new CacheFifo<>(44); 
	protected Geometry computeGeometry(Object object, QLTerm term)
			throws SAXException, IOException, ParserConfigurationException,
			NoSuchAuthorityCodeException, FactoryException,
			MalformedGeometryException {
		
		if(!term.equals(QLTerm.SHP_CLASS) && cache.containsKey(object)){
			return cache.get(object);
		}
		switch (term) {
		case SHP_CLASS:
			//return (org.gdal.ogr.Geometry) object;
			return (Geometry) object;
		case XPATH_CLASS:
			Geometry result1 = computeGeometry((String) object, term);
			cache.put(object, result1);
			return result1;
		case CSV_CLASS:
			Geometry result2 = computeGeometry((String) object, term);
			cache.put(object, result2);
			return result2;
		case JSONPATH_CLASS:
			Geometry result3 = computeGeometry((String) object, term);
			cache.put(object, result3);
			return result3;
		default:
			throw new MalformedGeometryException(
					"GeoTriples cannot recognize this type of geometry");
		}
		
	}

	private String convert2GML2(String value) {
		String[] tokens = value.split("\n");
		if (tokens[0].contains("Point")) {
			tokens[1] = tokens[1].replaceAll("pos", "coordinates");
			tokens[1] = tokens[1].replaceAll("List", "");
			String[] tokens2 = tokens[1].split("<|>");
			tokens2[2] = remakeCoords(tokens2[2]);
			tokens[1] = "<" + tokens2[1] + ">" + tokens2[2] + "<" + tokens2[3]
					+ ">";
		} else if (tokens[0].contains("MultiLineString")) {

		} else if (tokens[0].contains("LineString")) {
			tokens[1] = tokens[1].replaceAll("pos", "coordinates");
			tokens[1] = tokens[1].replaceAll("List", "");
			String[] tokens2 = tokens[1].split("<|>");
			tokens2[2] = remakeCoords(tokens2[2]);
			tokens[1] = "<" + tokens2[1] + ">" + tokens2[2] + "<" + tokens2[3]
					+ ">";
		} else if (tokens[0].contains("MultiPolygon")) {
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].contains("exterior")) {
					tokens[i] = tokens[i].replaceAll("exterior",
							"outerBoundaryIs");
				} else if (tokens[i].contains("interior")) {
					tokens[i] = tokens[i].replaceAll("interior",
							"innerBoundaryIs");
				} else if (tokens[i].contains("pos")) {
					tokens[i] = tokens[i].replaceAll("pos", "coordinates");
					tokens[i] = tokens[i].replaceAll("List", "");
					String[] tokens2 = tokens[i].split("<|>");
					tokens2[2] = remakeCoords(tokens2[2]);
					tokens[i] = "<" + tokens2[1] + ">" + tokens2[2] + "<"
							+ tokens2[3] + ">";
				}
			}
		} else if (tokens[0].contains("Polygon")) {
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].contains("exterior")) {
					tokens[i] = tokens[i].replaceAll("exterior",
							"outerBoundaryIs");
				} else if (tokens[i].contains("interior")) {
					tokens[i] = tokens[i].replaceAll("interior",
							"innerBoundaryIs");
				} else if (tokens[i].contains("pos")) {
					tokens[i] = tokens[i].replaceAll("pos", "coordinates");
					tokens[i] = tokens[i].replaceAll("List", "");
					String[] tokens2 = tokens[i].split("<|>");
					tokens2[2] = remakeCoords(tokens2[2]);
					tokens[i] = "<" + tokens2[1] + ">" + tokens2[2] + "<"
							+ tokens2[3] + ">";
				}
			}
		}
		String newValue = "";
		for (int i = 0; i < tokens.length; i++) {
			newValue += tokens[i];
		}
		return newValue;
	}

	private String remakeCoords(String value) {
		String coordBuilder = "";
		String[] furtherTokenizelvl2 = value.split(" ");
		for (int i = 0; i < furtherTokenizelvl2.length; i++) {
			coordBuilder += furtherTokenizelvl2[i];
			if (i < furtherTokenizelvl2.length - 1) {
				if (i % 2 == 0) {
					coordBuilder += ",";
				} else {
					coordBuilder += " ";
				}
			}
		}

		return coordBuilder;
	}
}
