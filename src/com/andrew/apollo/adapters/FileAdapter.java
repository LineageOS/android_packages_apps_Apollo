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

package com.andrew.apollo.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.FileObserver;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.andrew.apollo.R;
import com.andrew.apollo.model.FileList;
import com.andrew.apollo.ui.MusicHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This {@link android.widget.ArrayAdapter} is used to display all of the files on a user's
 * device for {@link com.andrew.apollo.ui.fragments.FileFragment}.
 *
 * @author Evgeny Omelchenko (elemir90@gmail.com)
 */
public class FileAdapter extends BaseAdapter {
    /**
     * Number of views (TextView)
     */
    private static final int VIEW_TYPE_COUNT = 1;
    /**
     * The resource Id of the layout to inflate
    */
    private static final int mLayoutId = R.layout.list_item_file;
    /**
     * Cache of folder icon bitmap
     */
    private final Bitmap mFolderIcon;
    /**
     * Link to saved context
     */
    private final Context mContext;
    /**
     *
     */
    private FileList mData;

    /**
     * Constructor of <code>FileAdapter</code>
     *
     * @param context The {@link android.content.Context} to use.
     */
    public FileAdapter(final Context context) {
        super();
        // Save context
        mContext = context;
        mFolderIcon = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.ic_folder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        if (mData != null)
            return mData.size();
        else return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getItem(int i) {
        if (mData != null)
            return mData.get(i);
        else return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        MusicHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mLayoutId, parent, false);
            holder = new MusicHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (MusicHolder)convertView.getTag();
        }

        if (mData.get(position).isDirectory()) {
            holder.mImage.get().setImageBitmap(mFolderIcon);
            holder.mImage.get().setVisibility(View.VISIBLE);
        } else
            holder.mImage.get().setVisibility(View.GONE);

        holder.mLineOne.get().setText(mData.getDisplayName(position));

        return convertView;
    }

    public void setListItems(FileList data) {
        mData = data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}
