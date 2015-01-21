package eu.linkedeodata.geotriples;

import java.util.Collection;
import java.util.List;

import org.d2rq.algebra.DownloadRelation;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.util.Context;

/**
 * TODO: Rename to Mapping?
 * TODO: Write documentation
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public interface GeneralCompiledMapping {

	// TODO: Probably unnecessary as we already had to connect in order to compile
	void connect();
	
	void close();

	PrefixMapping getPrefixes();

	Collection<GeneralTripleRelation> getTripleRelations();
	
	Collection<? extends DownloadRelation> getDownloadRelations();

	Collection<GeneralConnection> getConnections();
	
	List<String> getResourceCollectionNames();
	
	List<String> getResourceCollectionNames(Node resource);
	
	GeneralResourceCollection getResourceCollection(String name);
	
	Graph getAdditionalTriples();
	
	Context getContext();
}
