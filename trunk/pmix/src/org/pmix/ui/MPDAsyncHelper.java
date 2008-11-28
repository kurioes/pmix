package org.pmix.ui;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDServerException;
import org.a0z.mpd.MPDStatusMonitor;
import org.a0z.mpd.event.MPDConnectionStateChangedEvent;
import org.a0z.mpd.event.MPDPlaylistChangedEvent;
import org.a0z.mpd.event.MPDRandomChangedEvent;
import org.a0z.mpd.event.MPDRepeatChangedEvent;
import org.a0z.mpd.event.MPDStateChangedEvent;
import org.a0z.mpd.event.MPDTrackChangedEvent;
import org.a0z.mpd.event.MPDTrackPositionChangedEvent;
import org.a0z.mpd.event.MPDUpdateStateChangedEvent;
import org.a0z.mpd.event.MPDVolumeChangedEvent;
import org.a0z.mpd.event.StatusChangeListener;
import org.a0z.mpd.event.TrackPositionListener;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
/**
 * This Class implements the whole MPD Communication as Thread. It also "translates" the Monitor event
 * of the JMPDComm Library back to the GUI-Thread...
 * @author sag
 *
 */
public class MPDAsyncHelper extends Handler {
	
	private static final int EVENT_CONNECT = 0;
	private static final int EVENT_DISCONNECT = 1;
	private static final int EVENT_CONNECTFAILED = 2;
	private static final int EVENT_CONNECTSUCCEEDED = 3;
	private static final int EVENT_STARTMONITOR = 4;
	private static final int EVENT_STOPMONITOR = 5;
	private static final int EVENT_LOADARTIST = 6;
	
	
	private static final int EVENT_CONNECTIONSTATE = 11;
	private static final int EVENT_PLAYLIST = 12;
	private static final int EVENT_RANDOM = 13;
	private static final int EVENT_REPEAT = 14;
	private static final int EVENT_STATE = 15;
	private static final int EVENT_TRACK = 16;
	private static final int EVENT_UPDATESTATE = 17;
	private static final int EVENT_VOLUME = 18;
	private static final int EVENT_TRACKPOSITION = 19;
	
	
	private MPDAsyncWorker oMPDAsyncWorker;
	private HandlerThread oMPDAsyncWorkerThread;
	public MPD oMPD;

	public interface ConnectionListener {
		public void connectionFailed(String message);
		public void connectionSucceeded(String message);
	}
	
    private Collection<ConnectionListener> connectionListners;
    private Collection<StatusChangeListener> statusChangedListeners;
    private Collection<TrackPositionListener> trackPositionListeners;
	

	private MPDConnectionInfo conInfo;
    
	/**
	 * Private constructor for static class
	 */
	public MPDAsyncHelper() {	
 		oMPD = new MPD();
		oMPDAsyncWorkerThread = new HandlerThread("MPDAsyncWorker");
		oMPDAsyncWorkerThread.start();
		oMPDAsyncWorker = new MPDAsyncWorker(oMPDAsyncWorkerThread.getLooper());
		connectionListners = new LinkedList<ConnectionListener>();
        statusChangedListeners = new LinkedList<StatusChangeListener>();
        trackPositionListeners = new LinkedList<TrackPositionListener>();
        conInfo = new MPDConnectionInfo();
	}

	 public void handleMessage(Message msg) {
		 switch (msg.what) 
		 {
		 	case EVENT_CONNECTIONSTATE:
		 		for(StatusChangeListener listener : statusChangedListeners)
		 			listener.connectionStateChanged((MPDConnectionStateChangedEvent)msg.obj);
		 		// Also notify Connection Listener...
		 		if(((MPDConnectionStateChangedEvent)msg.obj).isConnected())
		 			for(ConnectionListener listener : connectionListners)
		 				listener.connectionSucceeded("");
		 		if(((MPDConnectionStateChangedEvent)msg.obj).isConnectionLost())
		 			for(ConnectionListener listener : connectionListners)
		 				listener.connectionFailed("Connection Lost");
		 		break;
		 	case EVENT_PLAYLIST:
		 		for(StatusChangeListener listener : statusChangedListeners)
		 			listener.playlistChanged((MPDPlaylistChangedEvent)msg.obj);
		 		break;
		 	case EVENT_RANDOM:
		 		for(StatusChangeListener listener : statusChangedListeners)
		 			listener.randomChanged((MPDRandomChangedEvent)msg.obj);
		 		break;
		 	case EVENT_REPEAT:
		 		for(StatusChangeListener listener : statusChangedListeners)
		 			listener.repeatChanged((MPDRepeatChangedEvent)msg.obj);
		 		break;
		 	case EVENT_STATE:
		 		for(StatusChangeListener listener : statusChangedListeners)
		 			listener.stateChanged((MPDStateChangedEvent)msg.obj);
		 		break;
		 	case EVENT_TRACK:
		 		for(StatusChangeListener listener : statusChangedListeners)
		 			listener.trackChanged((MPDTrackChangedEvent)msg.obj);
		 		break;
		 	case EVENT_UPDATESTATE:
		 		for(StatusChangeListener listener : statusChangedListeners)
		 			listener.updateStateChanged((MPDUpdateStateChangedEvent)msg.obj);
		 		break;
		 	case EVENT_VOLUME:
		 		for(StatusChangeListener listener : statusChangedListeners)
		 			listener.volumeChanged((MPDVolumeChangedEvent)msg.obj);
		 		break;
		 	case EVENT_TRACKPOSITION:
		 		for(TrackPositionListener listener : trackPositionListeners)
		 			listener.trackPositionChanged((MPDTrackPositionChangedEvent)msg.obj);
		 		break;
		 	case EVENT_CONNECTFAILED:
		 		for(ConnectionListener listener : connectionListners)
		 			listener.connectionFailed((String)msg.obj);
		 		break;
		 }
	 }

	/**
	 * Connect to MPD Server
	 * @param sServer
	 * @param iPort
	 * @param sPassword
	 */
	public void setConnectionInfo(String sServer, int iPort, String sPassword)
	{
		conInfo.sServer = sServer;
		conInfo.iPort = iPort;
		conInfo.sPassword = sPassword;
	}
	public void doConnect()
	{
		oMPDAsyncWorker.obtainMessage(EVENT_CONNECT, conInfo).sendToTarget();
	}
	public void startMonitor()
	{
		oMPDAsyncWorker.obtainMessage(EVENT_STARTMONITOR).sendToTarget();
	}
	public void stopMonitor()
	{
		oMPDAsyncWorker.obtainMessage(EVENT_STOPMONITOR).sendToTarget();
	}
	
	public void disconnect()
	{
		oMPDAsyncWorker.obtainMessage(EVENT_DISCONNECT).sendToTarget();
	}

	public void addStatusChangeListener(StatusChangeListener listener)
	{
		statusChangedListeners.add(listener);
	}
	public void addTrackPositionListener(TrackPositionListener listener)
	{
		trackPositionListeners.add(listener);
	}
	public void addConnectionListener(ConnectionListener listener)
	{
		connectionListners.add(listener);
	}
	
	public class MPDAsyncWorker extends Handler implements StatusChangeListener, TrackPositionListener {
		private MPDStatusMonitor monitor;
		public MPDAsyncWorker(Looper looper)
		{
			super(looper);
		}

		 public void handleMessage(Message msg) {
			 switch (msg.what) {
			 	case EVENT_CONNECT:
				 	try {
				 		MPDConnectionInfo conInfo = (MPDConnectionInfo)msg.obj;
				 		oMPD.connect(conInfo.sServer, conInfo.iPort);
				 		if(!conInfo.sPassword.equals(""))
				 			oMPD.password(conInfo.sPassword);
				 			
					} catch (MPDServerException e) {
						MPDAsyncHelper.this.obtainMessage(EVENT_CONNECTFAILED, e.getMessage()).sendToTarget();
					}
					break;
			 	case EVENT_STARTMONITOR:
					monitor = new MPDStatusMonitor(oMPD, 1000);
					monitor.addStatusChangeListener(this);
					monitor.addTrackPositionListener(this);
					monitor.start();
					break;
			 	case EVENT_STOPMONITOR:
			 		monitor.giveup();
			 		break;
			 	case EVENT_DISCONNECT:
			 		try {
						oMPD.disconnect();
					} catch (MPDServerException e) {
					} catch (NullPointerException ex) {
					}
			 		break;
			 		
				default:
					break;
			 }
		 }

		// Send all events as Messages back to the GUI-Thread
		public void connectionStateChanged(MPDConnectionStateChangedEvent event) {
			MPDAsyncHelper.this.obtainMessage(EVENT_CONNECTIONSTATE, event).sendToTarget();
		}

		public void playlistChanged(MPDPlaylistChangedEvent event) {
			MPDAsyncHelper.this.obtainMessage(EVENT_PLAYLIST, event).sendToTarget();
		}

		public void randomChanged(MPDRandomChangedEvent event) {
			MPDAsyncHelper.this.obtainMessage(EVENT_RANDOM, event).sendToTarget();
		}

		public void repeatChanged(MPDRepeatChangedEvent event) {
			MPDAsyncHelper.this.obtainMessage(EVENT_REPEAT, event).sendToTarget();
			
		}

		public void stateChanged(MPDStateChangedEvent event) {
			MPDAsyncHelper.this.obtainMessage(EVENT_STATE, event).sendToTarget();
			
		}

		public void trackChanged(MPDTrackChangedEvent event) {
			MPDAsyncHelper.this.obtainMessage(EVENT_TRACK, event).sendToTarget();
			
		}

		public void updateStateChanged(MPDUpdateStateChangedEvent event) {
			MPDAsyncHelper.this.obtainMessage(EVENT_UPDATESTATE, event).sendToTarget();
			
		}

		public void volumeChanged(MPDVolumeChangedEvent event) {
			MPDAsyncHelper.this.obtainMessage(EVENT_VOLUME, event).sendToTarget();
			
		}

		public void trackPositionChanged(MPDTrackPositionChangedEvent event) {
			MPDAsyncHelper.this.obtainMessage(EVENT_TRACKPOSITION, event).sendToTarget();
			
		}
	}
	
	private class MPDConnectionInfo {
		public String sServer;
		public int iPort;
		public String sPassword;
	}
	
}
