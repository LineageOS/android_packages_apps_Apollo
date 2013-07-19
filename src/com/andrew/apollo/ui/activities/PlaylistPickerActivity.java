package com.andrew.apollo.ui.activities;

import com.andrew.apollo.R;
import com.andrew.apollo.ui.fragments.PlaylistFragment;
import com.andrew.apollo.ui.fragments.phone.MusicBrowserPhoneFragment;

import android.os.Bundle;

public class PlaylistPickerActivity extends BasePickerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the music browser fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_base_content, new PlaylistFragment(true)).commit();
        }
    }

    @Override
    protected int setContentView() {
        return R.layout.activity_base_picker;
    }

}
