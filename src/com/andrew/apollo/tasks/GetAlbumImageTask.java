package com.andrew.apollo.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore.Audio;
import android.util.Log;
import com.andrew.apollo.lastfm.api.Album;
import com.andrew.apollo.lastfm.api.ImageSize;
import com.andrew.apollo.utils.ApolloUtils;
import com.andrew.apollo.utils.ImageUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import static com.andrew.apollo.Constants.LASTFM_API_KEY;

public class GetAlbumImageTask extends GetBitmapTask {
    private static final String TAG = "GetAlbumImageTask";
    private static final Uri ALBUM_ART_URI = Uri.parse("content://media/external/audio/albumart");
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();

    static {
        sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        sBitmapOptions.inDither = false;
    }

    private String mArtist;
    private String mAlbum;
    private long mId;

    public GetAlbumImageTask(long id, String artist, String album,
            OnBitmapReadyListener listener, String tag, Context context) {
        super(listener, tag, context);
        mId = id;
        mArtist = artist;
        mAlbum = album;
    }

    @Override
    protected Bitmap getBitmap(Context context) {
        if (mId < 0) {
            return null;
        }

        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(ALBUM_ART_URI, mId);
        if (uri == null) {
            return null;
        }

        Bitmap result = null;
        InputStream in = null;

        try {
            in = res.openInputStream(uri);
            result = BitmapFactory.decodeStream(in, null, sBitmapOptions);
        } catch (FileNotFoundException e) {
            // The album art thumbnail does not actually exist. Maybe the user deleted it, or
            // maybe it never existed to begin with.
            try {
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    result = BitmapFactory.decodeFileDescriptor(fd);
                }
            } catch (IllegalStateException e2) {
            } catch (FileNotFoundException e2) {
            }
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }

        if (ImageUtils.DEBUG && result != null) {
            Log.d(TAG, "Got provider bitmap for album " + mId + " (" + mArtist + " - " + mAlbum + "): " +
                    result.getWidth() + "x" + result.getHeight());
        }

        return result;
    }

    @Override
    protected File getFile(Context context, String extension) {
        String albumPart = ApolloUtils.escapeForFileSystem(mAlbum);
        String artistPart = ApolloUtils.escapeForFileSystem(mArtist);

        if (albumPart == null || artistPart == null) {
            Log.e(TAG, "Can't create file name for: " + mAlbum + " " + mArtist);
            return null;
        }

        return new File(context.getExternalFilesDir(null), artistPart + " - " + albumPart + extension);
    }

    @Override
    protected String getImageUrl() {
        try {
            Album album = Album.getInfo(mArtist, this.mAlbum, LASTFM_API_KEY);
            if (album == null) {
                if (ImageUtils.DEBUG) Log.w(TAG, "Album not found: " + mArtist + " - " + this.mAlbum);
                return null;
            }
            return album.getImageURL(ImageSize.LARGE); //TODO: ensure that there is an image available in the specified size
        } catch (Exception e) {
            if (ImageUtils.DEBUG) Log.w(TAG, "Error when retrieving album image url", e);
            return null;
        }
    }
}
