package be.ugent.mmlab.rml.mapgen;

import java.io.File;

public abstract class RMLMappingGenerator {
	protected String baseURI = "http://linkedeodata.eu/";
	protected File outputfile;
	public RMLMappingGenerator(String baseiri,String outputfile) {
		if (baseiri != null) {
			this.baseURI = baseiri;
			if (!this.baseURI.endsWith("/")) {
				this.baseURI += "/";
			}
		}
		this.outputfile = outputfile==null?null:new File(outputfile);
	}
}
