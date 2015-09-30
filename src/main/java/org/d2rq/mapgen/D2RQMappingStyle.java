package org.d2rq.mapgen;

import java.util.Iterator;
import java.util.List;

import org.d2rq.db.SQLConnection;
import org.d2rq.db.schema.ColumnDef;
import org.d2rq.db.schema.ColumnName;
import org.d2rq.db.schema.ForeignKey;
import org.d2rq.db.schema.Identifier;
import org.d2rq.db.schema.Key;
import org.d2rq.db.schema.TableDef;
import org.d2rq.db.schema.TableName;
import org.d2rq.values.TemplateValueMaker;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * Generates an original-style mapping. Unlike the W3C Direct Mapping, this
 * handles N:M link tables, includes label definitions and instance labels,
 * and uses different URI patterns.
 */
public class D2RQMappingStyle implements MappingStyle {
	private final MappingGenerator generator;
	private final Model model = ModelFactory.createDefaultModel();
	private final UniqueLocalNameGenerator stringMaker = new UniqueLocalNameGenerator();
	private String baseIRI;
	private String vocabBaseIRI;
	private final String geoBaseIRI;
	private final String entityBaseIRI;

	public D2RQMappingStyle(SQLConnection connection, String baseIRI, String fileName) {
		this.baseIRI = baseIRI;
		if (this.baseIRI.endsWith("/")) {
			this.baseIRI = this.baseIRI.substring(0, this.baseIRI.length()-1);
		}
		this.vocabBaseIRI = this.baseIRI + "ontology#";
		this.geoBaseIRI = this.baseIRI;
		this.entityBaseIRI = this.baseIRI;
		model.setNsPrefix("rdf", RDF.getURI());
		model.setNsPrefix("rdfs", RDFS.getURI());
		model.setNsPrefix("xsd", XSD.getURI());
		model.setNsPrefix("ogc", "http://www.opengis.net/ont/geosparql#");
		model.setNsPrefix("geof", "http://www.opengis.net/def/function/geosparql/");
		model.setNsPrefix("vocab", vocabBaseIRI);
		generator = new MappingGenerator(this, connection);
		generator.setGenerateLabelBridges(false);
		generator.setHandleLinkTables(true);
		generator.setGenerateDefinitionLabels(false);
		generator.setServeVocabulary(true);
		generator.setSkipForeignKeyTargetColumns(true);
		generator.setUseUniqueKeysAsEntityID(true);
	}
	
	public MappingGenerator getMappingGenerator() {
		return generator;
	}
	
	public String getBaseIRI() {
		return baseIRI;
	}
	
	public PrefixMapping getPrefixes() {
		return model;
	}
	
	public TemplateValueMaker getEntityIRITemplate(TableDef table, Key columns) {
		TemplateValueMaker.Builder builder = TemplateValueMaker.builder();
		// We don't use the base IRI here, so the template will produce relative IRIs
//		if (table.getName().getSchema() != null) {
//			builder.add(IRIEncoder.encode(table.getName().getSchema().getName()));
//			
//			//builder.add(IRIEncoder.encode(entityBaseIRI));
//			builder.add("/");
//		}
		//builder.add(IRIEncoder.encode(table.getName().getTable().getName()));
		builder.add(entityBaseIRI);
		builder.add("/");
		builder.add(table.getName().getTable().getCanonicalName());
		//builder.add("/");
		//builder.add(table.getName().toString().replaceAll("[\"]", ""));
		builder.add("/id");
		if (columns == null) {
			if (table.getColumnNames().contains(Identifier.createDelimited("gid"))) {
				columns = Key.create(ColumnName.create(table.getName(), Identifier.createDelimited("gid")));
			}
			else if (table.getColumnNames().contains(Identifier.createDelimited("id"))) {
				columns = Key.create(ColumnName.create(table.getName(), Identifier.createDelimited("id")));
			}
			//columns.getColumns().add(e)
		}
		if (columns != null) {
			for (Identifier column: columns) {
				builder.add("/");
				builder.add(table.getName().qualifyIdentifier(column));
				/*if (table.getColumnDef(column).getDataType().isIRISafe()) {
					builder.add(table.getName().qualifyIdentifier(column));
				} else {
					builder.add(table.getName().qualifyIdentifier(column), TemplateValueMaker.URLIFY);
				}*/
			}
		}
		return builder.build();
	}
	
	public TemplateValueMaker getGeometryIRITemplate(TableDef table, Key columns) {
		TemplateValueMaker.Builder builder = TemplateValueMaker.builder();
		// We don't use the base IRI here, so the template will produce relative IRIs
//		if (table.getName().getSchema() != null) {
//			builder.add(IRIEncoder.encode(table.getName().getSchema().getName()));
//			builder.add("/");
		//builder.add(IRIEncoder.encode(table.getName().getTable().getName()));
		//builder.add(geoBaseIRI);
		builder.add(geoBaseIRI);
		builder.add("/");
		builder.add(table.getName().getTable().getCanonicalName());
		builder.add("/");
		builder.add("Geometry");
		//builder.add("/");
		//builder.add(table.getName().toString().replaceAll("[\"]", ""));
		if (columns != null) {
			for (Identifier column: columns) {
				builder.add("/");
				if (table.getColumnDef(column).getDataType().isIRISafe()) {
					builder.add(table.getName().qualifyIdentifier(column));
				} else {
					builder.add(table.getName().qualifyIdentifier(column), TemplateValueMaker.URLIFY);
				}
			}
		}
		return builder.build();
	}
	
	public List<Identifier> getEntityPseudoKeyColumns(List<ColumnDef> columns) {
		return null;
	}
	
	public Resource getGeneratedOntologyResource() {
		return model.createResource(MappingGenerator.dropTrailingHash(vocabBaseIRI));
	}
	
	public Resource getTableClass(TableName tableName) {
		return model.createResource(vocabBaseIRI + 
				IRIEncoder.encode(stringMaker.toString(tableName)));
	}
	
	public Resource getStringClass(String chosenClass) {
		return model.createResource(vocabBaseIRI + IRIEncoder.encode(chosenClass));
	}
	
	public Property getColumnProperty(TableName tableName, Identifier column) {
		return model.createProperty(vocabBaseIRI + 
				IRIEncoder.encode(stringMaker.toString(tableName, column)));
	}
	
	/**
	 * TODO: maybe change it so that the user can provide the property with the full URI?
	 * @param tableName
	 * @param column
	 * @return
	 */
	public Property getTargetProperty(Identifier predicate) {
		return model.createProperty(vocabBaseIRI + 
				IRIEncoder.encode(predicate.getCanonicalName()));
	}
	
	public Property getGeometryColumnProperty(TableName tableName, Identifier column) {
		return model.createProperty("http://www.opengis.net/ont/geosparql#" + column.getName());
				//IRIEncoder.encode(stringMaker.toString(tableName, column)));
	}
	
	public Property getForeignKeyProperty(TableName tableName, ForeignKey fk) {
		return model.createProperty(vocabBaseIRI + 
				IRIEncoder.encode(stringMaker.toString(tableName, fk.getLocalColumns())));
	}	

	public Property getLinkProperty(TableName linkTable) {
		return model.createProperty(vocabBaseIRI + 
				IRIEncoder.encode(stringMaker.toString(linkTable)));
	}
	
	public TemplateValueMaker getEntityLabelTemplate(TableName tableName, Key columns) {
		TemplateValueMaker.Builder builder = TemplateValueMaker.builder();
		builder.add(tableName.getTable().getName());
		builder.add(" #");
		Iterator<Identifier> it = columns.iterator();
		while (it.hasNext()) {
			builder.add(tableName.qualifyIdentifier(it.next()));
			if (it.hasNext()) {
				builder.add("/");
			}
		}
		return builder.build();
	}
	
	public Property getLinkGeometryPropetry(Identifier column) {
		return model.createProperty("http://www.opengis.net/ont/geosparql#hasGeometry");
	}
	
	public Property getDefaultLinkGeometryPropetry(Identifier column) {
		return model.createProperty("http://www.opengis.net/ont/geosparql#hasDefaultGeometry");
	}

}
