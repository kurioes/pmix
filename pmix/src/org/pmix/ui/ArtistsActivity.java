package org.pmix.ui;

import java.util.ArrayList;
import java.util.List;

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

public class ArtistsActivity extends BrowseActivity implements AsyncExecListener {
	// Define this as public, more efficient due to the access of a anonymous inner class...
	// TODO: Is static really the solution? No, should be cashed in JMPDComm ,but it loads 
	// it only once with this "hotfix"...
	public static List<String> items = null;
	private int iJobID = -1;
	private ProgressDialog pd;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.artists);

		pd = ProgressDialog.show(ArtistsActivity.this, "Loading...", "Load Artists...");
		
		ListView list = getListView();
		registerForContextMenu(list);
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		if(items == null)
		{
			// Loading Artists asynchronous...
			MPDApplication app = (MPDApplication)getApplication();
			app.oMPDAsyncHelper.addAsyncExecListener(this);
			iJobID = app.oMPDAsyncHelper.execAsync(new Runnable(){
				@SuppressWarnings("unchecked")
				@Override
				public void run() 
				{
					try {
						MPDApplication app = (MPDApplication)getApplication();
						items = (List)app.oMPDAsyncHelper.oMPD.listArtists();
					} catch (MPDServerException e) {
						
					}
				}
			});
		}
		else
		{
			// Yes, its our job which is done...
			ArrayAdapter<String> artistsAdapter = new ArrayAdapter<String>(ArtistsActivity.this, android.R.layout.simple_list_item_1, items);
			setListAdapter(artistsAdapter);
			pd.dismiss();
		}
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
					MPDApplication app = (MPDApplication)getApplication();
					ArrayList<Music> songs = new ArrayList<Music>(app.oMPDAsyncHelper.oMPD.find(MPD.MPD_FIND_ARTIST, artist));
					app.oMPDAsyncHelper.oMPD.getPlaylist().add(songs);
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
	public void asyncExecSucceeded(int jobID) {
		if(iJobID == jobID)
		{
			// Yes, its our job which is done...
			ArrayAdapter<String> artistsAdapter = new ArrayAdapter<String>(ArtistsActivity.this, android.R.layout.simple_list_item_1, items);
			setListAdapter(artistsAdapter);
			// No need to listen further...
			MPDApplication app = (MPDApplication)getApplication();
			app.oMPDAsyncHelper.removeAsyncExecListener(this);
			pd.dismiss();
		}
	}
}
