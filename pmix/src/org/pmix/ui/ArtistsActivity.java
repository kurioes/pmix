package org.pmix.ui;

import java.util.ArrayList;
import java.util.List;

import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDServerException;
import org.a0z.mpd.Music;

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

public class ArtistsActivity extends ListActivity {

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
		MenuItem addArtist = menu.add(R.string.addArtist);
		addArtist.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			private String artist;
			public boolean onMenuItemClick(MenuItem item) {
				try {
					ArrayList<Music> songs = new ArrayList<Music>(Contexte.getInstance().getMpd().find(MPD.MPD_FIND_ARTIST, artist));
					Contexte.getInstance().getMpd().getPlaylist().add(songs);
				} catch (MPDServerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
			public OnMenuItemClickListener setArtist(String artist)
			{
				this.artist = artist;
				return this;
			}
		}.setArtist(artist));
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
