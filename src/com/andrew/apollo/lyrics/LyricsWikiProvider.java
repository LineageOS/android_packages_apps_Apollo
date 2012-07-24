package com.andrew.apollo.lyrics;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LyricsWikiProvider implements LyricsProvider {

	public static final String PROVIDER_NAME = "LyricsWiki";

	@Override
	public String getLyrics(String artist, String song) {
		String ret = null;
		artist = artist.replace(" ", "%20");
		song = song.replace(" ", "%20");
		try{
			// Get the lyrics URL
			URL url = new URL(String.format("http://lyrics.wikia.com/api.php?action=lyrics&fmt=json&func=getSong&artist=%1s&song=%1s", artist, song));
			String res = getUrlAsString(url);
			String songUrl = new JSONObject(res.replace("song = ", "")).getString("url");
			if(songUrl.endsWith("action=edit")){
				throw new RuntimeException("Lyrics not available");
			}

			// And now get the full lyrics
			url = new URL(songUrl);
			String html = getUrlAsString(url);
			// Clean that ugly html
			// -Take the relevant part only
			html = html.substring(html.indexOf("<div class='lyricbox'>"));
			html = html.substring(html.indexOf("</div>") + 6);
			html = html.substring(0, html.indexOf("<!--"));
			// -Replace new line html with characters
			html = html.replace("<br />", "\n;");
			// -Now parse the html entities
			String[] htmlChars = html.split(";");
			StringBuilder stringBuilder = new StringBuilder();
			String code = null;
			char caracter;
			for(String s : htmlChars){
				if(s.equals("\n")){
					stringBuilder.append(s);
				}else{
					code = s.replaceAll("&#", "");
					caracter = (char)Integer.valueOf(code).intValue();
					stringBuilder.append(caracter);
				}
			}
			// And that's it
			ret = stringBuilder.toString();
		}catch(Exception e){
			Log.d("Apollo", "Lyrics not found in " + getProviderName(), e);
		}

		return ret;
	}

	public static String getUrlAsString(URL url) throws IOException {
		// Realiza la petición GET para traerse las recetas
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		c.setRequestMethod("GET");
		c.setReadTimeout(15 * 1000);
		c.setUseCaches(false);
		c.connect();
		// Lee la salida del servidor
		BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		while((line = reader.readLine()) != null){
			stringBuilder.append(line + "\n");
		}
		return stringBuilder.toString();
	}

	@Override
	public String getProviderName() {
		return PROVIDER_NAME;
	}

}
