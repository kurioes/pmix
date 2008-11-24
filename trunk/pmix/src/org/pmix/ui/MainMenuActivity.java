package org.pmix.ui;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.Serializable;

import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDConnectionException;
import org.a0z.mpd.MPDPlaylist;
import org.a0z.mpd.MPDServerException;
import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.MPDStatusMonitor;
import org.a0z.mpd.Music;
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
import org.pmix.ui.CoverAsyncHelper.CoverDownloadListener;
import org.pmix.ui.MPDAsyncHelper.ConnectionListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.WifiManager;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;
import android.widget.ViewSwitcher.ViewFactory;


/**
 * MainMenuActivity is the starting activity of pmix
 * @author Rémi Flament, Stefan Agner
 * @version $Id:  $
 */
public class MainMenuActivity extends Activity implements StatusChangeListener, TrackPositionListener, OnSharedPreferenceChangeListener, CoverDownloadListener, ConnectionListener {

	private Logger myLogger = Logger.global;
	
	public static final String PREFS_NAME = "pmix.properties";

	public static final int PLAYLIST = 1;
	
	public static final int ARTISTS = 2;

	public static final int SETTINGS = 5;

	private TextView artistNameText;

	private TextView songNameText;

	private TextView albumNameText;


	public static final int ALBUMS = 4;

	public static final int FILES = 3;

	private TextView mainInfo = null;

	private SeekBar progressBarVolume = null;
	private SeekBar progressBarTrack = null;

	private TextView trackTime = null;

	private CoverAsyncHelper oCoverAsyncHelper = null;
	long lastSongTime = 0;
	long lastElapsedTime = 0;
	
	private ImageSwitcher coverSwitcher;

	private ProgressBar coverSwitcherProgress;

	private static final int VOLUME_STEP = 5;

	private static final int TRACK_STEP = 10;

	private static Toast notification = null;
	
	private AlertDialog ad;
	
	private ButtonEventHandler buttonEventHandler;
	
	// Change this... (sag)
	public static MPDAsyncHelper oMPDAsyncHelper = null;
	
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
	protected void onStart() {
		super.onStart();
		oMPDAsyncHelper = new MPDAsyncHelper();
		oMPDAsyncHelper.addStatusChangeListener(this);
		oMPDAsyncHelper.addTrackPositionListener(this);
		oMPDAsyncHelper.addConnectionListener(this);

		// Get Settings...
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());//getSharedPreferences("org.pmix", MODE_PRIVATE);
		settings.registerOnSharedPreferenceChangeListener(this);
		String sServer = settings.getString("hostname", "");
		int iPort = Integer.getInteger(settings.getString("port", "6600"), 6600);
		String sPassword = settings.getString("password", "");
		oMPDAsyncHelper.setConnectionInfo(sServer, iPort, sPassword);

		connectMPD();
	}
	
	private void connectMPD()
	{
		ad = new ProgressDialog(this);
		ad.setTitle("Connecting...");
		ad.setMessage("Connecting to MPD-Server.");
		ad.show();
		oMPDAsyncHelper.doConnect();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		oMPDAsyncHelper.startMonitor();
		oMPDAsyncHelper.stopMonitor();
		oMPDAsyncHelper.startMonitor();
		/*
		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		int wifistate = wifi.getWifiState();
		if(wifistate!=wifi.WIFI_STATE_ENABLED && wifistate!=wifi.WIFI_STATE_ENABLING)
		{
			setTitle("No WIFI");
			return;
		}
		while(wifistate!=wifi.WIFI_STATE_ENABLED)
			setTitle("Waiting for WIFI");
			*/
		/*
		
		*/

		
	}

	private void init() {
		setContentView(R.layout.main);
		
		mainInfo = (TextView) findViewById(R.id.mainInfo);
		artistNameText = (TextView) findViewById(R.id.artistName);
		albumNameText = (TextView) findViewById(R.id.albumName);
		songNameText = (TextView) findViewById(R.id.songName);

		progressBarTrack = (SeekBar) findViewById(R.id.progress_track);
		progressBarVolume = (SeekBar) findViewById(R.id.progress_volume);

		trackTime = (TextView) findViewById(R.id.trackTime);

		
		coverSwitcher = (ImageSwitcher) findViewById(R.id.albumCover);
		coverSwitcher.setFactory(new ViewFactory() {

			public View makeView() {
				ImageView i = new ImageView(MainMenuActivity.this);

				i.setBackgroundColor(0x00FF0000);
				i.setScaleType(ImageView.ScaleType.FIT_CENTER);
				//i.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

				
				return i;
			}
		});
		/* sag, would like to implement a loading Progress */
		coverSwitcherProgress = (ProgressBar) findViewById(R.id.albumCoverProgress); 
		coverSwitcherProgress.setIndeterminate(true);
		coverSwitcherProgress.setVisibility(ProgressBar.INVISIBLE);
		
		/*
		handler = new MyHandler(this);
		coverSwitcher.setFactory(handler);
		*/
		oCoverAsyncHelper = new CoverAsyncHelper();
		oCoverAsyncHelper.addCoverDownloadListener(this);
		buttonEventHandler = new ButtonEventHandler();
		ImageButton button = (ImageButton) findViewById(R.id.next);
		button.setOnClickListener(buttonEventHandler);
		
		button = (ImageButton) findViewById(R.id.prev);
		button.setOnClickListener(buttonEventHandler);

		button = (ImageButton) findViewById(R.id.back);
		button.setOnClickListener(buttonEventHandler);

		button = (ImageButton) findViewById(R.id.playpause);
		button.setOnClickListener(buttonEventHandler);
		button.setOnLongClickListener(buttonEventHandler);
		
		button = (ImageButton) findViewById(R.id.forward);
		button.setOnClickListener(buttonEventHandler);

		progressBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromTouch) {
				if(fromTouch)
				{
					try {
						oMPDAsyncHelper.oMPD.setVolume(progress);
					} catch (MPDServerException e) {
						e.printStackTrace();
					}
				}
				
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
		});
		progressBarTrack.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromTouch) {
				if(fromTouch)
				{
					try {
						oMPDAsyncHelper.oMPD.seek((int)progress);
					} catch (MPDServerException e) {
						e.printStackTrace();
					}
				}
				
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
		});
		
		myLogger.log(Level.INFO, "Initialization succeeded");
	}

	private class ButtonEventHandler implements Button.OnClickListener, Button.OnLongClickListener {

		public void onClick(View v) {
			MPD mpd = oMPDAsyncHelper.oMPD;
			try {
				switch(v.getId()) {
					case R.id.next:
						mpd.next();
						break;
					case R.id.prev:
						mpd.previous();
						break;
					case R.id.back:
						mpd.seek(lastElapsedTime - TRACK_STEP);
						break;
					case R.id.forward:
						mpd.seek(lastElapsedTime + TRACK_STEP);
						break;
					case R.id.playpause:
						/**
						 * If playing or paused, just toggle state, otherwise start playing.
						 * @author slubman
						 */
						String state = mpd.getStatus().getState();
						if(state.equals(MPDStatus.MPD_STATE_PLAYING)
							|| state.equals(MPDStatus.MPD_STATE_PAUSED)) {
							mpd.pause();
						} else {
							mpd.play();
						}
						break;

				}
			
			} catch (MPDServerException e) {
				myLogger.log(Level.WARNING, e.getMessage());
			}
		}

		public boolean onLongClick(View v) {
			MPD mpd = oMPDAsyncHelper.oMPD;
			try {
				switch(v.getId()) {
					case R.id.playpause:
						// Implements the ability to stop playing (may be useful for streams)
						mpd.stop();
						break;
					default:
						return false;
				}
				return true;
			} catch (MPDServerException e) {
				
			}
			return true;
		}
		
		
	}
	
	
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// mainInfo.setText(keyCode + "");
		try {
			switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_UP:
				progressBarVolume.incrementProgressBy(VOLUME_STEP);
				oMPDAsyncHelper.oMPD.adjustVolume(VOLUME_STEP);
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				progressBarVolume.incrementProgressBy(-VOLUME_STEP);
				oMPDAsyncHelper.oMPD.adjustVolume(-VOLUME_STEP);
				return true;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				oMPDAsyncHelper.oMPD.previous();
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				oMPDAsyncHelper.oMPD.next();
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
			startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS);
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
		case PLAYLIST:
			i = new Intent(this, PlaylistActivity.class);
			startActivityForResult(i, PLAYLIST);
			return true;
		default:
			// showAlert("Menu Item Clicked", "Not yet implemented", "ok", null,
			// false, null);
			return true;
		}

	}

	public void connectionStateChanged(MPDConnectionStateChangedEvent event) {
		ad.dismiss();
		String mpdVersion = oMPDAsyncHelper.oMPD.getMpdVersion();
		StringBuffer stringBuffer = new StringBuffer(100);
		stringBuffer.append("MPD version " + mpdVersion + " running at " +"" + "\n");
		mainInfo.setText(stringBuffer.toString());
		setTitle("PMix");
		myLogger.log(Level.INFO, "Connection State: " + event.toString());
	}

	//private MPDPlaylist playlist;
	public void playlistChanged(MPDPlaylistChangedEvent event) {
		try {
			oMPDAsyncHelper.oMPD.getPlaylist().refresh();
		} catch (MPDServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

			if(state.equals(MPDStatus.MPD_STATE_PLAYING))
			{
				ImageButton button = (ImageButton) findViewById(R.id.playpause);
				button.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
			} else {
				ImageButton button = (ImageButton) findViewById(R.id.playpause);
				button.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
			}
		}
	}
	private String lastArtist = "";
	private String lastAlbum = "";
	public void trackChanged(MPDTrackChangedEvent event) {

		MPDStatus status = event.getMpdStatus();
		if(status!=null)
		{
			String state = status.getState();
			if(state != null)
			{
				int songId = status.getSongPos();
				if(songId>=0)
				{
					
					Music actSong = oMPDAsyncHelper.oMPD.getPlaylist().getMusic(songId);
					String artist = actSong.getArtist();
					String title = actSong.getTitle();
					String album = actSong.getAlbum();
					artist = artist==null ? "" : artist;
					title = title==null ? "" : title;
					album = album==null ? "" : album;
					artistNameText.setText(artist);
					songNameText.setText(title);
					albumNameText.setText(album);
					progressBarTrack.setMax((int)actSong.getTime());
					if(!lastAlbum.equals(album) || !lastArtist.equals(artist))
					{
						coverSwitcher.setVisibility(ImageSwitcher.INVISIBLE);
						coverSwitcherProgress.setVisibility(ProgressBar.VISIBLE);
						oCoverAsyncHelper.downloadCover(artist, album);
						lastArtist = artist;
						lastAlbum = album;
					}
				}
				else
				{
					artistNameText.setText("");
					songNameText.setText("");
					albumNameText.setText("");
					progressBarTrack.setMax(0);
				}
			}
		}
	}

	public void updateStateChanged(MPDUpdateStateChangedEvent event) {
		// TODO Auto-generated method stub

	}

	public void volumeChanged(MPDVolumeChangedEvent event) {
		progressBarVolume.setProgress(event.getMpdStatus().getVolume());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		oMPDAsyncHelper.stopMonitor();
		/*
		myLogger.log(Level.INFO, "onPause");
		if(monitor != null)
			monitor.giveup();
		monitor = null;
		Contexte.getInstance().disconnect();
		*/
		
	}

	@Override
	protected void onStop() {
		super.onStop();
		myLogger.log(Level.INFO, "onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		oMPDAsyncHelper.disconnect();
		myLogger.log(Level.INFO, "onDestroy");
	}
	
	
	public SeekBar getVolumeSeekBar() {
		return progressBarVolume;
	}

	public SeekBar getProgressBarTrack() {
		return progressBarTrack;
	}


	public void trackPositionChanged(MPDTrackPositionChangedEvent event) {
		MPDStatus status = event.getMpdStatus();
		lastElapsedTime = status.getElapsedTime();
		lastSongTime = status.getTotalTime();
		trackTime.setText(timeToString(lastElapsedTime) + " - " + timeToString(lastSongTime));
		progressBarTrack.setProgress((int) status.getElapsedTime());
	}


	private static String timeToString(long seconds) {
		long min = seconds / 60;
		long sec = seconds - min * 60;
		return (min < 10 ? "0" + min : min) + ":" + (sec < 10 ? "0" + sec : sec);
	}
	


	public void onSharedPreferenceChanged(SharedPreferences settings, String arg1) {
		
		if(settings.contains("hostname") || settings.contains("port") || settings.contains("password"))
		{
			
			/*
			Contexte.getInstance().setServerAddress(settings.getString("hostname", ""));
			try {
				Contexte.getInstance().setServerPort(Integer.parseInt(settings.getString("port", "6600")));
			} catch (NumberFormatException e) {
				Contexte.getInstance().setServerPort(6600);
			}
			Contexte.getInstance().setServerPassword(settings.getString("password", ""));
			*/
		}
		
	}

	public ProgressBar getCoverSwitcherProgress() {
		return coverSwitcherProgress;
	}
	
	

    public static void notifyUser(String message, Context context) {
            if (notification != null) {
                    notification.setText(message);
                    notification.show();
            } else {
                    notification = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                    notification.show();
            }
    }

	public void onCoverDownloaded(Bitmap cover) {
		coverSwitcherProgress.setVisibility(ProgressBar.INVISIBLE);
		coverSwitcher.setImageDrawable(new BitmapDrawable(cover));
		coverSwitcher.setVisibility(ImageSwitcher.VISIBLE);
		
	}

	public void onCoverNotFound() {
		coverSwitcherProgress.setVisibility(ProgressBar.INVISIBLE);
		coverSwitcher.setImageResource(R.drawable.gmpcnocover);
		coverSwitcher.setVisibility(ImageSwitcher.VISIBLE);
		
	}

	private DialogClickListener oDialogClickListener;
	public void connectionFailed(String message) {
		oDialogClickListener = new DialogClickListener();
		AlertDialog.Builder test = new AlertDialog.Builder(this);
		test.setTitle("Connection Failed");
		test.setMessage("Connection to MPD-Server failed! Check if the Server is running and reachable.");
		test.setNegativeButton("Exit", oDialogClickListener);
		test.setNeutralButton("Settings", oDialogClickListener);
		test.setPositiveButton("Retry", oDialogClickListener);
		ad.dismiss();
		ad = test.show();
		
	}
	
	private class DialogClickListener implements OnClickListener {

		public void onClick(DialogInterface dialog, int which) {
			switch(which) {
				case AlertDialog.BUTTON3:
					// Show Settings
					MainMenuActivity.this.startActivityForResult(new Intent(MainMenuActivity.this, SettingsActivity.class), SETTINGS);
					break;
				case AlertDialog.BUTTON2:
					MainMenuActivity.this.finish();
					break;
				case AlertDialog.BUTTON1:
					connectMPD();
					break;
					
			}
		}
	}
	
	
}