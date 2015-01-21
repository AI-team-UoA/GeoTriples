package eu.linkedeodata.geotriples.gui;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.pivot.wtk.TextArea;

public class CustomOutputStream extends OutputStream {
	/**
	 * This class extends from OutputStream to redirect output to a JTextArrea
	 * @author www.codejava.net
	 *
	 */
	    private TextArea textArea;
	     
	    public CustomOutputStream(TextArea textArea) {
	        this.textArea = textArea;
	        this.textArea.setText("");
	    }
	     
	    @Override
	    public void write(int b) throws IOException {
	        // redirects data to the text area
	        textArea.setText(textArea.getText().concat(String.valueOf((char)b)));
	        // scrolls the text area to the end of data
	        //textArea.setCaretPosition(textArea.getDocument().getLength());
	    }
}
