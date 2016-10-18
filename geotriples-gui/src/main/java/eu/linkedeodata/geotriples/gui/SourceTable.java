package eu.linkedeodata.geotriples.gui;

import java.net.URL;
import java.util.Comparator;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.List.ItemIterator;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.ComponentStateListener;
import org.apache.pivot.wtk.Expander;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.SuggestionPopup;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewRowListener;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.content.TableViewRowComparator;
import org.d2rq.db.schema.TableName;

public class SourceTable extends TablePane.Row implements Bindable {

	@BXML private Label title;
	@BXML private TextInput className;
	@BXML private TableView tableView;
	@BXML private BoxPane detailPane;
	@BXML private Button removeSymbolsButton = null;
	@BXML private Button addSymbolButton = null;
	@BXML private ListButton symbolTextInput = null;
	
	@BXML private Expander expander=null;
	@BXML private Button removeTable=null;
	
	/*Manage Columns is case of SQL connection, eg Dont allow transformation*/
	@BXML private TextInput predicateColumnEditor=null;
	@BXML private ListButton transformationColumnEditor=null;
	@BXML private ListButton types =null;
	
	private List<ColumnReceipt> columnsfromsourcetable = null;
	
	private TableName tablemame = null; //for SQL usecase
	private boolean geometrytable=false;
	private SuggestionPopup suggestionClassesPopup = new SuggestionPopup();
	private SuggestionPopup suggestionPredicatesPopup = new SuggestionPopup();

	private List<String> classes=new ArrayList<String>();
	private List<String> predicates=new ArrayList<String>();
	
	public java.util.List<ColumnReceipt> getActiveColumns(){
		java.util.List<ColumnReceipt> result=new java.util.ArrayList<ColumnReceipt>();
		for(Object c:tableView.getTableData())
		{
			result.add((ColumnReceipt)c);
		}
		return result;
	}
	public void setRemoveTableAction(final Action action)
	{
		removeTable.setAction(new Action() {
			@Override
			public void perform(Component source) {
				SourceTable.this.getTablePane().getRows().remove(SourceTable.this);
				action.perform(null);
			}
		});
	}
	public void setLockPredicateColumn()
	{
		predicateColumnEditor.setEnabled(false);
	}
	public void setLockTransformationColumn()
	{
		transformationColumnEditor.setEnabled(false);
	}
	public void setLockDatatypeColumn()
	{
		types.setEnabled(false);
	}
	
	@Override
	public void initialize(Map<String, Object> namespace, URL location,
			Resources resources) {
		
		predicateColumnEditor.getComponentStateListeners().add(new ComponentStateListener() {
			
			@Override
			public void focusedChanged(Component component, Component obverseComponent) {
				if(predicateColumnEditor.isFocused() && predicateColumnEditor.getText().isEmpty())
				{
					suggestionPredicatesPopup.setSuggestionData(predicates);
					suggestionPredicatesPopup.open(predicateColumnEditor);
				}
			}
			
			@Override
			public void enabledChanged(Component component) {
			}
		});
		predicateColumnEditor.getTextInputContentListeners().add(new TextInputContentListener.Adapter() {
            @Override
            public void textInserted(TextInput textInput, int index, int count) {
                String text = textInput.getText();
                ArrayList<String> suggestions = new ArrayList<String>();
 
                for (String predicatename : predicates) {
                	if(predicatename==null)
                	{
                		continue; //needed for some reason
                	}
                    if (predicatename.toUpperCase().startsWith(text.toUpperCase())) {
                        suggestions.add(predicatename);
                    }
                }
 
                if (suggestions.getLength() > 0) {
                	suggestionPredicatesPopup.setSuggestionData(suggestions);
                	suggestionPredicatesPopup.open(textInput);
                }
                else
                {
                	suggestionPredicatesPopup.close();
                }
            }
 
            @Override
            public void textRemoved(TextInput textInput, int index, int count) {
                suggestionClassesPopup.close();
                if(textInput.getText().isEmpty())
                {
                	suggestionPredicatesPopup.setSuggestionData(predicates);
                	suggestionPredicatesPopup.open(predicateColumnEditor);
                }
            }
        });
		className.getComponentStateListeners().add(new ComponentStateListener() {
			
			@Override
			public void focusedChanged(Component component, Component obverseComponent) {
				if(className.isFocused() && className.getText().isEmpty())
				{
					suggestionClassesPopup.setSuggestionData(classes);
					suggestionClassesPopup.open(className);
				}
			}
			
			@Override
			public void enabledChanged(Component component) {
			}
		});
		className.getTextInputContentListeners().add(new TextInputContentListener.Adapter() {
            @Override
            public void textInserted(TextInput textInput, int index, int count) {
                String text = textInput.getText();
                ArrayList<String> suggestions = new ArrayList<String>();
 
                for (String classname : classes) {
                	if(classname==null)
                	{
                		continue; //needed for some reason
                	}
                    if (classname.toUpperCase().startsWith(text.toUpperCase())) {
                        suggestions.add(classname);
                    }
                }
 
                if (suggestions.getLength() > 0) {
                    suggestionClassesPopup.setSuggestionData(suggestions);
                    suggestionClassesPopup.open(textInput);
                }
                else
                {
                	suggestionClassesPopup.close();
                }
            }
 
            @Override
            public void textRemoved(TextInput textInput, int index, int count) {
                suggestionClassesPopup.close();
                if(textInput.getText().isEmpty())
                {
                	suggestionClassesPopup.setSuggestionData(classes);
    				suggestionClassesPopup.open(className);
                }
            }
        });
		tableView.getTableViewRowListeners().add(
				new TableViewRowListener.Adapter() {
					@Override
					public void rowsSorted(TableView tableView) {
						List<?> tableData = tableView.getTableData();
						if (tableData.getLength() > 0) {
							tableView.setSelectedIndex(0);
						}
					}
				});

		tableView.getTableViewSelectionListeners().add(
				new TableViewSelectionListener.Adapter() {
					@Override
					public void selectedRangesChanged(TableView tableView,
							Sequence<Span> previousSelectedRanges) {
						int firstSelectedIndex = tableView
								.getFirstSelectedIndex();
						if(firstSelectedIndex<0)
						{
							return;
						}
						if (!((ColumnReceipt) columnsfromsourcetable
								.get(firstSelectedIndex)).getColumnName()
								.equals("gid")) {
							removeSymbolsAction.setEnabled(firstSelectedIndex != -1);
						}
						else
						{
							removeSymbolsAction.setEnabled(false);
						}
						refreshDetail();
					}
				});

		tableView.getTableViewSortListeners().add(
				new TableViewSortListener.Adapter() {
					@Override
					@SuppressWarnings("unchecked")
					public void sortChanged(TableView tableView) {
						List<Object> tableData = (List<Object>) tableView
								.getTableData();
						tableData.setComparator(new TableViewRowComparator(
								tableView));
					}
				});

		tableView.getComponentKeyListeners().add(
				new ComponentKeyListener.Adapter() {
					@Override
					public boolean keyPressed(Component component, int keyCode,
							Keyboard.KeyLocation keyLocation) {
						if (keyCode == Keyboard.KeyCode.DELETE
								|| keyCode == Keyboard.KeyCode.BACKSPACE) {
							removeSymbolsAction.perform(component);
						} else if (keyCode == Keyboard.KeyCode.A
								&& Keyboard.isPressed(Platform
										.getCommandModifier())) {
							tableView.selectAll();
						}

						return false;
					}
				});
		// Add symbol text input event handlers
				symbolTextInput.getComponentMouseButtonListeners().add(
						new ComponentMouseButtonListener() {

							@Override
							public boolean mouseUp(Component arg0,
									org.apache.pivot.wtk.Mouse.Button arg1, int arg2,
									int arg3) {
								addSymbolAction.setEnabled(symbolTextInput
										.getSelectedIndex() >= 0);
								return false;
							}

							@Override
							public boolean mouseDown(Component arg0,
									org.apache.pivot.wtk.Mouse.Button arg1, int arg2,
									int arg3) {
								addSymbolAction.setEnabled(symbolTextInput
										.getSelectedIndex() >= 0);
								return false;
							}

							@Override
							public boolean mouseClick(Component arg0,
									org.apache.pivot.wtk.Mouse.Button arg1, int arg2,
									int arg3, int arg4) {
								addSymbolAction.setEnabled(symbolTextInput
										.getSelectedIndex() >= 0);
								return false;
							}
						});

				symbolTextInput.getComponentKeyListeners().add(
						new ComponentKeyListener.Adapter() {
							@Override
							public boolean keyPressed(Component component, int keyCode,
									Keyboard.KeyLocation keyLocation) {
								if (keyCode == Keyboard.KeyCode.ENTER) {
									if (addSymbolAction.isEnabled()) {
										addSymbolAction.perform(component);
									}
								}

								return false;
							}
						});
				addSymbolButton.setAction(addSymbolAction);
				removeSymbolsButton.setAction(removeSymbolsAction);
	}
	public void setTypes(List<String> data)
	{
		types.setListData(data);
	}
	public void addType(String data)
	{
		Object f =data;
		List<String> list=(List<String>) types.getListData();
		list.add(data);
	}
	public boolean setData(List<ColumnReceipt> quotes,List<ColumnReceipt> originallist)
	{
		columnsfromsourcetable=quotes;
		symbolTextInput.setListData(originallist);
		@SuppressWarnings("unchecked")
		List<ColumnReceipt> tableData = (List<ColumnReceipt>) tableView.getTableData();
		Comparator<ColumnReceipt> comparator = tableData.getComparator();
		quotes.setComparator(comparator);
		tableView.setTableData(quotes);
		refreshDetail();
		return true;
	}
	
	public boolean addData(ColumnReceipt data)
	{
		@SuppressWarnings("unchecked")
		List<Object> tableData = (List<Object>) tableView.getTableData();
		int index = tableData.add(data);
		tableView.setSelectedIndex(index);
		refreshDetail();
		return true;
	}
	public boolean removeData(int position)
	{
		int selectedIndex = tableView.getFirstSelectedIndex();
		if(selectedIndex<0)
		{
			return false;
		}
		ArrayList<Span> spanList = new ArrayList<Span>(tableView.getSelectedRanges());

		// remove spans in reverse order to prevent
		// IndexOutOfBoundsException
		ItemIterator<Span> it = spanList.iterator();
		it.toEnd();
		while (it.hasPrevious()) {
			Span span = it.previous();
			tableView.getTableData().remove(span.start,(int) span.getLength());
		}
		tableView.setSelectedIndex(selectedIndex);
		refreshDetail();
		return true;
	}

	private void refreshDetail() {
		ColumnReceipt stockQuote = null;

		int firstSelectedIndex = tableView.getFirstSelectedIndex();
		if (firstSelectedIndex != -1) {
			int lastSelectedIndex = tableView.getLastSelectedIndex();

			if (firstSelectedIndex == lastSelectedIndex) {
				@SuppressWarnings("unchecked")
				List<ColumnReceipt> tableData = (List<ColumnReceipt>) tableView.getTableData();
				stockQuote = tableData.get(firstSelectedIndex);
			} else {
				stockQuote = new ColumnReceipt();
			}
		} else {
			stockQuote = new ColumnReceipt();
		}

		detailPane.load(new BeanAdapter(stockQuote));
		
	}
	
	
	/*Actions*/
	// Action invoked to add a new symbol
		private Action addSymbolAction = new Action(false) {
			@Override
			public void perform(Component source) {
				ColumnReceipt stockQuote = new ColumnReceipt(
						(ColumnReceipt) symbolTextInput.getSelectedItem());
				addData(stockQuote);
			}
		};

		// Action invoke to remove selected symbols
		private Action removeSymbolsAction = new Action(false) {
			@Override
			public void perform(Component source) {
				int selectedIndex = tableView.getFirstSelectedIndex();
				removeData(selectedIndex);
				if (selectedIndex == -1) {
					refreshDetail();
					symbolTextInput.requestFocus();
				}
			}
		};
		public void setTitle(String stringtitle)
		{
			this.expander.setTitle(stringtitle);
			this.title.setText(stringtitle);
		}
		public String getTitle()
		{
			return this.title.getText();
		}
		
		public TableName getTablemame() {
			return tablemame;
		}
		public void setTablemame(TableName tablemame) {
			this.tablemame = tablemame;
		}
		public void setOntologyClass(String classname)
		{
			className.setText(classname);
		}
		public String getOntologyClass()
		{
			return className.getText();
		}
		public boolean isGeometrytable() {
			return geometrytable;
		}
		public void setGeometrytable(boolean geometrytable) {
			this.geometrytable = geometrytable;
		}
		public List<String> getClasses() {
			return classes;
		}
		public void setClasses(List<String> classes) {
			this.classes = classes;
		}
		
		public List<String> getPredicates() {
			return predicates;
		}
		public void setPredicates(List<String> classes) {
			this.predicates = classes;
		}
}
