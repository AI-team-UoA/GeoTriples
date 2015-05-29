package eu.linkedeodata.core;

import java.io.File;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class TemplateTest {

	protected String mappingFile;
	protected String originalRdfGraphFile;
	protected String outputFile = "/tmp/outfile.nt";
	String baseIRI;
	
	@Before
	public void before() throws SQLException, ClassNotFoundException {
		
	}
	
	@Test
	public void test() throws Exception {
		
	}
	
	@After
	public void after() throws SQLException {
		//destroy outputgraph file and sorted files
		File file = new File(originalRdfGraphFile + "_sorted.nt");
		file.delete();
		file = new File(outputFile);
		file.delete();
		file = new File(outputFile + "_sorted.nt");
		file.delete();
	}
}
