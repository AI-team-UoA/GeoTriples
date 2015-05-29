package eu.linkedeodata.core;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.linkedeodata.geotriples.dump_rdf;

public abstract class RMLTest extends TemplateTest {
	
	protected String srid;
	
	public RMLTest(String srid, String mappingFile, String baseIRI, String originalRDFGraphFile) {
		this.srid = srid;
		this.mappingFile = mappingFile;
		this.baseIRI = baseIRI;
		this.originalRdfGraphFile = originalRDFGraphFile;
	}
	@Before @Override
	public void before() {
		/*
		 * 
		 */
	}
	
	@Test @Override
	public void test() throws Exception {
		/*
		 * test the functionality: invoke the right connector that you want to test
		 */
		String [] args = { "-rml", "-o", this.outputFile, "-s", this.srid, this.mappingFile};
		new dump_rdf().process(args);
		Utils.sortFile(this.originalRdfGraphFile);
		Utils.sortFile(this.outputFile);
		boolean expr = Utils.checkDiff(this.originalRdfGraphFile+"_sorted.nt", this.outputFile+"_sorted.nt");
		assertTrue("RML Test:" + this.getClass() + ": RDF graph file and results do not match", !expr);
		
	}
	
	@After @Override
	public void after() throws SQLException {
		super.after();
	}

}
