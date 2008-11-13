package org.pmix.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDServerException;
import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.MPDStatusMonitor;
import org.a0z.mpd.event.MPDConnectionStateChangedEvent;
import org.a0z.mpd.event.MPDPlaylistChangedEvent;
import org.a0z.mpd.event.MPDRandomChangedEvent;
import org.a0z.mpd.event.MPDRepeatChangedEvent;
import org.a0z.mpd.event.MPDStateChangedEvent;
import org.a0z.mpd.event.MPDTrackChangedEvent;
import org.a0z.mpd.event.MPDTrackPositionChangedEvent;
import org.a0z.mpd.event.MPDUpdateStateChangedEvent;
import org.a0z.mpd.event.MPDVolumeChangedEvent;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Layout.Alignment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainMenuActivity extends Activity implements StatusChangeListener, TrackPositionListener, OnSharedPreferenceChangeListener {

	public static final String PREFS_NAME = "pmix.properties";

	public static final int PLAYLIST = 1;
	
	public static final int ARTISTS = 2;

	public static final int SETTINGS = 5;

	private TextView artistNameText;

	private TextView songNameText;

	private TextView albumNameText;

	private MPDStatusMonitor monitor;

	public static final int ALBUMS = 4;

	public static final int FILES = 3;

	private TextView mainInfo = null;

	private HorizontalSlider progressBar = null;

	private HorizontalSlider progressBarTrack = null;

	private TextView trackTime = null;

	private MyHandler handler;

	private ImageSwitcher coverSwitcher;

	private ProgressBar coverSwitcherProgress;

	private static final int VOLUME_STEP = 5;

	private static final int TRACK_STEP = 10;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case SETTINGS:
			init();
			break;

		default:
			break;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		try {
			monitor = new MPDStatusMonitor(Contexte.getInstance().getMpd(), 1000);
			monitor.addStatusChangeListener(this);
			monitor.addTrackPositionListener(this);
			monitor.start();
		} catch (MPDServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	private void init() {
		setContentView(R.layout.main);
		
		mainInfo = (TextView) findViewById(R.id.mainInfo);
		progressBar = (HorizontalSlider) findViewById(R.id.progress_volume);
		artistNameText = (TextView) findViewById(R.id.artistName);
		albumNameText = (TextView) findViewById(R.id.albumName);
		songNameText = (TextView) findViewById(R.id.songName);

		progressBarTrack = (HorizontalSlider) findViewById(R.id.progress_track);

		trackTime = (TextView) findViewById(R.id.trackTime);

		try {

			// Initial Connect => Set Host (TODO: fix, this is ugly)

			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());//getSharedPreferences("org.pmix", MODE_PRIVATE);
			settings.registerOnSharedPreferenceChangeListener(this);
			String serverAddress = settings.getString("hostname", "");
			Contexte.getInstance().setServerAddress(serverAddress);
			String mpdVersion = Contexte.getInstance().getMpd().getMpdVersion();

			((TextView) findViewById(R.id.volume)).setTextSize(12);
			((TextView) findViewById(R.id.track)).setTextSize(12);
			((TextView) findViewById(R.id.trackTime)).setTextSize(12);

			coverSwitcher = (ImageSwitcher) findViewById(R.id.albumCover);
			/*
			coverSwitcherProgress = (ProgressBar) findViewById(R.id.albumCoverProgress); 
			coverSwitcherProgress.setIndeterminate(true);
			coverSwitcherProgress.setVisibility(ProgressBar.INVISIBLE);
			*/
			handler = new MyHandler(this);
			coverSwitcher.setFactory(handler);

			StringBuffer stringBuffer = new StringBuffer(100);
			
			String serverAdress = settings.getString("hostname", "");
			stringBuffer.append("\nMPD version " + mpdVersion + " running at " + serverAdress + "\n");
			
			org.pmix.ui.Contexte.getInstance().getMpd().getPlaylist().refresh();
			
			monitor = new MPDStatusMonitor(Contexte.getInstance().getMpd(), 1000);
			monitor.addStatusChangeListener(this);
			monitor.addTrackPositionListener(this);

			ImageButton button = (ImageButton) findViewById(R.id.next);
			button.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {

					try {
						Contexte.getInstance().getMpd().next();
					} catch (MPDServerException e) {
						e.printStackTrace();
					}
				}
			});

			button = (ImageButton) findViewById(R.id.prev);
			button.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					try {
						Contexte.getInstance().getMpd().previous();
					} catch (MPDServerException e) {
						e.printStackTrace();
					}
				}
			});

			button = (ImageButton) findViewById(R.id.back);
			button.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {

					try {
						Contexte.getInstance().getMpd().seek(handler.getLastKnownElapsedTime() - TRACK_STEP);
					} catch (MPDServerException e) {
						e.printStackTrace();
					}
				}
			});

			button = (ImageButton) findViewById(R.id.playpause);
			button.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {

					try {
						if(Contexte.getInstance().getMpd().getStatus().getState() == Contexte.getInstance().getMpd().getStatus().MPD_STATE_PLAYING)
							Contexte.getInstance().getMpd().pause();
						else
							Contexte.getInstance().getMpd().play();
					} catch (MPDServerException e) {
						e.printStackTrace();
					}
				}
			});

			button = (ImageButton) findViewById(R.id.forward);
			button.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					try {
						Contexte.getInstance().getMpd().seek(handler.getLastKnownElapsedTime() + TRACK_STEP);
					} catch (MPDServerException e) {
						e.printStackTrace();
					}
				}
			});

			progressBar.setOnProgressChangeListener(new HorizontalSlider.OnProgressChangeListener() {

				@Override
				public void onProgressChanged(View v, int progress) {
					try {
						Contexte.getInstance().getMpd().setVolume(progress);
					} catch (MPDServerException e) {
						e.printStackTrace();
					}

				}
			});
			progressBarTrack.setOnProgressChangeListener(new HorizontalSlider.OnProgressChangeListener() {

				@Override
				public void onProgressChanged(View v, int progress) {
					try {
						int position = (progress * handler.getCurrentSongTime()) / 100;
						Contexte.getInstance().getMpd().seek(position);
					} catch (MPDServerException e) {
						e.printStackTrace();
					}

				}
			});
			
			monitor.start();
			mainInfo.setText(stringBuffer.toString());

		} catch (MPDServerException e) {
			this.setTitle("Error");

			
			mainInfo.setText(e.getMessage());
		}
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// mainInfo.setText(keyCode + "");
		try {
			switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_UP:
				progressBar.incrementProgressBy(VOLUME_STEP);
				Contexte.getInstance().getMpd().adjustVolume(VOLUME_STEP);
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				progressBar.incrementProgressBy(-VOLUME_STEP);
				Contexte.getInstance().getMpd().adjustVolume(-VOLUME_STEP);
				return true;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				Contexte.getInstance().getMpd().previous();
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				Contexte.getInstance().getMpd().next();
				return true;
			default:
				return false;
			}
		} catch (MPDServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());//getSharedPreferences("org.pmix", MODE_PRIVATE);
		if (settings.getString("hostname", "").equals("")) {
			this.startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS);
		}
		else 
		{
			init();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0,ARTISTS, 0, R.string.artists).setIcon(R.drawable.ic_menu_pmix_artists);
		menu.add(0,ALBUMS, 1, R.string.albums).setIcon(R.drawable.ic_menu_pmix_albums);
		menu.add(0,FILES, 2, R.string.files).setIcon(android.R.drawable.ic_menu_agenda);
		menu.add(0,PLAYLIST, 3, R.string.playlist).setIcon(R.drawable.ic_menu_pmix_playlist);
		menu.add(0,SETTINGS, 4, R.string.settings).setIcon(android.R.drawable.ic_menu_preferences);
		
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent i = null;
		
		switch (item.getItemId()) {

		case ARTISTS:

			i = new Intent(this, ArtistsActivity.class);
			startActivityForResult(i, ARTISTS);
			return true;
		case ALBUMS:

			i = new Intent(this, AlbumsActivity.class);
			startActivityForResult(i, ALBUMS);
			return true;
		case FILES:

			i = new Intent(this, FSActivity.class);
			startActivityForResult(i, FILES);
			return true;
		case SETTINGS:
			i = new Intent(this, SettingsActivity.class);
			startActivityForResult(i, SETTINGS);
			return true;
		default:
			// showAlert("Menu Item Clicked", "Not yet implemented", "ok", null,
			// false, null);
			return true;
		}

	}

	public void connectionStateChanged(MPDConnectionStateChangedEvent event) {
		// this.setTitle(event.isConnectionLost() + "");
	}

	public void playlistChanged(MPDPlaylistChangedEvent event) {
		// TODO Auto-generated method stub

	}

	public void randomChanged(MPDRandomChangedEvent event) {
		// TODO Auto-generated method stub

	}

	public void repeatChanged(MPDRepeatChangedEvent event) {
		// TODO Auto-generated method stub

	}

	public void stateChanged(MPDStateChangedEvent event) {
		
		if(event.getMpdStatus().getState().equals(event.getMpdStatus().MPD_STATE_PLAYING))
		{
			this.runOnUiThread(new Runnable(){
				@Override
				public void run() {
					ImageButton button = (ImageButton) findViewById(R.id.playpause);
					button.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
					
				}
			});
		}
		else
		{
			this.runOnUiThread(new Runnable(){
				@Override
				public void run() {
					ImageButton button = (ImageButton) findViewById(R.id.playpause);
					button.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
					
				}
			});
		}
	}

	public void trackChanged(MPDTrackChangedEvent event) {

		MPDStatus status = event.getMpdStatus();

		// if (status.getState().equals(MPDStatus.MPD_STATE_PLAYING)) {

		Message message = Message.obtain();
		message.obj = status;
		handler.sendMessage(message);
		// }

	}

	public TextView getMainInfo() {
		return mainInfo;
	}

	public void updateStateChanged(MPDUpdateStateChangedEvent event) {
		// TODO Auto-generated method stub

	}

	public void volumeChanged(MPDVolumeChangedEvent event) {
		MPDStatus status = event.getMpdStatus();

		Message message = Message.obtain();
		message.obj = status;
		handler.sendMessage(message);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Contexte.getInstance().disconnect();
	}
/*
	@Override
	
	protected void onFreeze(MainMenuActivity outState) {
		super.onFreeze(outState);
		Contexte.getInstance().disconnect();
	}
*/
	@Override
	protected void onPause() {
		super.onPause();
		if(monitor != null)
			monitor.stop();
		monitor = null;
		
	}

	@Override
	protected void onStop() {
		super.onStop();

		Contexte.getInstance().disconnect();
		//monitor.stop();
		//monitor = null;
		//Contexte.getInstance().disconnect();
		//savePreferences();
	}
/*
	private void savePreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("serverAddress", Settings.getInstance().getServerAddress());

		editor.commit();
	}
*/
	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public ProgressBar getProgressBarTrack() {
		return progressBarTrack;
	}

	public void setProgressBarTrack(HorizontalSlider progressBarTrack) {
		this.progressBarTrack = progressBarTrack;
	}

	public void trackPositionChanged(MPDTrackPositionChangedEvent event) {
		MPDStatus status = event.getMpdStatus();

		Message message = Message.obtain();
		message.obj = status;
		handler.sendMessage(message);

	}

	public TextView getTrackTime() {
		return trackTime;
	}

	public ImageSwitcher getCoverSwitcher() {
		return coverSwitcher;

	}

	public TextView getArtistNameText() {
		return artistNameText;
	}

	public TextView getSongNameText() {
		return songNameText;
	}

	public TextView getAlbumNameText() {
		return albumNameText;
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		if(arg0.contains("hostname"))
		{
			Contexte.getInstance().setServerAddress(arg0.getString("hostname", ""));
		}
		
	}

	public ProgressBar getCoverSwitcherProgress() {
		return coverSwitcherProgress;
	}
}