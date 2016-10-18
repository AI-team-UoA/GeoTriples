package be.ugent.mmlab.rml.processor.concrete;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPathException;

import org.jaxen.XPath;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.xom.XOMXPath;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import be.ugent.mmlab.rml.core.DependencyRMLPerformer;
import be.ugent.mmlab.rml.core.NodeRMLPerformer;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.core.RMLPerformer;
import be.ugent.mmlab.rml.function.Config;
import be.ugent.mmlab.rml.model.SubjectMap;
import be.ugent.mmlab.rml.model.TermMap;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.AbstractRMLProcessor;
import be.ugent.mmlab.rml.processor.RMLProcessor;
import be.ugent.mmlab.rml.processor.RMLProcessorFactory;
import be.ugent.mmlab.rml.tools.CriticalSection;
import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;
import be.ugent.mmlab.rml.xml.XOMBuilder;
import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.Namespaces;
import jlibs.xml.sax.SAXUtil;
import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.XMLDog;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.InstantEvaluationListener;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParentNode;
import nu.xom.Text;
import nu.xom.XPathContext;
import nu.xom.XPathTypeException;

/**
 * 
 * @author mielvandersande, andimou modified by dimis
 */
public class XPathProcessor extends AbstractRMLProcessor {
	private int enumerator = 0;
	protected TriplesMap map;
	private long geoTriplesID = 0;
	private static Logger log = LoggerFactory.getLogger(RMLMappingFactory.class);

	private XPathContext nsContext = new XPathContext();

	public XPathProcessor() {
		if (reader == null) {
			try {
				reader = SAXUtil.newSAXFactory(true, false, false);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(133);
			}
		}
	}

	private DefaultNamespaceContext get_namespaces() {
		// Get the namespaces from xml file?
		DefaultNamespaceContext dnc = new DefaultNamespaceContext();
		for (String key : Config.user_namespaces.keySet()) {
			this.nsContext.addNamespace(key, Config.user_namespaces.get(key));
			dnc.declarePrefix(key, Config.user_namespaces.get(key));
		}
		this.nsContext.addNamespace("xsd", Namespaces.URI_XSD);
		dnc.declarePrefix("xsd", Namespaces.URI_XSD);

		this.nsContext.addNamespace("xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		dnc.declarePrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		this.nsContext.addNamespace("simcore",
				"http://www.lbl.gov/namespaces/Sim/SimModelCore");
		dnc.declarePrefix("simcore",
				"http://www.lbl.gov/namespaces/Sim/SimModelCore");
		this.nsContext.addNamespace("simres",
				"http://www.lbl.gov/namespaces/Sim/ResourcesGeneral");
		dnc.declarePrefix("simres",
				"http://www.lbl.gov/namespaces/Sim/ResourcesGeneral");
		this.nsContext.addNamespace("simgeom",
				"http://www.lbl.gov/namespaces/Sim/ResourcesGeometry");
		dnc.declarePrefix("simgeom",
				"http://www.lbl.gov/namespaces/Sim/ResourcesGeometry");
		this.nsContext.addNamespace("simbldg",
				"http://www.lbl.gov/namespaces/Sim/BuildingModel");
		dnc.declarePrefix("simbldg",
				"http://www.lbl.gov/namespaces/Sim/BuildingModel");
		this.nsContext.addNamespace("simmep",
				"http://www.lbl.gov/namespaces/Sim/MepModel");
		dnc.declarePrefix("simmep",
				"http://www.lbl.gov/namespaces/Sim/MepModel");
		this.nsContext.addNamespace("simmodel",
				"http://www.lbl.gov/namespaces/Sim/Model");
		dnc.declarePrefix("simmodel", "http://www.lbl.gov/namespaces/Sim/Model");

		// spc
		this.nsContext
				.addNamespace("mml", "http://www.w3.org/1998/Math/MathML");
		dnc.declarePrefix("mml", "http://www.w3.org/1998/Math/MathML");
		this.nsContext.addNamespace("xlink", "http://www.w3.org/1999/xlink");
		dnc.declarePrefix("xlink", "http://www.w3.org/1999/xlink");
		this.nsContext.addNamespace("xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		dnc.declarePrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		this.nsContext.addNamespace("tp", "http://www.plazi.org/taxpub");
		dnc.declarePrefix("tp", "http://www.plazi.org/taxpub");

		return dnc;
	}

	private String replace(Node node, String expression) {
		Object expre = extractValueFromNode(node,
				expression.split("\\{")[1].split("\\}")[0]).get(0);
		log.info("[XPathProcessor:execute] expre " + expre);
		return expre.toString();
	}

	public String execute(Node node, String expression)
			throws SaxonApiException {

		Processor proc = new Processor(false);
		XPathCompiler xpath = proc.newXPathCompiler();
		DocumentBuilder builder = proc.newDocumentBuilder();

		String fileName = getClass().getResource(
				map.getLogicalSource().getIdentifier()).getFile();

		XdmNode doc = builder.build(new File(fileName));
		String expre = replace(node, expression);
		expression = expression.replaceAll(
				"\\{" + expression.split("\\{")[1].split("\\}")[0] + "\\}", "'"
						+ expre + "'");

		XPathSelector selector = xpath.compile(expression).load();
		selector.setContextItem(doc);

		// Evaluate the expression.
		Object result = selector.evaluate();

		return result.toString();

	}

	private NodeItem currentnode;

	@Override
	public Collection<Statement> execute(final SesameDataSet dataset, final TriplesMap map,
			final RMLPerformer performer, String fileName,final Boolean RETURN_ALL_STATEMENTS) {
		final List<Statement> statements=new LinkedList<>();
		
		if (dependencyTriplesMap != null || dependencyProcessor != null) {
			if (dependencyTriplesMap != null) {
				DependencyRMLPerformer dependencyPerformer = ((DependencyRMLPerformer) AbstractRMLProcessor.performersForFunctionInsideJoinCondition
						.get(dependencyTriplesMap));
				return execute_node_fromdependency(
						dataset,
						map.getLogicalSource()
								.getReference()
								.replaceFirst(
										dependencyPerformer.getOwnmap()
												.getLogicalSource()
												.getReference(), ""), map,
						performer, dependencyPerformer.getCurrentNode());
			} else {
				return execute_node_fromdependency(
						dataset,
						map.getLogicalSource()
								.getReference()
								.replaceFirst(
										dependencyProcessor
												.getCurrentTriplesMap()
												.getLogicalSource()
												.getReference(), ""), map,
						performer, dependencyProcessor.getCurrentNode());
			}
			//return 10; // since I don't know yet how to deal with multithreading
						// and keep total generated triples number I will return
						// 10 in order to avoid not visiting the "child" triples
						// maps, those that are after this XPath
		}
		final WrappedLong totalmatches = new WrappedLong();
		try {
			this.map = map;
			String reference = getReference(map.getLogicalSource());
			// Inititalize the XMLDog for processing XPath
			// an implementation of javax.xml.namespace.NamespaceContext
			// DefaultNamespaceContext dnc = new DefaultNamespaceContext();
			DefaultNamespaceContext dnc = get_namespaces();
			log.trace("Reference: "+reference);
			log.trace("Namespaces found: "+dnc.toString());
			XMLDog dog = new XMLDog(dnc);

			// adding expression to the xpathprocessor
			// System.out.println(reference + "mmmm");
			// System.out.println(fileName + "mmmm");

			dog.addXPath(reference);

			jlibs.xml.sax.dog.sniff.Event event = dog.createEvent();

			// event.setXMLBuilder(new DOMBuilder());
			// use XOM now
			event.setXMLBuilder(new XOMBuilder());
			final ExecutorService executor = Executors.newFixedThreadPool(8);
			final CriticalSection cs = new CriticalSection();
			event.setListener(new InstantEvaluationListener() {

				// When an XPath expression matches
				@Override
				public void onNodeHit(Expression expression, NodeItem nodeItem) {
					totalmatches.increase();
					Node node = (Node) nodeItem.xml;
					log.trace("Next hit: "+node.getValue());
					// if(!nodeItem.namespaceURI.isEmpty())
					// log.info("namespace? " + nodeItem.namespaceURI);
					// else
					// log.info("no namespace.");
					// Let the performer do its thing
					Pattern MY_PATTERN = Pattern.compile("\\[\\d+\\]");
					Matcher m = MY_PATTERN.matcher(nodeItem.location);
					String id = "";
					while (m.find()) {
						String s = m.group(0);
						// System.out.println(s.replaceAll("\\[|\\]", ""));
						id += s.replaceAll("\\[|\\]", "");
					}
					// System.out.println(nodeItem.location);
					currentnode = nodeItem;
					try {
						cs.enter_write();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						Element element = new Element(
								Config.GEOTRIPLES_AUTO_ID, "");

						element.appendChild(String.valueOf(id));

						ParentNode domNode = (ParentNode) nodeItem.xml;
						domNode.appendChild(element);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println(((Node) nodeItem.xml).toXML());
					}
					cs.exit_write();
					// NodeItem mm=new NodeItem((org.w3c.dom.Node) node,dcn);
					
					if(RETURN_ALL_STATEMENTS==true){
						statements.addAll(performer.perform(nodeItem, dataset, map));					
					}else{
						performer.perform(nodeItem, dataset, map);
					}
					
					
//					executor.execute(new WorkerPerform(performer, nodeItem,
//							dataset, map));

					// System.out.println("XPath: " + expression.getXPath() +
					// " has hit: " + node.getTextContent());
				}

				@Override
				public void finishedNodeSet(Expression expression) {
					// System.out.println("Finished Nodeset: " +
					// expression.getXPath());
					executor.shutdown();
					try {
						executor.awaitTermination(Long.MAX_VALUE,
								TimeUnit.NANOSECONDS);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				@Override
				public void onResult(Expression expression, Object result) {
					// this method is called only for xpaths which returns
					// primitive result
					// i.e result will be one of String, Boolean, Double
					// System.out.println("XPath: " + expression.getXPath() +
					// " result: " + result);
				}
			});
			// Execute the streaming

			dog.sniff(event, new InputSource(new FileInputStream(fileName)),
					reader.newSAXParser().getXMLReader());

		} catch (SAXPathException ex) {
			LoggerFactory.getLogger(XPathProcessor.class.getName()).error(ex.toString());
		} catch (XPathException ex) {
			LoggerFactory.getLogger(XPathProcessor.class.getName()).error(ex.toString());
		} catch (FileNotFoundException ex) {
			LoggerFactory.getLogger(XPathProcessor.class.getName()).error(ex.toString());
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(dataset.getSize());
		
		return statements;
		//return totalmatches.getValue();

	}

	private static SAXParserFactory reader = null;

	@Override
	public Collection<Statement> execute_node(SesameDataSet dataset, String expression,
			TriplesMap parentTriplesMap, RMLPerformer performer, Object node,
			Resource subject) {
		final List<Statement> statements=new LinkedList<>();
		
		// still need to make it work with more nore-results
		// currently it handles only one

		if (expression.startsWith("/"))
			expression = expression.substring(1);
		log.debug("[AbstractRMLProcessorProcessor] expression " + expression);

		Node node2 = (Node) node;
		Nodes nodes = null;
		try {
			nodes = node2.query(expression, nsContext);
		} catch (XPathTypeException e) {
			Object o = null;
			XPath bxp = null;
			try {
				bxp = new XOMXPath(expression);
			} catch (org.jaxen.JaxenException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// bxp.setFunctionContext(new SimpleFunctionContext());
			// SimpleNamespaceContext nc = new SimpleNamespaceContext();
			// nc.addNamespace("fn", "http://www.w3.org/2005/xpath-functions");
			// bxp.setNamespaceContext(nc);
			try {
				o = bxp.stringValueOf(node2);
			} catch (org.jaxen.JaxenException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// System.out.println(o);
			Text t = new Text(o.toString());
			nodes = new Nodes(t);
		}
		log.debug("[AbstractRMLProcessorProcessor:node] " + "nodes' size "
				+ nodes.size());
		ExecutorService executor = Executors.newFixedThreadPool(1);
		for (int i = 0; i < nodes.size(); i++) {
			Node n = nodes.get(i);
			log.trace("[AbstractRMLProcessorProcessor:node] " + "new node "
					+ n.toXML().toString());
			executor.execute(new WorkerPerformOnNode(performer, subject, n,
					dataset, parentTriplesMap, parentTriplesMap,statements));
		}
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return statements;

	}

	protected class WorkerPerform implements Runnable {
		private TriplesMap map;
		private SesameDataSet dataset;
		private RMLPerformer performer;
		private NodeItem nodeItem;

		public WorkerPerform(RMLPerformer performer, NodeItem nodeItem,
				SesameDataSet dataset, TriplesMap map) {
			this.performer = performer;
			this.nodeItem = nodeItem;
			this.dataset = dataset;
			this.map = map;
		}

		@Override
		public void run() {
			performer.perform(nodeItem, dataset, map);
		}

	}

	protected class WorkerPerformOnNode implements Runnable {
		private TriplesMap map;
		private TriplesMap parentTriplesMap;
		private SesameDataSet dataset;
		private Node n;
		private RMLPerformer performer;
		private Resource subject;
		private List<Statement> statements;
		
		public WorkerPerformOnNode(RMLPerformer performer, Resource subject,
				Node n, SesameDataSet dataset, TriplesMap parentTriplesMap,
				TriplesMap map, List<Statement> statements2) {
			this.performer = performer;
			this.subject = subject;
			this.n = n;
			this.dataset = dataset;
			this.parentTriplesMap = parentTriplesMap;
			this.map = map;
			this.statements=statements2;
		}

		@Override
		public void run() {
			if (subject == null)
				getStatements().addAll(performer.perform(n, dataset, parentTriplesMap));
			else {
				RMLProcessorFactory factory = new ConcreteRMLProcessorFactory();
				RMLProcessor subprocessor = factory.create(map
						.getLogicalSource().getReferenceFormulation());
				RMLPerformer subperformer = new NodeRMLPerformer(subprocessor);
				getStatements().addAll(subperformer.perform(n, dataset, parentTriplesMap, subject));
			}
		}

		/**
		 * @return the statements
		 */
		public List<Statement> getStatements() {
			return statements;
		}

	}

	// @Override
	public void execute_node_fromdependency_backup(SesameDataSet dataset,
			String expression, TriplesMap map, RMLPerformer performer,
			Object node) {

		geoTriplesID = 0;
		// still need to make it work with more nore-results
		// currently it handles only one
		this.map = map;
		if (expression.startsWith("/"))
			expression = expression.substring(1);
		log.debug("[AbstractRMLProcessorProcessor] expression " + expression);

		Node node2 = (Node) node;
		Nodes nodes = null;
		try {
			nodes = node2.query(expression, nsContext);
		} catch (XPathTypeException e) {
			Object o = null;
			XPath bxp = null;
			try {
				bxp = new XOMXPath(expression);
			} catch (org.jaxen.JaxenException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// bxp.setFunctionContext(new SimpleFunctionContext());
			// SimpleNamespaceContext nc = new SimpleNamespaceContext();
			// nc.addNamespace("fn", "http://www.w3.org/2005/xpath-functions");
			// bxp.setNamespaceContext(nc);
			try {
				o = bxp.stringValueOf(node2);
			} catch (org.jaxen.JaxenException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// System.out.println(o);
			Text t = new Text(o.toString());
			nodes = new Nodes(t);
		}
		log.debug("[AbstractRMLProcessorProcessor:node] " + "nodes' size "
				+ nodes.size());

		for (int i = 0; i < nodes.size(); i++) {
			Node n = nodes.get(i);
			NodeItem k;
			log.debug("[AbstractRMLProcessorProcessor:node] " + "new node "
					+ n.toXML().toString());
			// currentnode = n; if someday use this function, uncomment this
			// comment and fix the error, its difficult because of the type of
			// currentnode which is NodeItem, we really need this type to get
			// the location
			Element element = new Element(Config.GEOTRIPLES_AUTO_ID, "");
			Nodes ids = node2.query(Config.GEOTRIPLES_AUTO_ID, nsContext);

			element.appendChild(ids.get(0).getValue()
					+ String.valueOf(++geoTriplesID));
			ParentNode domNode = (ParentNode) n;

			domNode.appendChild(element);

			performer.perform(n, dataset, map);
		}

	}

	@Override
	public Collection<Statement> execute_node_fromdependency(final SesameDataSet dataset,
			String expression, final TriplesMap map,
			final RMLPerformer performer, Object node) {
		final List<Statement> statements=new LinkedList<>();
		/*
		 * if (expression.startsWith("/")) expression = expression.substring(1);
		 */
		expression = "/*" + expression;
		geoTriplesID = 0;
		NodeItem ni = (NodeItem) node;
		String parentid = "";
		Pattern MY_PATTERN = Pattern.compile("\\[\\d+\\]");
		Matcher m = MY_PATTERN.matcher(ni.location);
		while (m.find()) {
			String s = m.group(0);
			// System.out.println(s.replaceAll("\\[|\\]", ""));
			parentid += s.replaceAll("\\[|\\]", "");
		}
		final String pp = parentid;
		Node node2 = (Node) ni.xml;
		try {
			this.map = map;
			// String reference = getReference(map.getLogicalSource());
			// Inititalize the XMLDog for processing XPath
			// an implementation of javax.xml.namespace.NamespaceContext
			// DefaultNamespaceContext dnc = new DefaultNamespaceContext();
			DefaultNamespaceContext dnc = get_namespaces();
			XMLDog dog = new XMLDog(dnc);

			// adding expression to the xpathprocessor
			// System.out.println(reference + "mmmm");
			// System.out.println(fileName + "mmmm");

			// if(expression.contains("vigor"))
			// {
			// System.out.println("vigooorrrr");
			// }
			// System.out.println(dog.isAllowDefaultPrefixMapping());
			// dog.setAllowDefaultPrefixMapping(true);
			dog.addXPath(expression);

			jlibs.xml.sax.dog.sniff.Event event = dog.createEvent();

			// event.setXMLBuilder(new DOMBuilder());
			// use XOM now
			event.setXMLBuilder(new XOMBuilder());

			event.setListener(new InstantEvaluationListener() {

				// When an XPath expression matches
				@Override
				public void onNodeHit(Expression expression, NodeItem nodeItem) {
					Node node = (Node) nodeItem.xml;
					// if(!nodeItem.namespaceURI.isEmpty())
					// log.info("namespace? " + nodeItem.namespaceURI);
					// else
					// log.info("no namespace.");
					// Let the performer do its thing
					Pattern MY_PATTERN = Pattern.compile("\\[\\d+\\]");
					Matcher m = MY_PATTERN.matcher(nodeItem.location);
					String id = "";
					int i = 0;
					while (m.find()) {
						if (i == 0) {
							++i;
							continue;
						}
						String s = m.group(0);

						// System.out.println(s.replaceAll("\\[|\\]", ""));
						id += s.replaceAll("\\[|\\]", "");
					}
					// System.out.println(nodeItem.location);
					currentnode = nodeItem;
					Element element = new Element(Config.GEOTRIPLES_AUTO_ID, "");
					// System.out.println("parent's id: "+pp);
					// System.out.println("childs' id: "+id);
					element.appendChild(String.valueOf(pp + id));
					ParentNode domNode = (ParentNode) nodeItem.xml;
					domNode.appendChild(element);

					statements.addAll(performer.perform(nodeItem, dataset, map));
					// System.out.println("XPath: " + expression.getXPath() +
					// " has hit: " + node.getTextContent());
				}

				@Override
				public void finishedNodeSet(Expression expression) {
					// System.out.println("Finished Nodeset: " +
					// expression.getXPath());
				}

				@Override
				public void onResult(Expression expression, Object result) {
					// this method is called only for xpaths which returns
					// primitive result
					// i.e result will be one of String, Boolean, Double
					// System.out.println("XPath: " + expression.getXPath() +
					// " result: " + result);
				}
			});
			// Execute the streaming
			// System.out.println(node2.toXML());
			dog.sniff(
					event,
					new InputSource(
							new ByteArrayInputStream(
									("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + node2
											.toXML())
											.getBytes(StandardCharsets.UTF_8))),
					reader.newSAXParser().getXMLReader());
		} catch (SAXPathException ex) {
			LoggerFactory.getLogger(XPathProcessor.class.getName()).error(ex.toString());
		} catch (XPathException ex) {
			LoggerFactory.getLogger(XPathProcessor.class.getName()).error(ex.toString());
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception ge) {
			ge.printStackTrace();
			System.out.println("This is the node!");
			System.out.println(node2.toXML());
			System.out.println("This is the expression!");
			System.out.println(expression);
			System.out.println("This is the map");
			System.out.println(map);
		}
		return statements;
	}

	/**
	 * Process a XPath expression against an XML node
	 * 
	 * @param node
	 * @param expression
	 * @return value that matches expression
	 */
	private List<Object> extractValueFromNode(Node node, String expression) {
		DefaultNamespaceContext dnc = get_namespaces();
		List<Object> list = new ArrayList<>();

		if (expression.startsWith("count(")) {
			// Nodes result = node.query(expression, nsContext);
			String result;
			try {
				result = execute(node, expression);
				list.add(result.toString());
			} catch (SaxonApiException ex) {
				LoggerFactory.getLogger(XPathProcessor.class.getName()).error(ex.toString());
			}
		} else {
			// if there's nothing to uniquelly identify, use # - temporary
			// solution - challenge
			if (expression.equals("#")) {
				list.add(Integer.toString(enumerator++));
				return list;
			}
			Nodes nodes = null;
			try {
				nodes = node.query(expression, nsContext);
			} catch (XPathTypeException e) {
				Object o = null;
				XPath bxp = null;
				try {
					bxp = new XOMXPath(expression);
				} catch (org.jaxen.JaxenException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				// bxp.setFunctionContext(new SimpleFunctionContext());
				// SimpleNamespaceContext nc = new SimpleNamespaceContext();
				// nc.addNamespace("fn",
				// "http://www.w3.org/2005/xpath-functions");
				// bxp.setNamespaceContext(nc);
				try {
					o = bxp.stringValueOf(node);
				} catch (org.jaxen.JaxenException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				// System.out.println(o);
				// System.out.println("here");
				Text t = new Text(o.toString());
				t.setValue(o.toString());
				nodes = new Nodes(t);
				// System.out.println(t.getValue().isEmpty());
				// System.out.println("Thes size is : "+nodes.size());
				// System.out.println("The value is : "+t.getValue().toString());
				/*
				 * try { System.in.read(); } catch (IOException e1) { // TODO
				 * Auto-generated catch block e1.printStackTrace(); }
				 */
			}
			if (nodes.size() > 1 && expression.contains("*")) {
				String concatenatedxml = "";
				for (int i = 0; i < nodes.size(); i++) {
					Node n = nodes.get(i);
					concatenatedxml += n.toXML();
				}
				list.add(concatenatedxml);
				return list;
			}
			for (int i = 0; i < nodes.size(); i++) {

				Node n = nodes.get(i);

				// checks if the node has a value or children
				if (!n.getValue().isEmpty() || (n.getChildCount() != 0))
					// MVS's for extracting elements and not the string

					/*
					 * if (!(n instanceof Attribute) && n.getChild(0) instanceof
					 * Element) { list.add(n.toXML()); continue; } else {
					 * list.add(n.getValue()); }
					 */

					for (int child = 0; child < n.getChildCount(); ++child) {
						if (n.getChild(child) instanceof Element) {
							list.add(n.toXML());
							// return list;
						}
					}

				// checks if the node has children, then cleans up new lines and
				// extra spaces
				if (!(n instanceof Attribute) && n.getChildCount() > 1)
					list.add(n.getValue().trim().replaceAll("[\\t\\n\\r]", " ")
							.replaceAll(" +", " ").replaceAll("\\( ", "\\(")
							.replaceAll(" \\)", "\\)").replaceAll(" :", ":")
							.replaceAll(" ,", ","));
				else
					list.add(n.getValue().toString());

			}
		}
		return list;

	}

	@Override
	public List<Object> extractValueFromNode(Object node, String expression) {
		if (expression.contains(Config.GEOTRIPLES_AUTO_ID)
				&& expression.split("/").length > 1) { // this is when
														// referencing an
														// element and ask or
														// the GeoTriplesID
			// used in rr:template "mpla/GeoTriplesID"
			return extractValueFromNode((NodeItem) node,
					expression.replaceAll("/*" + Config.GEOTRIPLES_AUTO_ID, ""));
		}
		return extractValueFromNode((Node) (((NodeItem) node).xml), expression);
	}

	private List<Object> extractValueFromNode(NodeItem node, String expression) {
		final List<Object> list = new ArrayList<>();
		if (!expression.startsWith("/"))
			expression = "/*[1]/" + expression;
		else {
			expression = "/*[1]" + expression;
		}
		geoTriplesID = 0;
		NodeItem ni = (NodeItem) node;
		String parentid = "";
		Pattern MY_PATTERN = Pattern.compile("\\[\\d+\\]");
		Matcher m = MY_PATTERN.matcher(ni.location);
		while (m.find()) {
			String s = m.group(0);
			// System.out.println(s.replaceAll("\\[|\\]", ""));
			parentid += s.replaceAll("\\[|\\]", "");
		}
		final String pp = parentid;
		Node node2 = (Node) ni.xml;
		try {
			this.map = map;
			// String reference = getReference(map.getLogicalSource());
			// Inititalize the XMLDog for processing XPath
			// an implementation of javax.xml.namespace.NamespaceContext
			// DefaultNamespaceContext dnc = new DefaultNamespaceContext();
			DefaultNamespaceContext dnc = get_namespaces();
			XMLDog dog = new XMLDog(dnc);

			// adding expression to the xpathprocessor
			// System.out.println(reference + "mmmm");
			// System.out.println(fileName + "mmmm");

			// if(expression.contains("vigor"))
			// {
			// System.out.println("vigooorrrr");
			// }
			// System.out.println(dog.isAllowDefaultPrefixMapping());
			// dog.setAllowDefaultPrefixMapping(true);
			dog.addXPath(expression);

			jlibs.xml.sax.dog.sniff.Event event = dog.createEvent();

			// event.setXMLBuilder(new DOMBuilder());
			// use XOM now
			event.setXMLBuilder(new XOMBuilder());

			event.setListener(new InstantEvaluationListener() {

				// When an XPath expression matches
				@Override
				public void onNodeHit(Expression expression, NodeItem nodeItem) {
					Node node = (Node) nodeItem.xml;
					// if(!nodeItem.namespaceURI.isEmpty())
					// log.info("namespace? " + nodeItem.namespaceURI);
					// else
					// log.info("no namespace.");
					// Let the performer do its thing
					Pattern MY_PATTERN = Pattern.compile("\\[\\d+\\]");
					Matcher m = MY_PATTERN.matcher(nodeItem.location);
					String id = "";
					int i = 0;
					while (m.find()) {
						if (i == 0) {
							++i;
							continue;
						}
						String s = m.group(0);

						// System.out.println(s.replaceAll("\\[|\\]", ""));
						id += s.replaceAll("\\[|\\]", "");
					}
					// System.out.println(nodeItem.location);
					currentnode = nodeItem;
					Element element = new Element(Config.GEOTRIPLES_AUTO_ID, "");
					// System.out.println("parent's id: "+pp);
					// System.out.println("childs' id: "+id);
					element.appendChild(String.valueOf(pp + id));
					ParentNode domNode = (ParentNode) nodeItem.xml;
					//domNode.appendChild(element);
					list.add(String.valueOf(pp + id));

//					list.addAll(extractValueFromNode((Node) nodeItem.xml,
//							Config.GEOTRIPLES_AUTO_ID));
				}

				@Override
				public void finishedNodeSet(Expression expression) {
					// System.out.println("Finished Nodeset: " +
					// expression.getXPath());
				}

				@Override
				public void onResult(Expression expression, Object result) {
					// this method is called only for xpaths which returns
					// primitive result
					// i.e result will be one of String, Boolean, Double
					// System.out.println("XPath: " + expression.getXPath() +
					// " result: " + result);
				}
			});
			// Execute the streaming
			// System.out.println(node2.toXML());
			dog.sniff(
					event,
					new InputSource(
							new ByteArrayInputStream(
									("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + node2
											.toXML())
											.getBytes(StandardCharsets.UTF_8))),
					reader.newSAXParser().getXMLReader());
		} catch (SAXPathException ex) {
			LoggerFactory.getLogger(XPathProcessor.class.getName()).error(ex.toString());
		} catch (XPathException ex) {
			LoggerFactory.getLogger(XPathProcessor.class.getName()).error(ex.toString());
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;

	}

	@Override
	public QLTerm getFormulation() {
		return QLTerm.XPATH_CLASS;
	}

	@Override
	public List<Object> processTermMap(TermMap map, TriplesMap triplesMap,
			Resource subject, URI predicate, SesameDataSet dataset,
			boolean ignoreOwnerBecauseWeAreInJoin) {
		return processTermMap(map, currentnode, triplesMap, subject, predicate,
				dataset, ignoreOwnerBecauseWeAreInJoin);
	}

	@Override
	public Resource processSubjectMap(SesameDataSet dataset,
			SubjectMap subjectMap) {
		if(log.isDebugEnabled())
			log.debug("Finding subject map from currentnode "+currentnode);
		return processSubjectMap(dataset, subjectMap, currentnode);
	}

	@Override
	public Object getCurrentNode() {
		return currentnode;
	}

	@Override
	public TriplesMap getCurrentTriplesMap() {
		return map;
	}
}
