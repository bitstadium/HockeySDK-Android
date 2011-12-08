package net.hockeyapp.android;

import android.app.ListActivity;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class UpdateActivity extends ListActivity implements UpdateInfoListener {
  public static int iconDrawableId = -1;

  private DownloadFileTask downloadTask;
  private UpdateInfoAdapter adapter;
  
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setTitle("Application Update");
    setContentView(R.layout.update_view);

    ViewGroup headerView = (ViewGroup)findViewById(R.id.header_view); 
    View view = (View)findViewById(android.R.id.list);
    ViewHelper.moveViewBelowOrBesideHeader(this, view, headerView, 23, false);

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
    if (iconDrawableId != -1) {
      ImageView iconView = (ImageView)findViewById(R.id.icon_view);
      iconView.setImageDrawable(getResources().getDrawable(iconDrawableId));
    }
    
    TextView versionLabel = (TextView)findViewById(R.id.version_label);
    versionLabel.setText("Version " + adapter.getVersionString() + "\n" + adapter.getFileInfoString());
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
  }
  
  private void startDownloadTask() {
    final String url = getIntent().getStringExtra("url");
    downloadTask = new DownloadFileTask(this, url, new DownloadFileListener() {
      @Override
      public void downloadFailed(DownloadFileTask task, Boolean userWantsRetry) {
        startDownloadTask();
      }
    });
    downloadTask.execute();
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
 }
