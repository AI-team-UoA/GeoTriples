package eu.linkedeodata.geotriples.gui;

import java.net.URL;
import java.util.Collection;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.PushButton;
import org.d2rq.db.schema.TableName;

public class AddSourceTable extends Prompt implements Bindable {
	@BXML private ListView listView=null;
	@BXML private PushButton selectSource=null;
	
	private Collection<TableName> sources;
	private List<TableName> selectedItems;
	public void addSources(Collection<TableName> sources) {
		this.sources=sources;
		ArrayList<TableName> temp=new ArrayList<TableName>();
		for(TableName tn:sources)
		{
			temp.add(tn);
		}
		listView.setListData(temp);
	}
	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		
		selectSource.setAction(new Action() {
			
			@Override
			public void perform(Component source) {
				@SuppressWarnings({ "unchecked", "unused" })
				Sequence<TableName> mm=(Sequence<TableName>) listView.getSelectedItems();
				if( listView.getSelectedItems().getLength()==0)
				{
					return;
				}
				AddSourceTable.this.selectedItems=new ArrayList<TableName>();
				for(int i=0;i<mm.getLength();++i)
				{
					TableName selecteditem=mm.get(i);
					AddSourceTable.this.selectedItems.add(selecteditem);
				}
				
				close(true);
			}
		});
	}
	
	public List<TableName> getSelectedSources()
	{
		return selectedItems;
	}

}
