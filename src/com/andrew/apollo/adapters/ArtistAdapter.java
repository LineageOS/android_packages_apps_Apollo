
package com.andrew.apollo.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.andrew.apollo.R;
import com.andrew.apollo.grid.fragments.ArtistsFragment;
import com.andrew.apollo.utils.ImageUtils;
import com.andrew.apollo.utils.MusicUtils;
import com.andrew.apollo.views.ViewHolderGrid;
import com.androidquery.AQuery;

import java.lang.ref.WeakReference;

/**
 * @author Andrew Neal
 */
public class ArtistAdapter extends SimpleCursorAdapter {

    private AnimationDrawable mPeakOneAnimation, mPeakTwoAnimation;

    private WeakReference<ViewHolderGrid> holderReference;

    public ArtistAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);
        // ViewHolderGrid
        final ViewHolderGrid viewholder;

        if (view != null) {

            viewholder = new ViewHolderGrid(view);
            holderReference = new WeakReference<ViewHolderGrid>(viewholder);
            view.setTag(holderReference.get());

        } else {
            viewholder = (ViewHolderGrid)convertView.getTag();
        }

        // AQuery
        final AQuery aq = new AQuery(view);

        // Artist Name
        String artistName = mCursor.getString(ArtistsFragment.mArtistNameIndex);
        holderReference.get().mViewHolderLineOne.setText(artistName);

        // Number of albums
        int albums_plural = mCursor.getInt(ArtistsFragment.mArtistNumAlbumsIndex);
        boolean unknown = artistName == null || artistName.equals(MediaStore.UNKNOWN_STRING);
        String numAlbums = MusicUtils.makeAlbumsLabel(mContext, albums_plural, 0, unknown);
        holderReference.get().mViewHolderLineTwo.setText(numAlbums);

        ImageUtils.setArtistImage(viewholder.mViewHolderImage, artistName);

        // Now playing indicator
        long currentartistid = MusicUtils.getCurrentArtistId();
        long artistid = mCursor.getLong(ArtistsFragment.mArtistIdIndex);
        if (currentartistid == artistid) {
            holderReference.get().mPeakOne.setImageResource(R.anim.peak_meter_1);
            holderReference.get().mPeakTwo.setImageResource(R.anim.peak_meter_2);
            mPeakOneAnimation = (AnimationDrawable)holderReference.get().mPeakOne.getDrawable();
            mPeakTwoAnimation = (AnimationDrawable)holderReference.get().mPeakTwo.getDrawable();
            try {
                if (MusicUtils.mService.isPlaying()) {
                    mPeakOneAnimation.start();
                    mPeakTwoAnimation.start();
                } else {
                    mPeakOneAnimation.stop();
                    mPeakTwoAnimation.stop();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            holderReference.get().mPeakOne.setImageResource(0);
            holderReference.get().mPeakTwo.setImageResource(0);
        }
        return view;
    }
}
