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
		MenuItem addArtist = menu.add(R.string.removeSong);
		addArtist.setOnMenuItemClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0,CLEAR, 0, R.string.clear).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		
		return result;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case CLEAR:
			try {
				Contexte.getInstance().getMpd().getPlaylist().clear();
				songlist.clear();
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
    Music m = musics.get(position);

    try {
      Contexte.getInstance().getMpd().skipTo(m.getSongId());
    } catch (MPDServerException e) {
    }
		
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		try {
			Contexte.getInstance().getMpd().getPlaylist().removeSong(songId);
			songlist.remove(arrayListId);
			((SimpleAdapter)getListAdapter()).notifyDataSetChanged();
		} catch (MPDServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

}
