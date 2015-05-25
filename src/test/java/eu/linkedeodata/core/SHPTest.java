package eu.linkedeodata.core;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.linkedeodata.geotriples.dump_rdf;

public abstract class SHPTest extends TemplateTest {
	
	protected String shpfilePath;
	
	public SHPTest(String shpfilePath, String mappingFile, String baseIRI, String originalRDFGraphFile) {
		this.shpfilePath = shpfilePath;
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
		String [] args = { "-b", this.baseIRI, "-o", this.outputFile, "-sh", this.shpfilePath, this.mappingFile};
		new dump_rdf().process(args);
		Utils.sortFile(this.originalRdfGraphFile);
		Utils.sortFile(this.outputFile);
		boolean expr = Utils.checkDiff(this.originalRdfGraphFile+"_sorted.nt", this.outputFile+"_sorted.nt");
		assertTrue("SHP Test:" + this.getClass() + ": RDF graph file and results do not match", !expr);
		
	}
	
	@After @Override
	public void after() throws SQLException {
		super.after();
	}

}
