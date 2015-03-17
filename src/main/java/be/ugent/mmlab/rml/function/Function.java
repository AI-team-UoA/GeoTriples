package be.ugent.mmlab.rml.function;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.openrdf.model.Value;
import org.xml.sax.SAXException;

import be.ugent.mmlab.rml.core.MalformedGeometryException;

public interface Function {

	public List<? extends String> execute(List<? extends String> arguments) throws Exception;

}
