package be.ugent.mmlab.rml.function;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.GML.Version;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.gml3.GML;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.xml.sax.SAXException;

import be.ugent.mmlab.rml.core.MalformedGeometryException;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.gml2.GMLReader;
public abstract class AbstractFunction {
	
	protected Geometry computeGeometry(String value, QLTerm term)
			throws SAXException, IOException, ParserConfigurationException,
			NoSuchAuthorityCodeException, FactoryException, MalformedGeometryException {
		Geometry geometry = null;
		WKTReader wktReader = new WKTReader();
		try {
			geometry = wktReader.read(value);
			if(geometry!=null)
				return geometry;
		} catch (ParseException e1) {
			//just continue
		}

		switch (term) {
		case XPATH_CLASS:
			
			//I wrap the geometric value read with these tags so that the GML3 parser perceives it as an autonomous dataset
//			value = "<gml:FeatureCollection xmlns:gml=\"http://www.opengis.net/gml\" ><gml:featureMember><a>" + value;
//			value = value + "</a></gml:featureMember></gml:FeatureCollection>";
//			System.out.println(value);
			StringBuffer sbuffer = new StringBuffer("<gml:FeatureCollection xmlns:gml=\"http://www.opengis.net/gml\" ><gml:featureMember><a>");
			sbuffer.append(value);
			sbuffer.append("</a></gml:featureMember></gml:FeatureCollection>");
			//value = convert2GML2(value);
//			System.out.println(value);
			//GMLReader gmlreader = new GMLReader();
			//geometry = gmlreader.read(value,null);
			InputStream in = new ByteArrayInputStream(sbuffer.toString().getBytes());
			org.geotools.GML gml = new org.geotools.GML(Version.GML3);
			SimpleFeatureIterator iter = gml.decodeFeatureIterator(in);
			SimpleFeature feature = iter.next();
			geometry = (Geometry) feature.getDefaultGeometry();
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
			throw new MalformedGeometryException("GeoTriples cannot recognize this type of geometry");
		}
	}
	protected Geometry computeGeometry(Object object, QLTerm term)
			throws SAXException, IOException, ParserConfigurationException,
			NoSuchAuthorityCodeException, FactoryException, MalformedGeometryException {
		switch (term) {
		case SHP_CLASS:
			return (Geometry)object;
		case XPATH_CLASS:
			return computeGeometry((String) object, term);
		case CSV_CLASS:
			return computeGeometry((String) object, term);
		case JSONPATH_CLASS:
			return computeGeometry((String) object, term);
		default:
			throw new MalformedGeometryException("GeoTriples cannot recognize this type of geometry");
		}
	}
	
	private String convert2GML2(String value) {
		String [] tokens = value.split("\n");
		if (tokens[0].contains("Point")) {
			tokens[1] = tokens[1].replaceAll("pos", "coordinates");
			tokens[1] = tokens[1].replaceAll("List", "");
			String [] tokens2 = tokens[1].split("<|>");
			tokens2[2] = remakeCoords(tokens2[2]);
			tokens[1] = "<" + tokens2[1] + ">" + tokens2[2] + "<" + tokens2[3] + ">";
		}
		else if (tokens[0].contains("MultiLineString")) {
			
		}
		else if (tokens[0].contains("LineString")) {
			tokens[1] = tokens[1].replaceAll("pos", "coordinates");
			tokens[1] = tokens[1].replaceAll("List", "");
			String [] tokens2 = tokens[1].split("<|>");
			tokens2[2] = remakeCoords(tokens2[2]);
			tokens[1] = "<" + tokens2[1] + ">" + tokens2[2] + "<" + tokens2[3] + ">";
		}
		else if (tokens[0].contains("MultiPolygon")) {
			for (int i=0 ; i<tokens.length ; i++) {
				if (tokens[i].contains("exterior")) {
					tokens[i] = tokens[i].replaceAll("exterior", "outerBoundaryIs");
				}
				else if (tokens[i].contains("interior")) {
					tokens[i] = tokens[i].replaceAll("interior", "innerBoundaryIs");
				}
				else if (tokens[i].contains("pos")) {
					tokens[i] = tokens[i].replaceAll("pos", "coordinates");
					tokens[i] = tokens[i].replaceAll("List", "");
					String [] tokens2 = tokens[i].split("<|>");
					tokens2[2] = remakeCoords(tokens2[2]);
					tokens[i] = "<" + tokens2[1] + ">" + tokens2[2] + "<" + tokens2[3] + ">";
				}
			}
		}
		else if (tokens[0].contains("Polygon")) {
			for (int i=0 ; i<tokens.length ; i++) {
				if (tokens[i].contains("exterior")) {
					tokens[i] = tokens[i].replaceAll("exterior", "outerBoundaryIs");
				}
				else if (tokens[i].contains("interior")) {
					tokens[i] = tokens[i].replaceAll("interior", "innerBoundaryIs");
				}
				else if (tokens[i].contains("pos")) {
					tokens[i] = tokens[i].replaceAll("pos", "coordinates");
					tokens[i] = tokens[i].replaceAll("List", "");
					String [] tokens2 = tokens[i].split("<|>");
					tokens2[2] = remakeCoords(tokens2[2]);
					tokens[i] = "<" + tokens2[1] + ">" + tokens2[2] + "<" + tokens2[3] + ">";
				}
			}
		}
		String newValue = "";
		for (int i=0 ; i<tokens.length ; i++) {
			newValue += tokens[i];
		}
		return newValue;
	}
	
	private String remakeCoords(String value) {
		String coordBuilder = "";
		String [] furtherTokenizelvl2 = value.split(" ");
		for (int i=0 ; i<furtherTokenizelvl2.length ; i++) {
			coordBuilder += furtherTokenizelvl2[i];
			if (i < furtherTokenizelvl2.length - 1) {
				if (i%2 == 0) {
					coordBuilder += ",";
				}
				else {
					coordBuilder += " ";
				}
			}	
		}
		
		return coordBuilder;
	}
}
