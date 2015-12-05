package net.hockeyapp.android.tasks;

import android.content.Context;

import net.hockeyapp.android.listeners.DownloadFileListener;

import java.net.URL;
import java.net.URLConnection;

/**
 * <h3>Description</h3>
 * <p/>
 * Internal helper class. Determines the size of an externally hosted
 * .apk from the HTTP header.
 * <p/>
 * <h3>License</h3>
 * <p/>
 * <pre>
 * Copyright (c) 2011-2014 Bit Stadium GmbH
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
 * @author Sebastian Schuberth
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
        } catch (Exception e) {
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
