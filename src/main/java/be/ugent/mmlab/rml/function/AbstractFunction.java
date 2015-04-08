package be.ugent.mmlab.rml.function;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.geojson.geom.GeometryJSON;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.xml.sax.SAXException;

import be.ugent.mmlab.rml.core.MalformedGeometryException;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
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
			GMLReader gmlreader = new GMLReader();
			geometry = gmlreader.read(value,null );
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
		default:
			throw new MalformedGeometryException("GeoTriples cannot recognize this type of geometry");
		}
	}
}
