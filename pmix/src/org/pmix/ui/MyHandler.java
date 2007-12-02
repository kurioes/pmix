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

		lastKnownElapsedTime = status.getElapsedTime();
		mainMenuActivity.getProgressBarTrack().setProgress((int) (lastKnownElapsedTime * 100 / status.getTotalTime()));
	}
}
