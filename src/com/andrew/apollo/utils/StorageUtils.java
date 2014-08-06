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

package com.andrew.apollo.utils;

import android.os.Environment;

import java.io.File;

/**
 * Helpers for virtual chrooting in external storage
 * 
 * @author Evgeny Omelchenko (elemir90@gmail.com)
 */
public final class StorageUtils {
    /* This class is never initiated */
    public StorageUtils() {
    }

    public static File getChrootedParentFile(File file) {
        if (isStorageVolume(file.getAbsolutePath()))
            return null;
        else
            return file.getParentFile();
    }

    public static boolean isStorageVolume(String path) {
        return path.equals(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    public static String getChrootedPath(String absolutePath) {
        String chrootPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        if (absolutePath.startsWith(chrootPath))
            return absolutePath.substring(chrootPath.length());
        else
            return null;
    }

}
