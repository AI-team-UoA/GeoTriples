package be.ugent.mmlab.rml.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import be.ugent.mmlab.rml.function.Function;
import be.ugent.mmlab.rml.function.FunctionFactory;
import be.ugent.mmlab.rml.function.FunctionNotDefined;
import be.ugent.mmlab.rml.model.TermMap;
import be.ugent.mmlab.rml.model.TermMap.TermMapType;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.RMLProcessor;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

/**
 * Performer to do joins without any join conditions
 * 
 * @author mielvandersande, andimou
 */
public class DependencyRMLPerformer extends NodeRMLPerformer {

	private static Logger log = LoggerFactory.getLogger(RMLMappingFactory.class);
	private Resource subject;
	private URI predicate;
	private TriplesMap parentTriplesMap;
	private RMLProcessor parentprocessor; // may be the same with a processor in
											// the chain, but not necessary
	private RMLProcessor mainprocessor;
	private List<TermMap> termMaps;
	List<ArgumentPosition> positions;

	private List<URI> functionURIS;
	private RMLProcessor dependencyProcessor = null;
	private DependencyRMLPerformer dependencyPerformer = null;
	private List<List<Object>> arguments; // shared with all performers
	private List<List<QLTerm>> argumentsQLTerms; // shared with all performers
	private TriplesMap ownmap;

	public TriplesMap getOwnmap() {
		return ownmap;
	}

	private String logicalsource;
	private Object currentNode;
	private boolean hasStructuralChild = false;
	private String childsexpression = null;
	private boolean isReversedParentTriplesMap = false;

	public DependencyRMLPerformer(RMLProcessor processor, Resource subject,
			URI predicate, TriplesMap parentTriplesMap, List<TermMap> termMaps,
			List<ArgumentPosition> positions,
			DependencyRMLPerformer nextperformer,
			RMLProcessor dependencyProcessor, RMLProcessor parentprocessor,
			RMLProcessor mainprocessor, List<List<Object>> arguments2,
			List<List<QLTerm>> argumentsQLTerms, List<URI> functions,
			TriplesMap ownmap, String logicalsource,
			boolean isReversedParentTriplesMap) {
		super(processor);
		// the arguments must have maxArgs null values when passed in this
		// function
		this.subject = subject;
		this.predicate = predicate;
		this.parentTriplesMap = parentTriplesMap;
		this.parentprocessor = parentprocessor;

		this.isReversedParentTriplesMap = isReversedParentTriplesMap;

		this.mainprocessor = mainprocessor;

		this.termMaps = termMaps;
		this.positions = positions;
		this.arguments = arguments2;
		this.argumentsQLTerms = argumentsQLTerms;
		this.functionURIS = functions;

		this.dependencyPerformer = nextperformer;
		this.dependencyProcessor = dependencyProcessor;

		this.ownmap = ownmap;
		this.logicalsource = logicalsource;
	}

	/**
	 * Compare expressions from join to complete it
	 * 
	 * @param node
	 *            current object in parent iteration
	 * @param dataset
	 * @param map
	 */
	@Override
	public Collection<Statement> perform(Object node, SesameDataSet dataset, TriplesMap map) {
		List<Statement> statements=new LinkedList<>();
		// Value object = processor.processSubjectMap(dataset,
		// map.getSubjectMap(), node);
		currentNode = node;
		
		List<Object> objects;
		if (termMaps != null) {
			for (int i = 0; i < termMaps.size(); ++i) {
				TermMap tm = termMaps.get(i);
				// System.out.println(tm.getArgumentMap().get(0));
				if (!tm.getTermMapType().equals(
						TermMapType.TRANSFORMATION_VALUED))
					objects = processor.processTermMap(tm, node, null, null,
							null, null, true);
				else {
					if(dependencyProcessor!=null){
						try {
							throw new Exception("There exists a function at not last processor");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							System.exit(0);
						}
					}
					// it should happen only in last iterator, typically the
					// parenttriplesmap iterator
					// this is the case when there is a function argument and by
					// design must be calculated only through main processor,
					// the
					// home iterator of the join
					objects = mainprocessor.processTermMap(tm, null, null,
							null, null, false);
				}
				if (objects == null) {
					return statements; // handle this in sequence!!!!
					// throw exception
				}
				int argumentlist = positions.get(i).getArgumentList();
				int actualpos = positions.get(i).getActualPosition();
				arguments.get(argumentlist).remove(actualpos);
				arguments.get(argumentlist).add(actualpos,
						objects.size() == 0 ? null : objects.get(0));
				argumentsQLTerms.get(argumentlist).remove(actualpos);
				argumentsQLTerms.get(argumentlist).add(
						actualpos,
						tm.getTriplesMap().getLogicalSource()
								.getReferenceFormulation());
			}
		}

		if (dependencyProcessor != null) {
			// if (hasStructuralChild) {
			// dependencyProcessor.execute_node_fromdependency(dataset,
			// childsexpression, dependencyPerformer.ownmap,
			// dependencyPerformer, node);
			// } else {
			statements.addAll(dependencyProcessor.execute(dataset, dependencyPerformer.ownmap,
					dependencyPerformer, dependencyPerformer.logicalsource,true));
			// }
		} else {
			boolean flag = true;
			for (int funi = 0; funi < functionURIS.size(); ++funi) {
				URI functionURI = functionURIS.get(funi);
				Function function = null;
				try {
					function = FunctionFactory.get(functionURI);
				} catch (FunctionNotDefined e1) {
					e1.printStackTrace();
					System.exit(13);
				}
				List<? extends Object> results = null;
				try {
					// System.out.println("argsssss " + functionURI +
					// " function " + funi);
					// for(int i=0;i<arguments.get(funi).size();++i){
					// System.out.println(arguments.get(funi).get(i));
					// }
					results = function.execute(arguments.get(funi),
							argumentsQLTerms.get(funi));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					// System.exit(0);
					flag = false; // some node did not have the reference!! so
									// the row is incomplete, never mind go
									// ahead
					break;
				}
				if (results.size() == 1) {
					if (results.get(0).toString().equalsIgnoreCase("true")) {
						// System.out.println("argsssss from true!!! RESULT="+flag);
					} else {
						flag = false;
						// System.out.println("argsssss RESULT="+flag);
						break;
					}
				} else {
					flag = false;
					// System.out.println("argsssss RESULT="+flag);
					break;
				}
			}
			if (flag == true) {
				 ValueFactory myFactory = ValueFactoryImpl.getInstance();
				
				Resource object = parentprocessor.processSubjectMap(dataset,
						parentTriplesMap.getSubjectMap()); // subject of parent
															// processor with
													// its current node
				if(log.isDebugEnabled())
					log.debug("Subject map for parent processor= "+parentprocessor + " is "+object);
				if(object!=null){
				if (isReversedParentTriplesMap) {
					Statement st = myFactory.createStatement((Resource) object, predicate,
							(Value) subject);
	                //dataset.add(subject, predicate, object);
	                dataset.addStatement(st);
	                
	                statements.add(st);
					
					//dataset.add(object, predicate, subject);
				} else {
					Statement st = myFactory.createStatement((Resource) subject, predicate,
							(Value) object);
	                //dataset.add(subject, predicate, object);
	                dataset.addStatement(st);
	                
	                statements.add(st);
	                
					//dataset.add(subject, predicate, object);
				}}
			}
		}
		log.debug("[DependencyRMLPerformer:findPerms] ");
		return statements;
	}

	public List<Object> perform(String reference) {
		// called from term map evaluation when we are in level 2,3,4 function
		// etc inside a join condition
		return processor.extractValueFromNode(currentNode, reference);
	}
	
	@Override
	public Object getCurrentNode() {
		return currentNode;
	}

}
