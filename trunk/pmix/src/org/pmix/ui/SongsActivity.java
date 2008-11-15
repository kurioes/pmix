package org.pmix.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDServerException;
import org.a0z.mpd.Music;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SongsActivity extends ListActivity {

	private List<Music> musics = null;

	public final static int MAIN = 0;
	public final static int PLAYLIST = 3;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.artists);
		List<String> items = new ArrayList<String>();

		try {
			String album = (String) this.getIntent().getStringExtra("album");
			this.setTitle(album);
			musics = new ArrayList<Music>(Contexte.getInstance().getMpd().find(MPD.MPD_FIND_ALBUM, album));

			for (Music music : musics) {
				items.add(music.getTitle());
			}

			// items.addAll(Contexte.getInstance().getMpd()..listAlbums(artist));

			ArrayAdapter<String> notes = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
			setListAdapter(notes);
		} catch (MPDServerException e) {
			e.printStackTrace();
			this.setTitle(e.getMessage());
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		Music music = musics.get(position);
		try {

			int songId = -1;
			// try to find it in the current playlist first

			//Collection<Music> founds = Contexte.getInstance().getMpd().getPlaylist().find("filename", music.getFullpath());
			
			// not found
			//if (founds.isEmpty()) {
				//songId = 
				Contexte.getInstance().getMpd().getPlaylist().add(music);
			//} else {
				// found
			//	songId = founds.toArray(new Music[founds.size()])[0].getSongId();
			//}
			if (songId > -1) {
				Contexte.getInstance().getMpd().skipTo(songId);
			}
			
		} catch (MPDServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
