package com.andrew.apollo.lyrics;

public interface LyricsProvider {

	/**
	 * Gives the lyrics of the song, or null if they werent found
	 * 
	 * @param artist
	 *            Artist name
	 * @param song
	 *            Song title
	 * @return Full lyrics as a String, or null
	 */
	public String getLyrics(String artist, String song);

	/**
	 * Gives the name of the provider implementation
	 * 
	 * @return
	 */
	public String getProviderName();

}
