package org.pmix.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDServerException;

/**
 * Holds the JMPDComm object
 * @author Rémi Flament, Stefan Agner
 * @version $Id:  $
 */
public class Contexte {

	private MPD mpd = new MPD();
	private String serverAddress = "";
	private int serverPort = 6600;
	private String serverPassword = "";
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
			mpd.connect(getServerAddress(),getServerPort());
			if(!getServerPassword().equals(""))
				mpd.password(getServerPassword());
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

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPassword(String serverPassword) {
		this.serverPassword = serverPassword;
	}

	public String getServerPassword() {
		return serverPassword;
	}

}
