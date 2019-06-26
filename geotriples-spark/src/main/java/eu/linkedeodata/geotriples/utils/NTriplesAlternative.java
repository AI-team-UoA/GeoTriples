package eu.linkedeodata.geotriples.utils;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.ntriples.NTriplesUtil;


public class NTriplesAlternative implements RDFWriter {

    private boolean writingStarted;
    private StringBuilder sb;



    /**
     * Creates a new NTriplesWriter that will write to a StringBuilder.
     *
     */
    public NTriplesAlternative() {
        this.sb = new StringBuilder();
        writingStarted = false;
    }

    @Override
    public RDFFormat getRDFFormat() {
        return RDFFormat.NTRIPLES;
    }

    @Override
    public void startRDF()  throws RuntimeException {
        if (writingStarted)
            throw new RuntimeException("Document writing has already started");
        writingStarted = true;
    }

    @Override
    public void endRDF()  throws RuntimeException {
        if (!writingStarted)
            throw new RuntimeException("Document writing has not yet started");
        this.sb.setLength(0);
        writingStarted = false;
    }

    @Override
    public void handleNamespace(String prefix, String name) {
        // N-Triples does not support namespace prefixes.
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        if (!writingStarted)
            throw new RuntimeException("Document writing has not yet been started");
        try {
            NTriplesUtil.append(st.getSubject(), sb);
            sb.append(" ");
            NTriplesUtil.append(st.getPredicate(), sb);
            sb.append(" ");
            NTriplesUtilNoEscape.append(st.getObject(), sb);
            sb.append(" .\n");
        } catch (IOException e) {
            throw new RDFHandlerException(e);
        }
    }

    @Override
    public void handleComment(String comment){
        sb.append("# ");
        sb.append(comment);
        sb.append("\n");
    }

    public String getString(){
        //if (sb.charAt(sb.length()-1) == '\n')
        //    sb.deleteCharAt(sb.length() - 1);
        String triples = sb.toString();
        sb.setLength(0);
        return triples;
    }


    public void handleStatementIter(Collection<Statement> statements) throws RDFHandlerException {
        if (!writingStarted)
            throw new RuntimeException("Document writing has not yet been started");
        try {
            // WARNING if you want to produce blank nodes replace all the .append("<").append(X).append(">");
            // with   NTriplesUtil.append(st.getSubject(), sb);
            for(Statement st: statements){
                sb
                        .append("<")
                        .append(st.getSubject().toString())
                        .append("> <")
                        .append(st.getPredicate().toString())
                        .append("> ");
                NTriplesUtilNoEscape.append(st.getObject(), sb);
                sb.append(" .\n");
            }
        } catch (Exception e) {
            throw new RDFHandlerException(e);
        }
    }

}