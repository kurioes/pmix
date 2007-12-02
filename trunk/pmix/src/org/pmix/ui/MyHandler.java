package org.pmix.ui;

import org.a0z.mpd.MPDServerException;
import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.Music;

import android.os.Handler;
import android.os.Message;

public class MyHandler extends Handler {

	private MainMenuActivity mainMenuActivity = null;

	private long lastKnownElapsedTime;

	public long getLastKnownElapsedTime() {
		return lastKnownElapsedTime;
	}

	public MyHandler(MainMenuActivity mainMenuActivity) {
		this.mainMenuActivity = mainMenuActivity;
	}

	private static String timeToString(long seconds) {
		long min = seconds / 60;
		long sec = seconds - min * 60;
		return (min < 10 ? "0" + min : min) + ":" + (sec < 10 ? "0" + sec : sec);
	}

	@Override
	public void handleMessage(Message msg) {

		MPDStatus status = (MPDStatus) msg.obj;
		int songId = status.getSongPos();
		if (songId >= 0) {
			try {

				org.pmix.settings.Contexte.getInstance().getMpd().getPlaylist().refresh();

				Music current = org.pmix.settings.Contexte.getInstance().getMpd().getPlaylist().getMusic(songId);
				if (current != null) {

					mainMenuActivity.getMainInfo().setText((current.getArtist() != null ? (current.getArtist() + "\n") : "") + (current.getAlbum() != null ? (current.getAlbum() + "\n") : "") + current.getTitle());
				}
			} catch (MPDServerException e) {
				e.printStackTrace();
			}
		}

		mainMenuActivity.getProgressBar().setProgress(status.getVolume());
		if (status.getTotalTime() > 0) {
			mainMenuActivity.getTrackTime().setText(timeToString(status.getElapsedTime()) + " - " + timeToString(status.getTotalTime()));
		} else {
			mainMenuActivity.getTrackTime().setText("");
		}
		lastKnownElapsedTime = status.getElapsedTime();
		mainMenuActivity.getProgressBarTrack().setProgress((int) (lastKnownElapsedTime * 100 / status.getTotalTime()));
	}
}
