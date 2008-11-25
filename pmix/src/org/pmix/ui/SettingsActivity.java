package org.pmix.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDOutput;
import org.a0z.mpd.MPDServerException;
import org.a0z.mpd.event.MPDConnectionStateChangedEvent;
import org.a0z.mpd.event.MPDPlaylistChangedEvent;
import org.a0z.mpd.event.MPDRandomChangedEvent;
import org.a0z.mpd.event.MPDRepeatChangedEvent;
import org.a0z.mpd.event.MPDStateChangedEvent;
import org.a0z.mpd.event.MPDTrackChangedEvent;
import org.a0z.mpd.event.MPDUpdateStateChangedEvent;
import org.a0z.mpd.event.MPDVolumeChangedEvent;
import org.a0z.mpd.event.StatusChangeListener;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceClickListener;

public class SettingsActivity extends PreferenceActivity implements StatusChangeListener {

	private Logger myLogger = Logger.global;
	OnPreferenceClickListener onPreferenceClickListener;
	OnPreferenceClickListener onCheckPreferenceClickListener;
	HashMap<Integer, CheckBoxPreference> cbPrefs;
	PreferenceCategory pOutput;
	CheckBoxPreference pRandom;
	CheckBoxPreference pRepeat;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.settings);
		myLogger.log(Level.INFO, "onCreate");
		onPreferenceClickListener = new OutputPreferenceClickListener();
		onCheckPreferenceClickListener = new CheckPreferenceClickListener();
		cbPrefs = new HashMap<Integer, CheckBoxPreference>();
		pOutput = (PreferenceCategory)findPreference("outputs");
		pRandom = (CheckBoxPreference)findPreference("random");
		pRepeat = (CheckBoxPreference)findPreference("repeat");
		

		EditTextPreference pVersion = (EditTextPreference)findPreference("version");
		EditTextPreference pArtists = (EditTextPreference)findPreference("artists");
		EditTextPreference pAlbums = (EditTextPreference)findPreference("albums");
		EditTextPreference pSongs = (EditTextPreference)findPreference("songs");
		
		
		if(!MainMenuActivity.oMPDAsyncHelper.oMPD.isConnected())
		{
			pOutput.removeAll();
			pRandom.setEnabled(false);
			pRepeat.setEnabled(false);
			return;
		}
		MainMenuActivity.oMPDAsyncHelper.addStatusChangeListener(this);
		try {
			
			Collection<MPDOutput> list = MainMenuActivity.oMPDAsyncHelper.oMPD.getOutputs();
			
			for(MPDOutput out : list)
			{
				CheckBoxPreference pref = new CheckBoxPreference(this);
				pref.setPersistent(false);
				pref.setTitle(out.getName());
				pref.setChecked(out.isEnabled());
				pref.setKey("" + out.getId());
				pref.setOnPreferenceClickListener(onPreferenceClickListener);
				cbPrefs.put(out.getId(), pref);
				pOutput.addPreference(pref);
				
			}

			// Server is Connected...
			pRandom.setChecked(MainMenuActivity.oMPDAsyncHelper.oMPD.getStatus().isRandom());
			pRandom.setOnPreferenceClickListener(onCheckPreferenceClickListener);
			pRepeat.setChecked(MainMenuActivity.oMPDAsyncHelper.oMPD.getStatus().isRepeat());
			pRepeat.setOnPreferenceClickListener(onCheckPreferenceClickListener);
			pVersion.setSummary(MainMenuActivity.oMPDAsyncHelper.oMPD.getMpdVersion());
			pArtists.setSummary(""+MainMenuActivity.oMPDAsyncHelper.oMPD.getStatistics().getArtists());
			pAlbums.setSummary(""+MainMenuActivity.oMPDAsyncHelper.oMPD.getStatistics().getAlbums());
			pSongs.setSummary(""+MainMenuActivity.oMPDAsyncHelper.oMPD.getStatistics().getSongs());
			
		} catch (MPDServerException e) {
			pOutput.removeAll();
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		
		
	}
	@Override
	public void onResume(){
		super.onResume();
		myLogger.log(Level.INFO, "onResume");
	}
	class CheckPreferenceClickListener implements OnPreferenceClickListener {

		@Override
		public boolean onPreferenceClick(Preference pref) {
			CheckBoxPreference prefCB = (CheckBoxPreference)pref;
			MPD oMPD = MainMenuActivity.oMPDAsyncHelper.oMPD;
			try {
				if(prefCB.getKey().equals("random"))
					oMPD.setRandom(prefCB.isChecked());
				if(prefCB.getKey().equals("repeat"))
					oMPD.setRepeat(prefCB.isChecked());
				return prefCB.isChecked();
			} catch (MPDServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		
	}
	
	class OutputPreferenceClickListener implements OnPreferenceClickListener {
		@Override
		public boolean onPreferenceClick(Preference pref) {
			CheckBoxPreference prefCB = (CheckBoxPreference)pref;
			MPD oMPD = MainMenuActivity.oMPDAsyncHelper.oMPD;
			String id = prefCB.getKey();
			try {
				if(prefCB.isChecked())
				{
					oMPD.enableOutput(Integer.parseInt(id));
					return false;
				}
				else
				{
					oMPD.disableOutput(Integer.parseInt(id));
					return true;
				}
			} catch (MPDServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
	}

	@Override
	public void connectionStateChanged(MPDConnectionStateChangedEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void playlistChanged(MPDPlaylistChangedEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void randomChanged(MPDRandomChangedEvent event) {
		pRandom.setChecked(event.isRandom());
	}

	@Override
	public void repeatChanged(MPDRepeatChangedEvent event) {
		pRepeat.setChecked(event.isRepeat());
	}

	@Override
	public void stateChanged(MPDStateChangedEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void trackChanged(MPDTrackChangedEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateStateChanged(MPDUpdateStateChangedEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void volumeChanged(MPDVolumeChangedEvent event) {
		// TODO Auto-generated method stub
		
	}
	
}
