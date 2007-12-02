package org.pmix.ui;

import org.pmix.settings.Settings;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public class SettingsActivity extends Activity {

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.settings);
		EditText editText = (EditText) findViewById(R.id.serverAddress);
		
		editText.setText(Settings.getInstance().getServerAddress());
		
	}
	
}
