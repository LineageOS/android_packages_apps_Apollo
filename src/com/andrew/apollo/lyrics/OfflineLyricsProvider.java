package com.andrew.apollo.lyrics;

import java.io.File;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import android.util.Log;

public class OfflineLyricsProvider implements LyricsProvider {

	private File audioFile;

	public OfflineLyricsProvider(String filePath) {
		this.setTrackFile(filePath);
	}

	public void setTrackFile(String path) {
		audioFile = new File(path);
	}

	@Override
	public String getLyrics(String artist, String song) {
		String lyrics = null;
		try {
			if (audioFile == null) {
				throw new NullPointerException("You must call setTrackFile() first");
			}
			if (audioFile.exists()) {
				// Use jAudioTagger library to get the file's lyrics

				AudioFile f = AudioFileIO.read(audioFile);
				Tag tag = f.getTag();
				lyrics = tag.getFirst(FieldKey.LYRICS);
			}
		} catch (Exception e) {
		}
		if (lyrics != null && lyrics.isEmpty()) {
			lyrics = null;
		}
		return lyrics;
	}

	@Override
	public String getProviderName() {
		return "File metadata";
	}

	// I don't really like the way this method is here, but I'm not
	// really creative right now
	public static void saveLyrics(String lyrics, String filePath) {
		try {
			File file = new File(filePath);

			if (file.exists()) {
				// Use jAudioTagger library to set the file's lyrics
				AudioFile f = AudioFileIO.read(file);
				Tag tag = f.getTag();
				tag.setField(FieldKey.LYRICS, lyrics);
				f.commit();
			}
		} catch (Exception e) {
			Log.d("Lyrics", e.getMessage());
		}
	}

}
