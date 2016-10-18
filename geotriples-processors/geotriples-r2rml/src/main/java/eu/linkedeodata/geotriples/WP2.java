package eu.linkedeodata.geotriples;

import com.hp.hpl.jena.JenaRuntime;

/**
 * <p>
 * Provides various meta-data constants about the Jena package.
 * </p>
 */
public interface WP2
{
	/** The root package name for Jena */    
    public static final String PATH = "eu.linkedeodata.geotriples";
    static final String UNSET = "unset" ;
    
    /** The product name */    
    public static final String NAME = JenaRuntime.getMetadata( PATH + ".name", UNSET ) ;
    
    /** The Jena web site */    
    public static final String WEBSITE = JenaRuntime.getMetadata( PATH + ".website", UNSET ) ;
    
    /** The full name of the current Jena version */    
    public static final String VERSION = JenaRuntime.getMetadata( PATH + ".version", UNSET ) ;
    
    /** The date and time at which this release was built */    
    public static final String BUILD_DATE = JenaRuntime.getMetadata( PATH + ".build.datetime", UNSET ) ;

    /** @deprecated See the VERSION constant */ 
    @Deprecated 
    public static final String MAJOR_VERSION = "unset" ;
    
    /** @deprecated See the VERSION constant */ 
    @Deprecated 
    public static final String MINOR_VERSION = "unset" ;

    /** @deprecated See the VERSION constant */ 
    @Deprecated 
    public static final String REVISION_VERSION = "unset" ;
    
    /** @deprecated See the VERSION constant */ 
    @Deprecated 
    public static final String VERSION_STATUS = "unset" ;
    
//    /** The major version number for this release of Jena (ie '2' for Jena 2.6.0) */
//    public static final String MAJOR_VERSION = metadata.get ( PATH + ".version.major", UNSET ) ;
//    
//    /** The minor version number for this release of Jena (ie '6' for Jena 2.6.0) */
//    public static final String MINOR_VERSION = metadata.get ( PATH + ".version.minor", UNSET ) ;
//    
//    /** The minor version number for this release of Jena (ie '0' for Jena 2.6.0) */
//    public static final String REVISION_VERSION = metadata.get ( PATH + ".version.revision", UNSET ) ;
//    
//    /** The version status for this release of Jena (eg '-beta1' or the empty string) */
//    public static final String VERSION_STATUS = metadata.get ( PATH + ".version.status", UNSET ) ;
    
}
