package org.pmix.ui;

import android.app.Application;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class MPDApplication extends Application {

	private WifiManager mWifiManager;
	
	@Override
	public void onCreate() {
		super.onCreate();
    	System.err.println("onCreate Application");

        mWifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        
        
	}
	
	public String getCurrentSSID()
	{
		WifiInfo info = mWifiManager.getConnectionInfo();
        return info.getSSID();
	}
	
}
