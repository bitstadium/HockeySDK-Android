package net.hockeyapp.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.hockeyapp.android.listeners.DownloadFileListener;
import net.hockeyapp.android.objects.ErrorObject;
import net.hockeyapp.android.tasks.DownloadFileTask;
import net.hockeyapp.android.tasks.GetFileSizeTask;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.Util;
import net.hockeyapp.android.utils.VersionHelper;

/**
 * <h3>Description</h3>
 *
 * Activity to show update information and start the download
 * process if the user taps the corresponding button.
 *
 * <h3>License</h3>
 *
 * <pre>
 * Copyright (c) 2011-2014 Bit Stadium GmbH
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * </pre>
 *
 * @author Thomas Dohmke
 **/
public class UpdateActivity extends Activity implements UpdateActivityInterface, UpdateInfoListener, OnClickListener {
    /**
     * Parameter to supply the download URL of the update's APK
     */
    public static final String EXTRA_URL = "url";
    /**
     * Parameter to supply metadata about the update in JSON format
     */
    public static final String EXTRA_JSON = "json";
    private static final int DIALOG_ERROR_ID = 0;
    /**
     * Task to download the .apk file.
     */
    protected DownloadFileTask mDownloadTask;
    /**
     * Helper for version management.
     */
    protected VersionHelper mVersionHelper;
    private ErrorObject mError;
    private Context mContext;

    /**
     * Called when the activity is starting. Sets the title and content view.
     * Configures the list view adapter. Attaches itself to a previously
     * started download task.
     *
     * @param savedInstanceState Data it most recently supplied in
     *                           onSaveInstanceState(Bundle)
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("App Update");
        setContentView(getLayoutView());

        mContext = this;
        mVersionHelper = new VersionHelper(this, getIntent().getStringExtra(EXTRA_JSON), this);
        configureView();

        mDownloadTask = (DownloadFileTask) getLastNonConfigurationInstance();
        if (mDownloadTask != null) {
            mDownloadTask.attach(this);
        }
    }

    /**
     * Detaches the activity from the download task and returns the task
     * as last instance. This way the task is restored when the activity
     * is immediately re-created.
     *
     * @return The download task if present.
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        if (mDownloadTask != null) {
            mDownloadTask.detach();
        }
        return mDownloadTask;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return onCreateDialog(id, null);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
            case DIALOG_ERROR_ID:
                return new AlertDialog.Builder(this)
                        .setMessage("An error has occured")
                        .setCancelable(false)
                        .setTitle("Error")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mError = null;
                                dialog.cancel();
                            }
                        }).create();
        }

        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_ERROR_ID:
                AlertDialog messageDialogError = (AlertDialog) dialog;
                if (mError != null) {
                    /** If the ErrorObject is not null, display the ErrorObject message */
                    messageDialogError.setMessage(mError.getMessage());
                } else {
                    /** If the ErrorObject is null, display the general error message */
                    messageDialogError.setMessage("An unknown error has occured.");
                }

                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        enableUpdateButton();

        if (permissions.length == 0 || grantResults.length == 0) {
            // User cancelled permissions dialog -> don't do anything.
            return;
        }

        if (requestCode == Constants.UPDATE_PERMISSIONS_REQUEST) {
            // Check for the grant result on write permission
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, re-invoke download process
                prepareDownload();
            } else {
                // Permission denied, show user alert
                HockeyLog.warn("User denied write permission, can't continue with updater task.");

                UpdateManagerListener listener = UpdateManager.getLastListener();
                if (listener != null) {
                    listener.onUpdatePermissionsNotGranted();
                } else {
                    final UpdateActivity updateActivity = this;
                    new AlertDialog.Builder(mContext)
                            .setTitle(getString(R.string.hockeyapp_permission_update_title))
                            .setMessage(getString(R.string.hockeyapp_permission_update_message))
                            .setNegativeButton(getString(R.string.hockeyapp_permission_dialog_negative_button), null)
                            .setPositiveButton(getString(R.string.hockeyapp_permission_dialog_positive_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    updateActivity.prepareDownload();
                                }
                            })
                            .create()
                            .show();
                }
            }
        }
    }

    /**
     * Returns the current version of the app.
     *
     * @return The version code as integer.
     */
    public int getCurrentVersionCode() {
        int currentVersionCode = -1;

        try {
            currentVersionCode = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_META_DATA).versionCode;
        } catch (NameNotFoundException e) {
        }

        return currentVersionCode;
    }

    /**
     * Creates and returns a new instance of the update view.
     *
     * @return Update view
     */
    public ViewGroup getLayoutView() {
        LinearLayout layout = new LinearLayout(this);
        LayoutInflater.from(this).inflate(R.layout.hockeyapp_activity_update, layout);
        return layout;
    }

    /**
     * Called when the download button is tapped. Starts the download task and
     * disables the button to avoid multiple taps.
     */
    public void onClick(View v) {
        prepareDownload();
        v.setEnabled(false);
    }

    /**
     * Configures the content view by setting app name, the current version
     * and the listener for the download button.
     */
    protected void configureView() {
        TextView nameLabel = (TextView) findViewById(R.id.label_title);
        nameLabel.setText(getAppName());

        final TextView versionLabel = (TextView) findViewById(R.id.label_version);
        final String versionString = "Version " + mVersionHelper.getVersionString();
        final String fileDate = mVersionHelper.getFileDateString();

        String appSizeString = "Unknown size";
        long appSize = mVersionHelper.getFileSizeBytes();
        if (appSize >= 0L) {
            appSizeString = String.format("%.2f", appSize / (1024.0f * 1024.0f)) + " MB";
        } else {
            GetFileSizeTask task = new GetFileSizeTask(this, getIntent().getStringExtra(EXTRA_URL), new DownloadFileListener() {
                @Override
                public void downloadSuccessful(DownloadFileTask task) {
                    if (task instanceof GetFileSizeTask) {
                        long appSize = ((GetFileSizeTask) task).getSize();
                        String appSizeString = String.format("%.2f", appSize / (1024.0f * 1024.0f)) + " MB";
                        versionLabel.setText(versionString + "\n" + fileDate + " - " + appSizeString);
                    }
                }
            });
            AsyncTaskUtils.execute(task);
        }
        versionLabel.setText(versionString + "\n" + fileDate + " - " + appSizeString);

        Button updateButton = (Button) findViewById(R.id.button_update);
        updateButton.setOnClickListener(this);

        WebView webView = (WebView) findViewById(R.id.web_update_details);
        webView.clearCache(true);
        webView.destroyDrawingCache();
        webView.loadDataWithBaseURL(Constants.BASE_URL, getReleaseNotes(), "text/html", "utf-8", null);
    }

    /**
     * Returns the release notes as HTML.
     *
     * @return String with release notes.
     */
    protected String getReleaseNotes() {
        return mVersionHelper.getReleaseNotes(false);
    }

    /**
     * Starts the download task for the app and sets the listener
     * for a successful download, a failed download, and configuration
     * strings.
     */
    protected void startDownloadTask() {
        String url = getIntent().getStringExtra("url");
        startDownloadTask(url);
    }

    /**
     * Starts the download task and sets the listener for a successful
     * download, a failed download, and configuration strings.
     *
     * @param url URL of file that should be downloaded
     */
    protected void startDownloadTask(String url) {
        createDownloadTask(url, new DownloadFileListener() {
            public void downloadFailed(DownloadFileTask task, Boolean userWantsRetry) {
                if (userWantsRetry) {
                    startDownloadTask();
                } else {
                    enableUpdateButton();
                }
            }

            public void downloadSuccessful(DownloadFileTask task) {
                enableUpdateButton();
            }
        });
        AsyncTaskUtils.execute(mDownloadTask);
    }

    protected void createDownloadTask(String url, DownloadFileListener listener) {
        mDownloadTask = new DownloadFileTask(this, url, listener);
    }

    /**
     * Enables the download button.
     */
    public void enableUpdateButton() {
        View updateButton = findViewById(R.id.button_update);
        updateButton.setEnabled(true);
    }

    /**
     * Returns the app's name.
     *
     * @return The app's name as a String.
     */
    public String getAppName() {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo(getPackageName(), 0);
            return pm.getApplicationLabel(applicationInfo).toString();
        } catch (NameNotFoundException exception) {
            return "";
        }
    }

    /**
     * Checks if WRITE_EXTERNAL_STORAGE permission was added to the {@link Manifest} file
     *
     * @param context
     * @return
     */
    private boolean isWriteExternalStorageSet(Context context) {
        String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
        int res = context.checkCallingOrSelfPermission(permission);

        return (res == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Checks if Unknown Sources is checked from {@link Settings}
     *
     * @return
     */
    @SuppressLint("InlinedApi")
    @SuppressWarnings("deprecation")
    private boolean isUnknownSourcesChecked() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return (Settings.Global.getInt(getContentResolver(), Settings.Global.INSTALL_NON_MARKET_APPS) == 1);
            } else {
                return (Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) == 1);
            }
        } catch (Settings.SettingNotFoundException e) {
            return true;
        }
    }

    protected void prepareDownload() {
        if (!Util.isConnectedToNetwork(mContext)) {
            mError = new ErrorObject();
            mError.setMessage(getString(R.string.hockeyapp_error_no_network_message));

            runOnUiThread(new Runnable() {
                @SuppressWarnings("deprecation")
                public void run() {
                    showDialog(DIALOG_ERROR_ID);
                }
            });

            return;
        }
        if (!isWriteExternalStorageSet(mContext)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Only if we're running on Android M or later we can request permissions at runtime
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.UPDATE_PERMISSIONS_REQUEST);
                return;
            }

            mError = new ErrorObject();
            mError.setMessage("The permission to access the external storage permission is not set. Please contact the developer.");

            runOnUiThread(new Runnable() {
                @SuppressWarnings("deprecation")
                @Override
                public void run() {
                    showDialog(DIALOG_ERROR_ID);
                }
            });

            return;
        }

        if (!isUnknownSourcesChecked()) {
            mError = new ErrorObject();
            mError.setMessage("The installation from unknown sources is not enabled. Please check the device settings.");

            runOnUiThread(new Runnable() {
                @SuppressWarnings("deprecation")
                @Override
                public void run() {
                    showDialog(DIALOG_ERROR_ID);
                }
            });

            return;
        }

        startDownloadTask();
    }
}
