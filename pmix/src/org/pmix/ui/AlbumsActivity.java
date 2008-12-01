package org.pmix.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDServerException;
import org.a0z.mpd.Music;
import org.pmix.ui.MPDAsyncHelper.AsyncExecListener;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AlbumsActivity extends BrowseActivity implements AsyncExecListener {

	private static List<String> items;
	private int iJobID = -1;
	private ProgressDialog pd;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.artists);
		
		pd = ProgressDialog.show(AlbumsActivity.this, "Loading...", "Load Albums...");

		setTitle((String) getIntent().getStringExtra("artist"));
		
		ListView list = getListView();
		registerForContextMenu(list);
	}
	


	@Override
	protected void onStart() {
		super.onStart();
		
		// Loading Albums asynchronous...
		MainMenuActivity.oMPDAsyncHelper.addAsyncExecListener(this);
		iJobID = MainMenuActivity.oMPDAsyncHelper.execAsync(new Runnable(){
			@SuppressWarnings("unchecked")
			@Override
			public void run() 
			{
				try {
					if (getIntent().getStringExtra("artist") != null) {
						items = (List)MainMenuActivity.oMPDAsyncHelper.oMPD.listAlbums((String) getIntent().getStringExtra("artist"));
					} else {
						items = (List)MainMenuActivity.oMPDAsyncHelper.oMPD.listAlbums();
					}
				} catch (MPDServerException e) {
					
				}
			}
		});
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
	public void asyncExecSucceeded(int jobID) {
		if(iJobID == jobID)
		{
			// Yes, its our job which is done...
			ArrayAdapter<String> notes = new ArrayAdapter<String>(AlbumsActivity.this, android.R.layout.simple_list_item_1, items);
			setListAdapter(notes);
			// No need to listen further...
			MainMenuActivity.oMPDAsyncHelper.removeAsyncExecListener(this);
			pd.dismiss();
		}
	}
}
