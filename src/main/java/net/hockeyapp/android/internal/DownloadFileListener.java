package net.hockeyapp.android.internal;

import net.hockeyapp.android.StringListener;

public abstract class DownloadFileListener extends StringListener {
  public void downloadFailed(DownloadFileTask task, Boolean userWantsRetry) {
  }
  
  public void downloadSuccessful(DownloadFileTask task) {
  }
}
