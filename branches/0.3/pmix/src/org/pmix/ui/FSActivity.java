package org.pmix.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.a0z.mpd.Directory;
import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDServerException;
import org.a0z.mpd.Music;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
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

public class FSActivity extends ListActivity implements OnMenuItemClickListener {
	private List<String> items = new ArrayList<String>();
	public final static int MAIN = 0;
	public final static int PLAYLIST = 3;
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
				currentDirectory = Contexte.getInstance().getMpd().getRootDirectory().makeDirectory((String) this.getIntent().getStringExtra("directory"));
			} else {
				currentDirectory = Contexte.getInstance().getMpd().getRootDirectory();
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

				//Collection<Music> founds = Contexte.getInstance().getMpd().getPlaylist().("filename", music.getFullpath());
				
				// not found
				//if (founds.isEmpty()) {
					Contexte.getInstance().getMpd().getPlaylist().add(music);
					//songId = 
				//} else {
					// found
					//songId = founds.toArray(new Music[founds.size()])[0].getSongId();
				//}
				if (songId > -1) {
					Contexte.getInstance().getMpd().skipTo(songId);
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

	private Collection getAllFiles(Directory dir)
	{
		try {
			dir.refreshData();
		} catch (MPDServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Collection files = dir.getFiles();
		Collection dirs = dir.getDirectories();
		Iterator itr = dirs.iterator();
		
		while(itr.hasNext())
		{
			Directory actdir = (Directory)itr.next();
			files.addAll(getAllFiles(actdir));
			files.addAll(actdir.getFiles());
		}
		return files;
	}

	public boolean onMenuItemClick(MenuItem item) {
		try {
			Collection files = getAllFiles(currentContextDirectory);
			Contexte.getInstance().getMpd().getPlaylist().add(files);
			MainMenuActivity.notifyUser(getResources().getString(R.string.addedDirectoryToPlaylist), this);
			//((SimpleAdapter)getListAdapter()).notifyDataSetChanged();
		} catch (MPDServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
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
