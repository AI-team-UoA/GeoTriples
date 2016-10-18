package org.d2rq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All Log4j-specific stuff is encapsulated here.
 * 
 * Default configuration is in /etc/log4j.properties. We always
 * have to put that on the classpath so Log4j will find it.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class Log4jHelper {

//	public static void turnLoggingOff() {
//		System.err.println("Logging is turned off!");
//		LoggerFactory.getLogger("org.d2rq").setLevel(Level.OFF);
//	}
//	
//	public static void setVerboseLogging() {
//		// Adjust Log4j log level to show more stuff
//		LoggerFactory.getLogger("d2rq").setLevel(Level.INFO);
//		LoggerFactory.getLogger("org.d2rq").setLevel(Level.INFO);
//		LoggerFactory.getLogger("org.eclipse.jetty").setLevel(Level.INFO);
//		LoggerFactory.getLogger("org.joseki").setLevel(Level.INFO);
//	}
//	
//	public static void setDebugLogging() {
//		// Adjust Log4j log level to show MUCH more stuff 
//		LoggerFactory.getLogger("d2rq").setLevel(Level.ALL);
//		LoggerFactory.getLogger("org.d2rq").setLevel(Level.ALL);
//		LoggerFactory.getLogger("org.eclipse.jetty").setLevel(Level.INFO);
//		LoggerFactory.getLogger("org.joseki").setLevel(Level.INFO);
//	}
}
