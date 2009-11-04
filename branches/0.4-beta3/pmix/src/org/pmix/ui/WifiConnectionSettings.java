package org.pmix.ui;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

public class WifiConnectionSettings extends PreferenceActivity {

	private static final String KEY_WIFI_BASED_CATEGORY = "wifibasedCategory";
	private static final String KEY_WIFI_BASED_SCREEN = "wifibasedScreen";
	private static final String KEY_DEFAULT_CATEGORY = "defaultCategory";
	

	private PreferenceCategory mWifibasedCategory; 
	private PreferenceCategory mDefaultCategory; 
	
	private List<WifiConfiguration> mWifiList;
	private WifiManager mWifiManager;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.wificonnectionsettings);
		
		final PreferenceScreen preferenceScreen = getPreferenceScreen();

		mWifibasedCategory = (PreferenceCategory)preferenceScreen.findPreference(KEY_WIFI_BASED_CATEGORY);
		mDefaultCategory = (PreferenceCategory)preferenceScreen.findPreference(KEY_DEFAULT_CATEGORY);
		
		mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		MPDApplication app = (MPDApplication)getApplicationContext();
		app.setActivity(this);
	}


	@Override
	protected void onStop() {
		super.onStop();
		MPDApplication app = (MPDApplication)getApplicationContext();
		app.unsetActivity(this);
	}
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
    	//outState.putParcelableList("wifiNetworks", mWifiList);
    }
	
	/**
	 * Method is beeing called  on any click of an preference...
	 */
	
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
	{
		if(preference.getKey().equals(KEY_WIFI_BASED_SCREEN))
		{
			mWifiList = mWifiManager.getConfiguredNetworks();
			for(WifiConfiguration wifi : mWifiList)
			{
				// Friendly SSID-Name
				String ssid = wifi.SSID.replaceAll("\"", "");
				// Add PreferenceScreen for each network
				PreferenceScreen pref = getPreferenceManager().createPreferenceScreen(this);
				pref.setPersistent(false);
				pref.setKey("wifiNetwork"+ssid);
				pref.setTitle(ssid);
				
				Intent intent = new Intent(this, ConnectionSettings.class);
				//intent.setData(Uri.parse("content://connection/"+ssid));
				intent.putExtra("SSID", ssid);
				pref.setIntent(intent);
				/*
				PreferenceCategory prefCat = new PreferenceCategory(this);
				prefCat.setTitle(ssid);
				pref.addPreference(prefCat);
				*/
				if(WifiConfiguration.Status.CURRENT == wifi.status)
					pref.setSummary("Connected");
				else
					pref.setSummary("Not in range, remembered");
				mWifibasedCategory.addPreference(pref);
			}
		}
		/*
		if(preference.getKey().startsWith("wifiNetwork"))
		{
			// Wi-Fi PreferenceScreen
			PreferenceScreen pScreen = (PreferenceScreen)preference;
			
			// Wi-Fi name
			String wifiName = (String) pScreen.getTitle();
			PreferenceCategory prefCat = (PreferenceCategory)pScreen.getPreference(0);
			
			
			return true;
		}
		*/
		return false;
	}
}

