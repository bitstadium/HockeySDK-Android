package net.hockeyapp.android;

import net.hockeyapp.android.internal.DownloadFileListener;
import net.hockeyapp.android.internal.DownloadFileTask;
import net.hockeyapp.android.internal.UpdateInfoAdapter;
import net.hockeyapp.android.internal.UpdateView;
import android.app.ListActivity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
 * Copyright (c) 2012 Codenauts UG
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
public class UpdateActivity extends ListActivity implements UpdateActivityInterface, UpdateInfoListener, OnClickListener {
  /**
   * Task to download the .apk file.
   */
  private DownloadFileTask downloadTask;
  
  /**
   * Adapter to provide views and data for the list viw. 
   */
  private UpdateInfoAdapter adapter;
  
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

    setTitle("App Update");
    setContentView(getLayoutView());

    adapter = new UpdateInfoAdapter(this, getIntent().getStringExtra("json"), this);
    getListView().setDivider(null);
    setListAdapter(adapter);
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
    versionLabel.setText("Version " + adapter.getVersionString() + "\n" + adapter.getFileInfoString());
    
    Button updateButton = (Button)findViewById(UpdateView.UPDATE_BUTTON_ID);
    updateButton.setOnClickListener(this);
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
   * Starts the download task and sets the listener for a successful
   * download, a failed download, and configuration strings.
   */
  private void startDownloadTask() {
    final String url = getIntent().getStringExtra("url");
    downloadTask = new DownloadFileTask(this, url, new DownloadFileListener() {
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
   * Called when the download button is tapped. Starts the download task and
   * disables the button to avoid multiple taps.
   */
  public void onClick(View v) {
    startDownloadTask();
    v.setEnabled(false);
  }
}
