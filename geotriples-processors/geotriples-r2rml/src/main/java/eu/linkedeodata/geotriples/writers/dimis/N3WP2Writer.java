package eu.linkedeodata.geotriples.writers.dimis;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// To do:
//   Better detection of illegal characters in qnames (? and = for example) 

//import org.apache.commons.logging.*;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

import com.hp.hpl.jena.JenaRuntime;
import com.hp.hpl.jena.n3.N3JenaWriterCommon;
import com.hp.hpl.jena.n3.N3JenaWriterPP;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFWriter;

/** Entry point for N3 writers.  This writer will choose the actual writer
 *  to use by looking at the system property
 *  <code>com.hp.hpl.jena.n3.N3JenaWriter.writer</code> to get the
 *  writer name.
 *  <p>
 *  The following N3 writers are provided:
 *  <ul>
 *  <li>N3-PP: Pretty Printer (the default)</li>
 *  <li>N3-PLAIN: Plain, record/frame-oriented format</li> 
 *  <li>N3-TRIPLES: Triples, with prefixes.</li>
 *  </ul>
 *  </p>
 *
 * @author		Andy Seaborne
 * @version 	$Id: N3JenaWriter.java,v 1.1 2009-06-29 08:55:32 castagna Exp $
 */



public class N3WP2Writer implements RDFWriter
{
    //static Logger logger = LoggerFactory.getLogger(N3JenaWriter.class) ;
    static public boolean DEBUG = false ;
    
    // Note: properties are URIs, not java convention package/class names.
    static protected final String propBase = "http://jena.hpl.hp.com/n3/properties/" ;
    
    /** System property name that sets the default N3 writer name */   
    static public final String propWriterName = propBase+"writer" ;

    /**
     * General name for the N3 writer.  Will make a decision on exactly which
     * writer to use (pretty writer, plain writer or simple writer) when created.
     * Default is the pretty writer but can be overridden with system property
     * <code>com.hp.hpl.jena.n3.N3JenaWriter.writer</code>.  
     */
     
    static public final String n3Writer              = "N3" ;
    
    /**
     * Name of the N3 pretty writer.  The pretty writer
     * uses a frame-like layout, with prefixing, clustering like properties
     * and embedding one-referenced bNodes.
     */
    static public final String n3WriterPrettyPrinter = "N3-PP" ;
    
    /**
     * Name of the N3 plain writer.  The plain writer writes records
     * by subject.
     */
    static public final String n3WriterPlain         = "N3-PLAIN" ;
    
    /**
     * Name of the N3 triples writer. This writer writes one line per statement,
     * like N-Triples, but does N3-style prefixing.
     */
    static public final String n3WriterTriples       = "N3-TRIPLES" ;
    
    /**
     * Alternative name for the N3 triples writer.
     */
    static public final String n3WriterTriplesAlt    = "N3-TRIPLE" ;

    /**
     * Turtle writer.
     * http://www.dajobe.org/2004/01/turtle/
     */
    static public final String turtleWriter          = "TURTLE" ;
    static public final String turtleWriterAlt1      = "Turtle" ;
    static public final String turtleWriterAlt2      = "TTL" ;
    
    protected N3JenaWriterCommon writer = null ;
    
    public N3WP2Writer() { writer = chooseWriter() ; }
    public N3WP2Writer(N3JenaWriterCommon w) { writer = w ;}
    //dimis
    private Map<String, String> wellKnownPropsMapTurtle;
    private boolean allowTripleQuotedStrings;
    public N3WP2Writer(Map<String, String> wellKnownPropsMapTrtl,boolean allowTripleQuotedStrngs)
    {
    	//TODO make private variables and then in chooseWriter construct the writers with these
    	wellKnownPropsMapTurtle=wellKnownPropsMapTrtl;
    	allowTripleQuotedStrings=allowTripleQuotedStrngs;
    	writer=chooseWriter();
    }
    N3JenaWriterCommon chooseWriter()
    {
        // Choose the writer
        String writerName = JenaRuntime.getSystemProperty(propWriterName) ;
        if ( writerName == null ||
             writerName.equals("N3") || writerName.equals(n3WriterPrettyPrinter) )
            return new WP2N3JenaWriterPP(allowTripleQuotedStrings, wellKnownPropsMapTurtle) ;  //ok
        
        if ( writerName.equalsIgnoreCase(n3WriterPlain) )
            return new WP2N3JenaWriterCommon(allowTripleQuotedStrings, wellKnownPropsMapTurtle) ; //ok
        
        if ( writerName.equalsIgnoreCase(n3WriterTriples) ||
             writerName.equalsIgnoreCase(n3WriterTriplesAlt) )
            return new WP2N3JenaWriterTriples(allowTripleQuotedStrings, wellKnownPropsMapTurtle) ; //ok
            
        if ( writerName.equalsIgnoreCase(turtleWriter) )
        {
        	boolean useWellKnownPropertySymbols=false;
            N3JenaWriterPP w = new WP2N3JenaWriterPP(useWellKnownPropertySymbols,allowTripleQuotedStrings, wellKnownPropsMapTurtle) ;  //ok
            //w.useWellKnownPropertySymbols = false ;
            return w ;
        }
        
        // Don't know or default.
        return new WP2N3JenaWriterPP(allowTripleQuotedStrings, wellKnownPropsMapTurtle) ;  //ok
    }
    
    
    /** Write the model out in N3, encoded in in UTF-8
     * @see #write(Model,Writer,String)
     */

    @Override
    public void write(Model model, Writer out, String base) 
    {
        writer.write(model, out, base) ;
    }

    /** Write the model out in N3.  The writer should be one suitable for UTF-8 which
    * excludes a PrintWriter or a FileWriter which use default character set.
    *
    * Examples:
    * <pre>
    * try {
    *      Writer w =  new BufferedWriter(new OutputStreamWriter(output, "UTF-8")) ;
    *      model.write(w, base) ;
    *      try { w.flush() ; } catch (IOException ioEx) {}
    *  } catch (java.io.UnsupportedEncodingException ex) {} //UTF-8 is required so can't happen
    * </pre>
    * or
    * <pre>
    * try {
    *     OutputStream out = new FileOutputStream(file) ;
    *     Writer w =  new BufferedWriter(new OutputStreamWriter(out, "UTF-8")) ;
    *     model.write(w, base) ;
    * }
    * catch (java.io.UnsupportedEncodingException ex) {}
    * catch (java.io.FileNotFoundException noFileEx) { ... }
    * </pre>
    * @see #write(Model,Writer,String)
    */

    @Override
    public void write(Model model, OutputStream out, String base) 
    {
        writer.write(model, out, base) ;
   }


    /**
     * @see com.hp.hpl.jena.rdf.model.RDFWriter#setProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public Object setProperty(String propName, Object propValue) 
    {
        return writer.setProperty(propName, propValue) ;
    }

    /**
     * @see com.hp.hpl.jena.rdf.model.RDFWriter#setErrorHandler(com.hp.hpl.jena.rdf.model.RDFErrorHandler)
     */
    @Override
    public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler)
    {
        return writer.setErrorHandler(errHandler) ;
    }
   
}
