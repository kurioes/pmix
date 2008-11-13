package org.pmix.ui;

import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDServerException;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import dalvik.system.VMStack;

public class Contexte {

	private MPD mpd = new MPD();
	private String serverAddress = "";
	private static Contexte instance = new Contexte();

	private Logger myLogger = Logger.global;
	
	
	
	private Contexte() {

	}

	public static Contexte getInstance() {
		return instance;
	}

	public void disconnect() {
		if (mpd.isConnected()) {

			try {
				mpd.disconnect();
			} catch (MPDServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public MPD getMpd() throws MPDServerException {

		if (!mpd.isConnected()) {
			myLogger.log(Level.WARNING, "Opening Connection...");
			mpd.connect(getServerAddress());
		}
		return mpd;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
		disconnect();
	}

	public String getServerAddress() {
		return serverAddress;
	}

}
