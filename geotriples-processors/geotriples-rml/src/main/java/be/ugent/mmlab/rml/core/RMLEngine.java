package be.ugent.mmlab.rml.core;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.R2RMLDataError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;

import be.ugent.mmlab.rml.dataset.FileSesameDataset;
import be.ugent.mmlab.rml.model.LogicalSource;
import be.ugent.mmlab.rml.model.PredicateObjectMap;
import be.ugent.mmlab.rml.model.RMLMapping;
import be.ugent.mmlab.rml.model.ReferencingObjectMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.RMLProcessor;
import be.ugent.mmlab.rml.processor.RMLProcessorFactory;
import be.ugent.mmlab.rml.processor.concrete.ConcreteRMLProcessorFactory;
import be.ugent.mmlab.rml.tools.CriticalSection;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

/**
 * Engine that will perform the mapping starting from the TermMaps
 * 
 * @author mielvandersande, andimou, dimis
 */
public class RMLEngine {
	private CriticalSection cs=new CriticalSection();
	private String memory_input=null;
    // Logger
    private static Logger log = LoggerFactory.getLogger(RMLEngine.class);
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
            String baseIRI,RDFFormat format) throws SQLException,
            R2RMLDataError, UnsupportedEncodingException {
        return runRMLMapping(rmlMapping, baseIRI, null, false, false,format);
    }

    /**
     *
     * @param rmlMapping Parsed RML mapping
     * @param baseIRI base URI of the resulting RDF
     * @param pathToNativeStore path if triples have to be stored in sesame
     * triple store instead of memory
     * @param format 
     * @return
     * @throws SQLException
     * @throws R2RMLDataError
     * @throws UnsupportedEncodingException
     */
    public SesameDataSet runRMLMapping(RMLMapping rmlMapping,
            String baseIRI, String pathToNativeStore, boolean filebased, boolean source_properties, RDFFormat format) throws SQLException,
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

        SesameDataSet sesameDataSet=null ;
        // Update baseIRI
        this.baseIRI = baseIRI;
        log.debug("RMLEngine base IRI " + baseIRI);
        if (filebased) {
            log.debug("[RMLEngine:runRMLMapping] Use direct file "
                    + pathToNativeStore);
            sesameDataSet = new FileSesameDataset(pathToNativeStore,format);
//        } else if (pathToNativeStore != null) { // Check if use of native store is required
//            log.debug("[RMLEngine:runRMLMapping] Use native store "
//                    + pathToNativeStore);
//            sesameDataSet = new SesameDataSet(pathToNativeStore, false);
        } else {
            log.debug("[RMLEngine:runRMLMapping] Use default store (memory) ");
            //Repository rr= new SailRepository( new MemoryStore() );
            //RepositoryConnection rc=rr.getConnection();
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
    
    ConcurrentHashMap<String, List<String>> cacheToAvoidOpeningFiles=new ConcurrentHashMap<>();
    protected void generateRDFTriples(SesameDataSet sesameDataSet,
            RMLMapping r2rmlMapping, boolean filebased, boolean source_properties) throws SQLException, R2RMLDataError,
            UnsupportedEncodingException {
    	ExecutorService executor = Executors.newFixedThreadPool(8);
    	
        log.debug("[RMLEngine:generateRDFTriples] Generate RDF triples... ");
        int delta = 0;

        RMLProcessorFactory factory = new ConcreteRMLProcessorFactory();
        List<TriplesMap> tmaps = new ArrayList<TriplesMap>(r2rmlMapping.getTriplesMaps());
        Collections.sort(tmaps); //TODO uncomment for serial execution!!!
        for (TriplesMap triplesMap : tmaps) {
        	
            if (check_ReferencingObjectMap(r2rmlMapping, triplesMap)) 
                continue; //i think this check is pointless /dimis
            //FileInputStream input = null;
            LogicalSource ls=triplesMap.getLogicalSource();
            String logicalsource=ls.getIdentifier();
            String iteratorstr=ls.getReference();
            //System.out.println("[RMLEngine:generateRDFTriples] Generate RDF triples for " + triplesMap.getName());
            //System.out.println(ls.getReference());
            boolean shouldnotvisit=false;
            if(ls.getReferenceFormulation().equals(QLTerm.XPATH_CLASS)){
            	
            	
            	if(cacheToAvoidOpeningFiles.containsKey(logicalsource)){
            		
            		List<String> list = cacheToAvoidOpeningFiles.get(logicalsource);
            		for(String sub:list){
            			if(iteratorstr.startsWith(sub)){
            				shouldnotvisit=true;
            				break;
            			}
            		}
            	}
            }
            if(shouldnotvisit==true){
            	log.debug("Should not visit "+ iteratorstr);
            	continue;
            }
            log.info("Parsing iterator "+iteratorstr);
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
                //fileName = getClass().getResource(triplesMap.getLogicalSource().getIdentifier()).getFile();
            	fileName = triplesMap.getLogicalSource().getIdentifier();
            try {
                log.debug("[RMLEngine:generateRDFTriples] next file to be opened " + fileName);
                //add control in case rml:source is not declared
                getFileMap().put(fileName, fileName);
                //input = new FileInputStream(fileName);
                //getFileMap().load(input);
           } catch (Exception ex) {
                LoggerFactory.getLogger(RMLEngine.class.getName()).error(ex.toString());
           }

            //long triples=processor.execute(sesameDataSet, triplesMap, new NodeRMLPerformer(processor), fileName);
            Collection<Statement> statements = processor.execute(sesameDataSet, triplesMap, new NodeRMLPerformer(processor), fileName,false);
            //System.out.println("Toses tripletes: " +triples);
            //System.out.println("Current total triples: "+sesameDataSet.getSize());
            //executor.execute(new WorkerThread(processor, sesameDataSet, triplesMap, fileName,iteratorstr));
            //if(sesameDataSet.getSize() - delta ==0){
            if(statements.size()==0){
            	if(!cacheToAvoidOpeningFiles.containsKey(logicalsource))
            	{
            		cacheToAvoidOpeningFiles.put(logicalsource, new ArrayList<String>());
            	}
            	cacheToAvoidOpeningFiles.get(logicalsource).add(iteratorstr);
            }
            /*log.info("[RMLEngine:generateRDFTriples] "
                    + (sesameDataSet.getSize() - delta)
                    + " triples generated for " + triplesMap.getName());
            delta = sesameDataSet.getSize();*/
                        
//            try {
//                //input.close();
//            } catch (IOException ex) {
//                LoggerFactory.getLogger(RMLEngine.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }
        
//        executor.shutdown();
//        try {
//        	executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        if(filebased)
            try {
            	log.debug("Closing repository..");
                sesameDataSet.closeRepository();
            } catch (RepositoryException ex) {
                log.error("[RMLEngine:generateRDFTriples] Cannot close output repository", ex);
            }
    }
    /**
	 * @return the memory_input
	 */
	public String getMemory_input() {
		return memory_input;
	}

	/**
	 * @param memory_input the memory_input to set
	 */
	public void setMemory_input(String memory_input) {
		this.memory_input = memory_input;
	}
	public class WorkerThread implements Runnable {

        private String command;
		private RMLProcessor processor;
		private SesameDataSet dataset;
		private TriplesMap map;
		private String fileName;
		private String iteratorstr;

        public WorkerThread(RMLProcessor processor,SesameDataSet dataset, TriplesMap map, String fileName,String iteratorstr ){
            this.processor=processor;
            this.dataset=dataset;
            this.map=map;
            this.fileName=fileName;
            this.iteratorstr=iteratorstr;
        }

        @Override
        public void run() {
            
        	//long triples=processor.execute(dataset, map, new NodeRMLPerformer(processor), fileName);
        	Collection<Statement> statements = processor.execute(dataset, map, new NodeRMLPerformer(processor), fileName,false);
        	//note that this is not always return the accurate count of triples created with execute, this is by design
        	if(statements.size() ==0){
        		try {
					cs.enter_read();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
            	if(!cacheToAvoidOpeningFiles.containsKey(fileName))
            	{
            		try {
            			cs.exit_read();
						cs.enter_write();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
            		if(!cacheToAvoidOpeningFiles.containsKey(fileName))
            			cacheToAvoidOpeningFiles.put(fileName, new ArrayList<String>());
            		cs.exit_write();
            	}
            	try {
					cs.enter_write();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            		cacheToAvoidOpeningFiles.get(fileName).add(iteratorstr);
            	cs.exit_write();
            	
            }
            /*log.info("[RMLEngine:generateRDFTriples] "
                    + (triples)
                    + " triples generated (Not accurate) for " + triplesMap.getName());*/
        }

        

        @Override
        public String toString(){
            return this.command;
        }
    }
    
    
}
