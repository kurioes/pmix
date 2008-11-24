package org.pmix.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDOutput;
import org.a0z.mpd.MPDServerException;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceClickListener;

public class SettingsActivity extends PreferenceActivity {
	OnPreferenceClickListener onPreferenceClickListener;
	HashMap<Integer, CheckBoxPreference> cbPrefs;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.settings);
		
		onPreferenceClickListener = new OutputPreferenceClickListener();
		cbPrefs = new HashMap<Integer, CheckBoxPreference>();
		PreferenceCategory pc = (PreferenceCategory)findPreference("outputs");
		if(!MainMenuActivity.oMPDAsyncHelper.oMPD.isConnected())
		{
			pc.removeAll();
			return;
		}
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
				pc.addPreference(pref);
				
			}
			
			
		} catch (MPDServerException e) {
			pc.removeAll();
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		
		
	}
	
	class OutputPreferenceClickListener implements OnPreferenceClickListener {

		@Override
		public boolean onPreferenceClick(Preference pref) {
			CheckBoxPreference prefCB = (CheckBoxPreference)pref;
			MPD oMPD = MainMenuActivity.oMPDAsyncHelper.oMPD;
			String id = prefCB.getKey();
			CheckBoxPreference prefCB2 = cbPrefs.get(Integer.parseInt(id));
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
	
}
