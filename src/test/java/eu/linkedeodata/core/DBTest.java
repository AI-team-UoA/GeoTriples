package eu.linkedeodata.core;

import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import d2rq.dump_rdf;
import static org.junit.Assert.assertTrue;

public abstract class DBTest extends TemplateTest {

	protected String dbName;
	protected boolean postgres;					//postgis or monetdb (true for postgis)
	protected String username;
	protected String password;
	
	public DBTest(String dbName, String username, String password, boolean postgres, String baseIRI, String mappingFile, String originalFile) {
		this.dbName = dbName;
		this.postgres = postgres;
		this.username = username;
		this.password = password;
		this.baseIRI = baseIRI;
		this.mappingFile = mappingFile;
		this.originalRdfGraphFile = originalFile;
	}
	
	@Before @Override
	public void before() throws SQLException, ClassNotFoundException {
		Utils.loaddb(dbName, postgres);
	}
	
	@Test @Override
	public void test() throws Exception {
		String [] args = { "-b", this.baseIRI, "-o", this.outputFile, "-u", this.username, "-p", this.password, "-jdbc", this.postgres ? "jdbc:postgresql://"+Utils.serverName + ":"+Utils.postgresPort+ "/" + this.dbName : "jdbc:monetdb://"+Utils.serverName + ":" + Utils.monetdbPort + "/" + this.dbName, this.mappingFile };
		new dump_rdf().process(args);
		Utils.sortFile(originalRdfGraphFile);
		Utils.sortFile(this.outputFile);
		boolean expr = Utils.checkDiff(originalRdfGraphFile+"_sorted.nt", this.outputFile+"_sorted.nt");
		assertTrue("DB Test:" + this.getClass() + ": RDF graph file and results do not match", !expr);
	}
	
	@After @Override
	public void after() throws SQLException {
		Utils.unloaddb();
		super.after();
	}
}
