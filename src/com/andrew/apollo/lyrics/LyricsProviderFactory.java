package com.andrew.apollo.lyrics;

public class LyricsProviderFactory {

	public static LyricsProvider getOfflineProvider(String filePath){
		return new OfflineLyricsProvider(filePath);
	}
	
	public static LyricsProvider getMainOnlineProvider() {
		return new LyricsWikiProvider();
	}
	
	//TODO implement more providers, and also a system to iterate over them

}
