package org.pmix.ui;

import java.util.Collection;
import java.util.LinkedList;

import org.pmix.ui.MPDAsyncHelper.ConnectionListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

public class MPDConnectionHandler extends BroadcastReceiver {

	private static MPDConnectionHandler instance;

	public static MPDConnectionHandler getInstance()
	{
		if(instance==null)
			instance=new MPDConnectionHandler();
		return instance;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		MPDApplication app = (MPDApplication)context.getApplicationContext();
		String action = intent.getAction();
		if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
			System.out.println("WIFI-STATE:"+intent.getAction().toString());
			System.out.println("WIFI-STATE:"+(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)));
		} else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			System.out.println("NETW-STATE:"+intent.getAction().toString());
			NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			System.out.println("NETW-STATE: Connected: "+networkInfo.isConnected());
			System.out.println("NETW-STATE: Connected: "+networkInfo.getState().toString());
			
			
			if(networkInfo.isConnected())
				app.setWifiConnected(true);
			else
				app.setWifiConnected(false);
			
			
		}
		
	}
}
