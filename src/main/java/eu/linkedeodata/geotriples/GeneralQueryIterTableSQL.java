package eu.linkedeodata.geotriples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.d2rq.algebra.NodeRelation;
import org.d2rq.db.ResultRow;
import org.d2rq.db.SQLIterator;
import org.d2rq.db.op.DatabaseOp;
import org.d2rq.db.op.util.OpUtil;
import org.d2rq.db.vendor.Vendor;
import org.d2rq.nodes.BindingMaker;

import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton;

import eu.linkedeodata.geotriples.shapefile.ShapefileConnection;

/**
 * A {@link QueryIterator} over the bindings produced by a
 * {@link NodeRelation}. Works by running the underlying SQL
 * query using a {@link SQLIterator}.
 *
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class GeneralQueryIterTableSQL extends QueryIter {
	private final static Log log = LogFactory.getLog(GeneralQueryIterTableSQL.class);
	
	/**
	 * Creates an instance, or a simpler QueryIterator
	 * if optimization is possible (e.g., the relation is empty).
	 * @return A query iterator over the contents of the relation
	 */
	public static QueryIterator create(ShapefileConnection connection, DatabaseOp table, 
			Collection<BindingMaker> bindingMakers, ExecutionContext execCxt) {
		if (OpUtil.isEmpty(table) || bindingMakers.isEmpty()) {
			return new QueryIterNullIterator(execCxt);
		}
		if (OpUtil.isTrivial(table)) {
			ArrayList<Binding> bindingList = new ArrayList<Binding>();
			for (BindingMaker bindingMaker: bindingMakers) {
				Binding t = bindingMaker.makeBinding(ResultRow.NO_ATTRIBUTES);
				if (t == null) continue;
				bindingList.add(t);
			}
			return new QueryIterPlainWrapper(bindingList.iterator(), execCxt);				
		}
		return new GeneralQueryIterTableSQL(connection, table, bindingMakers, execCxt);
	}
	
	/**
	 * Creates an instance, or a simpler QueryIterator
	 * if optimization is possible (e.g., the relation is empty).
	 * @return A query iterator over the contents of the node relation
	 */
	public static QueryIterator create(GeneralNodeRelation table, ExecutionContext execCxt) {
		if (OpUtil.isEmpty(table.getBaseTabular())) {
			return new QueryIterNullIterator(execCxt);
		}
		if (OpUtil.isTrivial(table.getBaseTabular())) {
			return QueryIterSingleton.create(
					table.getBindingMaker().makeBinding(ResultRow.NO_ATTRIBUTES), 
					execCxt);
		}
		return new GeneralQueryIterTableSQL(table.getConnection(), table.getBaseTabular(), 
				Collections.singleton(table.getBindingMaker()), execCxt);
	}
	
	private final GeneralIterator wrapped;
	private final Collection<BindingMaker> bindingMakers;
	private final LinkedList<Binding> queue = new LinkedList<Binding>();

	private GeneralQueryIterTableSQL(GeneralConnection connection, DatabaseOp table, 
			Collection<BindingMaker> bindingMakers, ExecutionContext execCxt) {
		super(execCxt);
		this.bindingMakers = bindingMakers;
		GeneralSelectStatementBuilder builder = new GeneralSelectStatementBuilder(table, Vendor.MySQL);
		//wrapped = new ShapefileIterator(
		//		builder.getSQL(), builder.getColumnSpecs(), shpConnection);
		wrapped = new GeneralIterator(builder.getSQL(),
				builder.getTable().replaceAll("\"", ""), builder.getColumnSpecs(), connection);
	}
	
	@Override
	protected boolean hasNextBinding() {
		while (queue.isEmpty() && wrapped.hasNext()) {
			enqueueBindings(wrapped.next());
		}
		return !queue.isEmpty();
	}

	@Override
	protected Binding moveToNextBinding() {
		return queue.removeFirst();
	}

	@Override
	protected void closeIterator() {
		log.debug("closeIterator() called ...");
		wrapped.close();
	}

	@Override
	protected void requestCancel() {
		log.info("requestCancel() called ...");
		wrapped.cancel();
	}

	/**
	 * Create bindings from one database result row and put
	 * them onto the queue
	 */
	private void enqueueBindings(GeneralResultRow row) {
		for (BindingMaker bindingMaker: bindingMakers) {
			Binding binding = bindingMaker.makeBinding(row);
			if (binding == null) continue; 
			queue.add(binding);
		}
	}
}