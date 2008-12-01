package org.pmix.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.a0z.mpd.Directory;
import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDServerException;
import org.a0z.mpd.Music;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class FSActivity extends BrowseActivity implements OnMenuItemClickListener {
	private List<String> items = new ArrayList<String>();
	private Directory currentDirectory = null;
	private Directory currentContextDirectory = null;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.files);

		items.clear();
		try {

			if (this.getIntent().getStringExtra("directory") != null) {
				currentDirectory = MainMenuActivity.oMPDAsyncHelper.oMPD.getRootDirectory().makeDirectory((String) this.getIntent().getStringExtra("directory"));
			} else {
				currentDirectory = MainMenuActivity.oMPDAsyncHelper.oMPD.getRootDirectory();
			}
			currentDirectory.refreshData();

			Collection<Directory> directories = currentDirectory.getDirectories();
			for (Directory child : directories) {
				items.add(child.getName());
			}

			Collection<Music> musics = currentDirectory.getFiles();
			for (Music music : musics) {
				items.add(music.getTitle());
			}

			ArrayAdapter<String> notes = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
			setListAdapter(notes);
			registerForContextMenu(getListView());
		} catch (MPDServerException e) {
			e.printStackTrace();
			this.setTitle(e.getMessage());
		}

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		String dirname = (String) this.getListView().getItemAtPosition(info.position);
		if (info.position > currentDirectory.getDirectories().size() - 1 || currentDirectory.getDirectories().size() == 0) {
			// Its a file, no Menu needed...
			return;
		}
		currentContextDirectory = (Directory) currentDirectory.getDirectories().toArray()[info.position];
		menu.setHeaderTitle(dirname);
		MenuItem addArtist = menu.add(R.string.addDirectory);
		addArtist.setOnMenuItemClickListener(this);
	}
	
	
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		// click on a file
		if (position > currentDirectory.getDirectories().size() - 1 || currentDirectory.getDirectories().size() == 0) {

			Music music = (Music) currentDirectory.getFiles().toArray()[position - currentDirectory.getDirectories().size()];

			try {

				int songId = -1;
				// try to find it in the current playlist first

				//Collection<Music> founds = MainMenuActivity.oMPDAsyncHelper.oMPD.getPlaylist().("filename", music.getFullpath());
				
				// not found
				//if (founds.isEmpty()) {
					MainMenuActivity.oMPDAsyncHelper.oMPD.getPlaylist().add(music);
					//songId = 
				//} else {
					// found
					//songId = founds.toArray(new Music[founds.size()])[0].getSongId();
				//}
				if (songId > -1) {
					MainMenuActivity.oMPDAsyncHelper.oMPD.skipTo(songId);
				}
				
			} catch (MPDServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// click on a directory
			// open the same subactivity, it would be better to reuse the
			// same instance

			Intent intent = new Intent(this, FSActivity.class);
			String dir;

			dir = ((Directory) currentDirectory.getDirectories().toArray()[position]).getFullpath();

			intent.putExtra("directory", dir);
			startActivityForResult(intent, -1);

		}

	}

	public boolean onMenuItemClick(MenuItem item) {
		try {
			MainMenuActivity.oMPDAsyncHelper.oMPD.getPlaylist().add(currentContextDirectory);
			MainMenuActivity.notifyUser(getResources().getString(R.string.addedDirectoryToPlaylist), this);
			//((SimpleAdapter)getListAdapter()).notifyDataSetChanged();
		} catch (MPDServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
}
