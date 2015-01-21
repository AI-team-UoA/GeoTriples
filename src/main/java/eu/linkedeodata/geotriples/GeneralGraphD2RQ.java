package eu.linkedeodata.geotriples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.d2rq.find.TripleQueryIter;
import org.d2rq.jena.D2RQQueryHandler;
import org.d2rq.pp.PrettyPrinter;
import org.d2rq.tmp.QueryEngineD2RQ;

import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * A virtual read-only Jena graph backed by a relational database,
 * mapped via a D2RQ mapping or R2RML mapping.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class GeneralGraphD2RQ extends GraphBase implements Graph {
	private static final Log log = LogFactory.getLog(GeneralGraphD2RQ.class);
	
	static {
		QueryEngineD2RQ.register();
	}

	private final GeneralCompiledMapping mapping;
	private boolean connected = false;
	
	public GeneralGraphD2RQ(GeneralCompiledMapping mapping) {
		this.mapping = mapping;
		getPrefixMapping().setNsPrefixes(mapping.getPrefixes());
	}

	// TODO: Remove getMapping(); make this a cheap stateless wrapper around a GenericMapping
	public GeneralCompiledMapping getMapping() {
		return mapping;
	}
	
	@Override
	public ExtendedIterator<Triple> graphBaseFind(TripleMatch m) {
		checkOpen();
		Triple t = m.asTriple();
		if (log.isDebugEnabled()) {
			log.debug("Find: " + PrettyPrinter.toString(t, getPrefixMapping()));
		}
		ExecutionContext context = new ExecutionContext(mapping.getContext(), this, null, null);
		GeneralFindQuery query = new GeneralFindQuery(t, mapping.getTripleRelations(), context);
		ExtendedIterator<Triple> result = TripleQueryIter.create(query.iterator());
		result = result.andThen(mapping.getAdditionalTriples().find(t));
		return result;
    }

	@Override
	protected synchronized void checkOpen() {
		if (connected) return;
		connected = true;
		super.checkOpen();
		mapping.connect();
	}

	@Override
	public void close() {
		mapping.close();
		super.close();
	}

	@Override
	public QueryHandler queryHandler() {
		checkOpen();
		return new D2RQQueryHandler(this);
	}

	@Override
	public Capabilities getCapabilities() { 
		return capabilities;
	}
	private static final Capabilities capabilities = new Capabilities() {
		public boolean sizeAccurate() { return true; }
		public boolean addAllowed() { return addAllowed(false); }
		public boolean addAllowed(boolean every) { return false; }
		public boolean deleteAllowed() { return deleteAllowed(false); }
		public boolean deleteAllowed(boolean every) { return false; }
		public boolean canBeEmpty() { return true; }
		public boolean iteratorRemoveAllowed() { return false; }
		public boolean findContractSafe() { return false; }
		public boolean handlesLiteralTyping() { return true; }
	};
}