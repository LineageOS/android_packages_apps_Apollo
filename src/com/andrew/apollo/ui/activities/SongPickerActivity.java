package com.andrew.apollo.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.andrew.apollo.R;
import com.andrew.apollo.ui.fragments.PlaylistFragment;
import com.andrew.apollo.ui.fragments.phone.MusicBrowserPhoneFragment;
import com.andrew.apollo.utils.NavUtils;

public class SongPickerActivity extends BasePickerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the music browser fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_base_content, new MusicBrowserPhoneFragment(true)).commit();
        }
    }

    @Override
    protected int setContentView() {
        return R.layout.activity_base_picker;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
        case NavUtils.REQUEST_PICKER:
            if (resultCode == Activity.RESULT_OK) {
                setResult(resultCode, data);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
