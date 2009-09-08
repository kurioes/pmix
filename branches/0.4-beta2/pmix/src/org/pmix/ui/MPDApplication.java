package org.pmix.ui;

import java.util.Collection;
import java.util.LinkedList;

import org.pmix.ui.MPDAsyncHelper.ConnectionListener;
import org.pmix.ui.MPDApplication.DialogClickListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

public class MPDApplication extends Application implements ConnectionListener, OnSharedPreferenceChangeListener {
	
	private Collection<Activity> connectionLocks = new LinkedList<Activity>();
	private AlertDialog ad;
	private DialogClickListener oDialogClickListener;
	
	private boolean bWifiConnected = false;
	private Activity currentActivity;
	
	public static final int SETTINGS = 5;
	
	
	public void setActivity(Activity activity)
	{
		currentActivity = activity;
		connectionLocks.add(activity);
		checkMonitorNeeded();
		
	}
	
	public void unsetActivity(Activity activity)
	{
		connectionLocks.remove(activity);
		checkMonitorNeeded();
		if(currentActivity == activity)
			currentActivity=null;
	}
	
	private void checkMonitorNeeded()
	{
		if(connectionLocks.size()>0)
		{
			if(!oMPDAsyncHelper.isMonitorAlive())
				oMPDAsyncHelper.startMonitor();
		}
		else
			oMPDAsyncHelper.stopMonitor();
		
	}
	private void checkConnectionNeeded()
	{
		if(connectionLocks.size()>0)
		{
			if(!oMPDAsyncHelper.oMPD.isConnected() &&
			   !currentActivity.getClass().equals(SettingsActivity.class))
			{
				connect();
			}
				
		}
		else
		{
			disconnect();
		}
		
	}
	
	
	public void disconnect()
	{
		oMPDAsyncHelper.disconnect();	
	}
	private void connect()
	{
		// Get Settings...
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);//getSharedPreferences("org.pmix", MODE_PRIVATE);
		settings.registerOnSharedPreferenceChangeListener(this);

		String wifiSSID = getCurrentSSID();
		

		if (!settings.getString(wifiSSID + "hostname", "").equals("")) {
			String sServer = settings.getString(wifiSSID + "hostname", "");
			int iPort = Integer.getInteger(settings.getString(wifiSSID + "port", "6600"), 6600);
			String sPassword = settings.getString(wifiSSID + "password", "");
			oMPDAsyncHelper.setConnectionInfo(sServer, iPort, sPassword);	
		} else if (!settings.getString("hostname", "").equals("")) {
				String sServer = settings.getString("hostname", "");
				int iPort = Integer.getInteger(settings.getString("port", "6600"), 6600);
				String sPassword = settings.getString("password", "");
				oMPDAsyncHelper.setConnectionInfo(sServer, iPort, sPassword);
		} else {
			return;
		}
		connectMPD();

	}

	private void connectMPD()
	{

		if(ad!=null)
			ad.dismiss();
		
		ad = new ProgressDialog(currentActivity);
		ad.setTitle("Connecting...");
		ad.setMessage("Connecting to MPD-Server.");
		ad.setCancelable(false);
		ad.show();

		oMPDAsyncHelper.doConnect();
	}
	

	public void onSharedPreferenceChanged(SharedPreferences settings, String arg1) {
		String wifiSSID = getCurrentSSID();
		
		if (!settings.getString(wifiSSID + "hostname", "").equals("")) {
			String sServer = settings.getString(wifiSSID + "hostname", "");
			int iPort = Integer.getInteger(settings.getString(wifiSSID + "port", "6600"), 6600);
			String sPassword = settings.getString(wifiSSID + "password", "");
			oMPDAsyncHelper.setConnectionInfo(sServer, iPort, sPassword);	
		} else if (!settings.getString("hostname", "").equals("")) {
				String sServer = settings.getString("hostname", "");
				int iPort = Integer.getInteger(settings.getString("port", "6600"), 6600);
				String sPassword = settings.getString("password", "");
				oMPDAsyncHelper.setConnectionInfo(sServer, iPort, sPassword);
		} else {
			return;
		}
	}

	@Override
	public void connectionFailed(String message) {
		System.out.println("Connection Failed: "+message);
		if(ad!=null)
			ad.dismiss();
		if(connectionLocks.size()>0 && isWifiConnected()) 
		{
			if(currentActivity.getClass().equals(SettingsActivity.class))
			{
	
				AlertDialog.Builder test = new AlertDialog.Builder(currentActivity);
				test.setMessage("Connection failed, check your connection settings. (" + message + ")");
				test.setPositiveButton("OK", new OnClickListener(){
	
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				});
				ad = test.show();
			}
			else
			{
					System.out.println(this.getClass());
					oDialogClickListener = new DialogClickListener();
					AlertDialog.Builder test = new AlertDialog.Builder(currentActivity);
					test.setTitle("Connection Failed");
					test.setMessage("Connection to MPD-Server failed! Check if the Server is running and reachable. (" + message + ")");
					test.setNegativeButton("Exit", oDialogClickListener);
					test.setNeutralButton("Settings", oDialogClickListener);
					test.setPositiveButton("Retry", oDialogClickListener);
					ad = test.show();
			}
		}
		
	}
	
	@Override
	public void connectionSucceeded(String message) {
		ad.dismiss();
		//checkMonitorNeeded();
	}
	
	public class DialogClickListener implements OnClickListener {

		public void onClick(DialogInterface dialog, int which) {
			switch(which) {
				case AlertDialog.BUTTON3:
					// Show Settings
					currentActivity.startActivityForResult(new Intent(currentActivity, SettingsActivity.class), SETTINGS);
					break;
				case AlertDialog.BUTTON2:
					currentActivity.finish();
					break;
				case AlertDialog.BUTTON1:
					connectMPD();
					break;
					
			}
		}
	}

	private WifiManager mWifiManager;
	
	// Change this... (sag)
	public MPDAsyncHelper oMPDAsyncHelper = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
    	System.err.println("onCreate Application");
    	
		oMPDAsyncHelper = new MPDAsyncHelper();
		oMPDAsyncHelper.addConnectionListener((MPDApplication)getApplicationContext());

        mWifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        
        
	}
	
	public String getCurrentSSID()
	{
		WifiInfo info = mWifiManager.getConnectionInfo();
        return info.getSSID();
	}

	public void setWifiConnected(boolean bWifiConnected) {
		this.bWifiConnected = bWifiConnected;
		if(bWifiConnected) {
			if(ad!=null)
				ad.dismiss();
			connect();
			//checkMonitorNeeded();
		} else {
			disconnect();
			if(ad!=null)
				ad.dismiss();
			AlertDialog.Builder test = new AlertDialog.Builder(currentActivity);
			test.setMessage("Waiting for WLAN to be connected...");
			ad = test.show();
		}
	}

	public boolean isWifiConnected() {
		return bWifiConnected;
	}
	
}
