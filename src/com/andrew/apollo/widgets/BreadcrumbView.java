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

package  com.andrew.apollo.widgets;

import android.content.Context;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;

import com.andrew.apollo.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A view that holds a navigation breadcrumb pattern.
 */
public class BreadcrumbView extends RelativeLayout implements Breadcrumb,
        OnClickListener {

    HorizontalScrollView mScrollView;
    private ViewGroup mBreadcrumbBar;

    private List<BreadcrumbListener> mBreadcrumbListeners;

    /**
     * Constructor of <code>BreadcrumbView</code>.
     *
     * @param context The current context
     */
    public BreadcrumbView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor of <code>BreadcrumbView</code>.
     *
     * @param context The current context
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public BreadcrumbView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructor of <code>BreadcrumbView</code>.
     *
     * @param context The current context
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @param defStyle The default style to apply to this view. If 0, no style
     *        will be applied (beyond what is included in the theme). This may
     *        either be an attribute resource, whose value will be retrieved
     *        from the current theme, or an explicit style resource.
     */
    public BreadcrumbView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Method that initializes the view. This method loads all the necessary
     * information and create an appropriate layout for the view
     */
    private void init() {
        //Initialize the listeners
        mBreadcrumbListeners =
              Collections.synchronizedList(new ArrayList<BreadcrumbListener>());

        mScrollView = (HorizontalScrollView)inflate(getContext(),
                R.layout.breadcrumb_view, null);
        //Add the view of the breadcrumb
        addView(mScrollView);

        //Recovery all views
        mBreadcrumbBar = (ViewGroup)findViewById(R.id.breadcrumb);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addBreadcrumbListener(BreadcrumbListener listener) {
        mBreadcrumbListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeBreadcrumbListener(BreadcrumbListener listener) {
        mBreadcrumbListeners.remove(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changeBreadcrumbPath(final String newPath, final boolean isChrooted) {
        BreadcrumbItem item = createBreadcrumbItem();
        String []dirs = newPath.split(File.separator);
        String baseName = isChrooted ? Environment.getExternalStorageDirectory().getName() : File.separator;
        String path = isChrooted ? Environment.getExternalStorageDirectory().getAbsolutePath() : File.separator;

        mBreadcrumbBar.removeAllViews();

        item.setText(baseName);
        item.setItemPath(path);
        mBreadcrumbBar.addView(item);

        for (int i = 1; i < dirs.length; i++) {
            item = createBreadcrumbItem();

            mBreadcrumbBar.addView(createItemDivider());
            path += dirs[i] + File.separator;
            item.setText(dirs[i]);
            item.setItemPath(path);
            mBreadcrumbBar.addView(item);
        }

        //Set scrollbar at the end
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                BreadcrumbView.this.mScrollView.fullScroll(View.FOCUS_RIGHT);
            }
        });
    }

    /**
     * Method that creates a new path divider.
     *
     * @return View The path divider
     */
    private View createItemDivider() {
        LayoutInflater inflater =
                (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.breadcrumb_item_divider, mBreadcrumbBar, false);
    }

    /**
     * Method that creates a new breadcrumb item stub
     *
     * @return BreadcrumbItem The view create
     */
    private BreadcrumbItem createBreadcrumbItem() {
        LayoutInflater inflater =
                (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        BreadcrumbItem item =
                (BreadcrumbItem)inflater.inflate(
                        R.layout.breadcrumb_item, mBreadcrumbBar, false);
        item.setOnClickListener(this);

        return item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        BreadcrumbItem item = (BreadcrumbItem)v;
        int cc = this.mBreadcrumbListeners.size();
        for (int i = 0; i < cc; i++) {
            mBreadcrumbListeners.get(i).onBreadcrumbItemClick(item);
        }
    }

    /**
     * Interface with events from a breadcrumb.
     */
    public interface BreadcrumbListener {
        /**
         * This method is fired when a breadcrumb item is clicked.
         *
         * @param item The breadcrumb item click
         */
        void onBreadcrumbItemClick(BreadcrumbItem item);
    }
}
