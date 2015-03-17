package eu.linkedeodata.geotriples.writers;

import java.io.PrintWriter;
import java.io.Writer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.NTripleWriter;

public class WP2NTripleWriter extends NTripleWriter {
	@Override
    public void write(Model baseModel, Writer writer, String base)
         {
        try {
            Model model = ModelFactory.withHiddenStatements(baseModel);
            PrintWriter pw;
            if (writer instanceof PrintWriter) {
                pw = (PrintWriter) writer;
            } else {
                pw = new PrintWriter(writer);
            }

            StmtIterator iter = model.listStatements();
            Statement stmt = null;

            while (iter.hasNext()) {
                stmt = iter.nextStatement();
                String objectstr=stmt.getObject().toString();
                if(objectstr.startsWith("null"))
            	{
            		continue;
            	}
                writeResource(stmt.getSubject(), pw);
                pw.print(" ");
                writeResource(stmt.getPredicate(), pw);
                pw.print(" ");
                writeNode(stmt.getObject(), pw);
                pw.println(" .");
            }
            pw.flush();
        } catch (Exception e) {
        	System.out.println(e.getMessage());
           e.printStackTrace();
        }
    }
}
