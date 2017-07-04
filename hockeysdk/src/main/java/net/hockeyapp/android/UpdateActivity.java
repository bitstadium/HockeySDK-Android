package net.hockeyapp.android;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

/**
 * <h3>Description</h3>
 *
 * Activity to show update information and start the download
 * process if the user taps the corresponding button.
 *
 **/
public class UpdateActivity extends Activity {

    public static final String FRAGMENT_CLASS = "fragmentClass";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                finish();
                return;
            }
            String fragmentClass = extras.getString(FRAGMENT_CLASS, UpdateFragment.class.getName());
            Fragment fragment = Fragment.instantiate(this, fragmentClass, extras);
            getFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, fragment, UpdateFragment.FRAGMENT_TAG)
                    .commit();
        }
    }
}
