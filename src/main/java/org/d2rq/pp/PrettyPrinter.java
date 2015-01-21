package org.d2rq.pp;

import java.util.Iterator;

import org.d2rq.vocab.D2RConfig;
import org.d2rq.vocab.D2RQ;
import org.d2rq.vocab.RR;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;


/**
 * Pretty printer for various kinds of objects.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class PrettyPrinter {
	private final static String NEWLINE = System.getProperty("line.separator");

	static {
		// Make sure that the model behind all the
		// D2RQ vocabulary terms has the d2rq prefix
		D2RQ.ClassMap.getModel().setNsPrefix("d2rq", D2RQ.NS);
		// Same for D2RConfig
		D2RConfig.Server.getModel().setNsPrefix("d2r", D2RConfig.NS);
		RR.TriplesMap.getModel().setNsPrefix("rr", RR.NS);
	}

	/**
	 * Pretty-prints an RDF node.
	 * @param n An RDF node
	 * @return An N-Triples style textual representation
	 */
	public static String toString(Node n) {
		return toString(n, PrefixMapping.Standard);
	}
	
	/**
	 * Pretty-prints an RDF node and shortens URIs into QNames according to a
	 * {@link PrefixMapping}.
	 * @param n An RDF node
	 * @return An N-Triples style textual representation with URIs shortened to QNames
	 */
	public static String toString(Node n, PrefixMapping prefixes) {
		if (n.isURI()) {
			return qNameOrURI(n.getURI(), prefixes);
		}
		if (n.isBlank()) {
			return "_:" + n.getBlankNodeLabel();
		}
		if (n.isVariable()) {
			return "?" + n.getName();
		}
		if (Node.ANY.equals(n)) {
			return "?ANY";
		}
		// Literal
		String s = "\"" + n.getLiteralLexicalForm() + "\"";
		if (!"".equals(n.getLiteralLanguage())) {
			s += "@" + n.getLiteralLanguage();
		}
		if (n.getLiteralDatatype() != null) {
			s += "^^" + qNameOrURI(n.getLiteralDatatypeURI(), prefixes);
		}
		return s;
	}
	
	public static String qNameOrURI(String uri, PrefixMapping prefixes) {
		if (prefixes == null) {
			return "<" + uri + ">";
		}
		String qName = prefixes.qnameFor(uri);
		if (qName != null) {
			return qName;
		}
		return "<" + uri + ">";
		
	}
	
	public static String toString(Triple t) {
		return toString(t, PrefixMapping.Standard);
	}
	
	public static String toString(Triple t, PrefixMapping prefixes) {
		return toString(t.getSubject(), prefixes) + " "
				+ toString(t.getPredicate(), prefixes) + " "
				+ toString(t.getObject(), prefixes) + ".";
	}
	
	public static String toString(RDFDatatype datatype) {
		return qNameOrURI(datatype.getURI(), PrefixMapping.Standard);
	}
	
	public static String toString(RDFNode n) {
		if (n == null) return "null";
		if (n.isURIResource()) {
			Resource r = (Resource) n;
			return toString(r.asNode(), r.getModel());
		}
		return toString(n.asNode());
	}
	
	public static String toString(Model m, PrefixMapping prefixes) {
		StringBuilder result = new StringBuilder();
		Iterator<Triple> it = m.getGraph().find(Node.ANY, Node.ANY, Node.ANY);
		while (it.hasNext()) {
			result.append(toString(it.next(), prefixes));
			result.append(NEWLINE);
		}
		return result.toString();
	}
}
