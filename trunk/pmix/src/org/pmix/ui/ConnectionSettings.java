package org.pmix.ui;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

public class ConnectionSettings extends PreferenceActivity {

	private static final String KEY_CONNECTION_CATEGORY = "connectionCategory";
	
	private String mSSID;
	private PreferenceCategory mMasterCategory;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.connectionsettings);
		
		final PreferenceScreen preferenceScreen = getPreferenceScreen();
		
		mMasterCategory = (PreferenceCategory)preferenceScreen.findPreference(KEY_CONNECTION_CATEGORY);
		
		if(getIntent().getStringExtra("SSID") != null)
		{
			// WiFi-Based Settings
			mSSID = getIntent().getStringExtra("SSID");
			createDynamicSettings(mSSID,mMasterCategory);
		}
		else
		{
			// Default settings
			createDynamicSettings("",mMasterCategory);
			
		}
	}

	@Override
	protected void onStart() {
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
	
	private void createDynamicSettings(String keyPrefix, PreferenceCategory toCategory)
	{

		EditTextPreference prefHost = new EditTextPreference(this);
		prefHost.setDialogTitle(R.string.host);
		prefHost.setTitle(R.string.host);
		prefHost.setSummary(R.string.hostDescription);
		prefHost.setDefaultValue("");
		prefHost.setKey(keyPrefix+"hostname");
		toCategory.addPreference(prefHost);
		
		EditTextPreference prefPort = new EditTextPreference(this);
		prefPort.setDialogTitle(R.string.port);
		prefPort.setTitle(R.string.port);
		prefPort.setSummary(R.string.portDescription);
		prefPort.setDefaultValue("6600");
		prefPort.setKey(keyPrefix+"port");
		toCategory.addPreference(prefPort);
		
		EditTextPreference prefPassword = new EditTextPreference(this);
		prefPassword.setDialogTitle(R.string.password);
		prefPassword.setTitle(R.string.password);
		prefPassword.setSummary(R.string.passwordDescription);
		prefPassword.setDefaultValue("");
		prefPassword.setKey(keyPrefix+"password");
		toCategory.addPreference(prefPassword);
		onContentChanged();
		
	}
}
