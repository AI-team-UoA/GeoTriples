package eu.linkedeodata.geotriples.writers;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.PrintWriter;
import java.io.Writer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DAML_OIL;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.RDFSyntax;
//Writer;
//import java.io.PrintWriter;


import eu.linkedeodata.geotriples.utils.John_BaseXMLWriter;
import eu.linkedeodata.geotriples.utils.John_Unparser;

/**
 * Writes out RDF in the abbreviated syntax, for human consumption not only
 * machine readable. It is not normal to call the constructor directly, but to
 * use the method RDFWriterF.getWriter("RDF/XML-ABBREV"). Does not support the
 * <code>NSPREFIXPROPBASE</code> system properties. Use <code>setNsPrefix</code>
 * . For best results it is necessary to set the property
 * <code>"prettyTypes"</code>. See setProperty for information.
 * 
 * @see com.hp.hpl.jena.rdf.model.RDFWriterF#getWriter(String)
 */
@SuppressWarnings("deprecation")
public class WP2AbbreviatedWriter extends John_BaseXMLWriter implements
		RDFErrorHandler {

	private Resource types[] = new Resource[] {
			DAML_OIL.Ontology,
			OWL.Ontology,
			DAML_OIL.Datatype,
			// OWL.DataRange, named or orphaned dataranges unusual.
			RDFS.Datatype, DAML_OIL.Class, RDFS.Class, OWL.Class,
			DAML_OIL.Property, OWL.ObjectProperty, RDF.Property,
			DAML_OIL.ObjectProperty, OWL.DatatypeProperty,
			DAML_OIL.DatatypeProperty, OWL.TransitiveProperty,
			OWL.SymmetricProperty, OWL.FunctionalProperty,
			OWL.InverseFunctionalProperty, DAML_OIL.TransitiveProperty,
			DAML_OIL.UnambiguousProperty, DAML_OIL.UniqueProperty, };

	private boolean sReification;

	private boolean sIdAttr;
	private boolean sDamlCollection;
	private boolean sParseTypeCollectionPropertyElt;
	private boolean sListExpand;
	private boolean sParseTypeLiteralPropertyElt;
	private boolean sParseTypeResourcePropertyElt;
	private boolean sPropertyAttr;

	private boolean sResourcePropertyElt;
	
	public WP2AbbreviatedWriter() {
		super();
	}

	@Override
	protected void unblockAll() {
		setsDamlCollection(false);
		setsReification(false);
		setsResourcePropertyElt(false);
		setsParseTypeLiteralPropertyElt(false);
		setsParseTypeResourcePropertyElt(false);
		setsParseTypeCollectionPropertyElt(false);
		setsIdAttr(false);
		setsPropertyAttr(false);
		setsListExpand(false);
	}

	{
		unblockAll();
		blockRule(RDFSyntax.propertyAttr);
	}

	@Override
	protected void blockRule(Resource r) {
		if (r.equals(RDFSyntax.sectionReification))
			setsReification(true);
		// else if (r.equals(RDFSyntax.resourcePropertyElt))
		// sResourcePropertyElt=true;
		else if (r.equals(RDFSyntax.sectionListExpand))
			setsListExpand(true);
		else if (r.equals(RDFSyntax.parseTypeLiteralPropertyElt))
			setsParseTypeLiteralPropertyElt(true);
		else if (r.equals(RDFSyntax.parseTypeResourcePropertyElt))
			setsParseTypeResourcePropertyElt(true);
		else if (r.equals(RDFSyntax.parseTypeCollectionPropertyElt))
			setsParseTypeCollectionPropertyElt(true);
		else if (r.equals(RDFSyntax.idAttr)) {
			setsIdAttr(true);
			setsReification(true);
		} else if (r.equals(RDFSyntax.propertyAttr))
			setsPropertyAttr(true);
		else if (r.equals(DAML_OIL.collection))
			setsDamlCollection(true);
		else {
			logger.warn("Cannot block rule <" + r.getURI() + ">");
		}
	}

	@Override
	protected Resource[] setTypes(Resource[] propValue) {
		Resource[] rslt = types;
		types = propValue;
		return rslt;
	}

	@Override
	synchronized public void write(Model baseModel, Writer out, String base) {
		if (baseModel.getGraph().getCapabilities().findContractSafe() == false) {
			logger.warn("Workaround for bugs 803804 and 858163: using RDF/XML (not RDF/XML-ABBREV) writer  for unsafe graph "
					+ baseModel.getGraph().getClass());
			baseModel.write(out, "RDF/XML", base);
		} else
			super.write(baseModel, out, base);
	}

	@Override
	protected void writeBody(Model model, PrintWriter pw, String base,
			boolean useXMLBase) {
		John_Unparser unp = new John_Unparser(this, base, model, pw);

		unp.setTopLevelTypes(types);
		// unp.useNameSpaceDecl(nameSpacePrefices);
		if (useXMLBase)
			unp.setXMLBase(base);
		unp.write();
	}

	// Implemenatation of RDFErrorHandler
	@Override
	public void error(Exception e) {
		errorHandler.error(e);
	}

	@Override
	public void warning(Exception e) {
		errorHandler.warning(e);
	}

	@Override
	public void fatalError(Exception e) {
		errorHandler.fatalError(e);
	}

	public boolean issParseTypeLiteralPropertyElt() {
		return sParseTypeLiteralPropertyElt;
	}

	public void setsParseTypeLiteralPropertyElt(
			boolean sParseTypeLiteralPropertyElt) {
		this.sParseTypeLiteralPropertyElt = sParseTypeLiteralPropertyElt;
	}

	public boolean issParseTypeResourcePropertyElt() {
		return sParseTypeResourcePropertyElt;
	}

	public void setsParseTypeResourcePropertyElt(
			boolean sParseTypeResourcePropertyElt) {
		this.sParseTypeResourcePropertyElt = sParseTypeResourcePropertyElt;
	}

	public boolean issResourcePropertyElt() {
		return sResourcePropertyElt;
	}

	public void setsResourcePropertyElt(boolean sResourcePropertyElt) {
		this.sResourcePropertyElt = sResourcePropertyElt;
	}

	public boolean issListExpand() {
		return sListExpand;
	}

	public void setsListExpand(boolean sListExpand) {
		this.sListExpand = sListExpand;
	}

	public boolean issIdAttr() {
		return sIdAttr;
	}

	public void setsIdAttr(boolean sIdAttr) {
		this.sIdAttr = sIdAttr;
	}

	public boolean issReification() {
		return sReification;
	}

	public void setsReification(boolean sReification) {
		this.sReification = sReification;
	}

	public boolean issPropertyAttr() {
		return sPropertyAttr;
	}

	public void setsPropertyAttr(boolean sPropertyAttr) {
		this.sPropertyAttr = sPropertyAttr;
	}

	public boolean issDamlCollection() {
		return sDamlCollection;
	}

	public void setsDamlCollection(boolean sDamlCollection) {
		this.sDamlCollection = sDamlCollection;
	}

	public boolean issParseTypeCollectionPropertyElt() {
		return sParseTypeCollectionPropertyElt;
	}

	public void setsParseTypeCollectionPropertyElt(
			boolean sParseTypeCollectionPropertyElt) {
		this.sParseTypeCollectionPropertyElt = sParseTypeCollectionPropertyElt;
	}

}
