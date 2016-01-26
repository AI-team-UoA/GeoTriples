package be.ugent.mmlab.rml.main;

/***************************************************************************
 *
 * @author: dimis (dimis@di.uoa.gr)
 * 
 ****************************************************************************/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.function.Config;
import be.ugent.mmlab.rml.function.FunctionAdd;
import be.ugent.mmlab.rml.function.FunctionArea;
import be.ugent.mmlab.rml.function.FunctionAsGML;
import be.ugent.mmlab.rml.function.FunctionAsWKT;
import be.ugent.mmlab.rml.function.FunctionCentroidX;
import be.ugent.mmlab.rml.function.FunctionCentroidY;
import be.ugent.mmlab.rml.function.FunctionContains;
import be.ugent.mmlab.rml.function.FunctionCoordinateDimension;
import be.ugent.mmlab.rml.function.FunctionCrosses;
import be.ugent.mmlab.rml.function.FunctionDimension;
import be.ugent.mmlab.rml.function.FunctionDisjoint;
import be.ugent.mmlab.rml.function.FunctionDistance;
import be.ugent.mmlab.rml.function.FunctionEQUI;
import be.ugent.mmlab.rml.function.FunctionEquals;
import be.ugent.mmlab.rml.function.FunctionFactory;
import be.ugent.mmlab.rml.function.FunctionGreaterThan;
import be.ugent.mmlab.rml.function.FunctionHasSerialization;
import be.ugent.mmlab.rml.function.FunctionIntersects;
import be.ugent.mmlab.rml.function.FunctionIs3D;
import be.ugent.mmlab.rml.function.FunctionIsEmpty;
import be.ugent.mmlab.rml.function.FunctionIsSimple;
import be.ugent.mmlab.rml.function.FunctionLength;
import be.ugent.mmlab.rml.function.FunctionOverlaps;
import be.ugent.mmlab.rml.function.FunctionSpatialDimension;
import be.ugent.mmlab.rml.function.FunctionSubtract;
import be.ugent.mmlab.rml.function.FunctionTouches;
import be.ugent.mmlab.rml.function.FunctionWithin;
import be.ugent.mmlab.rml.model.RMLMapping;

/**
 * 
 * @author mielvandersande, andimou
 */
public class MainTrans {

	/**
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 */
	private final static Log log = LogFactory.getLog(MainTrans.class);

	public static void main(String[] args) throws Exception {
		try {
			Object file = null;
			String outfile = null, inputfile = null;
			String graphName = "";
			RDFFormat format = RDFFormat.NTRIPLES;
			// create Options object
			Options options = new Options();
			// add options
			options.addOption("sp", true, "source properties file");
			options.addOption("g", true, "Graph name");
			options.addOption("epsg", true, "EPSG code");
			options.addOption("f", true, "output RDF Format");
			options.addOption("ns", true, "file with namespaces for XML use only");

			options.addOption("gml3", false, "Use gml3 reader for the geometries");
			options.addOption("gml2", false, "Use gml2 reader for the geometries");
			options.addOption("kml", false, "Use kml reader for the geometries");

			options.addOption("readyeop", false, "Use GeoTriples' EOP 2.0 mapping document");
			options.addOption("readykml", false, "Use GeoTriples' KML 2.2 mapping document");
			options.addOption("readynetcdf", false, "Use GeoTriples' NetCDF v2.2 mapping document");
			options.addOption("i", true, "Input source. Use with -readyeop or -readykml");
			options.addOption("gdal", false, "Use GDAL as the library for the manipulation of the Geometries");
			options.addOption("olddbprocessor", false, "Use old db processor");

			// should be new DefaultParser() but requires cli 1.3 instead of
			// clli 1.2
			CommandLineParser parser = new BasicParser();
			CommandLine cmd = parser.parse(options, args);
			// execute(args[0],args[1]);
			// System.exit(0);

			if (cmd.hasOption("readykml") || cmd.hasOption("readyeop")) {
				if (!cmd.hasOption("i")) {
					HelpFormatter formatter = new HelpFormatter();
					formatter.printHelp("GeoTriples with RML command line tool", options);
					System.exit(1);
				} else {
					inputfile = cmd.getOptionValue("i");
				}

				// URL.setURLStreamHandlerFactory(new
				// ConfigurableStreamHandlerFactory("file", new Handler()));
				if (cmd.hasOption("readykml")) {
					file = Thread.currentThread().getContextClassLoader().getResource("mappingkml.ttl");
				} else if(cmd.hasOption("readyeop")) {
					throw new UnsupportedOperationException("Currently the -readyeop option is not supported. Please contact dimis@di.uoa.gr");
					//file = Thread.currentThread().getContextClassLoader().getResource("mappingeop.ttl");
				} else if(cmd.hasOption("readynetcdf")){
					file = Thread.currentThread().getContextClassLoader().getResource("mappingNetCDF.ttl");
				}
				File temp = File.createTempFile("temp-mapping.ttl", ".");

				InputStream fis = (((URL) file).openConnection().getInputStream());

				OutputStream fos = (new FileOutputStream(temp));

				String content = IOUtils.toString(fis);
				// System.out.println(inputfile);
				content = content.replaceAll("rml:source.*\n",
						"rr:source \"" + (new File(inputfile)).getAbsolutePath() + "\";\n");
				IOUtils.write(content, fos);
				fis.close();
				fos.close();
				file = temp.getAbsolutePath();
				log.info("The mapping file is at: " + file);
			} else {
				file = args[args.length - 2];
			}
			outfile = args[args.length - 1];
			RMLMapping mapping = RMLMappingFactory.extractRMLMapping(file);
			RMLEngine engine = new RMLEngine();
			registerFunctions();
			// but hack-fix but important

			// System.out.println("mapping document " + file);
			if (cmd.hasOption("epsg")) {
				Config.EPSG_CODE = cmd.getOptionValue("epsg");
			}
			if (cmd.hasOption("ns")) {
				readNamespaces(cmd.getOptionValue("ns"));
			} else {
				readNamespacesFromMapping(file);
			}
			if (cmd.hasOption("f")) {
				String formatValue = cmd.getOptionValue("f");
				if (formatValue.equalsIgnoreCase("N3")) {
					format = RDFFormat.N3;
				} else if (formatValue.equalsIgnoreCase("RDF/XML")) {
					format = RDFFormat.RDFXML;
				} else if (formatValue.equalsIgnoreCase("N-TRIPLE") || formatValue.equalsIgnoreCase("N-TRIPLES")
						|| formatValue.equalsIgnoreCase("NTRIPLE") || formatValue.equalsIgnoreCase("NTRIPLES")) {
					format = RDFFormat.NTRIPLES;
				} else if (formatValue.equalsIgnoreCase("TURTLE")) {
					format = RDFFormat.TURTLE;
				} else if (formatValue.equalsIgnoreCase("BIN")) {
					format = RDFFormat.BINARY;
				} else {
					format = RDFFormat.N3;
				}
			}
			if (cmd.hasOption("gml3"))
				Config.setGML3();
			if (cmd.hasOption("gml2"))
				Config.setGML2();
			if (cmd.hasOption("kml"))
				Config.setKML();

			if (cmd.hasOption("readyeop")) {
				Config.setGML3();

			}
			if (cmd.hasOption("readykml")) {
				Config.setKML();

			}
			if (cmd.hasOption("gdal")) {
				Config.setGDAL();
			}
			if(cmd.hasOption("olddbprocessor")){
				Config.setOldDBProcessor();
			}
			
			SesameDataSet output = null;
			FileInputStream source_properties = null;
			if (cmd.hasOption("sp")) {
				source_properties = new FileInputStream(cmd.getOptionValue("sp"));
				// System.out.println("source properties parameter is equal to "
				// + cmd.getOptionValue("sp"));
				// load the properties
				RMLEngine.getFileMap().load(source_properties);
				output = engine.runRMLMapping(mapping, graphName, outfile, true, true,format);
			} else {
				output = engine.runRMLMapping(mapping, graphName, outfile, (outfile!=null), false,format);

				// TODO: replace the above line with the two lines below:
				/*
				 * SesameDataSet output = engine.runRMLMapping(mapping, "");
				 * output.dumpRDF(new PrintStream(new File(outfile)),
				 * RDFFormat.RDFXML);
				 */
			}
			if (cmd.hasOption("g"))
				graphName = cmd.getOptionValue("g");
			if(outfile==null){
				output.dumpRDF(System.out ,format);
			}
			//output.dumpRDF((outfile == null) ? System.out : new PrintStream(new File(outfile)), format);
			// System.out.println("--------------------------------------------------------------------------------");
			// System.out.println("RML Processor");
			// System.out.println("--------------------------------------------------------------------------------");
			// System.out.println("");
			// System.out.println("Usage: mvn exec:java
			// -Dexec.args=\"<mapping_file> <output_file> [-sp
			// source.properties] [-g <graph>]\"");
			// System.out.println("");
			// System.out.println("With");
			// System.out.println(" <mapping_file> = The RML mapping document
			// conform with the RML specification
			// (http://semweb.mmlab.be/rml/spec.html)");
			// System.out.println(" <output_file> = The file where the output
			// RDF triples are stored; default in Turtle
			// (http://www.w3.org/TR/turtle/) syntax.");
			// System.out.println(" <sources_properties> = Java properties file
			// containing key-value pairs which configure the data sources used
			// in the mapping file.");
			// System.out.println(" <graph> (optional) = The named graph in
			// which the output RDF triples are stored.");
			// System.out.println("");
			// System.out.println(" An example '<sources_properties>' file
			// 'sources.properties' could contain:");
			// System.out.println("");
			// System.out.println(" #File: s)ources.properties");
			// System.out.println(" file1=/path/to/file1.csv");
			// System.out.println(" file2=/path/to/file2.json");
			// System.out.println(" file3=/path/to/file3.xml");
			// System.out.println("--------------------------------------------------------------------------------");
			// }
		} catch (IOException | InvalidR2RMLStructureException | InvalidR2RMLSyntaxException | R2RMLDataError
				| RepositoryException | RDFParseException | SQLException ex) {
			System.out.println(ex.getMessage());
		} catch (ParseException ex) {
			Logger.getLogger(MainTrans.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	private static boolean execute(String mappingURL, String outputURL) {
		try {
			RMLMapping mapping = RMLMappingFactory.extractRMLMapping(mappingURL);

			RMLEngine engine = new RMLEngine();

			SesameDataSet output = engine.runRMLMapping(mapping, "http://example.com",RDFFormat.NTRIPLES);
			PrintStream ps = new PrintStream(new File("outputtriples.txt"));
			output.dumpRDF(ps, RDFFormat.NTRIPLES);

			// SesameDataSet desiredOutput = new SesameDataSet();
			// desiredOutput.addFile(outputURL, RDFFormat.NTRIPLES);

			return true;
		} catch (SQLException | InvalidR2RMLStructureException | InvalidR2RMLSyntaxException | R2RMLDataError
				| RepositoryException | RDFParseException ex) {
			Logger.getLogger(MainTrans.class.getName()).log(Level.SEVERE, null, ex);
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(MainTrans.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(MainTrans.class.getName()).log(Level.SEVERE, null, ex);
		}

		return false;
	}

	private static void readNamespaces(String filename) throws Exception {
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			for (String line; (line = br.readLine()) != null;) {
				String[] tokens = line.split(" +|\t|,|;");
				if (tokens.length != 2) {
					throw new Exception("File with namespaces contains a bad line");
				}
				Config.user_namespaces.put(tokens[0], tokens[1]);
			}
			br.close();
		}
	}

	private static void readNamespacesFromMapping(Object filepath) throws Exception {
		if (filepath instanceof String) {
			try (BufferedReader br = new BufferedReader(new FileReader((String) filepath))) {
				for (String line; (line = br.readLine()) != null;) {
					if (line.trim().startsWith("@prefix")) {
						String[] tokens = line.split("\\s+");
						if (tokens.length < 3) {
							throw new Exception("Mapping file in contains a bad line with @prefix");
						}
//						System.out.println("adding namespace " + tokens[1].replace(":", "").trim() + ": "
//								+ tokens[2].replaceAll("[<>#]", ""));
						Config.user_namespaces.put(tokens[1].replace(":", "").trim(),
								tokens[2].replaceAll("[<>#]", ""));
					}

				}
				br.close();
			}
		} else {
			URL filenane = ((URL) filepath);
			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(filenane.openConnection().getInputStream()))) {
				for (String line; (line = br.readLine()) != null;) {
					if (line.trim().startsWith("@prefix")) {
						String[] tokens = line.split("\\s+");
						if (tokens.length < 3) {
							throw new Exception("Mapping file in contains a bad line with @prefix");
						}
//						System.out.println("adding namespace " + tokens[1].replace(":", "").trim() + ": "
//								+ tokens[2].replaceAll("[<>#]", ""));
						Config.user_namespaces.put(tokens[1].replace(":", "").trim(),
								tokens[2].replaceAll("[<>#]", ""));
					}

				}
				br.close();
			}

		}

	}

	private static void registerFunctions() {
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/equi"),
				new FunctionEQUI()); // dont delete or change this line, it
										// replaces the equi join functionality

		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/asWKT"),
				new FunctionAsWKT());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/hasSerialization"),
				new FunctionHasSerialization());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/asGML"),
				new FunctionAsGML());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/isSimple"),
				new FunctionIsSimple());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/isEmpty"),
				new FunctionIsEmpty());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/is3D"),
				new FunctionIs3D());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/spatialDimension"),
				new FunctionSpatialDimension());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/dimension"),
				new FunctionDimension());
		FunctionFactory.registerFunction(
				new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/coordinateDimension"),
				new FunctionCoordinateDimension());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/area"),
				new FunctionArea());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/length"),
				new FunctionLength());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/centroidx"),
				new FunctionCentroidX());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/centroidy"),
				new FunctionCentroidY());
		
		/*Tolopogical Relations RRXF namespace*/
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/contains"),
				new FunctionContains());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/equals"),
				new FunctionEquals());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/intersects"),
				new FunctionIntersects());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/disjoint"),
				new FunctionDisjoint());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/within"),
				new FunctionWithin());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/touches"),
				new FunctionTouches());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/crosses"),
				new FunctionCrosses());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/overlaps"),
				new FunctionOverlaps());
		
		/*Tolopogical Relations GeoSPARQL*/
		FunctionFactory.registerFunction(new URIImpl("http://www.opengis.net/def/geosparql/functions/sfContains"),
				new FunctionContains());
		FunctionFactory.registerFunction(new URIImpl("http://www.opengis.net/def/geosparql/functions/sfEquals"),
				new FunctionEquals());
		FunctionFactory.registerFunction(new URIImpl("http://www.opengis.net/def/geosparql/functions/sfIntersects"),
				new FunctionIntersects());
		FunctionFactory.registerFunction(new URIImpl("http://www.opengis.net/def/geosparql/functions/sfDisjoint"),
				new FunctionDisjoint());
		FunctionFactory.registerFunction(new URIImpl("http://www.opengis.net/def/geosparql/functions/sfWithin"),
				new FunctionWithin());
		FunctionFactory.registerFunction(new URIImpl("http://www.opengis.net/def/geosparql/functions/sfTouches"),
				new FunctionTouches());
		FunctionFactory.registerFunction(new URIImpl("http://www.opengis.net/def/geosparql/functions/sfCrosses"),
				new FunctionCrosses());
		FunctionFactory.registerFunction(new URIImpl("http://www.opengis.net/def/geosparql/functions/sfOverlaps"),
				new FunctionOverlaps());
		
		/*Some other functions*/
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/distance"),
				new FunctionDistance());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/greaterThan"),
				new FunctionGreaterThan());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/add"),
				new FunctionAdd());
		FunctionFactory.registerFunction(new URIImpl("http://www.w3.org/ns/r2rml-ext/functions/def/subtract"),
				new FunctionSubtract());

	}

}
