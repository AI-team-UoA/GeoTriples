package be.ugent.mmlab.rml.core;

import be.ugent.mmlab.rml.dataset.FileSesameDataset;
import be.ugent.mmlab.rml.model.LogicalSource;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.RMLMapping;
import be.ugent.mmlab.rml.model.ReferencingObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.RMLProcessor;
import be.ugent.mmlab.rml.processor.RMLProcessorFactory;
import be.ugent.mmlab.rml.processor.concrete.ConcreteRMLProcessorFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.R2RMLDataError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.repository.RepositoryException;

/**
 * Engine that will perform the mapping starting from the TermMaps
 * 
 * @author mielvandersande, andimou
 */
public class RMLEngine {

    // Log
    private static Log log = LogFactory.getLog(RMLEngine.class);
    // A base IRI used in resolving relative IRIs produced by the R2RML mapping.
    protected String baseIRI;
    public static boolean source_properties; //this is the only public modification from private , dimis
    //Properties containing the identifiers for files
    //There are probably better ways to do this than a static variable
    private static Properties fileMap = new Properties();

    public static Properties getFileMap() {
        return fileMap;
    }
    
    public static boolean getSourceProperties() {
        return source_properties;
    }
    
    protected String getIdentifier(LogicalSource ls) {
        return RMLEngine.getFileMap().getProperty(ls.getIdentifier());
    }

    /**
     * Generate RDF based on a RML mapping
     *
     * @param rmlMapping Parsed RML mapping
     * @param baseIRI base URI of the resulting RDF
     * @return dataset containing the triples
     * @throws SQLException
     * @throws R2RMLDataError
     * @throws UnsupportedEncodingException
     */
    public SesameDataSet runRMLMapping(RMLMapping rmlMapping,
            String baseIRI) throws SQLException,
            R2RMLDataError, UnsupportedEncodingException {
        return runRMLMapping(rmlMapping, baseIRI, null, false, false);
    }

    /**
     *
     * @param rmlMapping Parsed RML mapping
     * @param baseIRI base URI of the resulting RDF
     * @param pathToNativeStore path if triples have to be stored in sesame
     * triple store instead of memory
     * @return
     * @throws SQLException
     * @throws R2RMLDataError
     * @throws UnsupportedEncodingException
     */
    public SesameDataSet runRMLMapping(RMLMapping rmlMapping,
            String baseIRI, String pathToNativeStore, boolean filebased, boolean source_properties) throws SQLException,
            R2RMLDataError, UnsupportedEncodingException {
        RMLEngine.source_properties = source_properties;
        long startTime = System.nanoTime();

        log.debug("[RMLEngine:runRMLMapping] Run RML mapping... ");
        if (rmlMapping == null) {
            throw new IllegalArgumentException(
                    "[RMLEngine:runRMLMapping] No RML Mapping object found.");
        }
        if (baseIRI == null) {
            throw new IllegalArgumentException(
                    "[RMLEngine:runRMLMapping] No base IRI found.");
        }

        SesameDataSet sesameDataSet ;
        // Update baseIRI
        this.baseIRI = baseIRI;
        log.info("RMLEngine base IRI " + baseIRI);

        if (filebased) {
            log.debug("[RMLEngine:runRMLMapping] Use direct file "
                    + pathToNativeStore);
            sesameDataSet = new FileSesameDataset(pathToNativeStore);
        } else if (pathToNativeStore != null) { // Check if use of native store is required
            log.debug("[RMLEngine:runRMLMapping] Use native store "
                    + pathToNativeStore);
            sesameDataSet = new SesameDataSet(pathToNativeStore, false);
        } else {
            log.debug("[RMLEngine:runRMLMapping] Use default store (memory) ");
            sesameDataSet = new SesameDataSet();
        }
        // Explore RML Mapping TriplesMap objects  
 
        generateRDFTriples(sesameDataSet, rmlMapping, filebased, source_properties);
        
	log.info("[RMLEngine:generateRDFTriples] All triples were generated ");
        
	long endTime = System.nanoTime();
        long duration = endTime - startTime;
        log.debug("[RMLEngine:runRMLMapping] RML mapping done! Generated " + sesameDataSet.getSize() + " in " + ((double) duration) / 1000000000 + "s . ");
        return sesameDataSet;
    }
    //changed to protected by dimis
    protected boolean check_ReferencingObjectMap(RMLMapping mapping, TriplesMap map) {
        for (TriplesMap triplesMap : mapping.getTriplesMaps()) 
            for (PredicateObjectMap predicateObjectMap : triplesMap.getPredicateObjectMaps()) 
                if (predicateObjectMap.hasReferencingObjectMaps()) 
                    for (ReferencingObjectMap referencingObjectMap : predicateObjectMap.getReferencingObjectMaps()) 
                        if (referencingObjectMap.getJoinConditions().isEmpty() 
                                && referencingObjectMap.getParentTriplesMap() == map
                                && referencingObjectMap.getParentTriplesMap().getLogicalSource().getIdentifier().equals(triplesMap.getLogicalSource().getIdentifier())) 
                            return true;
        return false;
    }

    /**
     * This process adds RDF triples to the output dataset. Each generated
     * triple is placed into one or more graphs of the output dataset. The
     * generated RDF triples are determined by the following algorithm.
     *
     * @param sesameDataSet
     * @param rmlMapping
     * @throws SQLException
     * @throws R2RMLDataError
     * @throws UnsupportedEncodingException
     */
    protected void generateRDFTriples(SesameDataSet sesameDataSet,
            RMLMapping r2rmlMapping, boolean filebased, boolean source_properties) throws SQLException, R2RMLDataError,
            UnsupportedEncodingException {

        log.debug("[RMLEngine:generateRDFTriples] Generate RDF triples... ");
        int delta = 0;

        RMLProcessorFactory factory = new ConcreteRMLProcessorFactory();

        for (TriplesMap triplesMap : r2rmlMapping.getTriplesMaps()) {
            if (check_ReferencingObjectMap(r2rmlMapping, triplesMap)) 
                continue; //i think this check is pointless /dimis
            //FileInputStream input = null;
            System.out.println("[RMLEngine:generateRDFTriples] Generate RDF triples for " + triplesMap.getName());
            //need to add control if reference Formulation is not defined
            //need to add check for correct spelling, aka rml:queryLanguage and not rml:referenceFormulation otherwise breaks
            RMLProcessor processor = factory.create(triplesMap.getLogicalSource().getReferenceFormulation());
            //URL filePath = getClass().getProtectionDomain().getCodeSource().getLocation();
            String fileName;
            if(filebased)
                if(source_properties){
                    String file = triplesMap.getLogicalSource().getIdentifier();
                    fileName = RMLEngine.getFileMap().getProperty(file.toString());
                }
                else
                    fileName = triplesMap.getLogicalSource().getIdentifier();
            else
                fileName = getClass().getResource(triplesMap.getLogicalSource().getIdentifier()).getFile();
            try {
                log.info("[RMLEngine:generateRDFTriples] next file to be opened " + fileName);
                //add control in case rml:source is not declared
                getFileMap().put(fileName, fileName);
                //input = new FileInputStream(fileName);
                //getFileMap().load(input);
           } catch (Exception ex) {
                Logger.getLogger(RMLEngine.class.getName()).log(Level.SEVERE, null, ex);
           }

            processor.execute(sesameDataSet, triplesMap, new NodeRMLPerformer(processor), fileName);

            log.info("[RMLEngine:generateRDFTriples] "
                    + (sesameDataSet.getSize() - delta)
                    + " triples generated for " + triplesMap.getName());
            delta = sesameDataSet.getSize();
                        
//            try {
//                //input.close();
//            } catch (IOException ex) {
//                Logger.getLogger(RMLEngine.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }
        if(filebased)
            try {
                sesameDataSet.closeRepository();
            } catch (RepositoryException ex) {
                log.error("[RMLEngine:generateRDFTriples] Cannot close output repository", ex);
            }
    }
}
