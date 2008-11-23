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
	private static final int EVENT_CONNECTIONSTATE = 2;
	private static final int EVENT_PLAYLIST = 3;
	private static final int EVENT_RANDOM = 4;
	private static final int EVENT_REPEAT = 5;
	private static final int EVENT_STATE = 6;
	private static final int EVENT_TRACK = 7;
	private static final int EVENT_UPDATESTATE = 8;
	private static final int EVENT_VOLUME = 9;
	private static final int EVENT_TRACKPOSITION = 10;
	
	
	private MPDAsyncWorker oMPDAsyncWorker;
	private HandlerThread oMPDAsyncWorkerThread;
	public MPD oMPD;
	
    private Collection<StatusChangeListener> statusChangedListeners;
    private Collection<TrackPositionListener> trackPositionListeners;
	
	/**
	 * Private constructor for static class
	 */
	public MPDAsyncHelper() {	
		oMPDAsyncWorkerThread = new HandlerThread("MPDAsyncWorker");
		oMPDAsyncWorkerThread.start();
		oMPDAsyncWorker = new MPDAsyncWorker(oMPDAsyncWorkerThread.getLooper());
        statusChangedListeners = new LinkedList<StatusChangeListener>();
        trackPositionListeners = new LinkedList<TrackPositionListener>();
	}

	 public void handleMessage(Message msg) {
		 switch (msg.what) 
		 {
		 	case EVENT_CONNECTIONSTATE:
		 		for(StatusChangeListener listener : statusChangedListeners)
		 			listener.connectionStateChanged((MPDConnectionStateChangedEvent)msg.obj);
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
		 }
	 }
	 
	/**
	 * Connect to MPD Server
	 * @param sServer
	 * @param iPort
	 * @param sPassword
	 */
	public void doConnect(String sServer, int iPort, String sPassword)
	{
		MPDConnectionInfo conInfo = new MPDConnectionInfo();
		conInfo.sServer = sServer;
		conInfo.iPort = iPort;
		conInfo.sPassword = sPassword;
		oMPDAsyncWorker.obtainMessage(EVENT_CONNECT, conInfo).sendToTarget();
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
	
	public class MPDAsyncWorker extends Handler implements StatusChangeListener, TrackPositionListener {
		private MPDStatusMonitor monitor;
		public MPDAsyncWorker(Looper looper)
		{
			super(looper);
		}

		 public void handleMessage(Message msg) {

		 	try {
			 switch (msg.what) 
			 {
			 	case EVENT_CONNECT:
			 		MPDConnectionInfo conInfo = (MPDConnectionInfo)msg.obj;
			 		oMPD = new MPD();
			 		oMPD.connect(conInfo.sServer);
					monitor = new MPDStatusMonitor(oMPD, 1000);
					monitor.addStatusChangeListener(oMPDAsyncWorker);
					monitor.addTrackPositionListener(oMPDAsyncWorker);
					monitor.start();
					break;
			 	case EVENT_DISCONNECT:
			 		monitor.giveup();
			 		oMPD.disconnect();
			 		break;
			 		
				default:
				
			 
			 }

			} catch (MPDServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }

		// Send all events as Messages back to the GUI-Thread
		@Override
		public void connectionStateChanged(MPDConnectionStateChangedEvent event) {
			MPDAsyncHelper.this.obtainMessage(EVENT_CONNECTIONSTATE, event).sendToTarget();
		}

		@Override
		public void playlistChanged(MPDPlaylistChangedEvent event) {
			MPDAsyncHelper.this.obtainMessage(EVENT_PLAYLIST, event).sendToTarget();
		}

		@Override
		public void randomChanged(MPDRandomChangedEvent event) {
			MPDAsyncHelper.this.obtainMessage(EVENT_RANDOM, event).sendToTarget();
		}

		@Override
		public void repeatChanged(MPDRepeatChangedEvent event) {
			MPDAsyncHelper.this.obtainMessage(EVENT_REPEAT, event).sendToTarget();
			
		}

		@Override
		public void stateChanged(MPDStateChangedEvent event) {
			MPDAsyncHelper.this.obtainMessage(EVENT_STATE, event).sendToTarget();
			
		}

		@Override
		public void trackChanged(MPDTrackChangedEvent event) {
			MPDAsyncHelper.this.obtainMessage(EVENT_TRACK, event).sendToTarget();
			
		}

		@Override
		public void updateStateChanged(MPDUpdateStateChangedEvent event) {
			MPDAsyncHelper.this.obtainMessage(EVENT_UPDATESTATE, event).sendToTarget();
			
		}

		@Override
		public void volumeChanged(MPDVolumeChangedEvent event) {
			MPDAsyncHelper.this.obtainMessage(EVENT_VOLUME, event).sendToTarget();
			
		}

		@Override
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
