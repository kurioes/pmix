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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AlbumsActivity extends ListActivity {

	private List<String> items = new ArrayList<String>();

	public final static int MAIN = 0;
	public final static int PLAYLIST = 3;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.artists);

		try {
			items.clear();

			if (this.getIntent().getStringExtra("artist") != null) {
				items.addAll(Contexte.getInstance().getMpd().listAlbums((String) this.getIntent().getStringExtra("artist")));
				this.setTitle((String) this.getIntent().getStringExtra("artist"));
			} else {
				items.addAll(Contexte.getInstance().getMpd().listAlbums());
			}

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
		String album = (String) this.getListView().getItemAtPosition(info.position);

		menu.setHeaderTitle(album);
		MenuItem addArtist = menu.add(R.string.addAlbum);
		addArtist.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			private String album;
			public boolean onMenuItemClick(MenuItem item) {
				try {
					ArrayList<Music> songs = new ArrayList<Music>(Contexte.getInstance().getMpd().find(MPD.MPD_FIND_ALBUM, album));
					Contexte.getInstance().getMpd().getPlaylist().add(songs);
					MainMenuActivity.notifyUser("Album " + album + " added", AlbumsActivity.this);
				} catch (MPDServerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
			public OnMenuItemClickListener setAlbum(String album)
			{
				this.album = album;
				return this;
			}
		}.setAlbum(album));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(this, SongsActivity.class);
		intent.putExtra("album", items.get(position));
		startActivityForResult(intent, -1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0,MAIN, 0, R.string.mainMenu).setIcon(android.R.drawable.ic_menu_revert);
		menu.add(0,PLAYLIST, 1, R.string.playlist).setIcon(R.drawable.ic_menu_pmix_playlist);
		
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent i = null;
		
		switch (item.getItemId()) {

		case MAIN:
			i = new Intent(this, MainMenuActivity.class);
			startActivity(i);
			return true;
		case PLAYLIST:
			i = new Intent(this, PlaylistActivity.class);
			startActivityForResult(i, PLAYLIST);
			return true;
		}
		return false;
	}
}
