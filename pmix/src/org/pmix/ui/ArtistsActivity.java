package org.pmix.ui;

import java.util.ArrayList;
import java.util.List;

import org.a0z.mpd.MPDServerException;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemLongClickListener;

public class ArtistsActivity extends ListActivity implements OnItemLongClickListener {

	private List<String> items = new ArrayList<String>();

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.artists);

		items.clear();
		try {

			items.addAll(Contexte.getInstance().getMpd().listArtists());
			//ListView myList = (ListView)findViewById(R.id.android:list);
			//myList.setOnItemLongClickListener(this);
			/*
			ListView list = this.getListView();
			list.setOnItemLongClickListener(this);
			*/
			ArrayAdapter<String> notes = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
			setListAdapter(notes);
		} catch (MPDServerException e) {
			e.printStackTrace();
			this.setTitle(e.getMessage());
		}
		ListView list = this.getListView();
		registerForContextMenu(list);
		

	}	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		String artist = (String) this.getListView().getItemAtPosition(info.position);

		menu.setHeaderTitle(artist);
		MenuItem addArtist = menu.add("Add Artist");
		addArtist.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				
				return true;
			}
		});



	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(this, AlbumsActivity.class);
		intent.putExtra("artist", items.get(position));
		startActivityForResult(intent, -1);
	}
	

	/*
	 * @author slubman
	 *  This Override appear as an error in eclipse
	@Override
	*/
	public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long id) {
		Intent intent = new Intent(this, AlbumsActivity.class);
		intent.putExtra("artist", items.get(position));
		startActivityForResult(intent, -1);
		return false;
	}


}
