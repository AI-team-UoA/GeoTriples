package be.ugent.mmlab.rml.function;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.openrdf.model.Value;
import org.xml.sax.SAXException;

import be.ugent.mmlab.rml.core.MalformedGeometryException;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

public interface Function {

	public List<? extends Object> execute(List<? extends Object> list,List<? extends QLTerm> qlterms) throws Exception;
}
