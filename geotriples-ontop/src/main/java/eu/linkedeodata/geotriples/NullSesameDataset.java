package eu.linkedeodata.geotriples;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;

public class NullSesameDataset extends SesameDataSet{
	@Override
	public void add(Resource s, URI p, Value o, Resource... contexts) {
		return;
	}
	@Override
	public void addStatement(Statement s) {
		return;
	}
}
