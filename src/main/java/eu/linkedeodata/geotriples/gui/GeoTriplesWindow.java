/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.linkedeodata.geotriples.gui;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Scanner;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.SplitPane;
import org.apache.pivot.wtk.SplitPaneListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.SplitPane.ResizeMode;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TablePane.RowSequence;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.TextInputSelectionListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.effects.ReflectionDecorator;
import org.d2rq.db.SQLConnection;
import org.d2rq.db.op.TableOp;
import org.d2rq.db.schema.ColumnName;
import org.d2rq.db.schema.TableName;
import org.geotools.gml2.SrsSyntax;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import eu.linkedeodata.geotriples.GeneralConnection;
import eu.linkedeodata.geotriples.GeneralConnection.ConnectionType;
import eu.linkedeodata.geotriples.generate_mapping;
import eu.linkedeodata.geotriples.utils.OntologyLoader;

/**
 * Main Stock Tracker window.
 */
public class GeoTriplesWindow extends Window implements Bindable {
	GeneralConnection connection = null; // connection to shapefile, kml or
											// whatever
	GeneralConnection.ConnectionType connectiontype=null;
	//HERE LIE THE SHAPEFILE STUFF
	private SourceTable sourceTable=null;
	private SourceTable sourceTableGeometry=null;
	@BXML
	private TextArea r2rmlPreview = null;
	@BXML
	private TextInput baseIri = null;

	
	@BXML
	private Button generateMapping = null;
	@BXML
	private Button dumpRDF =null;

	

	@BXML
	private Label lastUpdateLabel = null;
	@BXML
	private Button geoTriplesLinkButton = null;
	@BXML
	private Button openShapefileButton =null;
	@BXML
	private TextInput epsgCode = null;
	
	/**
	 * from this component, the user may choose the output RDF format (N3, RDF/XML, 
	 */
	@BXML private ListButton dbRDFFormat = null;
	@BXML private SplitPane splitPane = null;
	
	@BXML private ListButton geoVoc = null;
	
	private ArrayList<String> symbols;
	//private GetQuery getQuery = null;

	private TableOp thematicable = null;
	private TableOp geometryvirtualtable = null;
	private String datasourceurl = null;
	private String outputurl = null;
	
	/*Only for Shapefile use*/
	private List<ColumnReceipt> columnsfromshapefile = null;
	private List<ColumnReceipt> columnsfromshapefilegeometry = null;
	/*-*/
	private Map<String, Object> namespace=null;
	
	
	private Map<TableName, String> tablesAndClasses = null;
	private List<SourceTable> sourceTables =new ArrayList<SourceTable>();
	/**
	 * a data model containing the current tables and columns as loaded by the db.
	 */
	private Map<TableName, List<ColumnReceipt>> tablesAndColumns = null;
	private String ontologyPath=null;
	
	private Action generateMappingAction = new Action(false) {
		@Override
		public void perform(Component source) {
			if(connectiontype==ConnectionType.SHAPEFILE)
			{
				RecipeMapping mappingreceipt = new RecipeMapping();
				//Shapefile case, we know that only one table exists
				for(SourceTable st:sourceTables)
				{
					if(st.isGeometrytable())
					{
						mappingreceipt.setClassGeometry(st.getOntologyClass());
						mappingreceipt.setReceiptGeometry(st.getActiveColumns());
					}
					else
					{
						mappingreceipt.setClassThematic(st.getOntologyClass());
						mappingreceipt.setReceiptThematic(st.getActiveColumns());
					}
				}
				
	
				List<String> args = new ArrayList<String>();
				args.add("-b");
				args.add(baseIri.getText());
				args.add("-o");
				args.add("mapping.ttl");
				args.add("-geov");
				args.add((String) geoVoc.getSelectedItem());
				if (epsgCode.getText() != null) {
					String a = epsgCode.getText();
					boolean flag = true;
					try {
						Integer.parseInt(a);
					}
					catch (NumberFormatException e) {
						flag = false;
					}
					if (flag) {
						args.add("-s");
						args.add(epsgCode.getText());
					}
				}
				args.add(datasourceurl);
				String [] yourArray = new String[args.getLength()];
				for (int i=0 ; i<args.getLength() ; i++) {
					yourArray[i] = args.get(i);
				}
//				String[] yourArray = { "-b", baseIri.getText() , "-o",
//						"nomatterwhat.ttl", shpurl };

				CustomOutputStream outputstream=new CustomOutputStream(r2rmlPreview);
				PrintStream printstream=new PrintStream(outputstream);
				try {
					//new generate_mapping(mappingreceipt,printstream)
					new generate_mapping(mappingreceipt,null)
							.process(yourArray);
					String content = new Scanner(new File("mapping.ttl")).useDelimiter("\\Z").next();
					r2rmlPreview.setText(content);

				} catch (Exception e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
					System.exit(0);
				}
			}
			else if(connectiontype==ConnectionType.SQL)
			{
				List<String> args = new ArrayList<String>();
				args.add("-b");
				args.add(baseIri.getText());
				args.add("--r2rml");
				args.add("-o");
				args.add("mapping.ttl");
				args.add("-geov");
				args.add((String) geoVoc.getSelectedItem());
				if (epsgCode.getText() != null) {
					String a = epsgCode.getText();
					boolean flag = true;
					try {
						Integer.parseInt(a);
					}
					catch (NumberFormatException e) {
						flag = false;
					}
					if (flag) {
						args.add("-s");
						args.add(epsgCode.getText());
					}
				}
				args.add("-u");
				args.add(((SQLConnection) connection).getUsername());
				args.add("-p");
				args.add(((SQLConnection) connection).getPassword());
				args.add("--r2rml");
				args.add(((SQLConnection)connection).getJdbcURL());
				String [] arguments = new String[args.getLength()];
				for (int i=0 ; i<args.getLength() ; i++) {
					arguments[i] = args.get(i);
				}
				
//				String [] arguments = {"-b", baseIri.getText(), "--r2rml", "-o", "mapping.ttl", "-u", ((SQLConnection)connection).getUsername(), "-p", 
//						((SQLConnection)connection).getPassword(),
//						((SQLConnection)connection).getJdbcURL()};
				try {
					java.util.Map<TableName, java.util.List<ColumnReceipt>> juTablesAndColumns = new java.util.HashMap<TableName, java.util.List<ColumnReceipt>>();
					java.util.Map<TableName, String> juTablesAndClasses = new java.util.HashMap<TableName, String>();
					
					for(SourceTable st:sourceTables){
						TableName tName = st.getTablemame();
						java.util.List<ColumnReceipt> juColumns = st.getActiveColumns();
						String classname = st.getOntologyClass();
						juTablesAndColumns.put(tName, juColumns);
						juTablesAndClasses.put(tName, classname);
					}
					
					new d2rq.generate_mapping(juTablesAndColumns, juTablesAndClasses, null).process(arguments);
					String content = new Scanner(new File("mapping.ttl")).useDelimiter("\\Z").next();
					r2rmlPreview.setText(content);
					
				} catch (Exception e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
					System.exit(0);
				}
			}
			Action.getNamedActions().get("dumpRDF").setEnabled(true); //enable dumpRDF functionality - button - menu
			dumpRDFAction.setEnabled(true);
			

		}
	};
	private Action dumpRDFAction = new Action(false) {
		@Override
		public void perform(Component source) {
			final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet();
			fileBrowserSheet.setName("Save RDF triples results");
			fileBrowserSheet.setMode(org.apache.pivot.wtk.FileBrowserSheet.Mode.SAVE_AS);
			fileBrowserSheet.open(GeoTriplesWindow.this, new SheetCloseListener() {
				@Override
				public void sheetClosed(Sheet sheet) {
					if (sheet.getResult()) {
						Sequence<File> selectedFiles = fileBrowserSheet
								.getSelectedFiles();
						String filename = null;
						try {
							filename = selectedFiles.get(0).getCanonicalPath();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						outputurl=filename;
						
						String inputmapping=r2rmlPreview.getText();
						//write it onto mapping.ttl
						BufferedWriter out = null;
						try  
						{
						    FileWriter fstream = new FileWriter("mapping.ttl", false); //true tells to append data.
						    out = new BufferedWriter(fstream);
						    out.write(inputmapping);
						    out.close();
						    out = null;
						}
						catch (IOException e)
						{
						    System.err.println("Error: " + e.getMessage());
						}
						
						String srid = epsgCode.getText();
						if (srid == null || srid == "") {
							srid = "4326";
						}
						File file = new File("mapping.ttl");
						try {
							if(connectiontype==ConnectionType.SHAPEFILE)
							{
								String[] yourArray = { "-b", baseIri.getText() , "-o",outputurl, "-sh" , datasourceurl,"-f",(String) dbRDFFormat.getSelectedItem(), "mapping.ttl"};
								new eu.linkedeodata.geotriples.dump_rdf(inputmapping).process(yourArray);
								file.delete();
							}
							else if(connectiontype==ConnectionType.SQL)
							{
								String[] yourArray = { "-b", baseIri.getText() , "-o",
										outputurl, "-u", ((SQLConnection)connection).getUsername(), "-p", ((SQLConnection)connection).getPassword(), "-jdbc",
										((SQLConnection)connection).getJdbcURL(), "mapping.ttl"};
							
								new d2rq.dump_rdf(inputmapping, (String) dbRDFFormat.getSelectedItem()).process(yourArray);
								file.delete();
							}
							else if (connectiontype == ConnectionType.RML) {
								//invoke rml processor
								String yourArray[] = {"-rml", "-s", srid, "-f", (String) dbRDFFormat.getSelectedItem(), "-o", outputurl, "mapping.ttl"};
								new eu.linkedeodata.geotriples.dump_rdf().process(yourArray);
								file.delete();
							}
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						if(outputurl!=null)
						{
							ArrayList<String> options = new ArrayList<>();
				            options.add("Open it!");
				            options.add("Cancel");
				            Component body = null;
				            BXMLSerializer serializer = new BXMLSerializer();
				            try {
				                //body = (Component) serializer.readObject(KitchenSink.class,
				                   // "alert.bxml");
				            } catch (Exception exception) {
				                System.err.println(exception);
				            }
						
							final Prompt pprompt = new Prompt(MessageType.QUESTION,
				                    "Conversion completed. You may now open the generated file:", options);
				                pprompt.setTitle("Select Icon");
				                pprompt.setSelectedOptionIndex(0);
				                pprompt.getDecorators().update(0, new ReflectionDecorator());
				                pprompt.open(GeoTriplesWindow.this.getWindow(),new SheetCloseListener() {
									
									@Override
									public void sheetClosed(Sheet sheet) {
										Object fff=pprompt.getSelectedOption();
										if((pprompt.getSelectedOption().equals("Open it!")))
										{
											try {
												java.awt.Desktop.getDesktop().open(new File(outputurl));
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
										}
								});
						}
					} else {
					}
				}
			});
		}
	};

	
	// Action invoked to refresh the symbol table view
	private Action refreshTableAction = new Action() {
		@Override
		public void perform(Component source) {
			refreshTables(null);
		}
	};

	
	// Action invoked to refresh the symbol table view
	@SuppressWarnings("unused")
	private Action refreshTableActionGeometryTable = new Action() {
		@Override
		public void perform(Component source) {
			//refreshGeometryTable();
		}
	};
	/* GeometryTable Actions END */
	private Action openConnection =new Action() {
		@Override
		public void perform(Component source) {   
			if(true)
			{
				BXMLSerializer bxmlSerializer = new BXMLSerializer(); 
				try {
					Locale locale = Locale.getDefault();
			        //System.out.println(locale);
			        //Resources resources = new Resources(GeoTriplesWindow.class.getName(),locale);
			        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			        @SuppressWarnings("unused")
					URL url=classLoader.getResource("OpenConnection.json");
			        
			        Resources resources = new Resources("OpenConnection",locale);
					OpenConnection prompt = (OpenConnection)bxmlSerializer.readObject(Thread.currentThread().getContextClassLoader().getResource("openconnection.bxml"),
					        resources);
					prompt.setMessageType(MessageType.QUESTION);
					prompt.setOptions(new ArrayList<String>("Cancel"));
					prompt.setMessage("Select type of connection");
					prompt.getDecorators().add(new ReflectionDecorator());
					prompt.open(GeoTriplesWindow.this.getWindow(),new SheetCloseListener() {
						
						@Override
						public void sheetClosed(Sheet sheet) {
							
							if (sheet.getResult() && ((OpenConnection)sheet).isConnected())
							{
								connection=((OpenConnection)sheet).getCon();
								Action.getNamedActions().get("addSourceTable").setEnabled(true);
								
								Action.getNamedActions().get("generateMapping").setEnabled(true);
								generateMappingAction.setEnabled(true);
								
								datasourceurl=((OpenConnection)sheet).getUrl();
								connectiontype=((OpenConnection)sheet).getContype();
								if (connection != null) {
									System.out.println("Connection established!");
									refreshTables(null);
									System.out.println("Refreshed tables done");
									if(connectiontype==ConnectionType.SHAPEFILE)
									{
										refreshGeometryTable(null);
									}
								}
								else {
									if (connectiontype==connectiontype.RML) {
										Action.getNamedActions().get("generateMapping").setEnabled(false);
										generateMappingAction.setEnabled(false);
										Action.getNamedActions().get("dumpRDF").setEnabled(true); //enable dumpRDF functionality - button - menu
										dumpRDFAction.setEnabled(true);
										try {
											BufferedReader reader = new BufferedReader( new FileReader (datasourceurl));
										    String         line = null;
										    StringBuilder  stringBuilder = new StringBuilder();
										    String         ls = System.getProperty("line.separator");

										    while( ( line = reader.readLine() ) != null ) {
										        stringBuilder.append( line );
										        stringBuilder.append( ls );
										    }

										    String rmlMapping = stringBuilder.toString();
										    r2rmlPreview.setText(rmlMapping);
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								}
							}
							}
					});		
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SerializationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
			
			
		}
	};
	private Action removeSourceTable =new Action() {
		@Override
		public void perform(Component source) {
			//remove table from list of tablesources , previously source table removed the object from mainTablePane in gui
			//so the size( list of tablessources) and the size(rows from gui) are not equal
			RowSequence rs=((TablePane)namespace.get("mainTablePane")).getRows();
			for(int i=0;i<rs.getLength();++i) //TODO ME,  na allaxtei afto, na mpei se ena row ena tablepain keno, kai se afto na paizei mpala , oxi se ola ta rows
			{ //giati an kapoios prosthesei ena row se afto to tablepane ti vapsame me afta ta 2 kai -2 :P
				if(rs.get(i) != sourceTables.get(i))
				{
					sourceTables.remove(i, 1);
					break;
				}
			}
			if((rs.getLength())!=sourceTables.getLength())
			{
				sourceTables.remove(sourceTables.getLength()-1, 1);
			}
				//sourceTables.remove((SourceTable)source);
		}
	};
	

	public static final String SERVICE_HOSTNAME = "download.finance.yahoo.com";
	public static final String SERVICE_PATH = "/d/quotes.csv";
	public static final long REFRESH_INTERVAL = 15000;
	public static final String GEOTRIPLES__HOME = "http://geotriples.eu";

	/*@Override
	protected void setSkin(org.apache.pivot.wtk.Skin skin) {
		// TODO Auto-generated method stub
		super.setSkin(new CustomWindowSkin());
	}*/
	public GeoTriplesWindow() {
		//Theme.getTheme().set(GeoTriplesWindow.class,CustomWindowSkin.class);
		/*BEGIN MENU STAFF*/
		Action.getNamedActions().put("fileNew", new Action() {
            @Override
            public void perform(Component source) {
                /*BXMLSerializer bxmlSerializer = new BXMLSerializer();
                bxmlSerializer.getNamespace().put("menuHandler", menuHandler);
 
                Component tab;
                try {
                    tab = new Border((Component)bxmlSerializer.readObject(ShapefileWindow.class, "document.bxml"));
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }
 
                tabPane.getTabs().add(tab);
                TabPane.setTabData(tab, "Document " + tabPane.getTabs().getLength());
                tabPane.setSelectedIndex(tabPane.getTabs().getLength() - 1);*/
            }
        });
 
        Action.getNamedActions().put("fileOpen", new Action() {
            @Override
            public void perform(Component source) {
                //fileBrowserSheet.open(ShapefileWindow.this);
            	openConnection.perform(GeoTriplesWindow.this);
            }
        });
        
        Action.getNamedActions().put("loadOntology", new Action() {
            @Override
            public void perform(Component source) {
                //fileBrowserSheet.open(ShapefileWindow.this);
            	loadOntologyFromFile();
            }
        });
 
        Action.getNamedActions().put("cut", new Action(false) {
            @Override
            public void perform(Component source) {
                TextInput textInput = (TextInput)GeoTriplesWindow.this.getFocusDescendant();
                textInput.cut();
            }
        });
 
        Action.getNamedActions().put("copy", new Action(false) {
            @Override
            public void perform(Component source) {
                TextInput textInput = (TextInput)GeoTriplesWindow.this.getFocusDescendant();
                textInput.copy();
            }
        });
 
        Action.getNamedActions().put("paste", new Action(false) {
            @Override
            public void perform(Component source) {
                TextInput textInput = (TextInput)GeoTriplesWindow.this.getFocusDescendant();
                textInput.paste();
            }
        });
        Action.getNamedActions().put("addSourceTable", new Action(false) {
            @Override
            public void perform(Component source) {
            	try{
            	BXMLSerializer bxmlSerializer = new BXMLSerializer(); 
					Locale locale = Locale.getDefault();
			        System.out.println(locale);
			        Resources resources = new Resources("AddSourceTable",locale);
	            	AddSourceTable prompt = (AddSourceTable)bxmlSerializer.readObject(Thread.currentThread().getContextClassLoader().getResource("AddSourceTable.bxml"),
					        resources);
	            	
	            	try {
	            		Collection<TableName> alltables = connection.getTableNames();
            			
	            		if(connectiontype==ConnectionType.SHAPEFILE)
	            		{
		            		TableName[] allaraytables=alltables.toArray(new TableName[alltables.size()]);
		            		for(int i=0;i<allaraytables.length;++i)
		            		{
		            			if(allaraytables[i].getTable().getName().endsWith("_geometry"))
		            			{
		            				alltables.remove(allaraytables[i]);
		            			}
		            		}
	            		}
						prompt.addSources(alltables);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					prompt.setMessageType(MessageType.QUESTION);
					prompt.setOptions(new ArrayList<String>("Cancel"));
					prompt.setMessage("Select one or more source tables to load");
					prompt.getDecorators().add(new ReflectionDecorator());
					prompt.open(GeoTriplesWindow.this.getWindow(), new SheetCloseListener() {
						
						@Override
						public void sheetClosed(Sheet sheet) {
							List<TableName> selectedItems = ((AddSourceTable)sheet).getSelectedSources();
							if(selectedItems==null)
							{
								return;
							}
							if(connectiontype==ConnectionType.SQL)
							{
								refreshTables(selectedItems);
							}
							else if(connectiontype==ConnectionType.SHAPEFILE)
							{
								refreshTables(selectedItems);
								refreshGeometryTable(selectedItems);
							}
						}
					});
            } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SerializationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            }
        });
        Action.getNamedActions().put("generateMapping", new Action(false) {
            @Override
            public void perform(Component source) {
                generateMappingAction.perform(null);
            }
        });
 
        Action.getNamedActions().put("dumpRDF", new Action(false) {
            @Override
            public void perform(Component source) {
                dumpRDFAction.perform(null);
            }
        });
        /*END MENU STAFF*/
        
        
		// Create the symbol list
		symbols = new ArrayList<String>();

		// Set a comparator on the symbol list so the entries are sorted
		symbols.setComparator(new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return s1.compareTo(s2);
			}
		});

		// Add action mapping to refresh the symbol table view
		Keyboard.Modifier commandModifier = Platform.getCommandModifier();
		Keyboard.KeyStroke refreshKeystroke = new Keyboard.KeyStroke(
				Keyboard.KeyCode.R, commandModifier.getMask());
		getActionMappings().add(
				new ActionMapping(refreshKeystroke, refreshTableAction));
	}
	
	@Override
	public void initialize(Map<String, Object> namespace, URL location,
			Resources resources) {
		this.namespace=namespace;
		dbRDFFormat.setSelectedIndex(1); //N-TRIPLE
		openShapefileButton.setAction(openConnection);
		BXMLSerializer bxmlSerializer = new BXMLSerializer(); 
		sourceTable =null;
		sourceTableGeometry=null;
		try {
			System.out.println(Thread.currentThread().getContextClassLoader());
			sourceTable = (SourceTable)bxmlSerializer.readObject(Thread.currentThread().getContextClassLoader().getResource("table_package.bxml"),
			        null);
			bxmlSerializer.getNamespace().clear();
			sourceTableGeometry = (SourceTable)bxmlSerializer.readObject(Thread.currentThread().getContextClassLoader().getResource("table_package.bxml"),
			        null);
		} catch (IOException | SerializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//SplitPaneListener.Adapter ada=new 
		//ada.
		try {
			//System.out.println("{font: {size: \""+(1-splitPane.getSplitRatio())*100+"%\"}}");
			r2rmlPreview.setStyles("{font: {size: \""+(1-splitPane.getSplitRatio())*200+"%\"}}");
		} catch (SerializationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		splitPane.getSplitPaneListeners().add(new SplitPaneListener() {
			
			@Override
			public void topLeftChanged(SplitPane splitPane, Component previousTopLeft) {
			}
			@Override
			public void splitRatioChanged(SplitPane splitPane, float previousSplitRatio) {
				try {
					//System.out.println("{font: {size: \""+(1-splitPane.getSplitRatio())*100+"%\"}}");
					r2rmlPreview.setStyles("{font: {size: \""+(1-splitPane.getSplitRatio())*200+"%\"}}");
				} catch (SerializationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			@Override
			public void resizeModeChanged(SplitPane splitPane,
					ResizeMode previousResizeMode) {
			}
			@Override
			public void primaryRegionChanged(SplitPane splitPane) {
			}
			@Override
			public void orientationChanged(SplitPane splitPane) {
			}
			@Override
			public void lockedChanged(SplitPane splitPane) {
			}
			@Override
			public void bottomRightChanged(SplitPane splitPane,
					Component previousBottomRight) {	
			}
		});
		
		generateMapping.setAction(generateMappingAction); // generate mapping
															// action perform
		//generateMappingAction.setEnabled(true);
		
		dumpRDF.setAction(dumpRDFAction); // generate mapping
		// action perform
		//dumpRDF.setEnabled(true);
		// Add stocks table view event handlers
		

		// Add a button press listener to open the Yahoo! Finance web page when
		// the link is clicked
		geoTriplesLinkButton.getButtonPressListeners().add(
				new ButtonPressListener() {
					@Override
					public void buttonPressed(Button button) {
						Desktop desktop = Desktop.getDesktop();

						try {
							desktop.browse(new URL(GEOTRIPLES__HOME).toURI());
						} catch (MalformedURLException exception) {
							throw new RuntimeException(exception);
						} catch (URISyntaxException exception) {
							throw new RuntimeException(exception);
						} catch (IOException exception) {
							System.out.println("Unable to open "
									+ GEOTRIPLES__HOME
									+ " in default browser.");
						}
					}
				});
		
		
		//TODO: INITIALIZE DATABASE COMPONENTS
		
		
	}

	@Override
	public void open(Display display, Window owner) {
		super.open(display, owner);
		

		/*
		 * ApplicationContext.scheduleRecurringCallback(new Runnable() {
		 * 
		 * @Override public void run() { refreshTable(); } }, REFRESH_INTERVAL);
		 */
		}

	private void refreshTables(List<TableName> selectedItems) {
		// Abort any outstanding query
		/*if (getQuery != null) {
			synchronized (getQuery) {
				if (getQuery.isPending()) {
					getQuery.abort();
				}
			}
		}*/
		Collection<TableName> tables = null;
		if(selectedItems==null)
		{
			try {
				tables = connection.getTableNames();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				System.exit(13);
			}
		}
		else
		{
			tables=new java.util.ArrayList<TableName>();
			for(TableName tablename:selectedItems)
			{
				tables.add(tablename);
			}
		}

		List<ColumnReceipt> originalcolumnsfromshapefile = null;

		// hardcoded
		//boolean found = false;
		for (TableName table : tables) {
			// hardcoded
			//if (found) {
			//	break;
			//}
			if (table.getTable().getName().contains("_geometry") && connectiontype==ConnectionType.SHAPEFILE) {
				continue;
			}// else {
			//	found = true;
			//}
			BXMLSerializer bxmlSerializer = new BXMLSerializer();
			//bxmlSerializer.setLocation(new URL(getClass().getCanonicalName()));
			SourceTable sourcetable = null;
			try {
				sourcetable = (SourceTable)bxmlSerializer.readObject(Thread.currentThread().getContextClassLoader().getResource("table_package.bxml"),null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SerializationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	sourcetable.setTitle(table.getTable().getName());
    		sourcetable.setOntologyClass(table.getTable().getName());
    		if(connectiontype==ConnectionType.SHAPEFILE)
    		{
        		sourcetable.setTypes(new ArrayList<String>("Int", "Date", "Double", "String" , "Geometry"));
    		}
    		else if(connectiontype==ConnectionType.SQL)
    		{
    			sourcetable.setTypes(new ArrayList<String>());
    			sourcetable.setLockDatatypeColumn();
    			sourcetable.setLockTransformationColumn();
    		}
    		sourcetable.setTablemame(table);
    		sourcetable.setRemoveTableAction(removeSourceTable);
    		
    		
			columnsfromshapefile = new ArrayList<ColumnReceipt>();
			originalcolumnsfromshapefile = new ArrayList<ColumnReceipt>();

			TableOp tableop = null;
			try {
				tableop = connection.getTable(table);
				setThematicable(tableop); // make this copy dimis
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				System.exit(13);
			}
			for (ColumnName column : tableop.getColumns()) {
				/*if( column.getColumn().getCanonicalName().equals("the_geom")) //column.getColumn().getCanonicalName().equals("gid") ||
				{
					continue;
				}*/
				ColumnReceipt columnshp = new ColumnReceipt();
				columnshp.setColumnName(column.getColumn().getName());
				columnshp.setDataType(tableop.getColumnType(column).name());
				if(connectiontype==ConnectionType.SQL)
				{
					sourcetable.addType(tableop.getColumnType(column).name());
				}
				columnshp.setPredicate("has_" + column.getColumn().getName());
				columnshp.setTableName(tableop.getTableName().getTable()
						.getName());
				
				/*Only for database*/
				columnshp.setD2rqTableName(table);
				columnshp.setD2rqDataType(tableop.getColumnType(column));
            	/*-*/
            	
				columnsfromshapefile.add((ColumnReceipt) columnshp);
				originalcolumnsfromshapefile.add(new ColumnReceipt((ColumnReceipt) columnshp));
			}
			List<ColumnReceipt> quotes = columnsfromshapefile;
			// Preserve any existing sort and selection
			
			
			sourcetable.setData(quotes,originalcolumnsfromshapefile);
			sourceTables.add(sourcetable); //add to list
			//tablesAndClasses.put(table, value)
			//tablesAndColumns.put(table, columnsfromshapefile);
			
    		int numberofrows= ((TablePane)namespace.get("mainTablePane")).getRows().getLength();
    		((TablePane)namespace.get("mainTablePane")).getRows().add(sourcetable);
    		//this.repaint();
		}
	}

	/* Geometry Table Refreshes */
	/* refreshGeometryTable is ONLY FOR SHAPEFILES*/
	private void refreshGeometryTable(List<TableName> selectedItems) {
		// Abort any outstanding query
		/*if (getQuery != null) {
			synchronized (getQuery) {
				if (getQuery.isPending()) {
					getQuery.abort();
				}
			}
		}*/
		Collection<TableName> tables = null;
		if(selectedItems==null)
		{
		try {
			tables = connection.getTableNames();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(13);
		}
		}
		else
		{
			tables=new java.util.ArrayList<TableName>();
			for(TableName tablename:selectedItems)
			{
				tables.add(tablename);
			}
		}

		List<ColumnReceipt> originalcolumnsfromshapefile = null;

		// hardcoded
		//boolean found = false;
		for (TableName table : tables) {
			// hardcoded
			//if (found) {
			//	break;
			//}
			if (!table.getTable().getName().contains("_geometry")) {
				continue;
			}// else {
			//	found = true;
			//}
			
			BXMLSerializer bxmlSerializer = new BXMLSerializer();
			//bxmlSerializer.setLocation(new URL(getClass().getCanonicalName()));
			SourceTable sourcetable = null;
			try {
				sourcetable = (SourceTable)bxmlSerializer.readObject(Thread.currentThread().getContextClassLoader().getResource("table_package.bxml"),null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SerializationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	sourcetable.setTitle(table.getTable().getName());
    		sourcetable.setOntologyClass(table.getTable().getName());
    		sourcetable.setTypes(new ArrayList<String>("Int", "Date", "Double", "String" , "Geometry"));
    		sourcetable.setTablemame(table);
    		sourcetable.setGeometrytable(true);
    		sourcetable.setRemoveTableAction(removeSourceTable);
    		
			columnsfromshapefilegeometry = new ArrayList<ColumnReceipt>();
			originalcolumnsfromshapefile = new ArrayList<ColumnReceipt>();

			TableOp tableop = null;
			try {
				tableop = connection.getTable(table);
				setGeometryvirtualtable(tableop); // make this copy dimis
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				System.exit(13);
			}
			for (ColumnName column : tableop.getColumns()) {
				if(column.getColumn().getCanonicalName().equals("gid"))
				{
					continue;
				}
				
				if(tableop.getColumnType(column).name().equals("Geometry"))
				{
						ColumnReceipt columnshp = new ColumnReceipt();
						columnshp.setColumnName(column.getColumn().getName());
					//columnshp.setDataType(tableop.getColumnType(column).name());
					//set default datatype
					columnshp.setDataType("Default");
					columnshp.setTransformation("asWKT");
					columnshp.setPredicate("asWKT");
					columnshp.setTableName(tableop.getTableName().getTable()
							.getName());
					columnsfromshapefilegeometry.add((ColumnReceipt) columnshp);
					originalcolumnsfromshapefile.add(new ColumnReceipt(
							(ColumnReceipt) columnshp));
					
					columnshp = new ColumnReceipt();
					columnshp.setColumnName(column.getColumn().getName());
					columnshp.setDataType("Default");
					columnshp.setTransformation("asGML");
					columnshp.setPredicate("asGML");
					columnshp.setTableName(tableop.getTableName().getTable()
							.getName());
					columnsfromshapefilegeometry.add((ColumnReceipt) columnshp);
					
					
					columnshp = new ColumnReceipt();
					columnshp.setColumnName(column.getColumn().getName());
					columnshp.setDataType("Default");
					columnshp.setTransformation("isSimple");
					columnshp.setPredicate("isSimple");
					columnshp.setTableName(tableop.getTableName().getTable()
							.getName());
					columnsfromshapefilegeometry.add((ColumnReceipt) columnshp);
					
					columnshp = new ColumnReceipt();
					columnshp.setColumnName(column.getColumn().getName());
					columnshp.setDataType("Default");
					columnshp.setTransformation("is3D");
					columnshp.setPredicate("is3D");
					columnshp.setTableName(tableop.getTableName().getTable()
							.getName());
					columnsfromshapefilegeometry.add((ColumnReceipt) columnshp);
					
					columnshp = new ColumnReceipt();
					columnshp.setColumnName(column.getColumn().getName());
					columnshp.setDataType("Default");
					columnshp.setTransformation("hasSerialization");
					columnshp.setPredicate("hasSerialization");
					columnshp.setTableName(tableop.getTableName().getTable()
							.getName());
					columnsfromshapefilegeometry.add((ColumnReceipt) columnshp);
					
					columnshp = new ColumnReceipt();
					columnshp.setColumnName(column.getColumn().getName());
					columnshp.setDataType("Default");
					columnshp.setTransformation("spatialDimension");
					columnshp.setPredicate("spatialDimension");
					columnshp.setTableName(tableop.getTableName().getTable()
							.getName());
					columnsfromshapefilegeometry.add((ColumnReceipt) columnshp);
					
					columnshp = new ColumnReceipt();
					columnshp.setColumnName(column.getColumn().getName());
					columnshp.setDataType("Default");
					columnshp.setTransformation("dimension");
					columnshp.setPredicate("dimension");
					columnshp.setTableName(tableop.getTableName().getTable()
							.getName());
					columnsfromshapefilegeometry.add((ColumnReceipt) columnshp);
					
					columnshp = new ColumnReceipt();
					columnshp.setColumnName(column.getColumn().getName());
					columnshp.setDataType("Default");
					columnshp.setTransformation("coordinateDimension");
					columnshp.setPredicate("coordinateDimension");
					columnshp.setTableName(tableop.getTableName().getTable()
							.getName());
					columnsfromshapefilegeometry.add((ColumnReceipt) columnshp);
					
				}
				else
				{
					ColumnReceipt columnshp = new ColumnReceipt();
					columnshp.setColumnName(column.getColumn().getName());
					columnshp.setDataType(tableop.getColumnType(column).name());
					columnshp.setPredicate("has_" + column.getColumn().getName());	
					columnshp.setTableName(tableop.getTableName().getTable()
							.getName());
					columnsfromshapefilegeometry.add((ColumnReceipt) columnshp);
					originalcolumnsfromshapefile.add(new ColumnReceipt(
							(ColumnReceipt) columnshp));
				}
				

				
				
			}
			List<ColumnReceipt> quotes = columnsfromshapefilegeometry;
			// Preserve any existing sort and selection
			sourcetable.setData(quotes,originalcolumnsfromshapefile);
			sourceTables.add(sourcetable); //add to list
			int numberofrows= ((TablePane)namespace.get("mainTablePane")).getRows().getLength();
    		((TablePane)namespace.get("mainTablePane")).getRows().add(sourcetable);

		}
	}

	public TableOp getThematicable() {
		return thematicable;
	}

	public void setThematicable(TableOp thematicable) {
		this.thematicable = thematicable;
	}

	public TableOp getGeometryvirtualtable() {
		return geometryvirtualtable;
	}

	public void setGeometryvirtualtable(TableOp geometryvirtualtable) {
		this.geometryvirtualtable = geometryvirtualtable;
	}
	
	
	
	
	
	private MenuHandler menuHandler = new MenuHandler.Adapter() {
        TextInputContentListener textInputTextListener = new TextInputContentListener.Adapter() {
            @Override
            public void textChanged(TextInput textInput) {
                updateActionState(textInput);
            }
        };
 
        TextInputSelectionListener textInputSelectionListener = new TextInputSelectionListener() {
            @Override
            public void selectionChanged(TextInput textInput, int previousSelectionStart,
                int previousSelectionLength) {
                updateActionState(textInput);
            }
        };
 
        @Override
        public void configureMenuBar(Component component, MenuBar menuBar) {
            if (component instanceof TextInput) {
                TextInput textInput = (TextInput)component;
 
                updateActionState(textInput);
                Action.getNamedActions().get("paste").setEnabled(true);
 
                textInput.getTextInputContentListeners().add(textInputTextListener);
                textInput.getTextInputSelectionListeners().add(textInputSelectionListener);
            } else {
                Action.getNamedActions().get("cut").setEnabled(false);
                Action.getNamedActions().get("copy").setEnabled(false);
                Action.getNamedActions().get("paste").setEnabled(false);
            }
        }
 
        @Override
        public void cleanupMenuBar(Component component, MenuBar menuBar) {
            if (component instanceof TextInput) {
                TextInput textInput = (TextInput)component;
                textInput.getTextInputContentListeners().remove(textInputTextListener);
                textInput.getTextInputSelectionListeners().remove(textInputSelectionListener);
            }
        }
 
        private void updateActionState(TextInput textInput) {
            Action.getNamedActions().get("cut").setEnabled(textInput.getSelectionLength() > 0);
            Action.getNamedActions().get("copy").setEnabled(textInput.getSelectionLength() > 0);
        }
    };
    public void loadOntologyFromFile() {
    	final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet();
		fileBrowserSheet.setName("Select an ontology file");
		fileBrowserSheet.setDisabledFileFilter(new Filter<File>() {                     
        @Override 
        public boolean include(File file) { 
            return (file.isFile() 
                && !file.getName().endsWith(".rdf")); 
        } 
    }); 
		fileBrowserSheet.setMode(org.apache.pivot.wtk.FileBrowserSheet.Mode.OPEN);
		fileBrowserSheet.open(GeoTriplesWindow.this, new SheetCloseListener() {
			@Override
			public void sheetClosed(Sheet sheet) {
				if (sheet.getResult()) {
					Sequence<File> selectedFiles = fileBrowserSheet
							.getSelectedFiles();

					ListView listView = new ListView();
					listView.setListData(new ArrayList<File>(selectedFiles));
					listView.setSelectMode(ListView.SelectMode.NONE);
					listView.getStyles().put("backgroundColor", null);
					
					try {
						ontologyPath = selectedFiles.get(0).getCanonicalPath();
						if(ontologyPath==null)
						{
							Alert.alert(MessageType.ERROR, "Something went wrong with the file,  please try again",GeoTriplesWindow.this);
							return;
						}
						
						OntologyLoader loader = new OntologyLoader(ontologyPath);
						loader.load();
						
						/**
			             * for properties
			             */
						List<String> preds = new ArrayList<String>();
		            	ExtendedIterator<DatatypeProperty> props = loader.getProperties();
		            	while (props.hasNext()) {
		            		preds.add(props.next().getLocalName());
		            	}
		            	
			            /**
			             * for classes
			             */
			            List<String> classes = new ArrayList<String>();
		            	ExtendedIterator<OntClass> classS = loader.getClasses();
		            	while (classS.hasNext()) {
		            		classes.add(classS.next().getLocalName());
		            	}

		            	for(SourceTable st:sourceTables)
			            {
			            	st.setClasses(classes);
			            	st.setPredicates(preds);
			            }
					} catch (IOException e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
						System.exit(13);
					}
				} 	
				}
		});			
		/*
		 * ApplicationContext.scheduleRecurringCallback(new Runnable() {
		 * 
		 * @Override public void run() { refreshTable(); } }, REFRESH_INTERVAL);
		 */
	}
}
