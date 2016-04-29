package net.hockeyapp.android.tasks;

import android.content.Context;

import net.hockeyapp.android.listeners.DownloadFileListener;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * <h3>Description</h3>
 *
 * Internal helper class. Determines the size of an externally hosted
 * .apk from the HTTP header.
 *
 **/
public class GetFileSizeTask extends DownloadFileTask {
    private long mSize;

    public GetFileSizeTask(Context context, String urlString, DownloadFileListener notifier) {
        super(context, urlString, notifier);
    }

    @Override
    protected Long doInBackground(Void... args) {
        try {
            URL url = new URL(getURLString());
            URLConnection connection = createConnection(url, MAX_REDIRECTS);
            return (long) connection.getContentLength();
        } catch (IOException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... args) {
        // Do not display any progress for this task.
    }

    @Override
    protected void onPostExecute(Long result) {
        mSize = result;
        if (mSize > 0L) {
            mNotifier.downloadSuccessful(this);
        } else {
            mNotifier.downloadFailed(this, false);
        }
    }

    public long getSize() {
        return mSize;
    }
}
