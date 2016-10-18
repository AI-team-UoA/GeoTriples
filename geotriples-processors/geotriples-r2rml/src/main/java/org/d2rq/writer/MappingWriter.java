package org.d2rq.writer;

import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

import org.d2rq.r2rml.TermMap;

import com.hp.hpl.jena.rdf.model.Property;

public interface MappingWriter {

	void write(OutputStream outStream);

	void write(Writer outWriter);

	void visitTermProperty(Property property, List<TermMap> termMaps);

}