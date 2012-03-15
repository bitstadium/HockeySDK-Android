package net.hockeyapp.android;

import android.app.ListActivity;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class UpdateActivity extends ListActivity implements UpdateActivityInterface, UpdateInfoListener {
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
    nameLabel.setText("APP NAME"); // TODO
    
    //TextView versionLabel = (TextView)findViewById(R.id.version_label);
    //versionLabel.setText("Version " + adapter.getVersionString() + "\n" + adapter.getFileInfoString());
  }

  @Override
  public Object onRetainNonConfigurationInstance() {
    if (downloadTask != null) {
      downloadTask.detach();
    }
    return downloadTask;
  }
  
  public void onClickUpdate(View v) {
    startDownloadTask();
    v.setEnabled(false);
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
    });
    downloadTask.execute();
  }
  
  public void enableUpdateButton() {
    View updateButton = findViewById(R.id.update_button);
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
  
  public int getLayout() {
    return R.layout.update_view;
  }

  public ViewGroup getLayoutView() {
    return new UpdateView(this);
  }
}
