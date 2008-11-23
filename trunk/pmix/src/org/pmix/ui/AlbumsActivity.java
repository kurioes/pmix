package org.pmix.ui;

import java.util.ArrayList;
import java.util.List;

import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDServerException;
import org.a0z.mpd.Music;

import android.app.ListActivity;
import android.app.ProgressDialog;
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

	private List<String> items;

	public final static int MAIN = 0;
	public final static int PLAYLIST = 3;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.artists);

		setTitle((String) getIntent().getStringExtra("artist"));
		Thread th = new Thread(){
			ProgressDialog pd;
			// Thread gets Album data...
			@Override
			public void start() {
				pd = ProgressDialog.show(AlbumsActivity.this, "Loading...", "Load Albums...");
				super.start();
			}
			@Override
			public void run() {
				try {
					if (getIntent().getStringExtra("artist") != null) {
						items = (List)MainMenuActivity.oMPDAsyncHelper.oMPD.listAlbums((String) getIntent().getStringExtra("artist"));
					} else {
						items = (List)MainMenuActivity.oMPDAsyncHelper.oMPD.listAlbums();
					}
					pd.dismiss();
					runOnUiThread(new Runnable(){
						// Sets Album data to the UI...
						public void run() {
							ArrayAdapter<String> notes = new ArrayAdapter<String>(AlbumsActivity.this, android.R.layout.simple_list_item_1, items);
							setListAdapter(notes);
							ListView list = getListView();
							registerForContextMenu(list);
						}
					});
				} catch (MPDServerException e) {
					e.printStackTrace();
					setTitle(e.getMessage());
				}
			}
		};
		th.start();
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
					ArrayList<Music> songs = new ArrayList<Music>(MainMenuActivity.oMPDAsyncHelper.oMPD.find(MPD.MPD_FIND_ALBUM, album));
					MainMenuActivity.oMPDAsyncHelper.oMPD.getPlaylist().add(songs);
					MainMenuActivity.notifyUser(String.format(getResources().getString(R.string.albumAdded),album), AlbumsActivity.this);
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
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
