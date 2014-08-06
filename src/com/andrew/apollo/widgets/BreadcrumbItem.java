/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andrew.apollo.widgets;

import android.content.Context;
import android.util.AttributeSet;

import com.andrew.apollo.utils.ThemeUtils;
import com.andrew.apollo.widgets.theme.HoloSelector;
import com.andrew.apollo.widgets.theme.ThemeableTextView;

/**
 * A class that represents a item of a breadcrumb.
 */
public class BreadcrumbItem extends ThemeableTextView {

    private String mItemPath;

    /**
     * Constructor of <code>BreadcrumbItem</code>.
     *
     * @param context The current context
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public BreadcrumbItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        setBackgroundDrawable(new HoloSelector(context));
        setTextColor((new ThemeUtils(context)).getColor("line_one"));
    }

    /**
     * Method that returns the item path associated with with this breadcrumb item.
     *
     * @return String The item path associated
     */
    public String getItemPath() {
        return mItemPath;
    }

    /**
     * Method that sets the item path associated with with this breadcrumb item.
     *
     * @param itemPath The item path
     */
    protected void setItemPath(String itemPath) {
        mItemPath = itemPath;
    }

}
