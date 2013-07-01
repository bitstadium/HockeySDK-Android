package net.hockeyapp.android;

import net.hockeyapp.android.listeners.DownloadFileListener;
import net.hockeyapp.android.objects.ErrorObject;
import net.hockeyapp.android.tasks.DownloadFileTask;
import net.hockeyapp.android.utils.VersionHelper;
import net.hockeyapp.android.views.UpdateView;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

/**
 * <h4>Description</h4>
 * 
 * Activity to show update information and start the download
 * process if the user taps the corresponding button.
 * 
 * <h4>License</h4>
 * 
 * <pre>
 * Copyright (c) 2011-2013 Bit Stadium GmbH
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
  private final int DIALOG_ERROR_ID = 0;
  private ErrorObject error;
  private Context context;
  
  /**
   * Task to download the .apk file.
   */
  protected DownloadFileTask downloadTask;
  
  /**
   * Helper for version management.
   */
  protected VersionHelper versionHelper;
  
  /**
   * Called when the activity is starting. Sets the title and content view.
   * Configures the list view adapter. Attaches itself to a previously 
   * started download task.
   * 
   * @param savedInstanceState Data it most recently supplied in 
   *                           onSaveInstanceState(Bundle)
   */
  @SuppressWarnings("deprecation")
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setTitle("App Update");
    setContentView(getLayoutView());

    context = this;
    versionHelper = new VersionHelper(getIntent().getStringExtra("json"), this);
    configureView();
    
    downloadTask = (DownloadFileTask)getLastNonConfigurationInstance();
    if (downloadTask != null) {
      downloadTask.attach(this);
    }
  }
  
  /**
   * Configures the content view by setting app name, the current version
   * and the listener for the download button. 
   */
  protected void configureView() {
    TextView nameLabel = (TextView)findViewById(UpdateView.NAME_LABEL_ID);
    nameLabel.setText(getAppName());
    
    TextView versionLabel = (TextView)findViewById(UpdateView.VERSION_LABEL_ID);
    versionLabel.setText("Version " + versionHelper.getVersionString() + "\n" + versionHelper.getFileInfoString());
    
    Button updateButton = (Button)findViewById(UpdateView.UPDATE_BUTTON_ID);
    updateButton.setOnClickListener(this);
    
    WebView webView = (WebView)findViewById(UpdateView.WEB_VIEW_ID);
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
    return versionHelper.getReleaseNotes(false);
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
    if (downloadTask != null) {
      downloadTask.detach();
    }
    return downloadTask;
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
   */
  protected void startDownloadTask(String url) {
    createDownloadTask(url, new DownloadFileListener() {
      public void downloadSuccessful(DownloadFileTask task) {
        enableUpdateButton();
      }
      
      public void downloadFailed(DownloadFileTask task, Boolean userWantsRetry) {
        if (userWantsRetry) {
          startDownloadTask();
        }
        else {
          enableUpdateButton();
        }
      }
      
      public String getStringForResource(int resourceID) {
        UpdateManagerListener listener = UpdateManager.getLastListener();
        if (listener != null) {
          return listener.getStringForResource(resourceID);
        }
        else {
          return null;
        }
      }
    });
    downloadTask.execute();
  }
  
  protected void createDownloadTask(String url, DownloadFileListener listener) {
    downloadTask = new DownloadFileTask(this, url, listener);
  }
  
  /**
   * Enables the download button.
   */
  public void enableUpdateButton() {
    View updateButton = findViewById(UpdateView.UPDATE_BUTTON_ID);
    updateButton.setEnabled(true);
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
    }
    catch (NameNotFoundException e) {
    }
    
    return currentVersionCode;
  }
  
  /**
   * Creates and returns a new instance of UpdateView.
   * 
   * @return Instance of UpdateView
   */
  public ViewGroup getLayoutView() {
    return new UpdateView(this);
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
    }
    catch (NameNotFoundException exception) {
      return "";
    }
  }

  /**
   * Checks if WRITE_EXTERNAL_STORAGE permission was added to the {@link Manifest} file
   * @param context
   * @return
   */
  private boolean isWriteExternalStorageSet(Context context) {
    String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
    int res = context.checkCallingOrSelfPermission(permission);
    
    return res == PackageManager.PERMISSION_GRANTED;
  }
  
  /**
   * Checks if Unknown Sources is checked from {@link Settings}
   * @return
   */
  private boolean isUnknownSourcesChecked() {
    String[] projection = new String[] {Settings.System.VALUE};
    String selection = Settings.Secure.NAME + " = ? AND " + Settings.Secure.VALUE + " = ?";

    Cursor query = null;
    if (android.os.Build.VERSION.SDK_INT >= 17) { 
      String[] selectionArgs = {Settings.Global.INSTALL_NON_MARKET_APPS, String.valueOf(1)};
      query = getContentResolver().query(Settings.Global.CONTENT_URI, projection, selection, selectionArgs, null);
    }
    else {
      @SuppressWarnings("deprecation")
      String[] selectionArgs = {Settings.Secure.INSTALL_NON_MARKET_APPS, String.valueOf(1)};
      query = getContentResolver().query(Settings.Secure.CONTENT_URI, projection, selection, selectionArgs, null);
    }
    if (query.getCount() == 1) {
      return true;
    }
    
    return false;
  }

  /**
   * Called when the download button is tapped. Starts the download task and
   * disables the button to avoid multiple taps.
   */
  public void onClick(View v) {
    if (!isWriteExternalStorageSet(context)) {
      error = new ErrorObject();
      error.setMessage("The permission to access the external storage permission is not set. Please contact the developer.");
      
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
      error = new ErrorObject();
      error.setMessage("The installation from unknown sources is not enabled. Please check the device settings.");
      
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
    v.setEnabled(false);
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    switch(id) {
      case DIALOG_ERROR_ID:
        return new AlertDialog.Builder(this)
          .setMessage("An error has occured")
          .setCancelable(false)
          .setTitle("Error")
          .setIcon(android.R.drawable.ic_dialog_alert)
          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              error = null;
              dialog.cancel();
            }
          }).create();
    }
  
    return null;
  }

  @Override
  protected void onPrepareDialog(int id, Dialog dialog) {
    switch(id) {
      case DIALOG_ERROR_ID:
        AlertDialog messageDialogError = (AlertDialog) dialog;
        if (error != null) {
          /** If the ErrorObject is not null, display the ErrorObject message */
          messageDialogError.setMessage(error.getMessage());
        } 
        else {
          /** If the ErrorObject is null, display the general error message */
          messageDialogError.setMessage("An unknown error has occured.");
        }
  
        break;
    }
  }
}
