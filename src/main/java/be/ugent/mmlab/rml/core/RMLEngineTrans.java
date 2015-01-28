package be.ugent.mmlab.rml.core;
/***************************************************************************
*
* @author: dimis (dimis@di.uoa.gr)
* 
****************************************************************************/
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.repository.RepositoryException;

import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.R2RMLDataError;
import be.ugent.mmlab.rml.dataset.FileSesameDataset;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.PredicateObjectMapTrans;
import be.ugent.mmlab.rml.model.RMLMapping;
import be.ugent.mmlab.rml.model.ReferencingObjectMap;
import be.ugent.mmlab.rml.model.TransformationObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.RMLProcessor;
import be.ugent.mmlab.rml.processor.RMLProcessorFactory;
import be.ugent.mmlab.rml.processor.concrete.ConcreteRMLProcessorFactoryTrans;

public class RMLEngineTrans extends RMLEngine{
	// Log
    private static Log log = LogFactory.getLog(RMLEngineTrans.class);
    private static boolean source_properties;
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
    @Override
   public SesameDataSet runRMLMapping(RMLMapping rmlMapping,
           String baseIRI, String pathToNativeStore, boolean filebased, boolean source_properties) throws SQLException,
           R2RMLDataError, UnsupportedEncodingException {
       RMLEngineTrans.source_properties = source_properties;
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
       log.debug("RMLEngine base IRI " + baseIRI);

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
       log.info("[RMLEngine:runRMLMapping] RML mapping done! Generated " + sesameDataSet.getSize() + " in " + ((double) duration) / 1000000000 + "s . ");
       return sesameDataSet;
   }
    
	private boolean check_TransformationObjectMap(RMLMapping mapping, TriplesMap map) {
        for (TriplesMap triplesMap : mapping.getTriplesMaps()) 
            for (PredicateObjectMap predicateObjectMapOrigin : triplesMap.getPredicateObjectMaps()) 
            {
            	PredicateObjectMapTrans predicateObjectMap = (PredicateObjectMapTrans)predicateObjectMapOrigin;
                if (predicateObjectMap.hasTransformationObjectMaps()) 
                    for (TransformationObjectMap transformationObjectMap : predicateObjectMap.getTransformationObjectMaps()) 
                        if (transformationObjectMap.getTransformationFunction().isEmpty() ) 
                            return true;
            }
        return false;
    }
	@Override
	protected void generateRDFTriples(SesameDataSet sesameDataSet,
            RMLMapping r2rmlMapping, boolean filebased, boolean source_properties) throws SQLException, R2RMLDataError,
            UnsupportedEncodingException {

        log.debug("[RMLEngine:generateRDFTriples] Generate RDF triples... ");
        int delta = 0;

        RMLProcessorFactory factory = new ConcreteRMLProcessorFactoryTrans();

        for (TriplesMap triplesMap : r2rmlMapping.getTriplesMaps()) {
            if (check_ReferencingObjectMap(r2rmlMapping, triplesMap) || check_TransformationObjectMap(r2rmlMapping, triplesMap)) 
                continue;
            FileInputStream input = null;
            log.info("[RMLEngine:generateRDFTriples] Generate RDF triples for " + triplesMap.getName());
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
            {
            	fileName = triplesMap.getLogicalSource().getIdentifier();
            	//System.out.println(triplesMap.getLogicalSource().getIdentifier());
                //fileName = getClass().getResource(triplesMap.getLogicalSource().getIdentifier()).getFile();
            }
            try {
                log.info("[RMLEngine:generateRDFTriples] next file to be opened " + fileName);
                //add control in case rml:source is not declared
                getFileMap().put(fileName, fileName);
                input = new FileInputStream(fileName);
                getFileMap().load(input);
           } catch (IOException ex) {
                Logger.getLogger(RMLEngine.class.getName()).log(Level.SEVERE, null, ex);
           }

            processor.execute(sesameDataSet, triplesMap, new NodeRMLPerformer(processor), fileName);

            log.info("[RMLEngine:generateRDFTriples] "
                    + (sesameDataSet.getSize() - delta)
                    + " triples generated for " + triplesMap.getName());
            delta = sesameDataSet.getSize();
                        
            try {
                input.close();
            } catch (IOException ex) {
                Logger.getLogger(RMLEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(filebased)
            try {
                sesameDataSet.closeRepository();
            } catch (RepositoryException ex) {
                log.error("[RMLEngine:generateRDFTriples] Cannot close output repository", ex);
            }
    }
}
