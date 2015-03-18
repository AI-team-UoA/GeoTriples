package be.ugent.mmlab.rml.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.xml.sax.SAXException;

import be.ugent.mmlab.rml.core.MalformedGeometryException;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

import com.vividsolutions.jts.geom.Geometry;


public class FunctionCoordinateDimension extends AbstractFunction implements Function {
	QLTerm termkind;
	public FunctionCoordinateDimension(QLTerm termkind) {
		this.termkind=termkind;
	}
	@Override
	public List<? extends String> execute(
			List<? extends String> arguments) throws SAXException, IOException, ParserConfigurationException, FactoryException, MalformedGeometryException {
		List<String> valueList = new ArrayList<>();
		
		Geometry geometry = computeGeometry(arguments.get(0), termkind);
		valueList.add(GTransormationFunctions.dimension(
				(Geometry) geometry));
		return valueList;
	}


}
