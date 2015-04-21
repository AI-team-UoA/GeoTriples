package eu.linkedeodata.geotriples.gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.DialogCloseListener;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.d2rq.db.SQLConnection;

import eu.linkedeodata.geotriples.GeneralConnection;
import eu.linkedeodata.geotriples.GeneralConnection.ConnectionType;
import eu.linkedeodata.geotriples.shapefile.ShapefileConnection;

public class OpenConnection extends Prompt implements Bindable {
	private GeneralConnection con;
	private ConnectionType contype;
	private String url;
	@BXML private PushButton selectChoice;
	@BXML private ButtonGroup connectiontype;
	
	@BXML private Button sqlChoice;
	@BXML private Button shapeFileChoice;
	@BXML private Button rmlChoice;
	
	private boolean isConnected=false;
	public boolean isConnected() {
		return isConnected;
	}
	public GeneralConnection getCon() {
		return con;
	}
	public void setCon(GeneralConnection con) {
		this.con = con;
	}
	public ConnectionType getContype() {
		return contype;
	}
	public void setContype(ConnectionType contype) {
		this.contype = contype;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		
        // Get a reference to the button group
        // Add a button press listener
		selectChoice.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
               if(connectiontype.getSelection()==shapeFileChoice)
               {
            	   contype=ConnectionType.SHAPEFILE;
            	   openShapeFile();
               }
               else if(connectiontype.getSelection()==sqlChoice)
               {
            	   contype=ConnectionType.SQL;
            	   openSQLconnection();
               }
               else if (connectiontype.getSelection() == rmlChoice) {
            	   contype=ConnectionType.RML;
            	   openRMLMapping();
               }
            }
        });
    }
	
	
	protected void openRMLMapping() {
		final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet();
		fileBrowserSheet.setName("Select an RML document");
		fileBrowserSheet.setDisabledFileFilter(new Filter<File>() {                     
        @Override 
        public boolean include(File file) { 
            return (file.isFile() 
                && !file.getName().endsWith(".ttl")); 
        } 
    }); 
		fileBrowserSheet.setMode(org.apache.pivot.wtk.FileBrowserSheet.Mode.OPEN);
		fileBrowserSheet.open(OpenConnection.this, new SheetCloseListener() {
			@Override
			public void sheetClosed(Sheet sheet) {
				if (sheet.getResult()) {
					Sequence<File> selectedFiles = fileBrowserSheet
							.getSelectedFiles();

					ListView listView = new ListView();
					listView.setListData(new ArrayList<File>(selectedFiles));
					listView.setSelectMode(ListView.SelectMode.NONE);
					listView.getStyles().put("backgroundColor", null);
					/*Alert.alert(MessageType.INFO,
							"You selected the shapefile:","Successfully Load", listView,
							OpenConnection.this,null);*/
					try {
						url = selectedFiles.get(0).getCanonicalPath();
					} catch (IOException e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
						System.exit(13);
					}
					// connection=con; //set connection
					
					isConnected=true;
					close(true);
					con = null;
					
				} else {
					/*Alert.alert(MessageType.ERROR,
							"You didn't select any shapefile.",
							OpenConnection.this, new DialogCloseListener() {

								@Override
								public void dialogClosed(Dialog dialog,
										boolean modal) {
									//System.exit(13);
								}
							});*/
					close(false);
				}
			}
		});
	}
	
	protected void openSQLconnection() {
		BXMLSerializer bxmlSerializer = new BXMLSerializer(); 
		try {
			Locale locale = Locale.getDefault();
	        System.out.println(locale);
	        //Resources resources = new Resources(GeoTriplesWindow.class.getName(),locale);
	        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	        @SuppressWarnings("unused")
			URL rurl=classLoader.getResource("sqlconnectionparameters.json");
	        
	        Resources resources = new Resources("sqlconnectionparameters",locale);
	        SqlForm prompt = (SqlForm)bxmlSerializer.readObject(Thread.currentThread().getContextClassLoader().getResource("sqlconnectionparameters.bxml"),
			        resources);
	        prompt.setOptions(new ArrayList<String>("Cancel"));
	        prompt.open(getDisplay(),OpenConnection.this.getWindow(),new SheetCloseListener() {
				
				@Override
				public void sheetClosed(Sheet sheet) {
					System.out.println(sheet.getResult() + " " + ((SqlForm)sheet).IsLoaded());
					if(sheet.getResult() && ((SqlForm)sheet).IsLoaded())
					{
						con=((SqlForm)sheet).getSqlConnection();
						url=((SQLConnection)con).getJdbcURL();
						isConnected=true;
						close(true);
						return;
					}
					close(false);
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
	private void openShapeFile(){
		final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet();
		fileBrowserSheet.setName("Select a shapefile");
		fileBrowserSheet.setDisabledFileFilter(new Filter<File>() {                     
        @Override 
        public boolean include(File file) { 
            return (file.isFile() 
                && !file.getName().endsWith(".shp")); 
        } 
    }); 
		fileBrowserSheet.setMode(org.apache.pivot.wtk.FileBrowserSheet.Mode.OPEN);
		fileBrowserSheet.open(OpenConnection.this, new SheetCloseListener() {
			@Override
			public void sheetClosed(Sheet sheet) {
				if (sheet.getResult()) {
					Sequence<File> selectedFiles = fileBrowserSheet
							.getSelectedFiles();

					ListView listView = new ListView();
					listView.setListData(new ArrayList<File>(selectedFiles));
					listView.setSelectMode(ListView.SelectMode.NONE);
					listView.getStyles().put("backgroundColor", null);
					/*Alert.alert(MessageType.INFO,
							"You selected the shapefile:","Successfully Load", listView,
							OpenConnection.this,null);*/
					try {
						url = selectedFiles.get(0).getCanonicalPath();
					} catch (IOException e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
						System.exit(13);
					}
					// connection=con; //set connection
					con = new ShapefileConnection(url);
					if (con == null) {
						Alert.alert(MessageType.ERROR,
								"Error opening shapefile. Aborting..","Error",null,
								OpenConnection.this,
								new DialogCloseListener() {

									@Override
									public void dialogClosed(Dialog dialog,
											boolean modal) {
										OpenConnection.this.close(false);
										System.exit(13);
									}
								});
					}
					isConnected=true;
					close(true);
					
				} else {
					/*Alert.alert(MessageType.ERROR,
							"You didn't select any shapefile.",
							OpenConnection.this, new DialogCloseListener() {

								@Override
								public void dialogClosed(Dialog dialog,
										boolean modal) {
									//System.exit(13);
								}
							});*/
					close(false);
				}
			}
		});
	}
	
}
