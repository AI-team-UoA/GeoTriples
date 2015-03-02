package be.ugent.mmlab.rml.main;
/***************************************************************************
*
* @author: dimis (dimis@di.uoa.gr)
* 
****************************************************************************/
import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.RMLEngineTrans;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.core.RMLMappingFactoryTrans;
import be.ugent.mmlab.rml.model.RMLMapping;
import be.ugent.mmlab.rml.model.transformation.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
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
     */
    public static void main(String[] args) {
        try {
            String graphName = "";
            // create Options object
            Options options = new Options();            
            // add options
            options.addOption("sp", true, "source properties file");
            options.addOption("g", true, "Graph name");
            options.addOption("epsg", true, "EPSG code");
            //should be new DefaultParser() but requires cli 1.3 instead of clli 1.2
            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse( options, args);
            //execute(args[0],args[1]);
            //System.exit(0);
            RMLMapping mapping = RMLMappingFactoryTrans.extractRMLMapping(args[0]);
            RMLEngine engine = new RMLEngineTrans();
            System.out.println("mapping document " + args[0]);
            if(cmd.hasOption("epsg"))
            {
            	Config.EPSG_CODE=cmd.getOptionValue("epsg");
            }
            FileInputStream source_properties = null;    
            if(cmd.hasOption("sp")) {
                source_properties = new FileInputStream(cmd.getOptionValue("sp"));
                System.out.println("source properties parameter is equal to " + cmd.getOptionValue("sp"));
                //load the properties
                RMLEngine.getFileMap().load(source_properties);
                engine.runRMLMapping(mapping, graphName, args[1], true, true);
            }
            else
                engine.runRMLMapping(mapping, graphName, args[1], true, false);
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
            RMLMapping mapping = RMLMappingFactoryTrans.extractRMLMapping(mappingURL);

            RMLEngine engine = new RMLEngineTrans();
                       
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
}
