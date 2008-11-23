package org.pmix.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDServerException;
import org.a0z.mpd.Music;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class ArtistsActivity extends ListActivity {
	// TODO: Is static really the solution? No, should be cashed in JMPDComm ,but it loads it only once with this "hotfix"...
	private static List<String> items = null;

	public final static int MAIN = 0;
	public final static int PLAYLIST = 3;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.artists);

		Thread th = new Thread(){
			ProgressDialog pd;
			// Thread gets Album data...
			@Override
			public void start() {
				pd = ProgressDialog.show(ArtistsActivity.this, "Loading...", "Load Artists...");
				super.start();
			}
			@Override
			public void run() {
				try {
					if(items == null)
						items = (List)MainMenuActivity.oMPDAsyncHelper.oMPD.listArtists();
					runOnUiThread(new Runnable(){
						// Sets Album data to the UI...
						public void run() {
							ArrayAdapter<String> artistsAdapter = new ArrayAdapter<String>(ArtistsActivity.this, android.R.layout.simple_list_item_1, items);
							setListAdapter(artistsAdapter);
						}
					});
					pd.dismiss();
				} catch (MPDServerException e) {
					e.printStackTrace();
				}
			}
		};
		th.start();
		ListView list = getListView();
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
					ArrayList<Music> songs = new ArrayList<Music>(MainMenuActivity.oMPDAsyncHelper.oMPD.find(MPD.MPD_FIND_ARTIST, artist));
					MainMenuActivity.oMPDAsyncHelper.oMPD.getPlaylist().add(songs);
					MainMenuActivity.notifyUser(String.format(getResources().getString(R.string.artistAdded), artist), ArtistsActivity.this);
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
            Intent intent = new Intent(this, AlbumsActivity.class);
            intent.putExtra("artist", items.get(position));
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
