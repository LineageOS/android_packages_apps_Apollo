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

package com.andrew.apollo.model;

import android.util.Log;

import com.andrew.apollo.utils.StorageUtils;

import java.io.File;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class that represents list of files in some directory
 * 
 * @author Evgeny Omelchenko (elemir90@gmail.com)
 */
public class FileList extends AbstractList<File> {
    /**
     * Current directory and his parent
     */
    private File mCurrentDirectory, mParentDirectory;
    /**
     * Containers for directories and files
     */
    private List<File> mDirectories, mFiles;

    /**
     * Offset for parent directory if it exist
     */
    private int mHasParentDirectory;

    public FileList() {
        mDirectories = new ArrayList<File>();
        mFiles = new ArrayList<File>();
    }

    public void changeDirectory(File directory) {
        File files[] = directory.listFiles();

        mCurrentDirectory = directory;
        mParentDirectory = StorageUtils.getChrootedParentFile(mCurrentDirectory);
        mHasParentDirectory = (mParentDirectory != null ? 1 : 0);

        mDirectories.clear();
        mFiles.clear();

        Arrays.sort(files);

        for (File file: files) {
            if (file.isDirectory())
                mDirectories.add(file);
            else mFiles.add(file);
        }
    }

    public String getDisplayName(int i) {
        if (i == 0 && mHasParentDirectory == 1)
            return "..";
        else
            return get(i).getName();
    }

    @Override
    public File get(int i) {
        if (i == 0 && mHasParentDirectory == 1)
            return mParentDirectory;
        else if (i < mDirectories.size() + mHasParentDirectory)
            return mDirectories.get(i - mHasParentDirectory);
        else
            return mFiles.get(i - mDirectories.size() - mHasParentDirectory);
    }

    @Override
    public int size() {
        return mDirectories.size() + mFiles.size() + mHasParentDirectory;
    }
}
