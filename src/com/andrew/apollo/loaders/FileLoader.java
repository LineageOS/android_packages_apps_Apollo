/*
 * Copyright (C) 2014 Evgeny Omelchenko Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.andrew.apollo.loaders;

import android.os.Environment;
import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.os.FileObserver;

import com.andrew.apollo.model.FileList;

import java.io.File;


/**
 * Used to generate useful list of files in directory
 *
 * @author Evgeny Omelchenko (elemir90@gmail.com)
 */
public class FileLoader extends AsyncTaskLoader<FileList> {

    private static final int FILE_OBSERVER_MASK = FileObserver.CREATE
            | FileObserver.DELETE | FileObserver.DELETE_SELF
            | FileObserver.MOVED_FROM | FileObserver.MOVED_TO
            | FileObserver.MODIFY | FileObserver.MOVE_SELF;

    private FileObserver mFileObserver;

    private FileList mData;
    private String mPath;

    public FileLoader(Context context, String path) {
        super(context);
        mPath = path;
    }

    @Override
    public FileList loadInBackground() {
        File directory = new File(mPath);
        FileList result;

        if (!directory.isDirectory())
            directory = Environment.getExternalStorageDirectory();

        result = new FileList();
        result.changeDirectory(directory);

        return result;
    }

    @Override
    public void deliverResult(FileList data) {
        if (isReset()) {
            onReleaseResources(data);
            return;
        }

        FileList oldData = mData;
        mData = data;

        if (isStarted())
            super.deliverResult(data);

        if (oldData != null && oldData != data)
            onReleaseResources(oldData);
    }

    @Override
    protected void onStartLoading() {
        if (mData != null)
            deliverResult(mData);

        if (mFileObserver == null) {
            mFileObserver = new FileObserver(mPath, FILE_OBSERVER_MASK) {
                @Override
                public void onEvent(int event, String path) {
                    onContentChanged();
                }
            };
        }
        mFileObserver.startWatching();

        if (takeContentChanged() || mData == null)
            forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();

        if (mData != null) {
            onReleaseResources(mData);
            mData = null;
        }
    }

    @Override
    public void onCanceled(FileList data) {
        super.onCanceled(data);

        onReleaseResources(data);
    }

    protected void onReleaseResources(FileList data) {
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
            mFileObserver = null;
        }
    }
}