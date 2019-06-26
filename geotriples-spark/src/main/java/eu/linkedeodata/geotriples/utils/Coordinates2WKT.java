package eu.linkedeodata.geotriples.utils;


import scala.collection.mutable.WrappedArray;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Functions that convert GeoJSON Geometries into WKT.
 */
public class Coordinates2WKT implements Serializable {

    static final Function<WrappedArray, StringBuilder> coords2String = ar_coords -> {
        StringBuilder  sb_wkt_coords = new StringBuilder();
        boolean was_array = false;
        scala.collection.Iterator coords_iter =  ar_coords.toIterable().toIterator();
        while(coords_iter.hasNext()) {
            Object o = coords_iter.next();
            if( o instanceof Double)
                sb_wkt_coords.append(o).append(" ");
            else{
                was_array = true;
                if(coords_iter.hasNext())
                    sb_wkt_coords.append(Coordinates2WKT.coords2String.apply((WrappedArray) o)).append(",");
                else
                    sb_wkt_coords.append(Coordinates2WKT.coords2String.apply((WrappedArray) o));
            }
        }
        if(was_array) sb_wkt_coords.insert(0,  "(").append(")");
        return sb_wkt_coords;
    };


    static final BiFunction<String, WrappedArray, String> convert = (type, coords) -> {
        StringBuilder sb_wkt_coords = coords2String.apply(coords);
        if(type.equals("Point")){
            sb_wkt_coords.insert(0, type.toUpperCase() + " (" ).append(")");
            return sb_wkt_coords.toString();
        }
        else{
            sb_wkt_coords.insert(0, type.toUpperCase() );
            return sb_wkt_coords.toString();
        }
    };

}
