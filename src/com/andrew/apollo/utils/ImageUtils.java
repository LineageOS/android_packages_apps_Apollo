package com.andrew.apollo.utils;

import android.widget.ImageView;

public class ImageUtils {

    private static ImageProvider imageProvider;

    public static final boolean DEBUG = true;

    public static void setArtistImage(ImageView imageView, String artist) {
        getImageProvider().setArtistImage(imageView, artist);
    }

    public static void setAlbumImage(ImageView imageView, long id, String artist, String album) {
        getImageProvider().setAlbumImage(imageView, id, artist, album);
    }

    private static ImageProvider getImageProvider() {
        if (imageProvider == null)
            imageProvider = new ImageProvider();
        return imageProvider;
    }
}
