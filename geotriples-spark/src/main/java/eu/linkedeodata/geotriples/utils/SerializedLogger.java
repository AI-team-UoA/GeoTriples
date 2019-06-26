package eu.linkedeodata.geotriples.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.Serializable;

public class SerializedLogger implements Serializable {
    public  Logger log;

    public SerializedLogger(String name, Level level){
        log = Logger.getLogger(name);
        log.setLevel(level);
    }

    public void info(String msg){
        log.info(msg);
    }

    public void warn(String msg){
        log.warn(msg);
    }

    public void error(String msg){
        log.error(msg);
    }
}
