package be.ugent.mmlab.rml.core;

import java.util.ArrayList;
import java.util.List;

import be.ugent.mmlab.rml.function.Function;
import be.ugent.mmlab.rml.function.FunctionFactory;
import be.ugent.mmlab.rml.model.TermMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.RMLProcessor;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;

/**
 * Performer to do joins without any join conditions
 *
 * @author mielvandersande, andimou
 */
public class DependencyRMLPerformer extends NodeRMLPerformer{
    
    private static Log log = LogFactory.getLog(RMLMappingFactory.class);
    private Resource subject;
    private URI predicate;
    private TriplesMap parentTriplesMap;
    private RMLProcessor parentprocessor; //may be the same with a processor in the chain, but not necessary
    
    private List<TermMap> termMaps;
    List<ArgumentPosition> positions;
    
    private List<URI> functionURIS;
    private RMLProcessor dependencyProcessor=null;
    private DependencyRMLPerformer dependencyPerformer=null;
    private List<List<String>> arguments; //shared with all performers
	private TriplesMap ownmap;
	private String logicalsource;
    
    public DependencyRMLPerformer(RMLProcessor processor, Resource subject, URI predicate,TriplesMap parentTriplesMap, List<TermMap> termMaps , List<ArgumentPosition> positions,
    		DependencyRMLPerformer nextperformer,RMLProcessor dependencyProcessor,
    		RMLProcessor parentprocessor,
    		List<List<String>> arguments, List<URI> functions,
    		TriplesMap ownmap , String logicalsource) {
        super(processor);
        //the arguments must have maxArgs null values when passed in this function
        this.subject = subject;
        this.predicate = predicate;
        this.parentTriplesMap=parentTriplesMap;
        this.parentprocessor=parentprocessor;
        this.termMaps = termMaps;
        this.positions=positions;
        this.arguments=arguments;
        this.functionURIS=functions;
        
        this.dependencyPerformer=nextperformer;
        this.dependencyProcessor=dependencyProcessor;
        
        this.ownmap=ownmap;
        this.logicalsource=logicalsource;
    }

    /**
     * Compare expressions from join to complete it
     * 
     * @param node current object in parent iteration
     * @param dataset
     * @param map 
     */
    @Override
    public void perform(Object node, SesameDataSet dataset, TriplesMap map) {
        //Value object = processor.processSubjectMap(dataset, map.getSubjectMap(), node);
    	
    	List<String> objects;
    	for(int i=0;i<termMaps.size();++i){
    		TermMap tm=termMaps.get(i);
    		
    		objects=processor.processTermMap(tm, node, null, null, null, null,true);
    		if (objects == null){
                return; //handle this in sequence!!!!
            }
    		int argumentlist=positions.get(i).getArgumentList();
    		int actualpos=positions.get(i).getActualPosition();
    		arguments.get(argumentlist).remove(actualpos);
        	arguments.get(argumentlist).add(actualpos,objects.get(0));
    	}
    	 
    	if(dependencyProcessor!=null){
    		dependencyProcessor.execute(dataset, dependencyPerformer.ownmap, dependencyPerformer, dependencyPerformer.logicalsource);
    	}else{
			boolean flag=true;
    		for(int funi=0;funi<functionURIS.size();++funi){
    			URI functionURI=functionURIS.get(funi);
	    		Function function = FunctionFactory.get(functionURI);
	    		List<? extends String> results = null;
				try {
					System.out.println("argsssss " + functionURI + " function " + funi);
					for(int i=0;i<arguments.get(funi).size();++i){
						System.out.println(arguments.get(funi).get(i));
					}
					results = function.execute(arguments.get(funi));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		if(results.size()==1){
	    			if(results.get(0).toString().equalsIgnoreCase("true")){
	    				System.out.println("argsssss from true!!! RESULT="+flag);
	    			}
	    			else{
	    				flag=false;
	    				System.out.println("argsssss RESULT="+flag);
	    				break;
	    			}
	    		}
	    		else{
	    			flag=false;
	    			System.out.println("argsssss RESULT="+flag);
	    			break;
	    		}
    		}
    		if(flag==true){
    			Resource object = parentprocessor.processSubjectMap(dataset, parentTriplesMap.getSubjectMap(), node);
    			dataset.add(subject, predicate, object);
    		}
    	}
        log.debug("[DependencyRMLPerformer:findPerms] ");

    }


}
