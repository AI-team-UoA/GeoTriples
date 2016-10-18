package eu.linkedeodata.geotriples.gui;

import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.validation.Validator;
import org.d2rq.mapgen.IRIEncoder;

public class BaseIRIValidator implements Validator,Label.TextBindMapping {

	@Override
	public boolean isValid(String str) {
		if(str.startsWith("http://") && str.length()>7)
		{
			return true;
		}
		return false;
	}

	@Override
	public String toString(Object str) {
		if(str instanceof String)
		{
			return IRIEncoder.encode((String)str);
		}
		return "";
	}

	@Override
	public Object valueOf(String arg0) {
		throw new UnsupportedOperationException();
	}

}
