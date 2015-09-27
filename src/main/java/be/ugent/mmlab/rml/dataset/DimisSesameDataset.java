/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.mmlab.rml.dataset;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;

/**
 *
 * @author dimis
 */
public class DimisSesameDataset extends SesameDataSet  {

	// Log
	private static Log log = LogFactory.getLog(SesameDataSet.class);

	private Repository currentRepository = null;

	// useful -local- constants
	static RDFFormat NTRIPLES = RDFFormat.NTRIPLES;
	static RDFFormat N3 = RDFFormat.N3;
	static RDFFormat RDFXML = RDFFormat.RDFXML;
	static String RDFTYPE = RDF.TYPE.toString();

	/**
	 * In memory Sesame repository without inferencing
	 */
	public DimisSesameDataset() {
		this(false);
	}

	/**
	 * In memory Sesame repository with optional inferencing
	 * 
	 * @param inferencing
	 */
	public DimisSesameDataset(boolean inferencing) {
		try {
			if (inferencing) {
				currentRepository = new SailRepository(
						new ForwardChainingRDFSInferencer(new MemoryStore()));
			} else {
				currentRepository = new SailRepository(new MemoryStore());
			}
			currentRepository.initialize();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	public DimisSesameDataset(String pathToDir, boolean inferencing) {
		File f = new File(pathToDir);
		try {
			if (inferencing) {
				currentRepository = new SailRepository(
						new ForwardChainingRDFSInferencer(new NativeStore(f)));
			} else {
				currentRepository = new SailRepository(new NativeStore(f));
			}
			currentRepository.initialize();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}

	}

	public DimisSesameDataset(String sesameServer, String repositoryID) {
		currentRepository = new HTTPRepository(sesameServer, repositoryID);
		try {
			currentRepository.initialize();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load data in specified graph (use default graph if contexts is null)
	 * 
	 * @param filePath
	 * @param format
	 * @param contexts
	 * @throws RepositoryException
	 * @throws IOException
	 * @throws RDFParseException
	 */
	public void loadDataFromFile(String filePath, RDFFormat format,
			Resource... contexts) throws RepositoryException,
			RDFParseException, IOException {
		RepositoryConnection con = null;
		try {
			con = currentRepository.getConnection();
			// upload a file
			File f = new File(filePath);
			con.add(f, null, format, contexts);
		} finally {
			try {
				con.close();
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}

	}

	public void loadDataFromURL(String stringURL) throws RepositoryException, RDFParseException, IOException {
		RepositoryConnection con = null;
		try {
			con = currentRepository.getConnection();
				// upload a URL
				URL url = new URL(stringURL);
				con.add(url, null, RDFFormat.TURTLE);
		} finally {
			try {
				con.close();
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Literal factory
	 * 
	 * @param s
	 *            the literal value
	 * @param typeuri
	 *            uri representing the type (generally xsd)
	 * @return
	 */
	public org.openrdf.model.Literal Literal(String s, URI typeuri) {
		try {
			RepositoryConnection con = currentRepository.getConnection();
			try {
				ValueFactory vf = con.getValueFactory();
				if (typeuri == null) {
					return vf.createLiteral(s);
				} else {
					return vf.createLiteral(s, typeuri);
				}
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Untyped Literal factory
	 * 
	 * @param s
	 *            the literal
	 * @return
	 */
	public org.openrdf.model.Literal Literal(String s) {
		return Literal(s, null);
	}

	/**
	 * URIref factory
	 * 
	 * @param uri
	 * @return
	 */
	public URI URIref(String uri) {
		try {
			RepositoryConnection con = currentRepository.getConnection();
			try {
				ValueFactory vf = con.getValueFactory();
				return vf.createURI(uri);
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * BNode factory
	 * 
	 * @return
	 */
	public BNode bnode() {
		try {
			RepositoryConnection con = currentRepository.getConnection();
			try {
				ValueFactory vf = con.getValueFactory();
				return vf.createBNode();
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Insert Triple/Statement into graph
	 * 
	 * @param s
	 *            subject uriref
	 * @param p
	 *            predicate uriref
	 * @param o
	 *            value object (URIref or Literal)
	 * @param contexts
	 *            varArgs context objects (use default graph if null)
	 */
	public void add(Resource s, URI p, Value o, Resource... contexts) {
		if (log.isDebugEnabled())
			log.trace("[SesameDataSet:add] Add triple (" + s.stringValue()
					+ ", " + p.stringValue() + ", " + o.stringValue() + ").");
		try {
			RepositoryConnection con = currentRepository.getConnection();
			try {
				ValueFactory myFactory = con.getValueFactory();
				Statement st = myFactory.createStatement((Resource) s, p,
						(Value) o);
				con.add(st, contexts);
				con.commit();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				con.close();
			}
		} catch (Exception e) {
			// handle exception
		}
	}

	public void remove(Resource s, URI p, Value o, Resource... context) {
		try {
			RepositoryConnection con = currentRepository.getConnection();
			try {
				ValueFactory myFactory = con.getValueFactory();
				Statement st = myFactory.createStatement((Resource) s, p,
						(Value) o);
				con.remove(st, context);
			} finally {
				con.close();
			}
		} catch (Exception e) {
			// handle exception
		}
	}

	/**
	 * Import RDF data from a string
	 * 
	 * @param rdfstring
	 *            string with RDF data
	 * @param format
	 *            RDF format of the string (used to select parser)
	 */
	public void addString(String rdfstring, RDFFormat format) {
		try {
			RepositoryConnection con = currentRepository.getConnection();
			try {
				StringReader sr = new StringReader(rdfstring);
				con.add(sr, "", format);
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Import RDF data from a file
	 * 
	 * @param location
	 *            of file (/path/file) with RDF data
	 * @param format
	 *            RDF format of the string (used to select parser)
	 */
	public void addFile(String filepath, RDFFormat format) {
		try {
			RepositoryConnection con = currentRepository.getConnection();
			try {
				con.add(new File(filepath), "", format);
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Import data from URI source Request is made with proper HTTP ACCEPT
	 * header and will follow redirects for proper LOD source negotiation
	 * 
	 * @param urlstring
	 *            absolute URI of the data source
	 * @param format
	 *            RDF format to request/parse from data source
	 */
	public void addURI(String urlstring, RDFFormat format) {
		try {
			RepositoryConnection con = currentRepository.getConnection();
			try {
				URL url = new URL(urlstring);
				URLConnection uricon = (URLConnection) url.openConnection();
				uricon.addRequestProperty("accept", format.getDefaultMIMEType());
				InputStream instream = uricon.getInputStream();
				con.add(instream, urlstring, format);
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Dump RDF graph
	 * 
	 * @param out
	 *            output stream for the serialization
	 * @param outform
	 *            the RDF serialization format for the dump
	 * @return
	 */
	public void dumpRDF(OutputStream out, RDFFormat outform) {
		try {
			RepositoryConnection con = currentRepository.getConnection();
			try {
				RDFWriter w = Rio.createWriter(outform, out);
				con.export(w);
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * dump RDF graph
	 * 
	 * @param filePath
	 *            destination file for the serialization
	 * @param outform
	 *            the RDF serialization format for the dump
	 * @return
	 */
	public void dumpRDF(String filePath, RDFFormat outform) {
		OutputStream output;
		try {
			output = new FileOutputStream(filePath);

			dumpRDF(output, outform);
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public String printRDF(RDFFormat outform) {
		try {
			RepositoryConnection con = currentRepository.getConnection();
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				RDFWriter w = Rio.createWriter(outform, out);

				con.export(w);
				String result = new String(out.toByteArray(), "UTF-8");
				return result;
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Convenience URI import for RDF/XML sources
	 * 
	 * @param urlstring
	 *            absolute URI of the data source
	 */
	public void addURI(String urlstring) {
		addURI(urlstring, RDFFormat.RDFXML);
	}

	/**
	 * Tuple pattern query - find all statements with the pattern, where null is
	 * a wildcard
	 * 
	 * @param s
	 *            subject (null for wildcard)
	 * @param p
	 *            predicate (null for wildcard)
	 * @param o
	 *            object (null for wildcard)
	 * @param contexts
	 *            varArgs contexts (use default graph if null)
	 * @return serialized graph of results
	 */
	public List<Statement> tuplePattern(Resource s, URI p, Value o,
			Resource... contexts) {
		try {
			RepositoryConnection con = currentRepository.getConnection();
			try {
				RepositoryResult<Statement> repres = con.getStatements(s, p, o,
						true, contexts);
				ArrayList<Statement> reslist = new ArrayList<Statement>();
				while (repres.hasNext()) {
					reslist.add(repres.next());
				}
				return reslist;
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Execute a CONSTRUCT/DESCRIBE SPARQL query against the graphs
	 * 
	 * @param qs
	 *            CONSTRUCT or DESCRIBE SPARQL query
	 * @param format
	 *            the serialization format for the returned graph
	 * @return serialized graph of results
	 */
	public String runSPARQL(String qs, RDFFormat format) {
		try {
			RepositoryConnection con = currentRepository.getConnection();
			try {
				GraphQuery query = con.prepareGraphQuery(
						org.openrdf.query.QueryLanguage.SPARQL, qs);
				StringWriter stringout = new StringWriter();
				RDFWriter w = Rio.createWriter(format, stringout);
				query.evaluate(w);
				return stringout.toString();
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Execute a SELECT SPARQL query against the graphs
	 * 
	 * @param qs
	 *            SELECT SPARQL query
	 * @return list of solutions, each containing a hashmap of bindings
	 */
	public List<HashMap<String, Value>> runSPARQL(String qs) {
		try {
			RepositoryConnection con = currentRepository.getConnection();
			try {
				TupleQuery query = con.prepareTupleQuery(
						org.openrdf.query.QueryLanguage.SPARQL, qs);
				TupleQueryResult qres = query.evaluate();
				ArrayList<HashMap<String, Value>> reslist = new ArrayList<HashMap<String, Value>>();
				while (qres.hasNext()) {
					BindingSet b = qres.next();

					Set<String> names = b.getBindingNames();
					HashMap<String, Value> hm = new HashMap<String, Value>();
					for (String n : names) {
						hm.put(n, b.getValue(n));
					}
					reslist.add(hm);
				}
				return reslist;
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Execute CONSTRUCT/DESCRIBE SPARQL queries against the graph from a SPARQL
	 * request file. This file contains only one request.
	 * 
	 * @param pathToFile
	 *            path to SPARQL request file
	 * @return list of solutions, each containing a hashmap of bindings
	 */
	public String runSPARQLFromFile(String pathToSPARQLFile, RDFFormat format) {
		// Read SPARQL request
		String s = null;
		StringBuffer sb = new StringBuffer();
		try {
			FileReader fr = new FileReader(new File(pathToSPARQLFile));
			// be sure to not have line starting with "--" or "/*" or any other
			// non aplhabetical character
			BufferedReader br = new BufferedReader(fr);
			while ((s = br.readLine()) != null) {
				sb.append(s);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (log.isDebugEnabled())
			log.debug("[Graph:runSPARQLFromFile] SPARQL query : "
					+ sb.toString());
		return runSPARQL(sb.toString(), format);
	}

	/**
	 * Execute SELECT SPARQL queries against the graph from a SPARQL request
	 * file. This file contains only one request.
	 * 
	 * @param pathToFile
	 *            path to SPARQL request file
	 * @return list of solutions, each containing a hashmap of bindings
	 */
	public List<HashMap<String, Value>> runSPARQLFromFile(
			String pathToSPARQLFile) {
		// Read SPARQL request
		String s = null;
		StringBuffer sb = new StringBuffer();
		try {
			FileReader fr = new FileReader(new File(pathToSPARQLFile));
			// be sure to not have line starting with "--" or "/*" or any other
			// non aplhabetical character
			BufferedReader br = new BufferedReader(fr);
			while ((s = br.readLine()) != null) {
				sb.append(s);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (log.isDebugEnabled())
			log.debug("[Graph:runSPARQLFromFile] SPARQL query : "
					+ sb.toString());
		return runSPARQL(sb.toString());
	}

	/**
	 * Close current repository.
	 * 
	 * @throws RepositoryException
	 */
	public void closeRepository() throws RepositoryException {
		currentRepository.shutDown();
	}

	/**
	 * Return the number of triples in the repository.
	 */
	public int getSize() {
		return tuplePattern(null, null, null).size();
	}

	public String toString() {
		String result = "{[SimpleGraph:toString] triples = ";
		List<Statement> triples = tuplePattern(null, null, null);
		for (Object o : triples)
			result += o + System.getProperty("line.separator");
		result += "}";
		return result;
	}

	/**
	 * Add directly a statement object.
	 */
	public void addStatement(Statement s) {
		add(s.getSubject(), s.getPredicate(), s.getObject(), s.getContext());
	}

	public boolean isBNode(Value value) {
		try {
			@SuppressWarnings("unused")
			BNode test = (BNode) value;
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}

	public boolean isEqualTo(SesameDataSet dataSet) {
		List<Statement> triples = tuplePattern(null, null, null);
		for (Statement triple : triples) {
			List<Statement> targetTriples = new ArrayList<Statement>();
			if (isBNode(triple.getSubject()) && isBNode(triple.getObject())) {
				targetTriples = dataSet.tuplePattern(null,
						triple.getPredicate(), null, triple.getContext());
				if (targetTriples.isEmpty()) {
					log.debug("[SesameDataSet:isEqualTo] No result for triple : "
							+ triple);
					return false;
				} else {
					boolean found = false;
					Statement foundTriple = null;
					for (Statement targetTriple : targetTriples) {
						if (isBNode(targetTriple.getSubject())
								&& isBNode(targetTriple.getObject())) {
							found = true;
							foundTriple = targetTriple;
							break;
						}
					}
					if (found) {
						log.debug("[SesameDataSet:isEqualTo] " + triple
								+ " == " + foundTriple);
					} else {
						log.debug("[SesameDataSet:isEqualTo] No BNode subject and BNode object found for "
								+ triple);
						return false;
					}
				}
			} else if (isBNode(triple.getSubject())) {
				targetTriples = dataSet.tuplePattern(null,
						triple.getPredicate(), triple.getObject(),
						triple.getContext());
				if (targetTriples.isEmpty()) {
					log.debug("[SesameDataSet:isEqualTo] No result for subject : "
							+ triple);
					return false;
				} else {
					boolean found = false;
					Statement foundTriple = null;
					for (Statement targetTriple : targetTriples) {
						if (isBNode(targetTriple.getSubject())) {
							found = true;
							foundTriple = targetTriple;
							break;
						}
					}
					if (found) {
						log.debug("[SesameDataSet:isEqualTo] " + triple
								+ " == " + foundTriple);
					} else {
						log.debug("[SesameDataSet:isEqualTo] No BNode subject found for "
								+ triple);
						return false;
					}
				}

			} else if (isBNode(triple.getObject())) {
				targetTriples = dataSet.tuplePattern(triple.getSubject(),
						triple.getPredicate(), null, triple.getContext());
				if (targetTriples.isEmpty()) {
					log.debug("[SesameDataSet:isEqualTo] No result for triple : "
							+ triple);
					return false;
				} else {
					boolean found = false;
					Statement foundTriple = null;
					for (Statement targetTriple : targetTriples) {
						if (isBNode(targetTriple.getObject())) {
							found = true;
							foundTriple = targetTriple;
							break;
						}
					}
					if (found) {

						log.debug("[SesameDataSet:isEqualTo] " + triple
								+ " == " + foundTriple);
					} else {
						log.debug("[SesameDataSet:isEqualTo] No BNode object found for "
								+ triple);
						return false;
					}
				}
			} else {
				targetTriples = dataSet.tuplePattern(triple.getSubject(),
						triple.getPredicate(), triple.getObject(),
						triple.getContext());
				if (targetTriples.size() > 1) {
					log.debug("[SesameDataSet:isEqualTo] Too many result for : "
							+ triple);
					return false;
				} else if (targetTriples.isEmpty()) {
					log.debug("[SesameDataSet:isEqualTo] No result for triple : "
							+ triple);
					return false;
				} else {
					log.debug("[SesameDataSet:isEqualTo] " + triple + " == "
							+ targetTriples.get(0));
				}
			}
		}
		if (dataSet.getSize() != getSize())
			log.debug("[SesameDataSet:isEqualTo] No same size : "
					+ dataSet.getSize() + " != " + getSize());
		return dataSet.getSize() == getSize();
	}
}
