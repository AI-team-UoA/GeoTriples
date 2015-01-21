package eu.linkedeodata.geotriples;

import java.util.Map;

import org.d2rq.db.op.DatabaseOp;
import org.d2rq.db.op.EmptyOp;
import org.d2rq.nodes.BindingMaker;
import org.d2rq.nodes.NodeMaker;

import com.hp.hpl.jena.sparql.core.Var;


/**
 * A {@link DatabaseOp} associated with a number of named {@link NodeMaker}s.
 * 
 * TODO: Rename to NodeTabular?
 * FIXME: Looks like the condition on a provided BindingMaker is sometimes ignored
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class GeneralNodeRelation {
	
	public static GeneralNodeRelation createEmpty(GeneralNodeRelation r) {
		return new GeneralNodeRelation(r.getConnection(), 
				EmptyOp.create(r.getBaseTabular()), 
				r.getBindingMaker());
	}
	
	private final GeneralConnection connection;
	private final DatabaseOp base;
	private final BindingMaker bindingMaker;
	
	public GeneralNodeRelation(GeneralConnection connection2, DatabaseOp base, Map<Var,NodeMaker> nodeMakers) {
		this(connection2, base, new BindingMaker(nodeMakers));
	}
	
	public GeneralNodeRelation(GeneralConnection connection, DatabaseOp base, BindingMaker bindingMaker) {
		this.connection = connection;
		this.base = base;
		this.bindingMaker = bindingMaker;
	}
	
	public GeneralConnection getConnection() {
		return connection;
		
	}
	public DatabaseOp getBaseTabular() {
		return base;
	}
	
	public BindingMaker getBindingMaker() {
		return bindingMaker;
	}
	
	public NodeMaker nodeMaker(Var variable) {
		return bindingMaker.get(variable);
	}

	public String toString() {
		StringBuffer result = new StringBuffer("NodeRelation(");
		result.append(base);
		result.append(", ");
		result.append(bindingMaker);
		result.append(")");
		return result.toString();
	}
}
