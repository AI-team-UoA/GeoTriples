package be.ugent.mmlab.rml.processor.concrete;

import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.core.RMLPerformer;
import be.ugent.mmlab.rml.model.LogicalSource;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.AbstractRMLProcessor;

import com.csvreader.CsvReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Resource;

/**
 *
 * @author mielvandersande, andimou
 * modified by dimis
 */
public class CSVProcessor extends AbstractRMLProcessor {

    private static Log log = LogFactory.getLog(RMLMappingFactory.class);
    
    private char getDelimiter(LogicalSource ls) {
        String d = RMLEngine.getFileMap().getProperty(ls.getIdentifier() + ".delimiter");
        if (d == null) {
            return ',';
        }
        return d.charAt(0);
    }

    @Override
    public void execute(SesameDataSet dataset, TriplesMap map, RMLPerformer performer, String fileName) {
        //InputStream fis = null;
        try {
            char delimiter = getDelimiter(map.getLogicalSource());

            //TODO: add character guessing
            //CsvReader reader = new CsvReader(fis, Charset.defaultCharset());
            log.info("[CSV Processor] filename " + fileName);
            CsvReader reader = new CsvReader(new FileInputStream(fileName), Charset.defaultCharset());
            reader.setDelimiter(delimiter);
            
            reader.readHeaders();
            //Iterate the rows
            while (reader.readRecord()) {
                HashMap<String, String> row = new HashMap<>();

                for (String header : reader.getHeaders()) {
                    row.put(header, reader.get(header));
                }
                //let the performer handle the rows
                performer.perform(row, dataset, map);
            }

        } catch (FileNotFoundException ex) {
            log.error(ex);
        } catch (IOException ex) {
            log.error(ex);
        }
    }

    @Override
    public List<String> extractValueFromNode(Object node, String expression) {
        HashMap<String, String> row = (HashMap<String, String>) node;
        //call the right header in the row
        List<String> list = new ArrayList();
        if (row.containsKey(expression)){
            list.add(row.get(expression));
        }
        
        return list;
    }

    @Override
    public void execute_node(SesameDataSet dataset, String expression, TriplesMap parentTriplesMap, RMLPerformer performer, Object node, Resource subject) {
        throw new UnsupportedOperationException("Not applicable for CSV sources."); //To change body of generated methods, choose Tools | Templates.
    }

}
