/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ugent.mmlab.rml.dataset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

/**
 *
 * @author mielvandersande
 */
public class FileSesameDataset extends SesameDataSet {

    // Log
    private static Log log = LogFactory.getLog(FileSesameDataset.class);
    private File target;
    private BufferedWriter fw;
    private RDFWriter writer;
    private RDFFormat format = RDFFormat.NTRIPLES;
    private int bnodeid = 0;
    
    private int size = 0;

    public FileSesameDataset(String target) {

        this.target = new File(target);

        try {
            fw = new BufferedWriter(new FileWriter(target));
            writer = Rio.createWriter(this.format, fw);
            writer.startRDF();

        } catch (IOException | RDFHandlerException ex) {
            log.error("", ex);
        } 

    }

    /**
     * Load data in specified graph (use default graph if contexts is null)
     *
     * @param filePath
     * @param format
     * @param contexts
     * @throws IOException
     * @throws RDFParseException
     */
    @Override
    public void loadDataFromFile(String filePath, RDFFormat format,
            Resource... contexts) throws RepositoryException,
            RDFParseException, IOException {

        // upload a file
        File f = new File(filePath);

        if (this.format.equals(format)) {
            //append to file
        } else {
            //convert, then append to file
        }


    }

    @Override
    public void loadDataFromURL(String stringURL) throws RepositoryException, RDFParseException, IOException {

        URL url = new URL(stringURL);

        RDFFormat format = RDFFormat.forFileName(stringURL);

        if (this.format.equals(format)) {
            //append to file
        } else {
            //convert, then append to file
        }

    }

    /**
     * Literal factory
     *
     * @param s the literal value
     * @param typeuri uri representing the type (generally xsd)
     * @return
     */
    @Override
    public org.openrdf.model.Literal Literal(String s, URI typeuri) {
        return new LiteralImpl(s, typeuri);
    }

    /**
     * Untyped Literal factory
     *
     * @param s the literal
     * @return
     */
    @Override
    public org.openrdf.model.Literal Literal(String s) {
        return Literal(s, null);
    }

    /**
     * URIref factory
     *
     * @param uri
     * @return
     */
    @Override
    public URI URIref(String uri) {
        return new URIImpl(uri);
    }

    /**
     * BNode factory
     *
     * @return
     */
    @Override
    public BNode bnode() {
        return new BNodeImpl("" + bnodeid++);
    }

    /**
     * Insert Triple/Statement into graph
     *
     * @param s subject uriref
     * @param p predicate uriref
     * @param o value object (URIref or Literal)
     * @param contexts varArgs context objects (use default graph if null)
     */
    @Override
    public void add(Resource s, URI p, Value o, Resource... contexts) {
        if (log.isDebugEnabled()) {
            log.debug("[FileSesameDataSet:add] Add triple (" + s.stringValue()
                    + ", " + p.stringValue() + ", " + o.stringValue() + ").");
        }

        Statement st = new StatementImpl(s, p, o);
        try {
            writer.handleStatement(st);
            size++;
        } catch (RDFHandlerException ex) {
            log.fatal(o);
        }

    }

    @Override
    public void remove(Resource s, URI p, Value o, Resource... context) {
        throw new UnsupportedOperationException("Cannot remove triples from File Dataset");
    }

    /**
     * Import RDF data from a string
     *
     * @param rdfstring string with RDF data
     * @param format RDF format of the string (used to select parser)
     */
    @Override
    public void addString(String rdfstring, RDFFormat format) {
        if (this.format.equals(format)) {
        } else {
            //convert, then append to file
        }
    }

    /**
     * Import RDF data from a file
     *
     * @param location of file (/path/file) with RDF data
     * @param format RDF format of the string (used to select parser)
     */
    public void addFile(String filepath, RDFFormat format) {
        if (this.format.equals(format)) {
            //append to file
        } else {
            //convert, then append to file
        }
    }

    /**
     * Import data from URI source Request is made with proper HTTP ACCEPT
     * header and will follow redirects for proper LOD source negotiation
     *
     * @param urlstring absolute URI of the data source
     * @param format RDF format to request/parse from data source
     */
    @Override
    public void addURI(String urlstring, RDFFormat format) {
        InputStream instream = null;

        try {
            URL url = new URL(urlstring);
            URLConnection uricon = (URLConnection) url.openConnection();
            uricon.addRequestProperty("accept", format.getDefaultMIMEType());
            instream = uricon.getInputStream();
            IOUtils.copy(instream, new FileOutputStream(target));
        } catch (IOException ex) {
            Logger.getLogger(FileSesameDataset.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                instream.close();
            } catch (IOException ex) {
                Logger.getLogger(FileSesameDataset.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * Dump RDF graph
     *
     * @param out output stream for the serialization
     * @param outform the RDF serialization format for the dump
     * @return
     */
    @Override
    public void dumpRDF(OutputStream out, RDFFormat outform) {
        //Convert and copy rdf
        if (!this.format.equals(format)) {
        }
    }

    /**
     * dump RDF graph
     *
     * @param filePath destination file for the serialization
     * @param outform the RDF serialization format for the dump
     * @return
     */
    @Override
    public void dumpRDF(String filePath, RDFFormat outform) {
        //convert RDF
    }

    @Override
    public String printRDF(RDFFormat outform) {
        try {
            FileInputStream fis = new FileInputStream(target);
            return fis.toString();
        } catch (IOException ex) {
            log.error(ex);
        }
        return null;
    }

    /**
     * Tuple pattern query - find all statements with the pattern, where null is
     * a wildcard
     *
     * @param s subject (null for wildcard)
     * @param p predicate (null for wildcard)
     * @param o object (null for wildcard)
     * @param contexts varArgs contexts (use default graph if null)
     * @return serialized graph of results
     */
    @Override
    public List<Statement> tuplePattern(Resource s, URI p, Value o,
            Resource... contexts) {
        FileInputStream fis = null;
        try {
            TupleMatcher statements = new TupleMatcher(s, p, o, contexts);
            fis = new FileInputStream(target);
            RDFParser rdfParser = Rio.createParser(format);
            rdfParser.setRDFHandler(statements);
            try {
                rdfParser.parse(fis, target.getAbsolutePath());
            } catch (IOException | RDFParseException | RDFHandlerException e) {
                // handle IO problems (e.g. the file could not be read)
                log.error(e);
            }
            return new ArrayList<>(statements.getStatements());
        } catch (FileNotFoundException ex) {
            log.error(ex);
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                log.error(ex);
            }
        }
        return new ArrayList<> ();
    }

    @Override
    public int getSize() {
        
        /*FileInputStream fis = null;
        try {
            StatementCounter counter = new StatementCounter();
            fis = new FileInputStream(target);
            RDFParser rdfParser = Rio.createParser(format);
            rdfParser.setRDFHandler(counter);
            try {
                rdfParser.parse(fis, target.getAbsolutePath());
            } catch (IOException | RDFParseException | RDFHandlerException e) {
                // handle IO problems (e.g. the file could not be read)
                log.error(e);
            }
            return counter.getCountedStatements();
        } catch (FileNotFoundException ex) {
            log.error(ex);
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                log.error(ex);
            }
        }*/
        return size;
    }
    
    

    /**
     * Execute a CONSTRUCT/DESCRIBE SPARQL query against the graphs
     *
     * @param qs CONSTRUCT or DESCRIBE SPARQL query
     * @param format the serialization format for the returned graph
     * @return serialized graph of results
     */
    @Override
    public String runSPARQL(String qs, RDFFormat format) {
        throw new UnsupportedOperationException("Cannot SPARQL File Dataset");
    }

    /**
     * Execute a SELECT SPARQL query against the graphs
     *
     * @param qs SELECT SPARQL query
     * @return list of solutions, each containing a hashmap of bindings
     */
    @Override
    public List<HashMap<String, Value>> runSPARQL(String qs) {
        throw new UnsupportedOperationException("Cannot SPARQL File Dataset");
    }

    /**
     * Execute CONSTRUCT/DESCRIBE SPARQL queries against the graph from a SPARQL
     * request file. This file contains only one request.
     *
     * @param pathToFile path to SPARQL request file
     * @return list of solutions, each containing a hashmap of bindings
     */
    @Override
    public String runSPARQLFromFile(String pathToSPARQLFile, RDFFormat format) {
        throw new UnsupportedOperationException("Cannot SPARQL File Dataset");
    }

    /**
     * Execute SELECT SPARQL queries against the graph from a SPARQL request
     * file. This file contains only one request.
     *
     * @param pathToFile path to SPARQL request file
     * @return list of solutions, each containing a hashmap of bindings
     */
    @Override
    public List<HashMap<String, Value>> runSPARQLFromFile(
            String pathToSPARQLFile) {
        throw new UnsupportedOperationException("Cannot SPARQL File Dataset");
    }

    /**
     * Close current repository.
     *
     * @throws RepositoryException
     */
    @Override
    public void closeRepository() throws RepositoryException {
        log.debug("[FileSesameDataSet:add] Closing file.");
        try {
            fw.flush();
            writer.endRDF();
            fw.close();
        } catch (RDFHandlerException ex) {
            log.error(ex);
        } catch (IOException ex) {
            Logger.getLogger(FileSesameDataset.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    @Override
    public String toString() {
        String result = "{[SimpleGraph:toString] triples = ";
        List<Statement> triples = tuplePattern(null, null, null);
        for (Object o : triples) {
            result += o + System.getProperty("line.separator");
        }
        result += "}";
        return result;
    }
}
