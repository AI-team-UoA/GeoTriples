package org.d2rq.mapgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.d2rq.db.SQLConnection;
import org.d2rq.db.schema.ColumnName;
import org.d2rq.db.schema.ForeignKey;
import org.d2rq.db.schema.Identifier;
import org.d2rq.db.schema.Key;
import org.d2rq.db.schema.TableName;
import org.d2rq.db.types.DataType;
import org.d2rq.db.vendor.Vendor;
import org.d2rq.r2rml.ColumnNameR2RML;
import org.d2rq.r2rml.ConstantIRI;
import org.d2rq.r2rml.ConstantShortcut;
import org.d2rq.r2rml.Join;
import org.d2rq.r2rml.LogicalTable;
import org.d2rq.r2rml.LogicalTable.BaseTableOrView;
import org.d2rq.r2rml.LogicalTable.R2RMLView;
import org.d2rq.r2rml.Mapping;
import org.d2rq.r2rml.PredicateObjectMap;
import org.d2rq.r2rml.ReferencingObjectMap;
import org.d2rq.r2rml.SQLQuery;
import org.d2rq.r2rml.StringTemplate;
import org.d2rq.r2rml.TableOrViewName;
import org.d2rq.r2rml.TermMap;
import org.d2rq.r2rml.TermMap.ColumnValuedTermMap;
import org.d2rq.r2rml.TermMap.TemplateValuedTermMap;
import org.d2rq.r2rml.TermMap.TermType;
import org.d2rq.r2rml.TriplesMap;
import org.d2rq.values.TemplateValueMaker;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

public class R2RMLTarget implements Target {
	private final Model model = ModelFactory.createDefaultModel();
	private final UniqueLocalNameGenerator stringMaker = 
			new UniqueLocalNameGenerator();
	private final List<IncompleteLinkMap> incompleteLinkMaps = 
			new ArrayList<IncompleteLinkMap>();
	private final Map<TableName,TemplateValueMaker> iriTemplates =
			new HashMap<TableName,TemplateValueMaker>();
	private Mapping mapping = null;
	private Vendor vendor = null;
	
	public Mapping getMapping() {
		return mapping;
	}
	
	public void init(String baseIRI, Resource generatedOntology, 
			boolean serveVocabulary, boolean generateDefinitionLabels) {
		mapping = new Mapping(baseIRI);
		addPrefix("map", baseIRI + "#");
		addPrefix("strdf", "http://strdf.di.uoa.gr/ontology#");
	}

	public void addPrefix(String prefix, String uri) {
		mapping.getPrefixes().setNsPrefix(prefix, uri);
		model.setNsPrefix(prefix, uri);
	}

	public void generateDatabase(SQLConnection connection,
			String startupSQLScript) {
		vendor = connection.vendor();
	}

	public void generateEntities(Resource class_, TableName tableName,
			TemplateValueMaker iriTemplate, List<Identifier> blankNodeColumns, boolean isGeometryTable) {
		TermMap subjectMap = (iriTemplate == null)
				? createTermMap(toTemplate(tableName, blankNodeColumns), 
						TermType.BLANK_NODE) 
				: createTermMap(iriTemplate, null);
		if (class_ != null) {
			subjectMap.getClasses().add(ConstantIRI.create(class_));
		}
		addTriplesMap(tableName, createBaseTableOrView(tableName), subjectMap);
		
		if (iriTemplate != null) {
			iriTemplates.put(tableName, iriTemplate);
		}
	}
	
	public void generateGeoEntities(Resource class_, TableName tableName,
			TemplateValueMaker iriTemplate, List<Identifier> blankNodeColumns, String sqlQuery) {
		TermMap subjectMap = (iriTemplate == null)
				? createTermMap(toTemplate(tableName, blankNodeColumns), 
						TermType.BLANK_NODE) 
				: createTermMap(iriTemplate, null);
		if (class_ != null) {
			subjectMap.getClasses().add(ConstantIRI.create(model.createResource("http://www.opengis.net/ont/geosparql#Geometry")));
		}
		addTriplesMap(tableName, createBaseR2RMLView(sqlQuery), subjectMap);
		
		if (iriTemplate != null) {
			iriTemplates.put(tableName, iriTemplate);
		}
	}
	
	public void generateQueriedEntities(Resource class_, TableName tableName,
			TemplateValueMaker iriTemplate, List<Identifier> blankNodeColumns, String sqlQuery) {
		TermMap subjectMap = (iriTemplate == null)
				? createTermMap(toTemplate(tableName, blankNodeColumns), 
						TermType.BLANK_NODE) 
				: createTermMap(iriTemplate, null);
		if (class_ != null) {
			subjectMap.getClasses().add(ConstantIRI.create(class_));
		}
		addTriplesMap(tableName, createBaseR2RMLView(sqlQuery), subjectMap);
		
		if (iriTemplate != null) {
			iriTemplates.put(tableName, iriTemplate);
		}
	}

	public void generateEntityLabels(TemplateValueMaker labelTemplate,
			TableName tableName) {
		addPredicateObjectMap(tableName, 
				createPredicateObjectMap(RDFS.label, 
						createTermMap(labelTemplate, TermType.LITERAL)));
	}
	
	public void generateTemplatePredicateObjectMap(Property property,TemplateValueMaker template,
			TableName tableName){
		addPredicateObjectMap(tableName, 
				createPredicateObjectMap(property, 
						createTermMap(template, TermType.IRI)));
	}
	
	private ReferencingObjectMap createRefObjectMap(TableName tableName, Key localKey, Key foreignKey, TableName foreignTable) {
		ReferencingObjectMap result = new ReferencingObjectMap();
		result.setParentTriplesMap(getTriplesMapResource(foreignTable));
		for(int i=0;i<localKey.size();++i)
		{
			Join join = new Join();
			join.setChild(ColumnNameR2RML.create(localKey.get(i),vendor));
			join.setParent(ColumnNameR2RML.create(foreignKey.get(i),vendor));
			Resource joinResource = model.createResource();
			mapping.joins().put(joinResource, join);
			result.getJoinConditions().add(joinResource);
		}
		
		return result;
		
	}
	
	//d2.1 new function
	public void generateGeometryProperty(Property property, TableName tableName,
			Identifier column, DataType datatype){
		
		addPredicateObjectMap(tableName, 
				createPredicateObjectMap(property, 
						createTermMap(column, datatype)));
	}
	
	public void generateLinkPredicateObjectMap(Property property, TableName currentTable, TableName foreignTable, Key localKey, Key foreignKey) {
		addPredicateObjectMap(currentTable, createPredicateObjectMap(property, createRefObjectMap(currentTable, localKey, foreignKey, foreignTable)));
	}

	public void generateColumnProperty(Property property, TableName tableName,
			Identifier column, DataType datatype) {
		addPredicateObjectMap(tableName, 
				createPredicateObjectMap(property, 
						createTermMap(column, datatype)));
	}

	public void generateRefProperty(Property property, TableName tableName,
			ForeignKey foreignKey) {
		addPredicateObjectMap(tableName, 
				createPredicateObjectMap(property, 
						createRefObjectMap(tableName, foreignKey)));
	}

	public void generateLinkProperty(Property property, TableName tableName,
			ForeignKey fk1, ForeignKey fk2) {
		TemplateValuedTermMap subjects = new TemplateValuedTermMap();
		incompleteLinkMaps.add(new IncompleteLinkMap(tableName, fk1, subjects));
		addTriplesMap(tableName, createBaseTableOrView(tableName), subjects);
		addPredicateObjectMap(tableName, 
				createPredicateObjectMap(property, 
						createRefObjectMap(tableName, fk2)));
	}

	public void skipColumn(TableName table, Identifier column, String reason) {
		// Do nothing
	}

	public void close() {
		for (IncompleteLinkMap linkMap: incompleteLinkMaps) {
			TemplateValueMaker template = iriTemplates.get(linkMap.foreignKey.getReferencedTable());
			template = substituteColumns(template, 
					linkMap.tableName, linkMap.foreignKey.getLocalColumns());
			linkMap.termMap.setTemplate(toStringTemplate(template));
		}
	}
	
	private Resource getTriplesMapResource(TableName tableName) {
		return model.createResource(mapping.getBaseIRI() + "#" + 
				IRIEncoder.encode(stringMaker.toString(tableName)));
	}
	
	private BaseTableOrView createBaseTableOrView(TableName tableName) {
		BaseTableOrView table = new BaseTableOrView();
		//tableName = TableName.create(null, null, tableName.getTable());
		table.setTableName(TableOrViewName.create(tableName, vendor));
		return table;
	}
	
	private R2RMLView createBaseR2RMLView(String sqlQuery) {
		R2RMLView table = new R2RMLView();
		table.setSQLQuery(SQLQuery.create(sqlQuery));
		return table;
	}
	
	private TermMap createTermMap(TemplateValueMaker template, TermType termType) {
		TemplateValuedTermMap result = new TemplateValuedTermMap();
		result.setTemplate(toStringTemplate(template));
		if (termType != null) {
			result.setSpecifiedTermType(termType);
		}
		return result;
	}
	
	private TermMap createTermMap(Identifier column, DataType dataType) {
		ColumnValuedTermMap result = new ColumnValuedTermMap();
		result.setColumnName(ColumnNameR2RML.create(column, vendor));
		result.setDatatype(ConstantIRI.create(dataType.rdfType()));
		return result;
	}
	
	private ReferencingObjectMap createRefObjectMap(TableName tableName, ForeignKey foreignKey) {
		ReferencingObjectMap result = new ReferencingObjectMap();
		result.setParentTriplesMap(getTriplesMapResource(foreignKey.getReferencedTable()));
		for (int i = 0; i < foreignKey.getLocalColumns().size(); i++) {
			Join join = new Join();
			join.setChild(ColumnNameR2RML.create(
					foreignKey.getLocalColumns().get(i), vendor));
			join.setParent(ColumnNameR2RML.create(
					foreignKey.getReferencedColumns().get(i), vendor));
			Resource joinResource = model.createResource();
			mapping.joins().put(joinResource, join);
			result.getJoinConditions().add(joinResource);
		}
		return result;
	}
	
	private PredicateObjectMap createPredicateObjectMap(Property property, TermMap objects) {
		Resource objectsResource = model.createResource();
		mapping.termMaps().put(objectsResource, objects);
		return createPredicateObjectMap(property, objectsResource);
	}
	
	private PredicateObjectMap createPredicateObjectMap(Property property, ReferencingObjectMap objects) {
		Resource objectsResource = model.createResource();
		mapping.referencingObjectMaps().put(objectsResource, objects);
		return createPredicateObjectMap(property, objectsResource);
	}

	private PredicateObjectMap createPredicateObjectMap(Property property, Resource objectsResource) {
		PredicateObjectMap result = new PredicateObjectMap();
		result.getPredicates().add(ConstantShortcut.create(property));
		result.getObjectMaps().add(objectsResource);
		return result;
	}
	
	private void addTriplesMap(TableName tableName, LogicalTable table, TermMap subjectMap) {
		Resource tableResource = model.createResource();
		mapping.logicalTables().put(tableResource, table);

		Resource subjectMapResource = model.createResource();
		mapping.termMaps().put(subjectMapResource, subjectMap);

		Resource triplesMapResource = getTriplesMapResource(tableName);
		TriplesMap map = new TriplesMap();
		map.setLogicalTable(tableResource);
		map.setSubjectMap(subjectMapResource);
		mapping.triplesMaps().put(triplesMapResource, map);
	}
	
	private void addPredicateObjectMap(TableName tableName, PredicateObjectMap poMap) {
		Resource poMapResource = model.createResource();
		mapping.predicateObjectMaps().put(poMapResource, poMap);
		mapping.triplesMaps().get(getTriplesMapResource(tableName)).getPredicateObjectMaps().add(poMapResource);
	}
	
	private TemplateValueMaker toTemplate(TableName tableName, List<Identifier> columns) {
		TemplateValueMaker.Builder builder = TemplateValueMaker.builder();
		for (ColumnName column: tableName.qualifyIdentifiers(columns)) {
			builder.add("@@");
			builder.add(column);
		}
		return builder.build();
	}
	
	private StringTemplate toStringTemplate(TemplateValueMaker template) {
		StringBuilder s = new StringBuilder();
		s.append(escapeForStringTemplate(template.firstLiteralPart()));
		int i = 0;
		while (i < template.columns().length) {
			s.append('{');
			s.append(vendor.toString(template.columns()[i].getColumn()));
			s.append('}');
			i++;
			s.append(escapeForStringTemplate(template.literalParts()[i]));
		}
		return StringTemplate.create(s.toString());
	}
	
	private String escapeForStringTemplate(String s) {
		return s.replaceAll("\\\\", "\\\\").replaceAll("\\{", "\\{").replaceAll("\\}", "\\}");
	}

	private TemplateValueMaker substituteColumns(TemplateValueMaker template,
			TableName newTable, Key newColumns) {
		return new TemplateValueMaker(
				template.literalParts(), 
				newTable.qualifyIdentifiers(newColumns.getColumns()).toArray(new ColumnName[newColumns.size()]), 
				template.functions());
	}
	
	private class IncompleteLinkMap {
		final TableName tableName;
		final ForeignKey foreignKey;
		final TemplateValuedTermMap termMap;
		IncompleteLinkMap(TableName table, ForeignKey fk, TemplateValuedTermMap terms) {
			tableName = table;
			foreignKey = fk;
			termMap = terms;
		}
	}

	@Override
	public void generateGeometryColumnProperty(Property propertyname,
			TableName table, Identifier column, DataType datatype,ConstantIRI functionname) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateHasGeometryPropertyWithTemplateTrick(Property property,
			TableName tableName, TemplateValueMaker iriTemplate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateTransformationProperty(Property property,
			TableName tableName, ConstantIRI function,
			List<TermMap> argumentMap, DataType datatype) {
		throw new UnsupportedOperationException();
	}

}
