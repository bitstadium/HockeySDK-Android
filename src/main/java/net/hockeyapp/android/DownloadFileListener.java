package net.hockeyapp.android;

public abstract class DownloadFileListener extends StringListener {
  public void downloadFailed(DownloadFileTask task, Boolean userWantsRetry) {
  }
  
  public void downloadSuccessful(DownloadFileTask task) {
  }
}
