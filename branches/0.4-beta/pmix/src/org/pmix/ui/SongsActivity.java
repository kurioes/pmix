package org.pmix.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDServerException;
import org.a0z.mpd.Music;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SongsActivity extends BrowseActivity {

	private List<Music> musics = null;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.artists);
		List<String> items = new ArrayList<String>();

		try {
			MPDApplication app = (MPDApplication)getApplication();
			String album = (String) this.getIntent().getStringExtra("album");
			this.setTitle(album);
			musics = new ArrayList<Music>(app.oMPDAsyncHelper.oMPD.find(MPD.MPD_FIND_ALBUM, album));

			for (Music music : musics) {
				items.add(music.getTitle());
			}

			// items.addAll(MainMenuActivity.oMPDAsyncHelper.oMPD..listAlbums(artist));

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
			MPDApplication app = (MPDApplication)getApplication();

			int songId = -1;
			// try to find it in the current playlist first

			//Collection<Music> founds = MainMenuActivity.oMPDAsyncHelper.oMPD.getPlaylist().find("filename", music.getFullpath());
			
			// not found
			//if (founds.isEmpty()) {
				//songId = 
				app.oMPDAsyncHelper.oMPD.getPlaylist().add(music);
			//} else {
				// found
			//	songId = founds.toArray(new Music[founds.size()])[0].getSongId();
			//}
			if (songId > -1) {
				app.oMPDAsyncHelper.oMPD.skipTo(songId);
			}
			
		} catch (MPDServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
