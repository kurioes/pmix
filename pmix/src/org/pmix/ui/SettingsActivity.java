package org.pmix.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends PreferenceActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.settings);
		
		//SharedPreferences pref = getSharedPreferences("org.pmix", MODE_PRIVATE);
		//pref.
		//EditTextPreference host = (EditTextPreference)findPreference("hostname");
		setPersistent(true);
		//getSharedPreferences("org.pmix", MODE_PRIVATE);
		
		//host.setText(Settings.getInstance().getServerAddress());
		/*
		setContentView(R.layout.settings);
		final EditText editText = (EditText) findViewById(R.id.serverAddress);
		
		editText.setText();
		
		Button button = (Button) findViewById(R.id.ok);
		button.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Settings.getInstance().setServerAddress(editText.getText().toString());
				finish();
				
			}
		});
		*/
	}
	
}
