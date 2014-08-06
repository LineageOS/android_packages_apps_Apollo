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

package com.andrew.apollo.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.andrew.apollo.R;
import com.andrew.apollo.adapters.FileAdapter;
import com.andrew.apollo.loaders.FileLoader;
import com.andrew.apollo.menu.CreateNewPlaylist;
import com.andrew.apollo.menu.FragmentMenuItems;
import com.andrew.apollo.model.FileList;
import com.andrew.apollo.recycler.RecycleHolder;
import com.andrew.apollo.utils.MusicUtils;
import com.andrew.apollo.utils.StorageUtils;
import com.andrew.apollo.widgets.Breadcrumb;
import com.andrew.apollo.widgets.BreadcrumbItem;
import com.andrew.apollo.widgets.BreadcrumbView;
import com.devspark.appmsg.AppMsg;

import java.io.File;

/**
 * This class is used to display files on a user's device.
 * 
 * @author Evgeny Omelchenko (elemir90@gmail.com)
 */
public class FileFragment extends Fragment implements OnItemClickListener,
        LoaderManager.LoaderCallbacks<FileList>, BreadcrumbView.BreadcrumbListener {
    /**
     * Used to keep context menu items from bleeding into other fragments
     */
    private static final int FILE_GROUP_ID = 6;
    private static final int DIR_GROUP_ID = 7;

    private static final int LOADER = 0;

    /**
     * Fragment UI
     */
    private ViewGroup mRootView;

    /**
     * The adapter for the list
     */
    private FileAdapter mAdapter;

    /**
     * The list view
     */
    private ListView mListView;

    /**
     * Breadcrumb
     */
    private Breadcrumb mBreadcrumb;
    /**
     * Selected file
     */
    private File mFile;

    /**
     * Path to current directory
     */
    private String mPath;

    /**
     * To restore a current position
     */
    private int mPosition;

    /**
     * Empty constructor as per the {@link android.support.v4.app.Fragment} documentation
     */
    public FileFragment() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create the adapter
        mAdapter = new FileAdapter(getActivity());

        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt("fileListPosition");
            mPath = savedInstanceState.getString("curDirectory");
        }

        if (mPath == null)
            mPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        getLoaderManager().restartLoader(LOADER, null, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("curDirectory", mPath);
        outState.putInt("fileListPosition", mListView.getFirstVisiblePosition());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        // The View for the fragment's UI
        mRootView = (ViewGroup)inflater.inflate(R.layout.list_breadcrumb, null);
        // Initialize the breadcrumb
        mBreadcrumb = (Breadcrumb)mRootView.findViewById(R.id.breadcrumb_bar);
        mBreadcrumb.addBreadcrumbListener(this);
        // Initialize the list
        mListView = (ListView)mRootView.findViewById(R.id.list_base);
        // Set the data behind the list
        mListView.setAdapter(mAdapter);
        // Release any references to the recycled Views
        mListView.setRecyclerListener(new RecycleHolder());
        // Listen for ContextMenus to be created
        mListView.setOnCreateContextMenuListener(this);
        // Play the selected song
        mListView.setOnItemClickListener(this);

        return mRootView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Enable the options menu
        setHasOptionsMenu(true);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // Get the position of the selected item
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;

        mFile = mAdapter.getItem(info.position);

        if (mFile.isDirectory()) {
            // Play the directory content
            menu.add(DIR_GROUP_ID, FragmentMenuItems.PLAY_SELECTION, Menu.NONE,
                    getString(R.string.context_menu_play_selection));

            // Add the directory content to the queue
            menu.add(DIR_GROUP_ID, FragmentMenuItems.ADD_TO_QUEUE, Menu.NONE,
                    getString(R.string.add_to_queue));

            // Add the directory content to a playlist
            final SubMenu subMenu = menu.addSubMenu(DIR_GROUP_ID, FragmentMenuItems.ADD_TO_PLAYLIST,
                    Menu.NONE, R.string.add_to_playlist);
            MusicUtils.makePlaylistMenu(getActivity(), DIR_GROUP_ID, subMenu, true);

            // Delete the directory
            menu.add(DIR_GROUP_ID, FragmentMenuItems.DELETE, Menu.NONE,
                    getString(R.string.context_menu_delete));
        } else {
            // Play the song
            menu.add(FILE_GROUP_ID, FragmentMenuItems.PLAY_SELECTION, Menu.NONE,
                    getString(R.string.context_menu_play_selection));

            // Play next
            menu.add(FILE_GROUP_ID, FragmentMenuItems.PLAY_NEXT, Menu.NONE,
                    getString(R.string.context_menu_play_next));

            // Add the song to the queue
            menu.add(FILE_GROUP_ID, FragmentMenuItems.ADD_TO_QUEUE, Menu.NONE,
                    getString(R.string.add_to_queue));

            // Add the song to a playlist
            final SubMenu subMenu = menu.addSubMenu(FILE_GROUP_ID, FragmentMenuItems.ADD_TO_PLAYLIST,
                    Menu.NONE, R.string.add_to_playlist);
            MusicUtils.makePlaylistMenu(getActivity(), FILE_GROUP_ID, subMenu, true);

            // Make the song a ringtone
            menu.add(FILE_GROUP_ID, FragmentMenuItems.USE_AS_RINGTONE, Menu.NONE,
                    getString(R.string.context_menu_use_as_ringtone));

            // Delete the song
            menu.add(FILE_GROUP_ID, FragmentMenuItems.DELETE, Menu.NONE,
                    getString(R.string.context_menu_delete));
        }
    }

    @Override
    public boolean onContextItemSelected(final android.view.MenuItem item) {
        long mSelectedIdList[] = MusicUtils.getSongListFromFile(getActivity(),
                mFile);

        switch (item.getItemId()) {
            case FragmentMenuItems.PLAY_SELECTION:
                MusicUtils.playAll(getActivity(), mSelectedIdList, 0, false);
                return true;
            case FragmentMenuItems.ADD_TO_QUEUE:
                MusicUtils.addToQueue(getActivity(), mSelectedIdList);
                return true;
            case FragmentMenuItems.NEW_PLAYLIST:
                CreateNewPlaylist.getInstance(mSelectedIdList).show(getFragmentManager(),
                        "CreatePlaylist");
                return true;
            case FragmentMenuItems.PLAYLIST_SELECTED:
                final long mPlaylistId = item.getIntent().getLongExtra("playlist", 0);
                MusicUtils.addToPlaylist(getActivity(), mSelectedIdList, mPlaylistId);
                return true;
            case FragmentMenuItems.DELETE:
                buildDeleteDialog().show();
                return true;
            default:
                if (item.getGroupId() == FILE_GROUP_ID && mSelectedIdList.length > 0) {
                    switch (item.getItemId()) {
                        case FragmentMenuItems.PLAY_NEXT:
                            MusicUtils.playNext(mSelectedIdList);
                            return true;
                        case FragmentMenuItems.USE_AS_RINGTONE:
                            MusicUtils.setRingtone(getActivity(), mSelectedIdList[0]);
                            return true;
                        default:
                            break;
                    }
                }
                break;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position,
            final long id) {
        File file = mAdapter.getItem(position);

        if (file.isDirectory()) {
            if (file.canExecute() && file.canRead())
                changeDirectory(file.getAbsolutePath());
            else
                AppMsg.makeText(getActivity(), R.string.permission_denied, AppMsg.STYLE_ALERT).show();
        } else {
            long mSelectedIdList[] = MusicUtils.getSongListFromFile(getActivity(),
                    file);
            if (mSelectedIdList != null)
                MusicUtils.playAll(getActivity(), mSelectedIdList, 0, false);
        }
    }

    private final AlertDialog buildDeleteDialog() {
        final FileFragment fragment = this;
        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.delete_dialog_title, mFile.getName()))
                .setPositiveButton(R.string.context_menu_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (mFile.delete())
                            getLoaderManager().restartLoader(LOADER, null, fragment);
                        else
                            AppMsg.makeText(getActivity(), R.string.cannot_delete, AppMsg.STYLE_ALERT).show();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                    }
                }).setMessage(R.string.cannot_be_undone).create();
    }

    @Override
    public Loader<FileList> onCreateLoader(int i, Bundle bundle) {
        return new FileLoader(getActivity(), mPath);
    }

    @Override
    public void onLoadFinished(Loader<FileList> loader, FileList data) {
        String chrootedPath = StorageUtils.getChrootedPath(mPath);
        mAdapter.setListItems(data);
        mAdapter.notifyDataSetChanged();

        mListView.clearFocus();
        mListView.post(new Runnable() {
            @Override
            public void run() {
                mListView.requestFocusFromTouch();
                mListView.setSelection(mPosition);
                mListView.requestFocus();
            }
        });

        if (chrootedPath != null)
            mBreadcrumb.changeBreadcrumbPath(chrootedPath, true);
        else
            mBreadcrumb.changeBreadcrumbPath(mPath, false);
    }

    @Override
    public void onLoaderReset(Loader<FileList> loader) {
    }

    @Override
    public void onBreadcrumbItemClick(BreadcrumbItem item) {
        changeDirectory(item.getItemPath());
    }

    private void changeDirectory(String path) {
        mPath = path;
        mPosition = 0;
        getLoaderManager().restartLoader(LOADER, null, this);
    }
}
