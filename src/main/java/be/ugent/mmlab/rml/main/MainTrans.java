package be.ugent.mmlab.rml.main;
/***************************************************************************
*
* @author: dimis (dimis@di.uoa.gr)
* 
****************************************************************************/
import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.function.Config;
import be.ugent.mmlab.rml.function.FunctionArea;
import be.ugent.mmlab.rml.function.FunctionAsGML;
import be.ugent.mmlab.rml.function.FunctionAsWKT;
import be.ugent.mmlab.rml.function.FunctionCentroidX;
import be.ugent.mmlab.rml.function.FunctionCentroidY;
import be.ugent.mmlab.rml.function.FunctionCoordinateDimension;
import be.ugent.mmlab.rml.function.FunctionDimension;
import be.ugent.mmlab.rml.function.FunctionFactory;
import be.ugent.mmlab.rml.function.FunctionHasSerialization;
import be.ugent.mmlab.rml.function.FunctionIs3D;
import be.ugent.mmlab.rml.function.FunctionIsEmpty;
import be.ugent.mmlab.rml.function.FunctionIsSimple;
import be.ugent.mmlab.rml.function.FunctionLength;
import be.ugent.mmlab.rml.function.FunctionSpatialDimension;
import be.ugent.mmlab.rml.model.RMLMapping;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLStructureException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLSyntaxException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.R2RMLDataError;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

/**
 *
 * @author mielvandersande, andimou
 */
public class MainTrans {

    /**
     * @param args the command line arguments
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        try {
        	String file, outfile;
            String graphName = "";
            RDFFormat format = RDFFormat.N3;
            // create Options object
            Options options = new Options();            
            // add options
            options.addOption("sp", true, "source properties file");
            options.addOption("g", true, "Graph name");
            options.addOption("epsg", true, "EPSG code");
            options.addOption("f", true, "output RDF Format");
            options.addOption("ns",true,"file with namespaces for XML use only");
            //should be new DefaultParser() but requires cli 1.3 instead of clli 1.2
            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse( options, args);
            //execute(args[0],args[1]);
            //System.exit(0);
            file = args[args.length-2];
            outfile = args[args.length - 1];
            RMLMapping mapping = RMLMappingFactory.extractRMLMapping(file);
            RMLEngine engine = new RMLEngine();
            registerFunctions();
            //but hack-fix but important
            
            System.out.println("mapping document " + file);
            if(cmd.hasOption("epsg"))
            {
            	Config.EPSG_CODE=cmd.getOptionValue("epsg");
            }
            if(cmd.hasOption("ns"))
            {
            	readNamespaces(cmd.getOptionValue("ns"));
            }
            if (cmd.hasOption("f")) {
            	String formatValue = cmd.getOptionValue("f");
            	if (formatValue.equalsIgnoreCase("N3")) {
            		format = RDFFormat.N3;
            	}
            	else if (formatValue.equalsIgnoreCase("RDF/XML")) {
            		format = RDFFormat.RDFXML;
            	}
            	else if (formatValue.equalsIgnoreCase("N-TRIPLE") || formatValue.equalsIgnoreCase("N-TRIPLES") || formatValue.equalsIgnoreCase("NTRIPLE") || formatValue.equalsIgnoreCase("NTRIPLES")) {
            		format = RDFFormat.NTRIPLES;
            	}
            	else if (formatValue.equalsIgnoreCase("TURTLE")) {
            		format = RDFFormat.TURTLE;
            	}
            	else if (formatValue.equalsIgnoreCase("BIN")) {
            		format = RDFFormat.BINARY;
            	}
            	else {
            		format = RDFFormat.N3;
            	}
            }
            FileInputStream source_properties = null;    
            if(cmd.hasOption("sp")) {
                source_properties = new FileInputStream(cmd.getOptionValue("sp"));
                //System.out.println("source properties parameter is equal to " + cmd.getOptionValue("sp"));
                //load the properties
                RMLEngine.getFileMap().load(source_properties);
                engine.runRMLMapping(mapping, graphName, outfile, true, true);
            }
            else {
            	engine.runRMLMapping(mapping, graphName, outfile, true, false);
            	//TODO: replace the above line with the two lines below:
                /*SesameDataSet output = engine.runRMLMapping(mapping, "");
                output.dumpRDF(new PrintStream(new File(outfile)), RDFFormat.RDFXML);*/
            }
            if(cmd.hasOption("g")) 
                    graphName = cmd.getOptionValue("g");           
            
//            System.out.println("--------------------------------------------------------------------------------");
//            System.out.println("RML Processor");
//            System.out.println("--------------------------------------------------------------------------------");
//            System.out.println("");
//            System.out.println("Usage: mvn exec:java -Dexec.args=\"<mapping_file> <output_file> [-sp source.properties] [-g <graph>]\"");
//            System.out.println("");
//            System.out.println("With");
//            System.out.println("    <mapping_file> = The RML mapping document conform with the RML specification (http://semweb.mmlab.be/rml/spec.html)");
//            System.out.println("    <output_file> = The file where the output RDF triples are stored; default in Turtle (http://www.w3.org/TR/turtle/) syntax.");
//            System.out.println("    <sources_properties> = Java properties file containing key-value pairs which configure the data sources used in the mapping file.");
//            System.out.println("    <graph> (optional) = The named graph in which the output RDF triples are stored.");
//            System.out.println("");
//            System.out.println("    An example '<sources_properties>' file 'sources.properties' could contain:");
//            System.out.println("");
//            System.out.println("    #File: sources.properties");
//            System.out.println("    file1=/path/to/file1.csv");
//            System.out.println("    file2=/path/to/file2.json");
//            System.out.println("    file3=/path/to/file3.xml");
//            System.out.println("--------------------------------------------------------------------------------");
            //}
        } catch (IOException | InvalidR2RMLStructureException | InvalidR2RMLSyntaxException | R2RMLDataError | RepositoryException | RDFParseException | SQLException ex) {
            System.out.println(ex.getMessage());
        } catch (ParseException ex) {
            Logger.getLogger(MainTrans.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    private static boolean execute(String mappingURL, String outputURL) {
        try {
            RMLMapping mapping = RMLMappingFactory.extractRMLMapping(mappingURL);

            RMLEngine engine = new RMLEngine();
                       
            SesameDataSet output = engine.runRMLMapping(mapping, "http://example.com");
            PrintStream ps=new PrintStream(new File("outputtriples.txt"));
            output.dumpRDF(ps, RDFFormat.NTRIPLES);

            //SesameDataSet desiredOutput = new SesameDataSet();
            //desiredOutput.addFile(outputURL, RDFFormat.NTRIPLES);

            return true;
        } catch (SQLException | InvalidR2RMLStructureException | InvalidR2RMLSyntaxException | R2RMLDataError | RepositoryException | RDFParseException ex) {
            Logger.getLogger(MainTrans.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MainTrans.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainTrans.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }
    private static void readNamespaces(String filename) throws Exception {
    	try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
    	    for(String line; (line = br.readLine()) != null; ) {
    	        String [] tokens=line.split(" +|\t|,|;");
    	        if(tokens.length!=2){
    	        	throw new Exception("File with namespaces contains a bad line");
    	        }
    	        Config.user_namespaces.put(tokens[0], tokens[1]);
    	    }
    	    br.close();
    	}
    }
    private static void registerFunctions() {
    	FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/asWKT"), new FunctionAsWKT(QLTerm.XPATH_CLASS));
    	FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/hasSerialization"), new FunctionHasSerialization(QLTerm.XPATH_CLASS));
    	FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/asGML"), new FunctionAsGML(QLTerm.XPATH_CLASS));
    	FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/isSimple"), new FunctionIsSimple(QLTerm.XPATH_CLASS));
    	FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/isEmpty"), new FunctionIsEmpty(QLTerm.XPATH_CLASS));
    	FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/is3D"), new FunctionIs3D(QLTerm.XPATH_CLASS));
    	FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/spatialDimension"), new FunctionSpatialDimension(QLTerm.XPATH_CLASS));
    	FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/dimension"), new FunctionDimension(QLTerm.XPATH_CLASS));
    	FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/coordinateDimension"), new FunctionCoordinateDimension(QLTerm.XPATH_CLASS));
    	FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/area"), new FunctionArea(QLTerm.XPATH_CLASS));
    	FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/length"), new FunctionLength(QLTerm.XPATH_CLASS));
    	FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/centroidx"), new FunctionCentroidX(QLTerm.XPATH_CLASS));
    	FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/centroidy"), new FunctionCentroidY(QLTerm.XPATH_CLASS));
    }
}
