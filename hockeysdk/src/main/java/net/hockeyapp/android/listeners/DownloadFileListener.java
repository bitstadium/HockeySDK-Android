package net.hockeyapp.android.listeners;

import net.hockeyapp.android.tasks.DownloadFileTask;

/**
 * <h3>Description</h3>
 *
 * Abstract class for callbacks to be invoked from the DownloadFileTask.
 **/
public abstract class DownloadFileListener {
    public void downloadFailed(DownloadFileTask task, Boolean userWantsRetry) {
    }

    public void downloadSuccessful(DownloadFileTask task) {
    }
}
