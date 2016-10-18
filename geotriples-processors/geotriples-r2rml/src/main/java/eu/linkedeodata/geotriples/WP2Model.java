package eu.linkedeodata.geotriples;

import org.d2rq.SystemLoader;
import org.d2rq.jena.GraphD2RQ;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.RDFWriterF;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;

import eu.linkedeodata.geotriples.writers.WP2RDFWriterFImpl;


/**
 * <p>A D2RQ read-only Jena model backed by a D2RQ-mapped non-RDF database.</p>
 *
 * <p>This class is a thin wrapper around a {@link GraphD2RQ} and provides only
 * convenience constructors. {@link SystemLoader} provides a more flexible
 * and powerful facade for generating models.</p>
 * 
 * @author Dimitrianos Savva dimis@di.uoa.gr
 *
 * @see GraphD2RQ
 * @see SystemLoader
 */
public class WP2Model extends ModelCom implements Model{
	private static final RDFWriterF writerFactoryWP2 = new WP2RDFWriterFImpl();
	/** 
	 * Create a non-RDF database-based model. The model is created
	 * from a D2RQ or R2RML mapping that will be loaded from the given URL.
	 * Its serialization format will be guessed from the
	 * file extension.
	 *
	 * @param mapURL URL of the D2RQ map to be used for this model
	 */

	public WP2Model(Graph g)
	{
		super( g );
	}
	/*@Override
	public StmtIterator listStatements()  
    { 
		
		System.out.println("cstay here");
		return IteratorFactory.asStmtIterator( graph.find(Node.ANY, Node.ANY, Node.createLiteral("null")), this); 
    }*/
	@Override
    public RDFWriter getWriter()  {
        return writerFactoryWP2.getWriter();
    }
    
    /**
        Get the model's writer after priming it with the model's namespace
        prefixes.
    */
    @Override
    public RDFWriter getWriter(String lang)  {
        return writerFactoryWP2.getWriter(lang);
    }
}