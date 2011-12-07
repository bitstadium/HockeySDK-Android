package net.hockeyapp.android;

public interface DownloadFileNotifier {
  public void downloadFailed(DownloadFileTask task, Boolean userWantsRetry);
}
