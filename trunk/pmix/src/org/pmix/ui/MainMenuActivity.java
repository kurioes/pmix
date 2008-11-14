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
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * MainMenuActivity is the starting activity of pmix
 * @author Rémi Flament, Stefan Agner
 * @version $Id:  $
 */
public class MainMenuActivity extends Activity implements StatusChangeListener, TrackPositionListener, OnSharedPreferenceChangeListener {

	private Logger myLogger = Logger.global;
	
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
	protected void onRestart() {
		super.onRestart();
		myLogger.log(Level.INFO, "onRestart");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		myLogger.log(Level.INFO, "onResume");
		try {
			String mpdVersion = Contexte.getInstance().getMpd().getMpdVersion();
			StringBuffer stringBuffer = new StringBuffer(100);
			String serverAdress = Contexte.getInstance().getServerAddress();
			stringBuffer.append("\nMPD version " + mpdVersion + " running at " + serverAdress + "\n");
			mainInfo.setText(stringBuffer.toString());

			Contexte.getInstance().getMpd().getPlaylist().refresh();
			
			monitor = new MPDStatusMonitor(Contexte.getInstance().getMpd(), 1000);
			monitor.addStatusChangeListener(this);
			monitor.addTrackPositionListener(this);
			monitor.start();
			myLogger.log(Level.INFO, "Monitor started");
		} catch (MPDServerException e) {
			setTitle("Error");
			myLogger.log(Level.WARNING, "Initialization failed...");
			mainInfo.setText(e.getMessage());
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

		// Initial Connect => Set Host in Context (TODO: fix, this is ugly)
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());//getSharedPreferences("org.pmix", MODE_PRIVATE);
		settings.registerOnSharedPreferenceChangeListener(this);
		Contexte.getInstance().setServerAddress(settings.getString("hostname", ""));
		try {
			Contexte.getInstance().setServerPort(Integer.parseInt(settings.getString("port", "6600")));
		} catch (NumberFormatException e) {
			Contexte.getInstance().setServerPort(6600);
		}
		Contexte.getInstance().setServerPassword(settings.getString("password", ""));
		
		

		((TextView) findViewById(R.id.volume)).setTextSize(12);
		((TextView) findViewById(R.id.track)).setTextSize(12);
		((TextView) findViewById(R.id.trackTime)).setTextSize(12);

		coverSwitcher = (ImageSwitcher) findViewById(R.id.albumCover);
		/* sag, would like to implement a loading Progress
		coverSwitcherProgress = (ProgressBar) findViewById(R.id.albumCoverProgress); 
		coverSwitcherProgress.setIndeterminate(true);
		coverSwitcherProgress.setVisibility(ProgressBar.INVISIBLE);
		*/
		handler = new MyHandler(this);
		coverSwitcher.setFactory(handler);

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
					/**
					 * If playing or paused, just toggle state, otherwise start playing.
					 * @author slubman
					 */
					if(Contexte.getInstance().getMpd().getStatus().getState().equals(MPDStatus.MPD_STATE_PLAYING)
						|| Contexte.getInstance().getMpd().getStatus().getState().equals(MPDStatus.MPD_STATE_PAUSED)) {
						Contexte.getInstance().getMpd().pause();
					} else {
						Contexte.getInstance().getMpd().play();
					}
				} catch (MPDServerException e) {
					e.printStackTrace();
				}
			}
		});
		
		/**
		 * Add the ability to stop playing (may be useful for streams)
		 * @author slubman
		 */
		button.setOnLongClickListener(new Button.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				try {
					Contexte.getInstance().getMpd().stop();
				} catch (MPDServerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
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
		
		myLogger.log(Level.WARNING, "Initialization succeeded");

		
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
		myLogger.log(Level.INFO, "onCreate");
		
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
		myLogger.log(Level.INFO, "Connection State: " + event.toString());
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
		MPDStatus status = event.getMpdStatus();
		String state = status.getState();
		if(state!=null)
		{
			// In some cases state is null (disconnect from server or somewhat similar...)
			// In my opinion its not how JMPDComm should behave, so we may have to fix it there...
			if(state.equals(MPDStatus.MPD_STATE_PLAYING))
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
	}

	public void trackChanged(MPDTrackChangedEvent event) {

		MPDStatus status = event.getMpdStatus();
		if(status!=null)
		{
			if (status.getState().equals(MPDStatus.MPD_STATE_PLAYING)) {
				Message message = Message.obtain();
				message.obj = status;
				handler.sendMessage(message);
			}
		}

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
	protected void onPause() {
		super.onPause();
		myLogger.log(Level.INFO, "onPause");
		if(monitor != null)
			monitor.giveup();
		monitor = null;
		Contexte.getInstance().disconnect();
		myLogger.log(Level.INFO, "Monitor closed");
		
	}

	@Override
	protected void onStop() {
		super.onStop();
		myLogger.log(Level.INFO, "onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		myLogger.log(Level.INFO, "onDestroy");
	}
	
	
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
	public void onSharedPreferenceChanged(SharedPreferences settings, String arg1) {
		if(settings.contains("hostname") || settings.contains("port") || settings.contains("password"))
		{
			Contexte.getInstance().setServerAddress(settings.getString("hostname", ""));
			try {
				Contexte.getInstance().setServerPort(Integer.parseInt(settings.getString("port", "6600")));
			} catch (NumberFormatException e) {
				Contexte.getInstance().setServerPort(6600);
			}
			Contexte.getInstance().setServerPassword(settings.getString("password", ""));
		}
		
	}

	public ProgressBar getCoverSwitcherProgress() {
		return coverSwitcherProgress;
	}
}