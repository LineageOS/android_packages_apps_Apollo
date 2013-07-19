/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.andrew.apollo.utils;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Intent;
import android.media.RingtoneManager;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;

import com.andrew.apollo.Config;
import com.andrew.apollo.R;
import com.andrew.apollo.model.Album;
import com.andrew.apollo.ui.activities.AudioPlayerActivity;
import com.andrew.apollo.ui.activities.HomeActivity;
import com.andrew.apollo.ui.activities.ProfileActivity;
import com.andrew.apollo.ui.activities.SearchActivity;
import com.andrew.apollo.ui.activities.SettingsActivity;
import com.devspark.appmsg.AppMsg;

/**
 * Various navigation helpers.
 * 
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public final class NavUtils {

    public static final int REQUEST_PICKER = 2;

    /**
     * Opens the profile of an artist.
     * 
     * @param context The {@link Activity} to use.
     * @param artistName The name of the artist
     */
    public static void openArtistProfile(final Activity context,
            final String artistName) {
        openArtistProfile(context, artistName, false);
    }

    public static void openArtistProfile(final Activity context,
            final String artistName, boolean isPicker) {

        // Create a new bundle to transfer the artist info
        final Bundle bundle = new Bundle();
        bundle.putLong(Config.ID, MusicUtils.getIdForArtist(context, artistName));
        bundle.putString(Config.MIME_TYPE, MediaStore.Audio.Artists.CONTENT_TYPE);
        bundle.putString(Config.ARTIST_NAME, artistName);

        // Create the intent to launch the profile activity
        final Intent intent = new Intent(context, ProfileActivity.class);
        if (isPicker) {
            bundle.putBoolean("picker", true);
        }
        intent.putExtras(bundle);
        if (isPicker) {
            context.startActivityForResult(intent, REQUEST_PICKER);
        } else {
            context.startActivity(intent);
        }
    }

    /**
     * Opens the profile of an album.
     * 
     * @param context The {@link Activity} to use.
     * @param albumName The name of the album
     * @param artistName The name of the album artist
     * @param albumId The id of the album
     */
    public static void openAlbumProfile(final Activity context,
            final String albumName, final String artistName, final long albumId) {
        openAlbumProfile(context, albumName, artistName, albumId, false);
    }

    /**
     * Opens the profile of an album.
     * 
     * @param context The {@link Activity} to use.
     * @param albumName The name of the album
     * @param artistName The name of the album artist
     * @param albumId The id of the album
     * @param isPicker if true picker argument will be passed to activity
     */
    public static void openAlbumProfile(final Activity context,
            final String albumName, final String artistName, final long albumId, boolean isPicker) {

        // Create a new bundle to transfer the album info
        final Bundle bundle = new Bundle();
        bundle.putString(Config.ALBUM_YEAR, MusicUtils.getReleaseDateForAlbum(context, albumName));
        bundle.putString(Config.ARTIST_NAME, artistName);
        bundle.putString(Config.MIME_TYPE, MediaStore.Audio.Albums.CONTENT_TYPE);
        bundle.putLong(Config.ID, albumId);
        bundle.putString(Config.NAME, albumName);

        if (isPicker) {
            bundle.putBoolean("picker", true);
        }
        // Create the intent to launch the profile activity
        final Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtras(bundle);
        if (isPicker) {
            context.startActivityForResult(intent, REQUEST_PICKER);
        } else {
            context.startActivity(intent);
        }
    }

    /**
     * Opens the sound effects panel or DSP manager in CM
     * 
     * @param context The {@link Activity} to use.
     */
    public static void openEffectsPanel(final Activity context) {
        try {
            final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
            effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, MusicUtils.getAudioSessionId());
            context.startActivity(effects);
        } catch (final ActivityNotFoundException notFound) {
            AppMsg.makeText(context, context.getString(R.string.no_effects_for_you),
                    AppMsg.STYLE_ALERT);
        }
    }

    /**
     * Opens to {@link SettingsActivity}.
     * 
     * @param activity The {@link Activity} to use.
     */
    public static void openSettings(final Activity activity) {
        final Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivity(intent);
    }

    /**
     * Opens to {@link AudioPlayerActivity}.
     * 
     * @param activity The {@link Activity} to use.
     */
    public static void openAudioPlayer(final Activity activity) {
        final Intent intent = new Intent(activity, AudioPlayerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    /**
     * Opens to {@link SearchActivity}.
     * 
     * @param activity The {@link Activity} to use.
     * @param query The search query.
     */
    public static void openSearch(final Activity activity, final String query) {
        final Bundle bundle = new Bundle();
        final Intent intent = new Intent(activity, SearchActivity.class);
        intent.putExtra(SearchManager.QUERY, query);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    /**
     * Opens to {@link HomeActivity}.
     * 
     * @param activity The {@link Activity} to use.
     */
    public static void goHome(final Activity activity) {
        final Intent intent = new Intent(activity, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void finishPicker(final Activity activity, final long id) {
        MusicUtils.applyRingtoneValues(activity, id);
        Uri song_uri = ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, id);
        Intent i = new Intent();
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, song_uri);
        activity.setResult(Activity.RESULT_OK, i);
        activity.finish();
    }
}
