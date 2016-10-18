package eu.linkedeodata.geotriples;

import java.util.Iterator;
import java.util.List;

import org.d2rq.db.schema.ColumnDef;
import org.d2rq.db.schema.ForeignKey;
import org.d2rq.db.schema.Identifier;
import org.d2rq.db.schema.Key;
import org.d2rq.db.schema.TableDef;
import org.d2rq.db.schema.TableName;
import org.d2rq.mapgen.IRIEncoder;
import org.d2rq.mapgen.MappingGenerator;
import org.d2rq.mapgen.MappingStyle;
import org.d2rq.mapgen.UniqueLocalNameGenerator;
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
 * 
 * @author Giannis Vlachopoulos, National and Kapodistrian University of Athens
 * @author Dimitrianos Savva, National and Kapodistrian University of Athens
 *
 */
public class GeneralMappingStyle implements MappingStyle {
	
	private final GeneralMappingGenerator generator;
	private final Model model = ModelFactory.createDefaultModel();
	private final UniqueLocalNameGenerator stringMaker = new UniqueLocalNameGenerator();
	private String baseIRI;
	private final String vocabBaseIRI;
	private final String geoBaseIRI;
	private final String entityBaseIRI;
	private String fileName;
	
	
	public GeneralMappingStyle(GeneralConnection connection, String baseIRI, String fileName) {
		this.baseIRI = baseIRI;
		this.fileName = fileName;
		if (this.baseIRI.endsWith("/")) {
			this.baseIRI = this.baseIRI.substring(0, this.baseIRI.length()-1);
		}
		this.vocabBaseIRI = this.baseIRI + "ontology#";
		this.geoBaseIRI = this.baseIRI;
		//this.geoBaseIRI = this.baseIRI + "/Geometry";
		if (fileName == null) {
			this.entityBaseIRI = this.baseIRI + "/Feature";
		}
		else {
			this.entityBaseIRI = this.baseIRI + "/" + fileName.substring(0, fileName.lastIndexOf('.'));
		}
		model.setNsPrefix("rdf", RDF.getURI());
		model.setNsPrefix("rdfs", RDFS.getURI());
		model.setNsPrefix("xsd", XSD.getURI());
		model.setNsPrefix("ogc", "http://www.opengis.net/ont/geosparql#");
		model.setNsPrefix("geof", "http://www.opengis.net/def/function/geosparql/");
		model.setNsPrefix("vocab", vocabBaseIRI);
		//model.setNsPrefix("tablename", vocabBaseIRI);
		generator = new GeneralMappingGenerator(this, connection);
	}
	
	public GeneralMappingGenerator getGeneralMappingGenerator() {
		return generator;
	}

	public String getBaseIRI() {
		return baseIRI;
	}
	
	public PrefixMapping getPrefixes() {
		return model;
	}
	
	//TODO FIX 
	
	public TemplateValueMaker getEntityIRITemplate(TableDef table, Key columns) {
		TemplateValueMaker.Builder builder = TemplateValueMaker.builder();
		// We don't use the base IRI here, so the template will produce relative IRIs
		if (table.getName().getSchema() != null) {
			builder.add(IRIEncoder.encode(table.getName().getSchema().getName()));
			
			//builder.add(IRIEncoder.encode(entityBaseIRI));
			builder.add("/");
		}
		//builder.add(IRIEncoder.encode(table.getName().getTable().getName()));
		builder.add(entityBaseIRI);
		//builder.add("/");
		//builder.add(table.getName().toString().replaceAll("[\"]", ""));
		builder.add("/id");
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
	
	
	// TODO FIX
	
	public TemplateValueMaker getGeometryIRITemplate(TableDef table, Key columns) {
		TemplateValueMaker.Builder builder = TemplateValueMaker.builder();
		// We don't use the base IRI here, so the template will produce relative IRIs
		if (table.getName().getSchema() != null) {
			builder.add(IRIEncoder.encode(table.getName().getSchema().getName()));
			builder.add("/");
		}
		//builder.add(IRIEncoder.encode(table.getName().getTable().getName()));
		//builder.add(geoBaseIRI);
		builder.add(geoBaseIRI);
		builder.add("/");
		if (table.getName().getTable().getCanonicalName().endsWith("geometry")) {
			builder.add(table.getName().getTable().getCanonicalName().substring(0, table.getName().getTable().getCanonicalName().lastIndexOf('_')));
		}
		else {
			builder.add(table.getName().getTable().getCanonicalName());
		}
		builder.add("/");
		builder.add("Geometry");
		
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
		if (tableName.getTable().getCanonicalName().endsWith("geometry")) {
			return model.createResource("http://www.opengis.net/ont/geosparql#" + "Geometry");
			
		}
//		return model.createResource(vocabBaseIRI + 
//				IRIEncoder.encode(stringMaker.toString(tableName)));
		
		return model.createResource(vocabBaseIRI + tableName.getTable().getName());
	}
	
	
	public Property getColumnProperty(TableName tableName, Identifier column) {
		if (tableName.getTable().getCanonicalName().toString().endsWith("geometry")) {
			return model.createProperty("http://www.opengis.net/ont/geosparql#" + column.getCanonicalName());
			
		}
		return model.createProperty(vocabBaseIRI + 
				IRIEncoder.encode("has_" + column.getCanonicalName()));
	}
	
	public Property getCustomColumnProperty(TableName tableName, Identifier column,boolean isgeosparql_property) { //d2.1
		if (tableName.getTable().getCanonicalName().toString().endsWith("geometry") || isgeosparql_property) {
			return model.createProperty("http://www.opengis.net/ont/geosparql#" + column.getName());
			
		}
		//System.out.println(column.getName());
		return model.createProperty(vocabBaseIRI + 
				IRIEncoder.encode(column.getName()));
	}
	
	public Property getStRDFColumnProperty(TableName tableName, Identifier column,boolean isgeosparql_property) { //d2.1
//		if (tableName.getTable().getCanonicalName().toString().endsWith("geometry") || isgeosparql_property) {
//			return model.createProperty("http://www.opengis.net/ont/geosparql#" + column.getName());
//			
//		}
		//System.out.println(column.getName());
		return model.createProperty(vocabBaseIRI + 
				IRIEncoder.encode(column.getName()));
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

	@Override
	public MappingGenerator getMappingGenerator() {
		// TODO Auto-generated method stub
		return null;
	}

	public Property getLinkGeometryPropetry(Identifier column) {
		return model.createProperty("http://www.opengis.net/ont/geosparql#hasGeometry");
	}
	
	public Property getDefaultLinkGeometryPropetry(Identifier column) {
		return model.createProperty("http://www.opengis.net/ont/geosparql#hasDefaultGeometry");
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public Property getGeometryColumnProperty(TableName name, Identifier column) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Property getTargetProperty(Identifier predicate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource getStringClass(String classname) {
		return model.createResource(vocabBaseIRI + classname);
	}
	

	

}
