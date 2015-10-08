package eu.linkedeodata.geotriples;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.UnsupportedDataTypeException;

import org.apache.log4j.Logger;
import org.d2rq.db.schema.ColumnDef;
import org.d2rq.db.schema.ForeignKey;
import org.d2rq.db.schema.Identifier;
import org.d2rq.db.schema.Key;
import org.d2rq.db.schema.TableDef;
import org.d2rq.db.schema.TableName;
import org.d2rq.db.types.DataType;
import org.d2rq.db.vendor.Vendor;
import org.d2rq.lang.Microsyntax;
import org.d2rq.mapgen.Filter;
import org.d2rq.mapgen.R2RMLTarget;
import org.d2rq.mapgen.Target;
import org.d2rq.r2rml.ColumnNameR2RML;
import org.d2rq.r2rml.ConstantIRI;
import org.d2rq.r2rml.TermMap;
import org.d2rq.r2rml.TermMap.ColumnValuedTermMap;
import org.d2rq.values.TemplateValueMaker;
import org.d2rq.vocab.GEOMETRY_FUNCTIONS;
import org.d2rq.vocab.GEOMETRY_FUNCTIONS.GEOMETRY_FUNCTIONS_DATATYPES;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.PrefixMapping.IllegalPrefixException;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

import eu.linkedeodata.geotriples.gui.ColumnReceipt;
import eu.linkedeodata.geotriples.gui.RecipeMapping;

/**
 * Generates a D2RQ mapping by introspecting a database schema. Result is
 * available as a high-quality Turtle serialization, or as a parsed model.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class GeneralMappingGenerator {
	private final static Logger log = Logger
			.getLogger(GeneralMappingGenerator.class);

	private final GeneralConnection connection;
	private final List<TableName> tablesWithoutUniqueKey = new ArrayList<TableName>();
	private Filter filter = Filter.ALL;
	private boolean generateLabelBridges = false;
	private boolean handleLinkTables = true;
	private boolean skipForeignKeyTargetColumns = true;
	private boolean useUniqueKeysAsEntityID = true;
	private boolean suppressWarnings = false;
	protected GeneralMappingStyle style;
	protected boolean finished = false;
	protected final PrefixMapping prefixes = new PrefixMappingImpl();
	protected Target target;
	protected boolean serveVocabulary = true;
	protected boolean generateDefinitionLabels = false;
	protected boolean generateClasses = true;
	protected TableDef thematicLogicalTable = null;
	protected TableDef geometricLogicalTable = null;

	public GeneralMappingGenerator(GeneralMappingStyle style,
			GeneralConnection connection) {
		this.style = style;
		this.connection = connection;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public void setStartupSQLScript(URI uri) {
	}

	/**
	 * @param flag
	 *            Generate an rdfs:label property bridge based on the PK?
	 */
	public void setGenerateLabelBridges(boolean flag) {
		this.generateLabelBridges = flag;
	}

	/**
	 * @param flag
	 *            Add <code>rdfs:label</code>s to auto-generated classes and
	 *            properties?
	 */
	public void setGenerateDefinitionLabels(boolean flag) {
		this.generateDefinitionLabels = flag;
	}

	/**
	 * @param flag
	 *            Generate a d2rq:class for every class map?
	 */
	public void setGenerateClasses(boolean flag) {
		this.generateClasses = flag;
	}

	/**
	 * @param flag
	 *            Handle Link Tables as properties (true) or normal tables
	 *            (false)
	 */
	public void setHandleLinkTables(boolean flag) {
		this.handleLinkTables = flag;
	}

	/**
	 * @param flag
	 *            Value for d2rq:serveVocabulary in map:Configuration
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

	public void generate(Target generationTarget, RecipeMapping recipe)
			throws Exception {
		if (finished)
			return;
		finished = true;
		target = generationTarget;
		target.init(style.getBaseIRI(), style.getGeneratedOntologyResource(),
				serveVocabulary, generateDefinitionLabels);
		for (String prefix : style.getPrefixes().getNsPrefixMap().keySet()) {
			target.addPrefix(prefix,
					style.getPrefixes().getNsPrefixMap().get(prefix));
			prefixes.setNsPrefix(prefix, style.getPrefixes().getNsPrefixMap()
					.get(prefix));
			// System.out.println(prefix + " - " +
			// style.getPrefixes().getNsPrefixMap().get(prefix));
		}
		List<TableName> tableNames = new ArrayList<TableName>();
		List<TableDef> tableDefs = new ArrayList<TableDef>();
		for (TableName tableName : connection.getTableNames()) {
			if (!filter.matches(tableName)) {
				log.info("Skipping table " + tableName);
				continue;
			}
			tableNames.add(tableName);
			TableDef tableDef = connection.getTable(tableName)
					.getTableDefinition();
			tableDefs.add(tableDef);

		}
		log.info("Filter '" + filter + "' matches " + tableDefs.size()
				+ " total tables");
		for (TableDef table : tableDefs) {
			processGeometryTableD21(
					TableDefUtils.generateVirtualGeometryTable(table), recipe);
			// processGeometryTableD21(table);

			processTable(table, recipe);

			// processGeometryTable(TableDefUtils.generateVirtualGeometryTable(table));
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

	private void processGeometryTableD21(TableDef table, RecipeMapping recipe) {
		if (Config.VOCABULARY.equals("stRDF")) {
			return;
		}
		if(recipe!=null && ! recipe.existGeometryTable())
		{
			return; //no geometry table exist , maybe user deleted this from gui
		}
		TableName tableName = table.getName();
		geometricLogicalTable = table;
		if (handleLinkTables && isLinkTable(table)) {
			Iterator<ForeignKey> it = table.getForeignKeys().iterator();
			ForeignKey fk1 = it.next();
			ForeignKey fk2 = it.next();
			TableName referencedTable1 = fk1.getReferencedTable();
			TableName referencedTable2 = fk2.getReferencedTable();
			if (!filter.matches(referencedTable1)
					|| !filter.matchesAll(referencedTable1,
							fk1.getLocalColumns())
					|| !filter.matchesAll(referencedTable1,
							fk1.getReferencedColumns())
					|| !filter.matches(referencedTable2)
					|| !filter.matchesAll(referencedTable2,
							fk2.getLocalColumns())
					|| !filter.matchesAll(referencedTable2,
							fk2.getReferencedColumns())) {
				log.info("Skipping link table " + tableName);
				return;
			}
			target.generateLinkProperty(style.getLinkProperty(tableName),
					tableName, fk1, fk2);
		} else {
			Resource class_ = generateClasses ? ((recipe != null)?style.getStringClass(recipe.getClassGeometry() ) : (style.getTableClass(tableName)
		)): null;
			class_ = style.getTableClass(tableName);
			TemplateValueMaker iriTemplate = null;
			List<Identifier> blankNodeColumns = null;
			Key key = findBestKey(table);
			
			if (key == null) {
				List<ColumnDef> filteredColumns = new ArrayList<ColumnDef>();
				for (ColumnDef columnDef : table.getColumns()) {
					if (filter.matches(tableName, columnDef.getName())) {
						filteredColumns.add(columnDef);
					}
				}
				if (style.getEntityPseudoKeyColumns(filteredColumns) == null) {
					tablesWithoutUniqueKey.add(tableName);
					iriTemplate = style.getGeometryIRITemplate(table, null);
				} else {
					blankNodeColumns = style
							.getEntityPseudoKeyColumns(filteredColumns);
				}
			} else {
				iriTemplate = style.getGeometryIRITemplate(table, key);
			}
			target.generateEntities(class_, tableName, iriTemplate,
					blankNodeColumns, true);
			if (class_ != null) {
				if (tableName.getSchema() != null) {
					tryRegisterPrefix(tableName.getSchema().getName()
							.toLowerCase(), class_.getNameSpace());
				}
				if (tableName.getCatalog() != null) {
					tryRegisterPrefix(tableName.getCatalog().getName()
							.toLowerCase(), class_.getNameSpace());
				}
			}
			if (generateLabelBridges && key != null) {
				target.generateEntityLabels(
						style.getEntityLabelTemplate(tableName, key), tableName);
			}
			// (now) don't ignore the primary key from the mapping

			// DataType type=new SQLExactNumeric("Int", Types.INTEGER, false);
			
			Map<String, List<ColumnReceipt>> geometryrecipe = (recipe == null) ? null
					: recipe.getReceiptGeometry();
			if (geometryrecipe != null) {
				for (String columnshp : geometryrecipe.keySet()) {
					for (ColumnReceipt column : geometryrecipe.get(columnshp)) {
						//don't care for datatype since we only support transformations!!!
						Property property = null;
						
							
						property = style.getCustomColumnProperty(tableName,
								Identifier.createDelimited(column.getPredicate()),true);
						
						
						ArrayList<TermMap> argumentMap=new ArrayList<TermMap>();
						argumentMap.add(createTermMap(Identifier.createDelimited(column.getColumnName())));
						target.generateTransformationProperty(property, tableName, GEOMETRY_FUNCTIONS.getGeometryFunctionOf(column.getTransformation()), argumentMap ,GEOMETRY_FUNCTIONS_DATATYPES.getDataTypeOf(column.getTransformation()));
						
						
//						target.generateGeometryColumnProperty(property, tableName,
//								Identifier.createDelimited(column.getColumnName()),
//								GEOMETRY_FUNCTIONS_DATATYPES.getDataTypeOf(column.getTransformation()),
//								GEOMETRY_FUNCTIONS.getGeometryFunctionOf(column.getTransformation()));

						tryRegisterPrefix(tableName.getTable().getName()
								.toLowerCase(), property.getNameSpace());
					}
				
				}
			}
			else
			{
			
			
			
			
			
			Property property = null;

			property = style.getCustomColumnProperty(tableName,
					Identifier.createDelimited("asWKT"),true);
			ArrayList<TermMap> argumentMap=new ArrayList<TermMap>();
			argumentMap.add(createTermMap(Identifier.createDelimited("the_geom")));
			target.generateTransformationProperty(property, tableName, GEOMETRY_FUNCTIONS.asWKT, argumentMap ,GEOMETRY_FUNCTIONS_DATATYPES.asWKT);
			
//			target.generateGeometryColumnProperty(property, tableName,
//					Identifier.createDelimited("the_geom"),
//					GEOMETRY_FUNCTIONS_DATATYPES.asWKT,
//					GEOMETRY_FUNCTIONS.asWKT);

			/*property = style.getCustomColumnProperty(tableName,
					Identifier.createDelimited("hasSerialization"),true);
			argumentMap=new ArrayList<TermMap>();
			argumentMap.add(createTermMap(Identifier.createDelimited("the_geom")));
			target.generateTransformationProperty(property, tableName, GEOMETRY_FUNCTIONS.hasSerialization, argumentMap ,GEOMETRY_FUNCTIONS_DATATYPES.hasSerialization);*/
//			target.generateGeometryColumnProperty(property, tableName,
//					Identifier.createDelimited("the_geom"),
//					GEOMETRY_FUNCTIONS_DATATYPES.hasSerialization,
//					GEOMETRY_FUNCTIONS.hasSerialization);

//			property = style.getCustomColumnProperty(tableName,
//					Identifier.createDelimited("asGML"),true);
//			argumentMap=new ArrayList<TermMap>();
//			argumentMap.add(createTermMap(Identifier.createDelimited("the_geom")));
//			target.generateTransformationProperty(property, tableName, GEOMETRY_FUNCTIONS.asGML, argumentMap ,GEOMETRY_FUNCTIONS_DATATYPES.asGML);
//			target.generateGeometryColumnProperty(property, tableName,
//					Identifier.createDelimited("the_geom"),
//					GEOMETRY_FUNCTIONS_DATATYPES.asGML,
//					GEOMETRY_FUNCTIONS.asGML);

			property = style.getCustomColumnProperty(tableName,
					Identifier.createDelimited("isSimple"),true);
			argumentMap=new ArrayList<TermMap>();
			argumentMap.add(createTermMap(Identifier.createDelimited("the_geom")));
			target.generateTransformationProperty(property, tableName, GEOMETRY_FUNCTIONS.isSimple, argumentMap ,GEOMETRY_FUNCTIONS_DATATYPES.isSimple);
//			target.generateGeometryColumnProperty(property, tableName,
//					Identifier.createDelimited("the_geom"),
//					GEOMETRY_FUNCTIONS_DATATYPES.isSimple,
//					GEOMETRY_FUNCTIONS.isSimple);

			property = style.getCustomColumnProperty(tableName,
					Identifier.createDelimited("isEmpty"),true);
			argumentMap=new ArrayList<TermMap>();
			argumentMap.add(createTermMap(Identifier.createDelimited("the_geom")));
			target.generateTransformationProperty(property, tableName, GEOMETRY_FUNCTIONS.isEmpty, argumentMap ,GEOMETRY_FUNCTIONS_DATATYPES.isEmpty);
//			target.generateGeometryColumnProperty(property, tableName,
//					Identifier.createDelimited("the_geom"),
//					GEOMETRY_FUNCTIONS_DATATYPES.isEmpty,
//					GEOMETRY_FUNCTIONS.isEmpty);

			property = style.getCustomColumnProperty(tableName,
					Identifier.createDelimited("is3D"),true);
			argumentMap=new ArrayList<TermMap>();
			argumentMap.add(createTermMap(Identifier.createDelimited("the_geom")));
			target.generateTransformationProperty(property, tableName, GEOMETRY_FUNCTIONS.is3D, argumentMap ,GEOMETRY_FUNCTIONS_DATATYPES.is3D);
//			target.generateGeometryColumnProperty(property, tableName,
//					Identifier.createDelimited("the_geom"),
//					GEOMETRY_FUNCTIONS_DATATYPES.is3D, GEOMETRY_FUNCTIONS.is3D);

			property = style.getCustomColumnProperty(tableName,
					Identifier.createDelimited("spatialDimension"),true);
			argumentMap=new ArrayList<TermMap>();
			argumentMap.add(createTermMap(Identifier.createDelimited("the_geom")));
			target.generateTransformationProperty(property, tableName, GEOMETRY_FUNCTIONS.spatialDimension, argumentMap ,GEOMETRY_FUNCTIONS_DATATYPES.spatialDimension);
//			target.generateGeometryColumnProperty(property, tableName,
//					Identifier.createDelimited("the_geom"),
//					GEOMETRY_FUNCTIONS_DATATYPES.spatialDimension,
//					GEOMETRY_FUNCTIONS.spatialDimension);

			property = style.getCustomColumnProperty(tableName,
					Identifier.createDelimited("dimension"),true);
			argumentMap=new ArrayList<TermMap>();
			argumentMap.add(createTermMap(Identifier.createDelimited("the_geom")));
			target.generateTransformationProperty(property, tableName, GEOMETRY_FUNCTIONS.dimension, argumentMap ,GEOMETRY_FUNCTIONS_DATATYPES.dimension);
//			target.generateGeometryColumnProperty(property, tableName,
//					Identifier.createDelimited("the_geom"),
//					GEOMETRY_FUNCTIONS_DATATYPES.dimension,
//					GEOMETRY_FUNCTIONS.dimension);

			property = style.getCustomColumnProperty(tableName,
					Identifier.createDelimited("coordinateDimension"),true);
			argumentMap=new ArrayList<TermMap>();
			argumentMap.add(createTermMap(Identifier.createDelimited("the_geom")));
			target.generateTransformationProperty(property, tableName, GEOMETRY_FUNCTIONS.coordinateDimension, argumentMap ,GEOMETRY_FUNCTIONS_DATATYPES.coordinateDimension);
//			target.generateGeometryColumnProperty(property, tableName,
//					Identifier.createDelimited("the_geom"),
//					GEOMETRY_FUNCTIONS_DATATYPES.coordinateDimension,
//					GEOMETRY_FUNCTIONS.coordinateDimension);
			}
//			for (ForeignKey fk : table.getForeignKeys()) {
//				if (!filter.matches(fk.getReferencedTable())
//						|| !filter.matchesAll(tableName, fk.getLocalColumns())
//						|| !filter.matchesAll(fk.getReferencedTable(),
//								fk.getReferencedColumns())) {
//					log.info("Skipping foreign key: " + fk);
//					continue;
//				}
//				target.generateRefProperty(
//						style.getForeignKeyProperty(tableName, fk), tableName,
//						fk);
//			}
		}
	}

	private void processTable(TableDef table, RecipeMapping recipe) {
		if(recipe!=null && ! recipe.existThematicTable())
		{
			return; //no thematic table exist , maybe user deleted this from gui
		}
		TableName tableName = table.getName();
		thematicLogicalTable = table;
		if (handleLinkTables && isLinkTable(table)) {
			Iterator<ForeignKey> it = table.getForeignKeys().iterator();
			ForeignKey fk1 = it.next();
			ForeignKey fk2 = it.next();
			TableName referencedTable1 = fk1.getReferencedTable();
			TableName referencedTable2 = fk2.getReferencedTable();
			if (!filter.matches(referencedTable1)
					|| !filter.matchesAll(referencedTable1,
							fk1.getLocalColumns())
					|| !filter.matchesAll(referencedTable1,
							fk1.getReferencedColumns())
					|| !filter.matches(referencedTable2)
					|| !filter.matchesAll(referencedTable2,
							fk2.getLocalColumns())
					|| !filter.matchesAll(referencedTable2,
							fk2.getReferencedColumns())) {
				log.info("Skipping link table " + tableName);
				return;
			}
			target.generateLinkProperty(style.getLinkProperty(tableName),
					tableName, fk1, fk2);
		} else {
			Resource class_ = generateClasses ? ((recipe != null)?style.getStringClass(recipe.getClassThematic() ) : (style.getTableClass(tableName)
					)): null;
			TemplateValueMaker iriTemplate = null;
			@SuppressWarnings("unused")
			TemplateValueMaker hasGeometryTemplate = null; // d2.1
			List<Identifier> blankNodeColumns = null;
			Key key = findBestKey(table);

			if (key == null) {
				List<ColumnDef> filteredColumns = new ArrayList<ColumnDef>();
				for (ColumnDef columnDef : table.getColumns()) {
					if (filter.matches(tableName, columnDef.getName())) {
						filteredColumns.add(columnDef);
					}
				}
				if (style.getEntityPseudoKeyColumns(filteredColumns) == null) {
					tablesWithoutUniqueKey.add(tableName);
					iriTemplate = style.getEntityIRITemplate(table, null);
				} else {
					blankNodeColumns = style
							.getEntityPseudoKeyColumns(filteredColumns);
				}
			} else {
				iriTemplate = style.getEntityIRITemplate(table, key);
			}
			hasGeometryTemplate = style.getGeometryIRITemplate(table, key);
			target.generateEntities(class_, tableName, iriTemplate,
					blankNodeColumns, false);
			if (class_ != null) {
				if (tableName.getSchema() != null) {
					tryRegisterPrefix(tableName.getSchema().getName()
							.toLowerCase(), class_.getNameSpace());
				}
				if (tableName.getCatalog() != null) {
					tryRegisterPrefix(tableName.getCatalog().getName()
							.toLowerCase(), class_.getNameSpace());
				}
			}
			if (generateLabelBridges && key != null) {
				target.generateEntityLabels(
						style.getEntityLabelTemplate(tableName, key), tableName);
			}
			Map<String, List<ColumnReceipt>> thematicreceipt = (recipe == null) ? null
					: recipe.getReceiptThematic();
			for (Identifier column : table.getColumnNames()) {
				if (column.getCanonicalName().equals("the_geom")) {
					continue;
				}

				if (recipe != null) {
					if (!thematicreceipt.containsKey(column.getCanonicalName())) {
						continue;
					}
				}
				if (skipForeignKeyTargetColumns
						&& isInForeignKey(column, table))
					continue;
				if (!filter.matches(tableName, column)) {
					log.info("Skipping filtered column " + column);
					continue;
				}

				if (thematicreceipt != null) {
					for (ColumnReceipt columnshp : thematicreceipt.get(column
							.getCanonicalName())) {
						DataType type = null;
						try {
							type = TableDefUtils
									.TranslateDataTypeToSQLType(columnshp
											.getDataType());
						} catch (UnsupportedDataTypeException e) {
							e.printStackTrace();
						}
						if (type == null) {
							String message = "The datatype is unknown to D2RQ.\n";
							message += "You can override the column's datatype using d2rq:xxxColumn and add a property bridge.";
							if (!suppressWarnings) {
								log.warn(message);
							}
							target.skipColumn(tableName, column, message);
							continue;
						}
						if (type.isUnsupported()) {
							String message = "The datatype " + type
									+ " cannot be mapped to RDF.";
							if (!suppressWarnings) {
								log.warn(message);
							}
							target.skipColumn(tableName, column, message);
							continue;
						}
						Property property = null;
						if (column.getName().equals("gid")) {
							if (Config.VOCABULARY.equals("stRDF")) {
								property = style.getStRDFColumnProperty(tableName,
										Identifier.createDelimited("hasGeometry"),true);
								List<TermMap> argumentMap=new ArrayList<TermMap>();
								argumentMap.add(createTermMap(Identifier.createDelimited("the_geom")));
								target.generateTransformationProperty(property, tableName, 
										GEOMETRY_FUNCTIONS.strdfWKT, argumentMap ,
										GEOMETRY_FUNCTIONS_DATATYPES.strdfWKT);
								continue;
							}
							//otherwise we want geosparql which means join conditions between the two virtual tables
//							Key geoKey = Key.create(Identifier
//									.createDelimited("gid"));
//							Key themKey = Key.create(Identifier
//									.createDelimited("gid"));
//							((GeneralR2RMLTarget) target)
//									.generateLinkPredicateObjectMap(
//											style.getLinkGeometryPropetry(null),
//											thematicLogicalTable.getName(),
//											geometricLogicalTable.getName(),
//											themKey, geoKey);
							((GeneralR2RMLTarget)target).generateTemplatePredicateObjectMap(style.getLinkGeometryPropetry(null),style.getGeometryIRITemplate(table, key) , tableName);
							// d2.1 \dimis thewrw oti afto (to join) einai pio
							// diskolo kai de xreiazetai kai anyhow den doulevei
							// akrivws ekso apo to for ftiaxnw ena template kai
							// linetai to themataki
							continue;
						} else {

							property = style.getCustomColumnProperty(tableName,
									Identifier.createUndelimited(columnshp
											.getPredicate()),false);
							target.generateColumnProperty(property, tableName,
									column, type);
						}

						tryRegisterPrefix(tableName.getTable().getName()
								.toLowerCase(), property.getNameSpace());
					}
				} else {
					DataType type = null;
					type = table.getColumnDef(column).getDataType();
					if (type == null) {
						String message = "The datatype is unknown to D2RQ.\n";
						message += "You can override the column's datatype using d2rq:xxxColumn and add a property bridge.";
						if (!suppressWarnings) {
							log.warn(message);
						}
						target.skipColumn(tableName, column, message);
						continue;
					}
					if (type.isUnsupported()) {
						String message = "The datatype " + type
								+ " cannot be mapped to RDF.";
						if (!suppressWarnings) {
							log.warn(message);
						}
						target.skipColumn(tableName, column, message);
						continue;
					}
					Property property = null;
					if (column.getName().equals("gid")) {
						if (Config.VOCABULARY.equals("stRDF")) {
							property = style.getStRDFColumnProperty(tableName,
									Identifier.createDelimited("hasGeometry"),true);
							List<TermMap> argumentMap=new ArrayList<TermMap>();
							argumentMap.add(createTermMap(Identifier.createDelimited("the_geom")));
							target.generateTransformationProperty(property, tableName, 
									GEOMETRY_FUNCTIONS.strdfWKT, argumentMap ,
									GEOMETRY_FUNCTIONS_DATATYPES.strdfWKT);
							continue;
						}
						//otherwise we want geosparql which means join conditions between the two virtual tables
//						Key geoKey = Key.create(Identifier
//								.createDelimited("gid"));
//						Key themKey = Key.create(Identifier
//								.createDelimited("gid"));
//						((GeneralR2RMLTarget) target)
//								.generateLinkPredicateObjectMap(
//										style.getLinkGeometryPropetry(null),
//										thematicLogicalTable.getName(),
//										geometricLogicalTable.getName(),
//										themKey, geoKey);
						((GeneralR2RMLTarget)target).generateTemplatePredicateObjectMap(style.getLinkGeometryPropetry(null),style.getGeometryIRITemplate(table, key) , tableName);
						// d2.1 \dimis thewrw oti afto (to join) einai pio
						// diskolo kai de xreiazetai kai anyhow den doulevei
						// akrivws ekso apo to for ftiaxnw ena template kai
						// linetai to themataki
						continue;
					} else {

						property = style.getColumnProperty(tableName, column);
						target.generateColumnProperty(property, tableName,
								column, type);
					}

					tryRegisterPrefix(tableName.getTable().getName()
							.toLowerCase(), property.getNameSpace());
				}
			}
			// Property property = style.getLinkGeometryPropetry(null); //d2.1
			// hasGeometry THE dimis way
			// target.generateHasGeometryProperty(property, tableName,
			// hasGeometryTemplate); THE dimis way
			for (ForeignKey fk : table.getForeignKeys()) {
				if (!filter.matches(fk.getReferencedTable())
						|| !filter.matchesAll(tableName, fk.getLocalColumns())
						|| !filter.matchesAll(fk.getReferencedTable(),
								fk.getReferencedColumns())) {
					log.info("Skipping foreign key: " + fk);
					continue;
				}
				target.generateRefProperty(
						style.getForeignKeyProperty(tableName, fk), tableName,
						fk);
			}
		}
	}

	private void tryRegisterPrefix(String prefix, String uri) {
		if (prefixes.getNsPrefixMap().containsKey(prefix))
			return;
		if (prefixes.getNsPrefixMap().containsValue(uri))
			return;
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
		if (!useUniqueKeysAsEntityID)
			return null;
		for (Key uniqueKey : table.getUniqueKeys()) {
			if (!isExcluded(table, uniqueKey, true)) {
				return uniqueKey;
			}
		}
		return null;
	}

	private boolean isExcluded(TableDef table, Key columns,
			boolean requireDistinct) {
		for (Identifier column : columns) {
			if (isExcluded(table, column, requireDistinct))
				return true;
		}
		return false;
	}

	private boolean isExcluded(TableDef table, Identifier column,
			boolean requireDistinct) {
		if (!filter.matches(table.getName(), column))
			return true;
		DataType type = table.getColumnDef(column).getDataType();
		return type == null || type.isUnsupported()
				|| (requireDistinct && !type.supportsDistinct());
	}

	/**
	 * A table T is considered to be a link table if it has exactly two foreign
	 * key constraints, and the constraints reference other tables (not T), and
	 * the constraints cover all columns of T, and there are no foreign keys
	 * from other tables pointing to this table
	 */
	private boolean isLinkTable(TableDef table) {
		if (table.getForeignKeys().size() != 2)
			return false;
		if (connection.isReferencedByForeignKey(table.getName()))
			return false;
		List<Identifier> columns = new ArrayList<Identifier>();
		for (ColumnDef qualified : table.getColumns()) {
			columns.add(qualified.getName());
		}
		for (ForeignKey foreignKey : table.getForeignKeys()) {
			if (foreignKey.getReferencedTable().equals(table.getName()))
				return false;
			columns.removeAll(foreignKey.getLocalColumns().getColumns());
		}
		return columns.isEmpty();
	}

	/**
	 * @return <code>true</code> iff the table contains this column as a local
	 *         column in a foreign key
	 */
	private boolean isInForeignKey(Identifier column, TableDef table) {
		for (ForeignKey fk : table.getForeignKeys()) {
			if (fk.getLocalColumns().contains(column))
				return true;
		}
		return false;
	}

	public static String dropTrailingHash(String uri) {
		if (!uri.endsWith("#")) {
			return uri;
		}
		return uri.substring(0, uri.length() - 1);
	}
	
	private TermMap createTermMap(Identifier column) {
		ColumnValuedTermMap result = new ColumnValuedTermMap();
		result.setColumnName(ColumnNameR2RML.create(column, Vendor.MySQL));
		//result.setDatatype(ConstantIRI.create(dType.rdfType()));
		return result;
	}
}
