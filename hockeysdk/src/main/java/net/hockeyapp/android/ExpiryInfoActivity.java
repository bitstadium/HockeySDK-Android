package net.hockeyapp.android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import net.hockeyapp.android.utils.Util;

/**
 * <h3>Description</h3>
 *
 * The expiry activity is shown when the build is expired.
 *
 **/
public class ExpiryInfoActivity extends Activity {
    /**
     * Called when the activity is starting. Sets the title and content view.
     * Configures the list view adapter. Attaches itself to a previously
     * started download task.
     *
     * @param savedInstanceState Data it most recently supplied in
     *                           onSaveInstanceState(Bundle)
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.hockeyapp_expiry_info_title);
        setContentView(R.layout.hockeyapp_activity_expiry_info);

        String appName = Util.getAppName(this);
        String text = getString(R.string.hockeyapp_expiry_info_text, appName);
        TextView messageView = findViewById(R.id.label_message);
        messageView.setText(text);
    }

}
