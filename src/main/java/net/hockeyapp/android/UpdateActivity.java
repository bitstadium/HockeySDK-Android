package net.hockeyapp.android;

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

public class UpdateActivity extends ListActivity implements UpdateActivityInterface, UpdateInfoListener, OnClickListener {
  private DownloadFileTask downloadTask;
  private UpdateInfoAdapter adapter;
  
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
  
  protected void configureView() {
    TextView nameLabel = (TextView)findViewById(UpdateView.NAME_LABEL_ID);
    nameLabel.setText(getAppName());
    
    TextView versionLabel = (TextView)findViewById(UpdateView.VERSION_LABEL_ID);
    versionLabel.setText("Version " + adapter.getVersionString() + "\n" + adapter.getFileInfoString());
    
    Button updateButton = (Button)findViewById(UpdateView.UPDATE_BUTTON_ID);
    updateButton.setOnClickListener(this);
  }

  @Override
  public Object onRetainNonConfigurationInstance() {
    if (downloadTask != null) {
      downloadTask.detach();
    }
    return downloadTask;
  }
  
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
  
  public void enableUpdateButton() {
    View updateButton = findViewById(UpdateView.UPDATE_BUTTON_ID);
    updateButton.setEnabled(true);
  }
  
  public int getCurrentVersionCode() {
    int currentVersionCode = -1;
    
    try {
      currentVersionCode = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_META_DATA).versionCode;
    }
    catch (NameNotFoundException e) {
    }
    
    return currentVersionCode;
  }
  
  public ViewGroup getLayoutView() {
    return new UpdateView(this);
  }

  public CharSequence getAppName() {
    try {
      PackageManager pm = getPackageManager();
      ApplicationInfo applicationInfo = pm.getApplicationInfo(getPackageName(), 0);
      return pm.getApplicationLabel(applicationInfo);
    }
    catch (NameNotFoundException exception) {
      return "";
    }
  }

  public void onClick(View v) {
    startDownloadTask();
    v.setEnabled(false);
  }
}
