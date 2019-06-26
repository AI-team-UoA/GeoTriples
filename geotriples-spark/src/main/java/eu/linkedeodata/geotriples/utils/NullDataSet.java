package eu.linkedeodata.geotriples.utils;

import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;


public class NullDataSet extends SesameDataSet {

    public NullDataSet() {
    }

    @Override
    public void add(Resource s, URI p, Value o, Resource... contexts) {
        return;
    }

    @Override
    public void addStatement(Statement s) {
        return;
    }

}