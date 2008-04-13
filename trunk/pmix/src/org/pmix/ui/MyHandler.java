package org.pmix.ui;

import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.Music;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

public class MyHandler extends Handler implements ViewSwitcher.ViewFactory {

	private MainMenuActivity mainMenuActivity = null;

	private long lastKnownElapsedTime;

	private int currentSongTime;

	public int getCurrentSongTime() {
		return currentSongTime;
	}

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

				//	mainMenuActivity.getMainInfo().setText((current.getArtist() != null ? (current.getArtist() + "\n") : "") + (current.getAlbum() != null ? (current.getAlbum() + "\n") : "") + current.getTitle());
					mainMenuActivity.getArtistNameText().setText(current.getArtist() != null ? current.getArtist() : "");
					mainMenuActivity.getAlbumNameText().setText((current.getAlbum() != null ? (current.getAlbum()) : ""));
					mainMenuActivity.getSongNameText().setText((current.getTitle() != null ? (current.getTitle()) : ""));

					// String album = current.getAlbum();
					/*
					 * if (album != null && !previousAlbum.equals(album)) { byte
					 * data[] =
					 * org.pmix.settings.Contexte.getInstance().getMpd().getCover(album);
					 * 
					 * if (data != null) {
					 * 
					 * Drawable drawable = new BitmapDrawable(new
					 * ByteArrayInputStream(data));
					 * mainMenuActivity.getCoverSwitcher().setImageDrawable(drawable);
					 * 
					 * previousAlbum = album; } }
					 */
				} else {
					mainMenuActivity.getMainInfo().setText(songId + " " + status.getSongId());
				}
			} catch (Exception e) {
				mainMenuActivity.getMainInfo().setText(songId + "#" + status.getSongId() + " " + e);
				e.printStackTrace();
			}
		} else
			mainMenuActivity.getMainInfo().setText(songId + "#" + status.getSongId());
		mainMenuActivity.getProgressBar().setProgress(status.getVolume());

		if (status.getTotalTime() > 0) {
			mainMenuActivity.getTrackTime().setText(timeToString(status.getElapsedTime()) + " - " + timeToString(status.getTotalTime()));
		} else {
			mainMenuActivity.getTrackTime().setText("");
		}

		lastKnownElapsedTime = status.getElapsedTime();
		currentSongTime = (int) status.getTotalTime();

		mainMenuActivity.getProgressBarTrack().setProgress((int) (lastKnownElapsedTime * 100 / status.getTotalTime()));
	}

	public View makeView() {
		ImageView i = new ImageView(mainMenuActivity);

		i.setBackgroundColor(0x00FF0000);
		i.setScaleType(ImageView.ScaleType.FIT_CENTER);
		i.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		return i;
	}
}
