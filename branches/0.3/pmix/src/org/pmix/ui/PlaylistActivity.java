package org.pmix.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.a0z.mpd.MPD;
import org.a0z.mpd.Music;
import org.a0z.mpd.MPDPlaylist;
import org.a0z.mpd.MPDServerException;
import org.pmix.ui.Contexte;

import android.app.Activity;
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
import android.widget.SimpleAdapter;

public class PlaylistActivity extends ListActivity implements OnMenuItemClickListener {
	private ArrayList<HashMap<String,Object>> songlist = new ArrayList<HashMap<String,Object>>();
	private List<Music> musics;
	private int arrayListId;
	private int songId;
	private String title;
	
	public static final int MAIN = 0;
	public static final int CLEAR = 1;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
			setContentView(R.layout.artists);
		
		try {
			MPDPlaylist playlist = Contexte.getInstance().getMpd().getPlaylist();
			playlist.refresh();
			musics = playlist.getMusics();
			for(Music m : musics) {
				HashMap<String,Object> item = new HashMap<String,Object>();
				item.put( "songid", m.getSongId() );
				item.put( "artist", m.getArtist() );
				item.put( "title", m.getTitle() );
				songlist.add(item);
			}
			SimpleAdapter songs = new SimpleAdapter( 
					this, 
					songlist,
					android.R.layout.simple_list_item_2,
					new String[] { "title","artist" },
					new int[] { android.R.id.text1, android.R.id.text2 }  );
			setListAdapter( songs );
		} catch (MPDServerException e) {
		}

		ListView list = getListView();
		registerForContextMenu(list);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		arrayListId = info.position;
		songId = (Integer)songlist.get(info.position).get("songid");
		title = (String)songlist.get(info.position).get("title");

		menu.setHeaderTitle(title);
		MenuItem addArtist = menu.add(menu.NONE, 0, 0, R.string.removeSong);
		addArtist.setOnMenuItemClickListener(this);
	}


	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			try {
				Contexte.getInstance().getMpd().getPlaylist().removeSong(songId);
				songlist.remove(arrayListId); 
				Contexte.getInstance().getMpd().getPlaylist().refresh(); // If not refreshed an intern Array of JMPDComm get out of sync and throws IndexOutOfBound
				MainMenuActivity.notifyUser(getResources().getString(R.string.deletedSongFromPlaylist), this);
				((SimpleAdapter)getListAdapter()).notifyDataSetChanged();
			} catch (MPDServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
	
	/*
	 * Create Menu for Playlist View
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0,MAIN, 0, R.string.mainMenu).setIcon(android.R.drawable.ic_menu_revert);
		menu.add(0,CLEAR, 1, R.string.clear).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return result;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Menu actions...
		switch (item.getItemId()) {
		case MAIN:
			Intent i = new Intent(this, MainMenuActivity.class);
			startActivity(i);
			return true;
		case CLEAR:
			try {
				Contexte.getInstance().getMpd().getPlaylist().clear();
				songlist.clear();
				MainMenuActivity.notifyUser(getResources().getString(R.string.playlistCleared), this);
				((SimpleAdapter)getListAdapter()).notifyDataSetChanged();
			} catch (MPDServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		default:
			return false;
		}

	}

	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// Play selected Song
		Music m = musics.get(position);
	    try {
	    	Contexte.getInstance().getMpd().skipTo(m.getSongId());
	    } catch (MPDServerException e) {
	    }
			
	}

}
