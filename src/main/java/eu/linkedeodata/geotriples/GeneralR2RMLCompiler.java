package eu.linkedeodata.geotriples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.d2rq.D2RQException;
import org.d2rq.D2RQOptions;
import org.d2rq.algebra.DownloadRelation;
import org.d2rq.db.op.AliasOp;
import org.d2rq.db.op.DatabaseOp;
import org.d2rq.db.op.InnerJoinOp;
import org.d2rq.db.op.NamedOp;
import org.d2rq.db.op.ProjectOp;
import org.d2rq.db.op.ProjectionSpec;
import org.d2rq.db.op.SQLOp;
import org.d2rq.db.schema.ColumnName;
import org.d2rq.db.schema.Identifier;
import org.d2rq.db.schema.Identifier.IdentifierParseException;
import org.d2rq.db.schema.Key;
import org.d2rq.db.types.DataType;
import org.d2rq.db.types.DataType.GenericType;
import org.d2rq.db.types.SQLCharacterString;
import org.d2rq.db.vendor.Vendor;
import org.d2rq.nodes.FixedNodeMaker;
import org.d2rq.nodes.NodeMaker;
import org.d2rq.nodes.TypedNodeMaker;
import org.d2rq.nodes.TypedNodeMaker.NodeType;
import org.d2rq.nodes.TypedNodeTransformationMaker;
import org.d2rq.nodes.TypedNodeTransformationMakerList;
import org.d2rq.r2rml.ConstantIRI;
import org.d2rq.r2rml.ConstantShortcut;
import org.d2rq.r2rml.GeometryFunction;
import org.d2rq.r2rml.GeometryParametersTerms;
import org.d2rq.r2rml.Join;
import org.d2rq.r2rml.LogicalTable.BaseTableOrView;
import org.d2rq.r2rml.LogicalTable.R2RMLView;
import org.d2rq.r2rml.Mapping;
import org.d2rq.r2rml.PredicateObjectMap;
import org.d2rq.r2rml.R2RMLReader;
import org.d2rq.r2rml.RDFComparator;
import org.d2rq.r2rml.ReferencingGeometryObjectMap;
import org.d2rq.r2rml.ReferencingObjectMap;
import org.d2rq.r2rml.StringTemplate;
import org.d2rq.r2rml.TermMap;
import org.d2rq.r2rml.TermMap.ColumnOrTemplateValuedTermMap;
import org.d2rq.r2rml.TermMap.ColumnValuedTermMap;
import org.d2rq.r2rml.TermMap.ConstantValuedTermMap;
import org.d2rq.r2rml.TermMap.Position;
import org.d2rq.r2rml.TermMap.TemplateValuedTermMap;
import org.d2rq.r2rml.TermMap.TermType;
import org.d2rq.r2rml.TermMap.TransformationValuedTermMap;
import org.d2rq.r2rml.TriplesMap;
import org.d2rq.values.BaseIRIValueMaker;
import org.d2rq.values.ColumnValueMaker;
import org.d2rq.values.TemplateValueMaker;
import org.d2rq.values.TemplateValueMaker.ColumnFunction;
import org.d2rq.values.TransformationValueMaker;
import org.d2rq.values.ValueMaker;
import org.hamcrest.core.IsInstanceOf;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

public class GeneralR2RMLCompiler implements GeneralCompiledMapping{
	public final static Log log = LogFactory.getLog(GeneralR2RMLCompiler.class);

	private final Mapping mapping;
	private final GeneralConnection connection;
	private final Collection<GeneralTripleRelation> tripleRelations = 
		new ArrayList<GeneralTripleRelation>();
	private final Map<String,GeneralResourceCollection> resourceCollections =
		new HashMap<String,GeneralResourceCollection>();
	private Resource currentTriplesMapResource;
	private final Collection<GeneralTripleRelation> currentTripleRelations = 
		new ArrayList<GeneralTripleRelation>();
	//private final Collection<>
	private NamedOp currentTable = null;
	private NodeMaker subjectMaker = null;
	private boolean fastMode = false;
	private boolean compiled = false;
	
	public GeneralR2RMLCompiler(Mapping mapping, GeneralConnection generalConnection) {
		this.mapping = mapping;
		this.connection = generalConnection;
	}
	//TODO CONNECT DISCONNECT IF NEEDED
	public void connect() {
		//shpConnection.connection();
	}
	
	public void close() {
		//shpConnection.close();
	}
	
	public PrefixMapping getPrefixes() {
		return mapping.getPrefixes();
	}
	
	public Collection<GeneralTripleRelation> getTripleRelations() {
		checkCompiled();
		return tripleRelations;
	}
	
	public Collection<? extends DownloadRelation> getDownloadRelations() {
		return Collections.emptySet();
	}


	public List<String> getResourceCollectionNames() {
		checkCompiled();
		List<String> result = new ArrayList<String>(resourceCollections.keySet());
		Collections.sort(result);
		return result;
	}

	public List<String> getResourceCollectionNames(Node forNode) {
		checkCompiled();
		List<String> result = new ArrayList<String>();
		for (String name: resourceCollections.keySet()) {
			if (resourceCollections.get(name).mayContain(forNode)) {
				result.add(name);
			}
		}
		Collections.sort(result);
		return result;
	}

	public GeneralResourceCollection getResourceCollection(String name) {
		checkCompiled();
		return resourceCollections.get(name);
	}

	public Graph getAdditionalTriples() {
		return GraphFactory.createPlainGraph();
	}

	public void setFastMode(boolean fastMode) {
		this.fastMode = fastMode;
	}
	
	public Context getContext() {
		return D2RQOptions.getContext(fastMode);
	}
	
	private void checkCompiled() {
		if (!compiled) {
			compiled = true;
			visitComponent(mapping);
		}
	}
	//d2.1 
	static int counter=0;
	@SuppressWarnings("unused")
	private void visitComponent(Mapping mapping) {
		for (Resource r: mapping.triplesMaps().resources()) {
			log.info("Processing logical table: " + r.getLocalName());
			currentTriplesMapResource = r;
			if(r==null)
			{
				System.out.println("oh");
			}
			log.info("Namespace: "+r.getNameSpace());
			visitComponent(mapping.triplesMaps().get(r));
		}
		/*for (GeometryFunction poMap: mapping.gfunctions().components()) {
			//System.out.println(poMap.getPredicateMaps());
			//System.out.println(poMap.getType());
			//System.out.println(poMap.getPredicates());
			//System.out.println(poMap.getGraphMaps());
			//System.out.println(poMap.getObjectMaps());

			visitComponent(poMap);
		}*/
	}
	
	private TermMap getSubjectMap(TriplesMap triplesMap) {
		return triplesMap.getSubject() == null
				? mapping.termMaps().get(triplesMap.getSubjectMap())
				: triplesMap.getSubject().asTermMap();
	}
	
	private void visitComponent(TriplesMap triplesMap) {
		currentTable = createTabular(triplesMap);
		TermMap subjectMap = getSubjectMap(triplesMap);
		if(subjectMap==null)
		{
			System.out.println(triplesMap.getType());
		}
		subjectMaker = createNodeMaker(
				subjectMap, Position.SUBJECT_MAP, currentTable);
		
		//System.out.println(mapping.predicateObjectMaps().size());

		for (PredicateObjectMap poMap: mapping.predicateObjectMaps().getAll(
				triplesMap.getPredicateObjectMaps())) {
			//System.out.println(poMap.getPredicateMaps());
			//System.out.println(poMap.getType());
			//System.out.println(poMap.getPredicates());
			//System.out.println(poMap.getGraphMaps());
			//System.out.println(poMap.getObjectMaps());

			visitComponent(poMap);
		}
		for (ConstantIRI classIRI: subjectMap.getClasses()) {
			currentTripleRelations.add(createTripleRelation(
					new FixedNodeMaker(RDF.type.asNode()),
					new FixedNodeMaker(Node.createURI(classIRI.toString())), 
					currentTable));
		}
		
		
		
		if (subjectMaker == null) {
			// TODO: Remove this check once we handle all term map types
			log.warn("null subject map");
		} else {
			resourceCollections.put(getTriplesMapName(), 
					new GeneralResourceCollection(this, connection, subjectMaker, 
							createTabular(currentTable, subjectMaker.projectionSpecs()), 
							currentTripleRelations));
		}
		tripleRelations.addAll(currentTripleRelations);
		currentTripleRelations.clear();
	}
	
	private Key getChildKey(List<Join> joins) {
		List<Identifier> result = new ArrayList<Identifier>();
		for (Join join: joins) {
			result.add(join.getChild().asIdentifier(Vendor.MySQL));
		}
		return Key.createFromIdentifiers(result);
	}
	
	private Key getParentKey(List<Join> joins) {
		List<Identifier> result = new ArrayList<Identifier>();
		for (Join join: joins) {
			result.add(join.getParent().asIdentifier(Vendor.MySQL));
		}
		return Key.createFromIdentifiers(result);
	}
	
	/*private void visitComponent(GeometryFunction poMap) {
		Collection<TermMap> predicateMaps = new ArrayList<TermMap>();
		for (ConstantShortcut predicates: poMap.getPredicates()) {
			predicateMaps.add(predicates.asTermMap());
		}
		predicateMaps.addAll(mapping.termMaps().getAll(poMap.getPredicateMaps()));
		for (TermMap predicateMap: predicateMaps) {
			NodeMaker predicateMaker = createNodeMaker(
					predicateMap, Position.PREDICATE_MAP, currentTable);
			Collection<TermMap> objectMaps = new ArrayList<TermMap>();
			for (ConstantShortcut objects: poMap.getObjects()) {
				objectMaps.add(objects.asTermMap());
			}
			objectMaps.addAll(mapping.termMaps().getAll(poMap.getObjectMaps()));
			for (ReferencingObjectMap refObjectMap: 
					mapping.referencingObjectMaps().getAll(poMap.getObjectMaps())) {
				TriplesMap parentTriplesMap = mapping.triplesMaps().get(refObjectMap.getParentTriplesMap());
				if (refObjectMap.getJoinConditions().isEmpty()) {
					objectMaps.add(getSubjectMap(parentTriplesMap));
				} else {
					NamedOp parentTable = createTabular(parentTriplesMap);
					if (refObjectMap.getParentTriplesMap().equals(currentTriplesMapResource)) {
						parentTable = AliasOp.create(parentTable, "PARENT");
					}
					NodeMaker objectMaker = createNodeMaker(
							getSubjectMap(parentTriplesMap), Position.SUBJECT_MAP, parentTable);
					List<Join> joins = new ArrayList<Join>(
							mapping.joins().getAll(refObjectMap.getJoinConditions()));
					DatabaseOp joinedTables = InnerJoinOp.join(
							currentTable, 
							parentTable, 
							getChildKey(joins),
							getParentKey(joins));
					currentTripleRelations.add(createTripleRelation(
							predicateMaker, objectMaker, joinedTables));
				}
			}
			for (TermMap objectMap: objectMaps) {
				NodeMaker objectMaker = createNodeMaker(
						objectMap, Position.OBJECT_MAP, currentTable);
				currentTripleRelations.add(createTripleRelation(
						predicateMaker, objectMaker, currentTable));
			}
		}
	}*/
	private void visitComponent(PredicateObjectMap poMap) {
		Collection<TermMap> predicateMaps = new ArrayList<TermMap>();
		for (ConstantShortcut predicates: poMap.getPredicates()) {
			predicateMaps.add(predicates.asTermMap());
		}
		predicateMaps.addAll(mapping.termMaps().getAll(poMap.getPredicateMaps()));
		for (TermMap predicateMap: predicateMaps) {
			NodeMaker predicateMaker = createNodeMaker(
					predicateMap, Position.PREDICATE_MAP, currentTable);
			Collection<TermMap> objectMaps = new ArrayList<TermMap>();
			//Collection<GeometryFunction> geometryfunctions = new ArrayList<GeometryFunction>();  //d2.1
			
			for (ConstantShortcut objects: poMap.getObjects()) {
				objectMaps.add(objects.asTermMap());
			}
			objectMaps.addAll(mapping.termMaps().getAll(poMap.getObjectMaps()));
			//Set<GeometryFunction> ll=(Set<GeometryFunction>)poMap.getObjectMaps();
			//geometryfunctions.addAll(mapping.gfunctions().getAll(poMap.getObjectMaps()));
			
			for (ReferencingObjectMap refObjectMap: 
					mapping.referencingObjectMaps().getAll(poMap.getObjectMaps())) {
				TriplesMap parentTriplesMap = mapping.triplesMaps().get(refObjectMap.getParentTriplesMap());
				if (! (refObjectMap instanceof ReferencingGeometryObjectMap))
				{
				if (refObjectMap.getJoinConditions().isEmpty()) {
					objectMaps.add(getSubjectMap(parentTriplesMap));
				} else {
					NamedOp parentTable = createTabular(parentTriplesMap);
					if (refObjectMap.getParentTriplesMap().equals(currentTriplesMapResource)) {
						parentTable = AliasOp.create(parentTable, "PARENT");
					}
					NodeMaker objectMaker = createNodeMaker(
							getSubjectMap(parentTriplesMap), Position.SUBJECT_MAP, parentTable);
					List<Join> joins = new ArrayList<Join>(
							mapping.joins().getAll(refObjectMap.getJoinConditions()));
					DatabaseOp joinedTables = InnerJoinOp.join(
							currentTable, 
							parentTable, 
							getChildKey(joins),
							getParentKey(joins));
					currentTripleRelations.add(createTripleRelation(
							predicateMaker, objectMaker, joinedTables));
				}
				}
				else if (refObjectMap instanceof ReferencingGeometryObjectMap)
				{
					ReferencingGeometryObjectMap rgom=(ReferencingGeometryObjectMap) refObjectMap;
					for (GeometryFunction gg: mapping.gfunctions().getAll( rgom.getGeometryFunctions()))					
					{
						//System.out.println("dimis!");
						//d2.1
						//here happens the transformation
						//NodeMaker objectMaker = createNodeMaker(
						//		getSubjectMap(parentTriplesMap), Position.SUBJECT_MAP, currentTable);
						
						
						@SuppressWarnings("unused")
						Collection<GeometryParametersTerms> gparameters=mapping.gparameters().getAll(gg.getObjectMaps().keySet());
						//GeometryParametersTerms gpt=gparameters.iterator().next();
						//NodeMaker objectMaker = createNodeMaker(
						//		gpt, GeometryParametersTerms.Position.SUBJECT_MAP, currentTable);	
						NodeMaker objectMaker = createNodeMaker(
								gg, GeometryParametersTerms.Position.SUBJECT_MAP, currentTable ,rgom.getDatatype());
						
						
						currentTripleRelations.add(createTripleRelation(
								predicateMaker, objectMaker, currentTable));
					}
				}
			}	
			/*Set<Resource> objmaps=poMap.getObjectMaps();
			for(Resource x:objmaps)
			{
				for(Resource fun:getResources(x,RRX.function))
				{
					GeometryFunction gf = new GeometryFunction();
					
					List<Resource> temp=getResources(fun, RRX.function);
					List<Resource> temp2=getResources(fun, RRX.argumentMap);
					
					gf.setFunction(ConstantIRI.create(getResources(fun, RRX.function).get(0).getURI()));
					
					gf.getObjectMaps().addAll(getResources(fun, RRX.argumentMap));
					mapping.gfunctions().put(fun, gf);
				}
			}*
			/*for (GeometryFunction refObjectMap: 
				mapping.gfunctions().getAll(poMap.getObjectMaps())) {
			System.out.println("dimis");
		}*/
			for (TermMap objectMap: objectMaps) {
				NodeMaker objectMaker = createNodeMaker(
						objectMap, Position.OBJECT_MAP, currentTable);
				currentTripleRelations.add(createTripleRelation(
						predicateMaker, objectMaker, currentTable));
			}
		}
	}
	public List<RDFNode> getRDFNodes(Resource r, Property p, R2RMLReader.NodeType acceptableNodes) {
		List<RDFNode> result = new ArrayList<RDFNode>();
		StmtIterator it = r.listProperties(p);
		while (it.hasNext()) {
			Statement stmt = it.next();
			if (acceptableNodes.isTypeOf(stmt.getObject())) {
				result.add(stmt.getObject());
			} else {
				if (acceptableNodes.coerce(stmt.getObject()) != null) {
					result.add(acceptableNodes.coerce(stmt.getObject()));
				}
			}
		}
		Collections.sort(result, RDFComparator.getRDFNodeComparator());
		return result;
	}
	public List<Resource> getResources(Resource r, Property p, R2RMLReader.NodeType acceptableNodeTypes) {
		List<Resource> result = new ArrayList<Resource>();
		for (RDFNode node: getRDFNodes(r, p, acceptableNodeTypes)) {
			result.add(node.asResource());
		}
		return result;
	}

	public List<Resource> getResources(Resource r, Property p) {
		return getResources(r, p, R2RMLReader.NodeType.IRI_OR_BLANK);
	}
	
	private GeneralTripleRelation createTripleRelation(NodeMaker predicateMaker,
			NodeMaker objectMaker, DatabaseOp baseOp) {
		if (subjectMaker == null || predicateMaker == null || objectMaker == null) {
			// TODO: Remove this check once we handle all term map types
			log.warn("null term map");
			return null;
		}
		Set<ProjectionSpec> columns = new HashSet<ProjectionSpec>();
		columns.addAll(subjectMaker.projectionSpecs());
		columns.addAll(predicateMaker.projectionSpecs());
		columns.addAll(objectMaker.projectionSpecs());
		return new GeneralTripleRelation(connection, createTabular(baseOp, columns), 
				subjectMaker, predicateMaker, objectMaker);
	}
	
	private DatabaseOp createTabular(DatabaseOp baseOp, Set<ProjectionSpec> columns) {
		return ProjectOp.create(baseOp, columns);
	}
	
	private NamedOp createTabular(TriplesMap triplesMap) {
		RelationCompiler compiler = new RelationCompiler();
		triplesMap.accept(compiler);
		return compiler.result;
	}
	
	
	private NodeMaker createNodeMaker(TermMap termMap, Position position, NamedOp table) {
		NodeMakerCompiler compiler = new NodeMakerCompiler(table);
		termMap.acceptAs(compiler, position);
		return compiler.result;
	}
	/*private NodeMaker createNodeMaker(GeometryParametersTerms termMap, GeometryParametersTerms.Position position, NamedOp table) {
		NodeMakerCompiler compiler = new NodeMakerCompiler(table);
		termMap.acceptAs(compiler, position);
		return compiler.result;
	}*/
	private NodeMaker createNodeMaker(GeometryFunction termMap, GeometryParametersTerms.Position position, NamedOp table,ConstantIRI datatype) {
		NodeMakerCompiler compiler = new NodeMakerCompiler(table);
		termMap.acceptAs(compiler, position , datatype);
		return compiler.result;
	}
	/*private NodeMaker createNodeMaker(GeometryFunction geofunction, Position position, NamedOp table) {
		NodeMakerCompiler compiler = new NodeMakerCompiler(table);
		geofunction.acceptAs(compiler, position);
		return compiler.result;
	}*/
	
	private TemplateValueMaker toTemplate(StringTemplate template, TermType termType, NamedOp table) {
		String[] literalParts = template.getLiteralParts().clone();
		if (termType == TermType.IRI && !literalParts[0].matches("[a-zA-Z][a-zA-Z0-9.+-]*:.*")) {
			literalParts[0] = mapping.getBaseIRI() + literalParts[0];
		}
		ColumnName[] qualifiedColumns = new ColumnName[template.getColumnNames().length];
		ColumnFunction[] functions = new ColumnFunction[template.getColumnNames().length];
		try {
			for (int i = 0; i < qualifiedColumns.length; i++) {
				Identifier column = Vendor.MySQL.parseIdentifiers(
						template.getColumnNames()[i], 1, 1)[0];
				
					qualifiedColumns[i] = ColumnName.create(table.getTableName(), column);
				functions[i] = termType == TermType.IRI ? 
						TemplateValueMaker.ENCODE : TemplateValueMaker.IDENTITY;
			}
			return new TemplateValueMaker(literalParts, qualifiedColumns, functions);
		} catch (IdentifierParseException ex) {
			throw new D2RQException(ex.getMessage(), ex, D2RQException.SQL_INVALID_IDENTIFIER);
		}
	}
	
	private String getTriplesMapName() {
		if (currentTriplesMapResource.isAnon()) {
			return currentTriplesMapResource.toString();
		}
		return currentTriplesMapResource.getLocalName();
	}
	
	private class RelationCompiler extends org.d2rq.r2rml.MappingVisitor.TreeWalkerImplementation {
		private NamedOp result;
		private RelationCompiler() {
			super(mapping);
		}
		@Override
		public void visitComponent(BaseTableOrView table) {
			try {
				result = connection.getTable(
						table.getTableName().asQualifiedTableName(Vendor.MySQL));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		@Override
		public void visitComponent(R2RMLView query) {
			String sql = query.getSQLQuery().toString();
			String name = "VIEW" + Integer.toHexString(sql.hashCode());
			SQLOp selectStatement = connection.getSelectStatement(sql);
			result = AliasOp.create(selectStatement, name);
		}
	}
	
	private class NodeMakerCompiler extends org.d2rq.r2rml.MappingVisitor.DoNothingImplementation {
		private NodeMaker result;
		private final NamedOp table;
		private NodeMakerCompiler(NamedOp table) {
			this.table = table;
		}
		@Override
		public void visitComponent(TermMap termMap, Position position) {
			if(termMap instanceof ConstantValuedTermMap)
			{
				TermMap.ConstantValuedTermMap t=(ConstantValuedTermMap) termMap;
				visitComponent(t,position);
			}
			else if(termMap instanceof TemplateValuedTermMap)
			{
				TermMap.TemplateValuedTermMap t=(TemplateValuedTermMap) termMap;
				visitComponent(t,position);
			}
			else if(termMap instanceof ColumnValuedTermMap)
			{
				TermMap.ColumnValuedTermMap t=(ColumnValuedTermMap) termMap;
				visitComponent(t,position);
			}
			else if(termMap instanceof TransformationValuedTermMap)
			{
				TermMap.TransformationValuedTermMap t=(TransformationValuedTermMap) termMap;
				visitComponent(t,position);
			}
			else if(termMap instanceof ColumnOrTemplateValuedTermMap)
			{
				TermMap.ColumnOrTemplateValuedTermMap t=(ColumnOrTemplateValuedTermMap) termMap;
				visitComponent(t,position);
			}
		}
		@Override
		public void visitComponent(ConstantValuedTermMap termMap, Position position) {
			result = new FixedNodeMaker(termMap.getConstant().asNode());
		}
		@Override
		public void visitComponent(ColumnValuedTermMap termMap, Position position) {
			ColumnName qualified = ColumnName.create(table.getTableName(), 
					termMap.getColumnName().asIdentifier(Vendor.MySQL));
			NodeType nodeType = getNodeType(termMap, position, table.getColumnType(qualified));
			ValueMaker baseValueMaker = new ColumnValueMaker(qualified);
			if (nodeType == TypedNodeMaker.URI) {
				baseValueMaker = new BaseIRIValueMaker(mapping.getBaseIRI(), baseValueMaker);
			}
			result = new TypedNodeMaker(nodeType, baseValueMaker);
		}
		
		@Override
		public void visitComponent(TransformationValuedTermMap termMap, Position position) {
			List<TermMap> list=termMap.getTermMaps();
			List<NodeMaker>argumentsNodeMaker = new ArrayList<NodeMaker>();
			List<ValueMaker> argumentsValueMaker =new ArrayList<ValueMaker>();
			for(int i=0;i<list.size();++i)
			{
				visitComponent(list.get(i), position);
				argumentsNodeMaker.add(result);
				argumentsValueMaker.add(result.getValueMaker());
			}
			NodeType nodeType = getNodeType(termMap, position, new SQLCharacterString("text", true));
			ValueMaker baseValueMaker = new TransformationValueMaker(nodeType,argumentsValueMaker,termMap.getFunction(), connection);
			/*if (nodeType == TypedNodeMaker.URI) {
				baseValueMaker = new BaseIRIValueMaker(mapping.getBaseIRI(), baseValueMaker);
			}*/
			result = new TypedNodeTransformationMakerList(nodeType, baseValueMaker);
		}
		
		
		
		//TODO need all the same but like GeometryParametersTerms.ColumnValuedTermMap
		/*public void visitComponent(GeometryParametersTerms.ColumnValuedTermMap termMap,GeometryParametersTerms.Position position) {
			ColumnName qualified = ColumnName.create(table.getTableName(), 
					termMap.getColumnName().asIdentifier(Vendor.MySQL));
			NodeType nodeType = getNodeType(termMap, position, table.getColumnType(qualified));
			ValueMaker baseValueMaker = new ColumnValueMaker(qualified);
			if (nodeType == TypedNodeMaker.URI) {
				baseValueMaker = new BaseIRIValueMaker(mapping.getBaseIRI(), baseValueMaker);
			}
			result = new TypedNodeTransformationMaker(nodeType, baseValueMaker);
		}*/
		
		
		public void visitComponent(GeometryFunction gf,GeometryParametersTerms.Position position, ConstantIRI datatype) {
			GeometryParametersTerms termMaptemp =null;
			for(Resource r:gf.getObjectMaps().keySet())
			{
				termMaptemp=gf.getObjectMaps().get(r);
			}
			GeometryParametersTerms.ColumnValuedTermMap termMap=(GeometryParametersTerms.ColumnValuedTermMap)termMaptemp;
			
			ColumnName qualified = ColumnName.create(table.getTableName(), 
					termMap.getColumnName().asIdentifier(Vendor.MySQL));
			
			NodeType nodeType=null;
			if(datatype!=null)
			{
					nodeType= TypedNodeMaker.typedLiteral(
							TypeMapper.getInstance().getSafeTypeByName(datatype.toString()));
			}
			else
			{
				nodeType = TypedNodeMaker.URI;//getNodeType(termMap, position, table.getColumnType(qualified));
			}
			
			
			
			ValueMaker baseValueMaker = new ColumnValueMaker(qualified);
			
			
			if (nodeType == TypedNodeMaker.URI) {
				baseValueMaker = new BaseIRIValueMaker(mapping.getBaseIRI(), baseValueMaker);
			}
			result = new TypedNodeTransformationMaker(nodeType, baseValueMaker,gf.getFunction(), connection);
		}
		//END TODO
		
		public void visitComponent(TemplateValuedTermMap termMap, Position position) {
			TemplateValueMaker pattern = toTemplate(termMap.getTemplate(), 
					termMap.getTermType(position), table);
			DataType characterType = GenericType.CHARACTER.dataTypeFor(Vendor.MySQL);
			result = new TypedNodeMaker(getNodeType(termMap, position, characterType), pattern);
		}
		private NodeType getNodeType(ColumnOrTemplateValuedTermMap termMap, Position position, DataType naturalType) {
			if (termMap.getTermType(position) == TermType.IRI) {
				return TypedNodeMaker.URI;
			}
			if (termMap.getTermType(position) == TermType.BLANK_NODE) {
				return TypedNodeMaker.BLANK;
			}
			if (termMap.getLanguageTag() != null) {
				return TypedNodeMaker.languageLiteral(
						termMap.getLanguageTag().toString());
			}
			if (termMap.getDatatype() != null) {
				return TypedNodeMaker.typedLiteral(
						TypeMapper.getInstance().getSafeTypeByName(termMap.getDatatype().toString()));
			}
			if (!XSD.xstring.getURI().equals(naturalType.rdfType())) {
				return TypedNodeMaker.typedLiteral(TypeMapper.getInstance().getSafeTypeByName(naturalType.rdfType()));
			}
			return TypedNodeMaker.PLAIN_LITERAL;
		}
		
}
	

	@Override
	public Collection<GeneralConnection> getConnections() {
		return Collections.singleton(connection);
	}
}
