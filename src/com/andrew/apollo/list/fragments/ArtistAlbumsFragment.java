/**
 * 
 */

package com.andrew.apollo.list.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.andrew.apollo.Constants;
import com.andrew.apollo.R;
import com.andrew.apollo.activities.TracksBrowser;
import com.andrew.apollo.adapters.ArtistAlbumAdapter;
import com.andrew.apollo.service.ApolloService;
import com.andrew.apollo.tasks.GetCachedImages;
import com.andrew.apollo.tasks.LastfmGetAlbumImages;
import com.andrew.apollo.utils.ApolloUtils;
import com.andrew.apollo.utils.MusicUtils;

/**
 * @author Andrew Neal
 * @Note This is used in the @TracksBrowser after touching an artist from @ArtistsFragment
 */
public class ArtistAlbumsFragment extends Fragment implements LoaderCallbacks<Cursor>,
        OnItemClickListener, Constants {

    // Adapter
    private ArtistAlbumAdapter mArtistAlbumAdapter;

    // Audio columns
    public static int mAlbumIdIndex, mAlbumNameIndex, mSongCountIndex, mArtistNameIndex;

    // ListView
    private ListView mListView;

    // Options
    private final int PLAY_SELECTION = 15;

    private final int ADD_TO_PLAYLIST = 16;

    private final int SEARCH = 17;

    // Album ID
    private String mCurrentAlbumId;

    // Cursor
    private Cursor mCursor;

    public ArtistAlbumsFragment() {
    }

    public ArtistAlbumsFragment(Bundle args) {
        setArguments(args);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // AlbumAdapter
        mArtistAlbumAdapter = new ArtistAlbumAdapter(getActivity(), R.layout.listview_items, null,
                new String[] {}, new int[] {}, 0);
        mListView.setOnCreateContextMenuListener(this);
        mListView.setAdapter(mArtistAlbumAdapter);
        mListView.setOnItemClickListener(this);

        // Important!
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.listview, container, false);
        mListView = (ListView)root.findViewById(android.R.id.list);

        // Set the header for @TrackBrowser
        String header = getActivity().getResources().getString(R.string.album_header);
        int left = getActivity().getResources().getInteger(R.integer.listview_padding_left);
        int right = getActivity().getResources().getInteger(R.integer.listview_padding_right);
        ApolloUtils.listHeader(this, root, header);
        ApolloUtils.setListPadding(this, mListView, left, 0, right, 0);
        return root;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                BaseColumns._ID, AlbumColumns.ALBUM, AlbumColumns.NUMBER_OF_SONGS,
                AlbumColumns.ARTIST
        };
        if (getArguments() != null) {
            long artistId = getArguments().getLong((BaseColumns._ID));
            Uri uri = Audio.Artists.Albums.getContentUri(EXTERNAL, artistId);
            String sortOrder = Audio.Albums.DEFAULT_SORT_ORDER;
            return new CursorLoader(getActivity(), uri, projection, null, null, sortOrder);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Check for database errors
        if (data == null) {
            return;
        }

        mAlbumIdIndex = data.getColumnIndexOrThrow(BaseColumns._ID);
        mAlbumNameIndex = data.getColumnIndexOrThrow(AlbumColumns.ALBUM);
        mSongCountIndex = data.getColumnIndexOrThrow(AlbumColumns.NUMBER_OF_SONGS);
        mArtistNameIndex = data.getColumnIndexOrThrow(AlbumColumns.ARTIST);
        mArtistAlbumAdapter.changeCursor(data);
        mCursor = data;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mArtistAlbumAdapter != null)
            mArtistAlbumAdapter.changeCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putAll(getArguments() != null ? getArguments() : new Bundle());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        tracksBrowser(id);
    }

    /**
     * Update the list as needed
     */
    private final BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mListView != null) {
                mArtistAlbumAdapter.notifyDataSetChanged();
            }
        }

    };

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ApolloService.META_CHANGED);
        filter.addAction(ApolloService.PLAYSTATE_CHANGED);
        getActivity().registerReceiver(mMediaStatusReceiver, filter);
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(mMediaStatusReceiver);
        super.onStop();
    }

    /**
     * @param index
     * @param id
     */
    private void tracksBrowser(long id) {
        String artistName = mCursor.getString(mArtistNameIndex);
        String albumName = mCursor.getString(mAlbumNameIndex);

        Bundle bundle = new Bundle();
        bundle.putString(MIME_TYPE, Audio.Albums.CONTENT_TYPE);
        bundle.putString(ALBUM_KEY, albumName);
        bundle.putString(ARTIST_KEY, artistName);
        bundle.putLong(BaseColumns._ID, id);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(getActivity(), TracksBrowser.class);
        intent.putExtras(bundle);
        getActivity().startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.add(0, PLAY_SELECTION, 0, getResources().getString(R.string.play_all));
        menu.add(0, ADD_TO_PLAYLIST, 0, getResources().getString(R.string.add_to_playlist));
        menu.add(0, SEARCH, 0, getResources().getString(R.string.search));

        mCurrentAlbumId = mCursor.getString(mCursor.getColumnIndexOrThrow(BaseColumns._ID));

        menu.setHeaderView(setHeaderLayout());
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case PLAY_SELECTION: {
                long[] list = MusicUtils.getSongListForAlbum(getActivity(),
                        Long.parseLong(mCurrentAlbumId));
                MusicUtils.playAll(getActivity(), list, 0);
                break;
            }
            case ADD_TO_PLAYLIST: {
                Intent intent = new Intent(INTENT_ADD_TO_PLAYLIST);
                long[] list = MusicUtils.getSongListForAlbum(getActivity(),
                        Long.parseLong(mCurrentAlbumId));
                intent.putExtra(INTENT_PLAYLIST_LIST, list);
                getActivity().startActivity(intent);
                break;
            }
            case SEARCH: {
                MusicUtils.doSearch(getActivity(), mCursor, mAlbumNameIndex);
                break;
            }
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * @return A custom ContextMenu header
     */
    public View setHeaderLayout() {
        // Get album name
        String albumName = mCursor.getString(mAlbumNameIndex);
        // Get artist name
        String artistName = mCursor.getString(mArtistNameIndex);

        // Inflate the header View
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View header = inflater.inflate(R.layout.context_menu_header, null, false);

        // Artist image
        ImageView headerImage = (ImageView)header.findViewById(R.id.header_image);

        // Only download images we don't already have
        if (ApolloUtils.getImageURL(albumName, ALBUM_IMAGE, getActivity()) == null)
            new LastfmGetAlbumImages(getActivity(), null, 0).executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR, artistName, albumName);

        // Get and set cached image
        new GetCachedImages(getActivity(), 1, headerImage).executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR, albumName);

        // Set artist name
        TextView headerText = (TextView)header.findViewById(R.id.header_text);
        headerText.setText(albumName);
        headerText.setBackgroundColor(getResources().getColor(R.color.transparent_black));
        return header;
    }
}
