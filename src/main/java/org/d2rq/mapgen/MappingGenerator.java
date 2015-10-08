package org.d2rq.mapgen;

import java.net.URI;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.d2rq.db.SQLConnection;
import org.d2rq.db.schema.ColumnDef;
import org.d2rq.db.schema.ColumnName;
import org.d2rq.db.schema.ForeignKey;
import org.d2rq.db.schema.Identifier;
import org.d2rq.db.schema.Key;
import org.d2rq.db.schema.TableDef;
import org.d2rq.db.schema.TableName;
import org.d2rq.db.types.DataType;
import org.d2rq.db.types.SQLBoolean;
import org.d2rq.db.types.SQLExactNumeric;
import org.d2rq.db.types.StrdfWKT;
import org.d2rq.lang.Microsyntax;
import org.d2rq.values.TemplateValueMaker;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.PrefixMapping.IllegalPrefixException;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

import eu.linkedeodata.geotriples.Config;
import eu.linkedeodata.geotriples.WKTLiteral;
import eu.linkedeodata.geotriples.gui.ColumnReceipt;


/**
 * Generates a D2RQ mapping by introspecting a database schema.
 * Result is available as a high-quality Turtle serialization, or
 * as a parsed model.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class MappingGenerator {
	private final static Logger log = Logger.getLogger(MappingGenerator.class);

	private final MappingStyle style;
	private final SQLConnection sqlConnection;
	private final List<TableName> tablesWithoutUniqueKey = 
			new ArrayList<TableName>();
	private final PrefixMapping prefixes = new PrefixMappingImpl();
	private Filter filter = Filter.ALL;
	private boolean finished = false;
	private boolean generateClasses = true;
	private boolean generateLabelBridges = false;
	private boolean generateDefinitionLabels = false;
	private boolean handleLinkTables = true;
	private boolean serveVocabulary = true;
	private boolean skipForeignKeyTargetColumns = true;
	private boolean useUniqueKeysAsEntityID = true;
	private boolean suppressWarnings = false;
	private URI startupSQLScript;
	private Target target;
	private TableDef geoTable = null;

	private String onlytable=null;
	
	public MappingGenerator(MappingStyle style, SQLConnection sqlConnection) {
		this.style = style;
		this.sqlConnection = sqlConnection;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	
	public void setStartupSQLScript(URI uri) {
		startupSQLScript = uri;
	}
	
	public void setOnlyTable(String onlytable){
		this.onlytable=onlytable;
	}
	
	/**
	 * @param flag Generate an rdfs:label property bridge based on the PK?
	 */
	public void setGenerateLabelBridges(boolean flag) {
		this.generateLabelBridges = flag;
	}

	/**
	 * @param flag Add <code>rdfs:label</code>s to auto-generated classes and properties?
	 */
	public void setGenerateDefinitionLabels(boolean flag) {
		this.generateDefinitionLabels = flag;
	}
	
	/**
	 * @param flag Generate a d2rq:class for every class map?
	 */
	public void setGenerateClasses(boolean flag) {
		this.generateClasses = flag;
	}
		

	/**
	 * @param flag Handle Link Tables as properties (true) or normal tables (false)
	 */
	public void setHandleLinkTables(boolean flag) {
		this.handleLinkTables = flag;
	}

	/**
	 * @param flag Value for d2rq:serveVocabulary in map:Configuration
	 */
	public void setServeVocabulary(boolean flag) {
		this.serveVocabulary = flag;
	}

	public void setSkipForeignKeyTargetColumns(boolean flag) {
		skipForeignKeyTargetColumns = flag;
	}
	
	public void setUseUniqueKeysAsEntityID(boolean flag) {
		useUniqueKeysAsEntityID = flag;
	}
	
	public void setSuppressWarnings(boolean flag) {
		suppressWarnings = flag;
	}
	
	
	public void generate(Target generationTarget) {
		java.util.Map<TableName, java.util.List<ColumnReceipt>> tablesAndColumns = Config.tablesAndColumns;
		java.util.Map<TableName, String> tablesAndClasses = Config.tablesAndClasses;
		if (finished) return;
		finished = true;
		target = generationTarget;
		target.init(style.getBaseIRI(), style.getGeneratedOntologyResource(),
				serveVocabulary, generateDefinitionLabels);
		for (String prefix: style.getPrefixes().getNsPrefixMap().keySet()) {
			target.addPrefix(prefix, style.getPrefixes().getNsPrefixMap().get(prefix));
			prefixes.setNsPrefix(prefix, style.getPrefixes().getNsPrefixMap().get(prefix));
		}
		target.generateDatabase(sqlConnection, 
				startupSQLScript == null ? null : startupSQLScript.toString());
		List<TableName> tableNames = new ArrayList<TableName>();
		for (TableName tableName: sqlConnection.getTableNames(filter.getSingleSchema())) {
			//ignore meta-tables in the mapping
			if (!filter.matches(tableName) || tableName.getTable().getName().equals("geography_columns") || tableName.getTable().getName().equals("spatial_ref_sys") || tableName.getTable().getName().equals("geometry_columns")) {
				log.info("Skipping table " + tableName);
				System.out.println("Skipping table " + tableName);
				continue;
			}
			if(onlytable!=null){
				if(!onlytable.equalsIgnoreCase(tableName.getTable().getName())){
					continue;
				}
			}
			tableNames.add(tableName);
		}
		log.info("Filter '" + filter + "' matches " + tableNames.size() + " total tables");
		for (TableName tableName: tableNames) {
			
			if (tablesAndColumns != null) {
				if (tablesAndColumns.get(tableName) == null) {
					continue;
				}
			}
			//check if the there is the_geom property
			TableDef table = sqlConnection.getTable(tableName).getTableDefinition();
			List<ColumnDef> columns = table.getColumns();
			boolean hasGeom = false;
			String geoColumn = null;
			for (int i=0 ; i<columns.size() ; i++) {
				if (columns.get(i).getDataType().name().equalsIgnoreCase("GEOMETRY")) {
					geoColumn = columns.get(i).getName().getCanonicalName();
					hasGeom = true;
					break;
				}
			}
			
			if (tablesAndColumns != null) {
				java.util.List<ColumnReceipt> cols = tablesAndColumns.get(tableName);
				if (cols != null) {
					boolean found = false;
					for (int i=0 ; i<cols.size() ; i++) {
						//System.out.println(cols.get(i).getDataType());
						if(cols.get(i).getDataType().equalsIgnoreCase("geometry")){
						//if (cols.get(i).getColumnName().contains("geom")) {
							//TODO: check the recognition of geocolumn
							found = true;
							break;
						}
					}
					if (!found) {
						hasGeom = false;
					}
				}
			}
			//System.out.println(hasGeom + " " + Config.VOCABULARY);
			if (hasGeom && Config.VOCABULARY.equals("GeoSPARQL")) {
				processGeometry(tableName, geoColumn);
			}
			if (hasGeom && Config.VOCABULARY.equals("stRDF")) {
				processAtOnce(tableName, hasGeom, tablesAndColumns, tablesAndClasses, geoColumn);
			}
			else {
				processTable(tableName, hasGeom, tablesAndColumns, tablesAndClasses);
			}
		}
		if (!tablesWithoutUniqueKey.isEmpty()) {
			StringBuilder s = new StringBuilder();
			s.append("Sorry, I don't know which columns to put into the ");
			s.append("d2rq:uriPattern of tables that don't have a ");
			s.append("primary or unique key. Please specify them manually: ");
			Iterator<TableName> it = tablesWithoutUniqueKey.iterator();
			while (it.hasNext()) {
				s.append(Microsyntax.toString(it.next()));
				if (it.hasNext()) {
					s.append(", ");
				}
			}
			if (!suppressWarnings) {
				log.warn(s.toString());
			}
		}
		target.close();
	}
	
	private void processAtOnce(TableName tableName, boolean hasGeom, 
			java.util.Map<TableName, java.util.List<ColumnReceipt>> tablesAndColumns, 
			java.util.Map<TableName, String> tablesAndClasses, String geoColumn) {
		TableDef table = sqlConnection.getTable(tableName).getTableDefinition();
		ColumnDef additionalColumn = new ColumnDef(Identifier.createDelimited("hasGeometry"), new StrdfWKT("WKT"), true);
		table.getColumns().add(additionalColumn);
		table.getColumnNames().add(additionalColumn.getName());
		boolean hasprimarykey=table.getPrimaryKey()!=null;

		//SQLOp op = sqlConnection.getSelectStatement("");
		//but first create its geometry view?
		if (handleLinkTables && isLinkTable(table)) {
			Iterator<ForeignKey> it = table.getForeignKeys().iterator();
			ForeignKey fk1 = it.next();
			ForeignKey fk2 = it.next();
			TableName referencedTable1 = fk1.getReferencedTable();
			TableName referencedTable2 = fk2.getReferencedTable();
			if (!filter.matches(referencedTable1) || 
					!filter.matchesAll(referencedTable1, fk1.getLocalColumns()) || 
					!filter.matchesAll(referencedTable1, fk1.getReferencedColumns()) ||
					!filter.matches(referencedTable2) || 
					!filter.matchesAll(referencedTable2, fk2.getLocalColumns()) || 
					!filter.matchesAll(referencedTable2, fk2.getReferencedColumns())) {
				log.info("Skipping link table " + tableName);
				return;
			}
			target.generateLinkProperty(
					style.getLinkProperty(tableName), tableName, fk1, fk2);
		} else {
			Resource class_ = null;
			if (tablesAndClasses==null || tablesAndClasses.get(tableName) == null) {
				class_ = generateClasses ? 
						style.getTableClass(tableName) : null;
			}
			else {
				class_ = generateClasses ? style.getStringClass(tablesAndClasses.get(tableName)) : null;
			}
					
			//System.out.println(class_.getLocalName());
			TemplateValueMaker iriTemplate = null;
			List<Identifier> blankNodeColumns = null;
			Key key = findBestKey(table);
			if (key == null) {
				/*
				List<ColumnDef> filteredColumns = new ArrayList<ColumnDef>();
				for (ColumnDef columnDef: table.getColumns()) {
					if (filter.matches(tableName, columnDef.getName())) {
						filteredColumns.add(columnDef);
					}
				}
				if (style.getEntityPseudoKeyColumns(filteredColumns) == null) {
					tablesWithoutUniqueKey.add(tableName);
					iriTemplate = style.getEntityIRITemplate(table, null);
				} else {
					blankNodeColumns = style.getEntityPseudoKeyColumns(filteredColumns);
				}*/
				key=makeKey(table);
			} /*else {*/
				iriTemplate = style.getEntityIRITemplate(table, key);
			/*}*/
			String query = "SELECT *," + "CONCAT(st_astext(" + geoColumn + "), \'; <http://www.opengis.net/def/crs/EPSG/0/\', ST_SRID(" + geoColumn + "), \'>') as \"hasGeometry\" FROM " + tableName.toString();
			if (sqlConnection.getJdbcURL().contains("monetdb")) {
				query = "SELECT *," + " CONCAT(CONCAT(REPLACE(CAST(" + geoColumn + " AS TEXT), '\"', ''), '; '), \'<http://www.opengis.net/def/crs/EPSG/0/" + Config.EPSG_CODE +">\') as \"hasGeometry\" FROM " + tableName.toString();
			}
			target.generateQueriedEntities(class_, table.getName(), 
					iriTemplate, blankNodeColumns, query);
			//System.out.println(class_.getURI());
			if (class_ != null) {
				if (tableName.getSchema() != null) {
					tryRegisterPrefix(tableName.getSchema().getName().toLowerCase(),
							class_.getNameSpace());
				}
				if (tableName.getCatalog() != null) {
					tryRegisterPrefix(tableName.getCatalog().getName().toLowerCase(),
							class_.getNameSpace());
				}
			}
			if (generateLabelBridges && key != null) {
				target.generateEntityLabels(
						style.getEntityLabelTemplate(tableName, key), tableName);
			}
			/*else if (generateLabelBridges && key == null) { //key is never null //
				Key themkey = null;
				if (table.getColumnNames().contains(Identifier.createDelimited("id"))) {
					themkey = Key.create(ColumnName.create(table.getName(), Identifier.createDelimited("id")));
				}
				else if (table.getColumnNames().contains(Identifier.createDelimited("gid"))) {
					themkey = Key.create(Identifier.createDelimited("gid"));
				}
				
				if (themkey == null) {
//					target.generateEntityLabels(
//							style.getEntityLabelTemplate(tableName, themkey), tableName);
				}
			}*/
			for (Identifier column: table.getColumnNames()) {
				ColumnReceipt colReceipt = null;
				if (tablesAndColumns != null) {
					java.util.List<ColumnReceipt> cols = tablesAndColumns.get(tableName);
					if (cols != null) {
						boolean found = false;
						for (int i=0 ; i<cols.size() ; i++) {
							if (cols.get(i).getColumnName().equals(column.getCanonicalName())) {
								colReceipt = cols.get(i);
								found = true;
								break;
							}
						}
						if (!found && !column.getCanonicalName().equals("hasGeometry")) {
							continue;
						}
					}
				}
				if (key != null && hasprimarykey) { //need hasprimarykay because if has not, then must not get in this if
					if (key.contains(column)) {
						continue;
					}
				}
				else {
					if (column.getCanonicalName().equals("gid")) {
						continue;
					}
				}
				if (skipForeignKeyTargetColumns && isInForeignKey(column, table)) continue;
				if (!filter.matches(tableName, column)) {
					log.info("Skipping filtered column " + column);
					continue;
				}
				DataType type = table.getColumnDef(column).getDataType();
				if (type == null) {
					String message = "The datatype is unknown to D2RQ.\n";
					message += "You can override the column's datatype using d2rq:xxxColumn and add a property bridge.";
					if (!suppressWarnings) {
						log.warn(message);
					}
					target.skipColumn(tableName, column, message);
					continue;
				}
				if (type.name().equalsIgnoreCase("GEOMETRY")) {
					continue;
				}
				if (type.isUnsupported()) {
					String message = "The datatype " + type + " cannot be mapped to RDF.";
					if (!suppressWarnings) {
						log.warn(message);
					}
					target.skipColumn(tableName, column, message);
					continue;
				}
				Property property = null;
				if (colReceipt == null) {
					property = style.getColumnProperty(tableName, column);
				}
				else {
					property = style.getTargetProperty(Identifier.createDelimited(colReceipt.getPredicate()));
				}
				target.generateColumnProperty(property, tableName, column, type);
				tryRegisterPrefix(
						tableName.getTable().getName().toLowerCase(), 
						property.getNameSpace());
			}
			for (ForeignKey fk: table.getForeignKeys()) {
				if (!filter.matches(fk.getReferencedTable()) || 
						!filter.matchesAll(tableName, fk.getLocalColumns()) || 
						!filter.matchesAll(fk.getReferencedTable(), fk.getReferencedColumns())) {
					log.info("Skipping foreign key: " + fk);
					continue;
				}
				target.generateRefProperty(
						style.getForeignKeyProperty(tableName, fk), 
						tableName, fk);
			}
		}
	}
	
	private void processTable(TableName tableName, boolean hasGeom, 
			java.util.Map<TableName, java.util.List<ColumnReceipt>> tablesAndColumns, 
			java.util.Map<TableName, String> tablesAndClasses) {
		TableDef table = sqlConnection.getTable(tableName).getTableDefinition();
		boolean hasprimarykey=table.getPrimaryKey()!=null;

		//SQLOp op = sqlConnection.getSelectStatement("");
		//but first create its geometry view?
		//if(!filter.matches(tableName) )return;
		if (handleLinkTables && isLinkTable(table)) {
			Iterator<ForeignKey> it = table.getForeignKeys().iterator();
			ForeignKey fk1 = it.next();
			ForeignKey fk2 = it.next();
			TableName referencedTable1 = fk1.getReferencedTable();
			TableName referencedTable2 = fk2.getReferencedTable();
			if (!filter.matches(referencedTable1) || 
					!filter.matchesAll(referencedTable1, fk1.getLocalColumns()) || 
					!filter.matchesAll(referencedTable1, fk1.getReferencedColumns()) ||
					!filter.matches(referencedTable2) || 
					!filter.matchesAll(referencedTable2, fk2.getLocalColumns()) || 
					!filter.matchesAll(referencedTable2, fk2.getReferencedColumns())) {
				log.info("Skipping link table " + tableName);
				return;
			}
			target.generateLinkProperty(
					style.getLinkProperty(tableName), tableName, fk1, fk2);
		} else {
			Resource class_ = null;
			if (tablesAndClasses==null || tablesAndClasses.get(tableName) == null) {
				class_ = generateClasses ? 
						style.getTableClass(tableName) : null;
			}
			else {
				class_ = generateClasses ? style.getStringClass(tablesAndClasses.get(tableName)) : null;
			}
					
					
			TemplateValueMaker iriTemplate = null;
			List<Identifier> blankNodeColumns = null;
			Key key = findBestKey(table);
			if (key == null) {
				/*
				List<ColumnDef> filteredColumns = new ArrayList<ColumnDef>();
				for (ColumnDef columnDef: table.getColumns()) {
					if (filter.matches(tableName, columnDef.getName())) {
						filteredColumns.add(columnDef);
					}
				}
				if (style.getEntityPseudoKeyColumns(filteredColumns) == null) {
					tablesWithoutUniqueKey.add(tableName);
					iriTemplate = style.getEntityIRITemplate(table, null);
				} else {
					blankNodeColumns = style.getEntityPseudoKeyColumns(filteredColumns);
				}*/
				key=makeKey(table);
			} /*else {*/
				iriTemplate = style.getEntityIRITemplate(table, key);
			/*}*/
			target.generateEntities(class_, tableName, 
					iriTemplate, blankNodeColumns, false);
			if (class_ != null) {
				if (tableName.getSchema() != null) {
					tryRegisterPrefix(tableName.getSchema().getName().toLowerCase(),
							class_.getNameSpace());
				}
				if (tableName.getCatalog() != null) {
					tryRegisterPrefix(tableName.getCatalog().getName().toLowerCase(),
							class_.getNameSpace());
				}
			}
			if (generateLabelBridges && key != null) {
				target.generateEntityLabels(
						style.getEntityLabelTemplate(tableName, key), tableName);
			}
			/*else if (generateLabelBridges && key == null) { //key is never null //
				Key themkey = null;
				if (table.getColumnNames().contains(Identifier.createDelimited("id"))) {
					themkey = Key.create(ColumnName.create(table.getName(), Identifier.createDelimited("id")));
				}
				else if (table.getColumnNames().contains(Identifier.createDelimited("gid"))) {
					themkey = Key.create(Identifier.createDelimited("gid"));
				}
				
				if (themkey == null) {
//					target.generateEntityLabels(
//							style.getEntityLabelTemplate(tableName, themkey), tableName);
				}
			}*/
			for (Identifier column: table.getColumnNames()) {
				ColumnReceipt colReceipt = null;
				if (tablesAndColumns != null) {
					java.util.List<ColumnReceipt> cols = tablesAndColumns.get(tableName);
					if (cols != null) {
						boolean found = false;
						for (int i=0 ; i<cols.size() ; i++) {
							if (cols.get(i).getColumnName().equals(column.getCanonicalName())) {
								colReceipt = cols.get(i);
								found = true;
								break;
							}
						}
						if (!found) {
							continue;
						}
					}
				}
				if (key != null && hasprimarykey) { //need hasprimarykay because if has not, then must not get in this if
					if (key.contains(column)) {
						continue;
					}
				}
				else {
					if (column.getCanonicalName().equals("gid")) {
						continue;
					}
				}
				if (skipForeignKeyTargetColumns && isInForeignKey(column, table)) continue;
				if (!filter.matches(tableName, column)) {
					log.info("Skipping filtered column " + column);
					continue;
				}
				DataType type = table.getColumnDef(column).getDataType();
				if (type == null) {
					String message = "The datatype is unknown to D2RQ.\n";
					message += "You can override the column's datatype using d2rq:xxxColumn and add a property bridge.";
					if (!suppressWarnings) {
						log.warn(message);
					}
					target.skipColumn(tableName, column, message);
					continue;
				}
				if (type.name().equalsIgnoreCase("GEOMETRY")) {
					continue;
				}
				if (type.isUnsupported()) {
					String message = "The datatype " + type + " cannot be mapped to RDF.";
					if (!suppressWarnings) {
						log.warn(message);
					}
					target.skipColumn(tableName, column, message);
					continue;
				}
				Property property = null;
				if (colReceipt == null) {
					property = style.getColumnProperty(tableName, column);
				}
				else {
					property = style.getTargetProperty(Identifier.createDelimited(colReceipt.getPredicate()));
				}
				target.generateColumnProperty(property, tableName, column, type);
				tryRegisterPrefix(
						tableName.getTable().getName().toLowerCase(), 
						property.getNameSpace());
			}
			for (ForeignKey fk: table.getForeignKeys()) {
				if (!filter.matches(fk.getReferencedTable()) || 
						!filter.matchesAll(tableName, fk.getLocalColumns()) || 
						!filter.matchesAll(fk.getReferencedTable(), fk.getReferencedColumns())) {
					log.info("Skipping foreign key: " + fk);
					continue;
				}
				target.generateRefProperty(
						style.getForeignKeyProperty(tableName, fk), 
						tableName, fk);
			}
			if (hasGeom){				
				((R2RMLTarget)target).generateTemplatePredicateObjectMap(style.getLinkGeometryPropetry(null),style.getGeometryIRITemplate(table, key) , tableName);
			}
			if (false && hasGeom) {
				
				/*if (key == null) {
					Key themKey = table.getPrimaryKey();
					Key geoKey = null;
					if (themKey == null) {
						themKey = Key.create(Identifier.createDelimited("gid"));
						geoKey = Key.create(Identifier.createDelimited("gid"));
					}
					else {
						geoKey = Key.createFromIdentifiers(themKey.getColumns());
					}
					((R2RMLTarget) target).generateLinkPredicateObjectMap(style.getLinkGeometryPropetry(null), table.getName(), 
							geoTable.getName(), themKey, geoKey);
					hasGeom = false;
				}
				*/
				/*else {*/
					Key themKey = key;//table.getPrimaryKey();
					Key geoKey = key;//Key.createFromIdentifiers(themKey.getColumns());
					((R2RMLTarget) target).generateLinkPredicateObjectMap(style.getLinkGeometryPropetry(null), table.getName(), 
								geoTable.getName(), themKey, geoKey);
					hasGeom = false;
				/*}*/
			}
		}
	}
	
	
	
	private void processGeometry(TableName tableName, String geoColumn) {
		TableDef auxTable = sqlConnection.getTable(tableName).getTableDefinition();
		List<ColumnDef> columns = new ArrayList<ColumnDef>();
		Identifier ide = Identifier.create(true, "gid");
		if (auxTable.getPrimaryKey() != null) {
			ide = auxTable.getPrimaryKey().get(0);
		}
		//ColumnDef id = new ColumnDef(ide, new SQLExactNumeric("int",Types.INTEGER , false), false);
		ColumnDef dimension = new ColumnDef(Identifier.create(true, "dimension"), new SQLExactNumeric("int",Types.INTEGER, false), false);
		ColumnDef coordinateDimension = new ColumnDef(Identifier.create(true, "coordinateDimension"), new SQLExactNumeric("int",Types.INTEGER, false), false);
		ColumnDef spatialDimension = new ColumnDef(Identifier.create(true, "spatialDimension"), new SQLExactNumeric("int",Types.INTEGER, false), false);
		ColumnDef isEmpty = new ColumnDef(Identifier.create(true, "isEmpty"), new SQLBoolean("Boolean"), false);
		ColumnDef isSimple = new ColumnDef(Identifier.create(true, "isSimple"), new SQLBoolean("Boolean"), false);
		ColumnDef is3D = new ColumnDef(Identifier.create(true, "is3D"), new SQLBoolean("Boolean"), false);
		/*ColumnDef hasSerialization = new ColumnDef(Identifier.create(true, "hasSerialization"), new WKTLiteral("wktLiteral"), false);*/
		ColumnDef asWKT = new ColumnDef(Identifier.create(true, "asWKT"), new WKTLiteral("wktLiteral"), false);
		//Key primaryKey = Key.create(id.getName());
		//columns.add(id);
		columns.add(dimension);
		columns.add(coordinateDimension);
		columns.add(spatialDimension);
		columns.add(isEmpty);
		columns.add(isSimple);
		columns.add(is3D);
		/*columns.add(hasSerialization);*/
		columns.add(asWKT);
		Identifier ident = Identifier.createDelimited(tableName.getTable().getCanonicalName() + "Geo");
		TableDef table = new TableDef(TableName.create(auxTable.getName().getCatalog(), auxTable.getName().getSchema(), ident), columns, null, new HashSet<Key>(), new HashSet<ForeignKey>());
		geoTable = table;
		//SQLOp op = sqlConnection.getSelectStatement("");
		//but first create its geometry view?
		if (handleLinkTables && isLinkTable(table)) {
			Iterator<ForeignKey> it = table.getForeignKeys().iterator();
			ForeignKey fk1 = it.next();
			ForeignKey fk2 = it.next();
			TableName referencedTable1 = fk1.getReferencedTable();
			TableName referencedTable2 = fk2.getReferencedTable();
			if (!filter.matches(referencedTable1) || 
					!filter.matchesAll(referencedTable1, fk1.getLocalColumns()) || 
					!filter.matchesAll(referencedTable1, fk1.getReferencedColumns()) ||
					!filter.matches(referencedTable2) || 
					!filter.matchesAll(referencedTable2, fk2.getLocalColumns()) || 
					!filter.matchesAll(referencedTable2, fk2.getReferencedColumns())) {
				log.info("Skipping link table " + tableName);
				return;
			}
			target.generateLinkProperty(
					style.getLinkProperty(tableName), tableName, fk1, fk2);
		} else {
			Resource class_ = generateClasses ? 
					style.getTableClass(tableName) : null;
			TemplateValueMaker iriTemplate = null;
			List<Identifier> blankNodeColumns = null;
			Key key = findBestKey(auxTable);
			if (key == null) {
				key=makeKey(auxTable);
			}
			/*if (key == null) {
				List<ColumnDef> filteredColumns = new ArrayList<ColumnDef>();
				for (ColumnDef columnDef: table.getColumns()) {
					if (filter.matches(tableName, columnDef.getName())) {
						filteredColumns.add(columnDef);
					}
				}
				if (style.getEntityPseudoKeyColumns(filteredColumns) == null) {
					tablesWithoutUniqueKey.add(tableName);
					iriTemplate = style.getGeometryIRITemplate(table, key);
					
				} else {
					blankNodeColumns = style.getEntityPseudoKeyColumns(filteredColumns);
					iriTemplate = style.getGeometryIRITemplate(table, key);
				}
			} else {*/
				iriTemplate = style.getGeometryIRITemplate(auxTable, key);
			/*}*/
			
			String query = "SELECT *," + " st_dimension(" + geoColumn + ") as \"dimension\", st_coorddim(" + geoColumn
					+ ") as \"coordinateDimension\", st_coorddim(" + geoColumn
					+ ") as \"spatialDimension\", CASE WHEN st_issimple(" + geoColumn
					+ ") THEN 'true' ELSE 'false' END as \"isSimple\", CASE WHEN st_isempty(" + geoColumn
					+ ") THEN 'true' ELSE 'false' END as \"isEmpty\", CASE WHEN st_coorddim("+geoColumn+")=3 THEN 'true' ELSE 'false' END as \"is3D\", CONCAT(\'<http://www.opengis.net/def/crs/EPSG/0/\', ST_SRID(" + geoColumn + "), \'> \' ,st_astext(" + geoColumn + ")) as \"asWKT\" FROM " + tableName.toString();
			if (sqlConnection.getJdbcURL().contains("monetdb")) {
				query = "SELECT *," + " st_dimension(" + geoColumn + ") as \"dimension\", st_dimension(" + geoColumn + ") as \"coordinateDimension\", st_dimension(" + geoColumn + ") as \"spatialDimension\",  st_issimple(" + geoColumn + ") as \"isSimple\", st_isempty(" + geoColumn + ") as \"isEmpty\", CASE WHEN st_dimension("+geoColumn+")=3 THEN 'true' ELSE 'false' END as \"is3D\", CONCAT(\'<http://www.opengis.net/def/crs/EPSG/0/" + Config.EPSG_CODE +"> \' , REPLACE(CAST(" + geoColumn + " AS TEXT), '\"', '')) as \"asWKT\" FROM " + tableName.toString();
			}
			target.generateGeoEntities(class_, table.getName(), 
					iriTemplate, blankNodeColumns, query);
			if (class_ != null) {
				if (tableName.getSchema() != null) {
					tryRegisterPrefix(tableName.getSchema().getName().toLowerCase(),
							class_.getNameSpace());
				}
				if (tableName.getCatalog() != null) {
					tryRegisterPrefix(tableName.getCatalog().getName().toLowerCase(),
							class_.getNameSpace());
				}
			}
			if (generateLabelBridges && key != null) {
				target.generateEntityLabels(
						style.getEntityLabelTemplate(table.getName(), key), table.getName());
			}
			for (Identifier column: table.getColumnNames()) {
				if (skipForeignKeyTargetColumns && isInForeignKey(column, table)) continue;
				if (!filter.matches(table.getName(), column)) {
					log.info("Skipping filtered column " + column);
					continue;
				}
				if (column.getName().equals("the_geom")) {
					continue;
				}
				if (column.getName().equals("type")) {
					//TODO: treat it differently
					continue;
				}
				/*if (column.getName().equals("gid")) {
					continue;
				}*/
				DataType type = table.getColumnDef(column).getDataType();
				if (type == null) {
					String message = "The datatype is unknown to D2RQ.\n";
					message += "You can override the column's datatype using d2rq:xxxColumn and add a property bridge.";
					if (!suppressWarnings) {
						log.warn(message);
					}
					target.skipColumn(table.getName(), column, message);
					continue;
				}
				if (type.isUnsupported()) {
					String message = "The datatype " + type + " cannot be mapped to RDF.";
					if (!suppressWarnings) {
						log.warn(message);
					}
					target.skipColumn(table.getName(), column, message);
					continue;
				}
				Property property = style.getGeometryColumnProperty(table.getName(), column);
				target.generateColumnProperty(property, table.getName(), column, type);
				tryRegisterPrefix(
						table.getName().getTable().getName().toLowerCase(), 
						property.getNameSpace());
			}
			for (ForeignKey fk: table.getForeignKeys()) {
				if (!filter.matches(fk.getReferencedTable()) || 
						!filter.matchesAll(table.getName(), fk.getLocalColumns()) || 
						!filter.matchesAll(fk.getReferencedTable(), fk.getReferencedColumns())) {
					log.info("Skipping foreign key: " + fk);
					continue;
				}
				target.generateRefProperty(
						style.getForeignKeyProperty(table.getName(), fk), 
						tableName, fk);
			}
		}
		//geoTable = null;
	}

	private void tryRegisterPrefix(String prefix, String uri) {
		if (prefixes.getNsPrefixMap().containsKey(prefix)) return;
		if (prefixes.getNsPrefixMap().containsValue(uri)) return;
		try {
			prefixes.setNsPrefix(prefix, uri);
			target.addPrefix(prefix, uri);
		} catch (IllegalPrefixException ex) {
			// Oh well, no prefix then.
		}
	}

	private Key findBestKey(TableDef table) {
		if (table.getPrimaryKey() != null) {
			if (!isExcluded(table, table.getPrimaryKey(), true)) {
				return table.getPrimaryKey();
			}
		}
		if (!useUniqueKeysAsEntityID) return null;
		for (Key uniqueKey: table.getUniqueKeys()) {
			if (!isExcluded(table, uniqueKey, true)) {
				return uniqueKey;
			}
		}
		return null;
	}
	
	private boolean isExcluded(TableDef table, Key columns, boolean requireDistinct) {
		for (Identifier column: columns) {
			if (isExcluded(table, column, requireDistinct)) return true;
		}
		return false;
	}
	
	private boolean isExcluded(TableDef table, Identifier column, boolean requireDistinct) {
		if (!filter.matches(table.getName(), column)) return true;
		DataType type = table.getColumnDef(column).getDataType();
		return type == null || type.isUnsupported() || (requireDistinct && !type.supportsDistinct());
	}

	private Key makeKey(TableDef table)
	{
		ArrayList<Identifier> temp=new ArrayList<Identifier>();
		for (Identifier partKey: table.getColumnNames()) {
			if(!partKey.getName().equalsIgnoreCase("geom"))
			{
				temp.add(partKey);
			}
		}
		return Key.createFromIdentifiers(temp);
	}
	/**
	 * A table T is considered to be a link table if it has exactly two
	 * foreign key constraints, and the constraints reference other
	 * tables (not T), and the constraints cover all columns of T,
	 * and there are no foreign keys from other tables pointing to this table
	 */
	private boolean isLinkTable(TableDef table) {
		if (table.getForeignKeys().size() != 2) return false;
		if (sqlConnection.isReferencedByForeignKey(table.getName())) return false;
		List<Identifier> columns = new ArrayList<Identifier>();
		for (ColumnDef qualified: table.getColumns()) {
			columns.add(qualified.getName());
		}
		for (ForeignKey foreignKey: table.getForeignKeys()) {
			if (foreignKey.getReferencedTable().equals(table.getName())) return false;
			columns.removeAll(foreignKey.getLocalColumns().getColumns());
		}
		return columns.isEmpty();
	}

	/**
	 * @return <code>true</code> iff the table contains this column as a local column in a foreign key
	 */
	private boolean isInForeignKey(Identifier column, TableDef table) {
		for (ForeignKey fk: table.getForeignKeys()) {
			if (fk.getLocalColumns().contains(column)) return true;
		}
		return false;
	}
	
	public static String dropTrailingHash(String uri) {
		if (!uri.endsWith("#")) {
			return uri;
		}
		return uri.substring(0, uri.length() - 1);
	}
}
