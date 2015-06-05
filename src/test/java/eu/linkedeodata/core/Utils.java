package eu.linkedeodata.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;

public class Utils {

	public static String serverName = "localhost";
	public static Connection conn = null;
	
	/*
	 * postgis specific
	 */
	public static String dbTemplate = "template_postgis";
	public static String postgresUsername = "postgres";
	public static String postgresPassword = "postgres";
	public static int postgresPort = 5432;
	
	/*
	 * monetdb specific
	 */
	private static String monetdbUsername = "monetdb";
	private static String monetdbPassword = "monetdb";
	
	public static int monetdbPort = 50000;	
	
	public static void createdb(String name, String sqlPath, boolean postgres) throws SQLException {
		String url = "";
		List<String> databases = new ArrayList<String>();
		if (postgres) {
			url = "jdbc:postgresql://"+serverName +":"+postgresPort;
			conn = DriverManager.getConnection(url, postgresUsername, postgresPassword);
			PreparedStatement pst = conn.prepareStatement("SELECT * FROM pg_catalog.pg_database");
	        ResultSet rs = pst.executeQuery();

	        while (rs.next())
	        {
	        	databases.add(rs.getString(1));
	        }
	        
	        assertFalse("Database " + name + " already exists. Use another name or drop the database first", databases.contains(name));
	        pst = conn.prepareStatement("CREATE DATABASE "+name+" TEMPLATE " + dbTemplate);
			pst.executeUpdate();
			pst.close();
			conn.close();

			url = "jdbc:postgresql://"+serverName+":"+postgresPort+"/"+name;
			conn = DriverManager.getConnection(url, postgresUsername, postgresPassword);
		}
		else {
			//create monetdb db;
		}
	}
	
	public static void dropdb(String name, boolean postgres) throws SQLException {
		conn.close();
		if (postgres) {
			String url = "jdbc:postgresql://"+serverName+":"+postgresPort;
			conn = DriverManager.getConnection(url, postgresUsername, postgresPassword);
			
			PreparedStatement pst = conn.prepareStatement("DROP DATABASE "+name);
			pst.executeUpdate();
			pst.close();
			conn.close();
		}
		else {
			//monetdb
		}
		
	}
	
	public static void loaddb(String name, boolean postgres) throws SQLException, ClassNotFoundException {
		String url = "";
		if (postgres) {
			url = "jdbc:postgresql://"+serverName+":"+postgresPort+"/"+name;
			conn = DriverManager.getConnection(url, postgresUsername, postgresPassword);
		}
		else {
			Class.forName("nl.cwi.monetdb.jdbc.MonetDriver");
			url = "jdbc:monetdb://"+serverName+":"+monetdbPort+"/"+name;
			conn = DriverManager.getConnection(url, monetdbUsername, monetdbPassword);
		}
	}

	public static void unloaddb() throws SQLException {
		conn.close();		
	}
	
	public static void sortFile(String filename) throws IOException, InterruptedException {
		Process p = Runtime.getRuntime().exec("sort " + filename + " -o " + filename + "_sorted.nt");
		p.waitFor();
	}
	
	public static boolean checkDiff(String file1, String file2) throws IOException, InterruptedException {
		Process p = Runtime.getRuntime().exec("diff " + file1 + " " + file2);
		//p.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		
		StringBuilder sb = new StringBuilder("");
		String line = "";
		while ((line = reader.readLine())!= null) {
			sb.append(line + "\n");
		}
		if (sb.toString().equals("")) {
			return false;
		}
		return true;
		
	}
	
}
