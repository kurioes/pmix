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

public class MPDConnectionHandler extends BroadcastReceiver implements ConnectionListener, OnSharedPreferenceChangeListener {
	
	private static MPDConnectionHandler oMPDConnectionHandler;
	private Collection<Context> connectionLocks = new LinkedList<Context>();
	public Context actContext;
	private AlertDialog ad;
	private DialogClickListener oDialogClickListener;
	
	private boolean bWifiConnected = false;
	
	public static final int SETTINGS = 5;
	
	private MPDConnectionHandler()
	{
	}
	
	public static MPDConnectionHandler getInstance()
	{
		if(oMPDConnectionHandler == null)
		{
			oMPDConnectionHandler = new MPDConnectionHandler();
		}
		return oMPDConnectionHandler; 	
	}
	
	public void getLock(Context locker)
	{
		actContext = locker;
		connectionLocks.add(locker);
		checkMonitorNeeded();
	}
	
	public void releaseLock(Context locker)
	{
		if(actContext == locker)
			actContext=null;
		connectionLocks.remove(locker);
		checkMonitorNeeded();
	}
	
	private void checkMonitorNeeded()
	{
		if(connectionLocks.size()>0)
		{
			if(!MainMenuActivity.oMPDAsyncHelper.isMonitorAlive())
				MainMenuActivity.oMPDAsyncHelper.startMonitor();
		}
		else
			MainMenuActivity.oMPDAsyncHelper.stopMonitor();
		
	}
	
	private void checkConnectionNeeded()
	{
		if(connectionLocks.size()>0)
		{
			if(!MainMenuActivity.oMPDAsyncHelper.oMPD.isConnected() &&
			   !actContext.getClass().equals(SettingsActivity.class))
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
		MainMenuActivity.oMPDAsyncHelper.disconnect();	
	}
	private void connect()
	{

		// Get Settings...
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(actContext.getApplicationContext());//getSharedPreferences("org.pmix", MODE_PRIVATE);
		settings.registerOnSharedPreferenceChangeListener(this);
		
		if (settings.getString("hostname", "").equals("")) {
			//con.startActivityForResult(new Intent(con, SettingsActivity.class), 1);
		}
		else 
		{
				String sServer = settings.getString("hostname", "");
				int iPort = Integer.getInteger(settings.getString("port", "6600"), 6600);
				String sPassword = settings.getString("password", "");
				MainMenuActivity.oMPDAsyncHelper.setConnectionInfo(sServer, iPort, sPassword);
	
				connectMPD();
		}

	}

	private void connectMPD()
	{

		if(ad!=null)
			ad.dismiss();
			
		ad = new ProgressDialog(actContext);
		ad.setTitle("Connecting...");
		ad.setMessage("Connecting to MPD-Server.");
		ad.setCancelable(false);
		ad.show();
		
		MainMenuActivity.oMPDAsyncHelper.doConnect();
	}
	

	public void onSharedPreferenceChanged(SharedPreferences settings, String arg1) {
		
		if(settings.contains("hostname") || settings.contains("port") || settings.contains("password"))
		{
			String sServer = settings.getString("hostname", "");
			int iPort = Integer.getInteger(settings.getString("port", "6600"), 6600);
			String sPassword = settings.getString("password", "");
			MainMenuActivity.oMPDAsyncHelper.setConnectionInfo(sServer, iPort, sPassword);
			MainMenuActivity.oMPDAsyncHelper.disconnect();
			connectMPD();
		}
		
	}

	@Override
	public void connectionFailed(String message) {
		System.out.println("Connection Failed: "+message);
		if(ad!=null)
			ad.dismiss();
		if(connectionLocks.size()>0)
		{
			if(actContext.getClass().equals(SettingsActivity.class))
			{
	
				AlertDialog.Builder test = new AlertDialog.Builder(actContext);
				test.setMessage("Connection failed, check your connection settings...");
				test.setPositiveButton("OK", new OnClickListener(){
	
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				});
				test.show();
			}
			else
			{
					System.out.println(actContext.getClass());
					oDialogClickListener = new DialogClickListener();
					AlertDialog.Builder test = new AlertDialog.Builder(actContext);
					test.setTitle("Connection Failed");
					test.setMessage("Connection to MPD-Server failed! Check if the Server is running and reachable.");
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
		checkMonitorNeeded();
	}

	private class DialogClickListener implements OnClickListener {

		public void onClick(DialogInterface dialog, int which) {
			switch(which) {
				case AlertDialog.BUTTON3:
					// Show Settings
					((Activity)actContext).startActivityForResult(new Intent(actContext, SettingsActivity.class), SETTINGS);
					break;
				case AlertDialog.BUTTON2:
					((Activity)actContext).finish();
					break;
				case AlertDialog.BUTTON1:
					connectMPD();
					break;
					
			}
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
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
			{
				connect();
				checkMonitorNeeded();
			}
			else
			{
				disconnect();
			}
			
			
		}
		
	}
}
