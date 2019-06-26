package eu.linkedeodata.geotriples.utils;

import java.io.IOException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.rio.ntriples.NTriplesUtil;



public class NTriplesUtilNoEscape {
    public static void append(Value value, Appendable appendable)
            throws IOException
    {

        // WARNING if you want to produce blank nodes replace all the .append("<").append(X).append(">");
        // with   NTriplesUtil.append(st.getSubject(), sb);
        if (value instanceof Resource) {
            appendable.append("<").append(value.stringValue()).append(">");
        }
        else if (value instanceof Literal) {
            append((Literal)value, appendable);
        }
        else {
            throw new IllegalArgumentException("Unknown value type: " + value.getClass());
        }
    }


    public static void append(Literal lit, Appendable appendable)
            throws IOException
    {
        // Do some character escaping on the label:
        appendable.append("\"");
        appendable.append(lit.getLabel());
        appendable.append("\"");

        if (lit.getDatatype() != null) {
            // Append the literal's datatype
            appendable.append("^^");
            appendable.append("<").append(lit.getDatatype().stringValue()).append(">");
        }
        else if (lit.getLanguage() != null) {
            // Append the literal's language
            appendable.append("@");
            appendable.append(lit.getLanguage());
        }
    }
}