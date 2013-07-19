package com.andrew.apollo.ui.activities;

import com.andrew.apollo.utils.ThemeUtils;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public abstract class BasePickerActivity extends FragmentActivity {

    private ThemeUtils mResources;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        // Initialze the theme resources
        mResources = new ThemeUtils(this);

        // Set the overflow style
        mResources.setOverflowStyle(this);

        // Fade it in
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        // Set the layout
        setContentView(setContentView());
    }

    protected abstract int setContentView();

}
