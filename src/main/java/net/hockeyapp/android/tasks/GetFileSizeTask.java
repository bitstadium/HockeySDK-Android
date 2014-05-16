package net.hockeyapp.android.tasks;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import net.hockeyapp.android.Strings;
import net.hockeyapp.android.listeners.DownloadFileListener;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

/**
 * <h4>Description</h4>
 * 
 * Internal helper class. Determines the size of an externally hosted
 * .apk from the HTTP header.
 *
 * <h4>License</h4>
 * 
 * <pre>
 * Copyright (c) 2014 Bit Stadium GmbH
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
  private int size;

  public GetFileSizeTask(Context context, String urlString, DownloadFileListener notifier) {
    super(context, urlString, notifier);
  }

  @Override
  protected Integer doInBackground(Void... args) {
    try {
      URL url = new URL(getURLString());
      URLConnection connection = createConnection(url, MAX_REDIRECTS);
      return connection.getContentLength();
    }
    catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }

  @Override
  protected void onProgressUpdate(Integer... args) {
    // Do not display any progress for this task.
  }

  @Override
  protected void onPostExecute(Integer result) {
    size = result;
    if (size > 0) {
      notifier.downloadSuccessful(this);
    }
    else {
      notifier.downloadFailed(this, false);
    }
  }

  public int getSize() {
    return size;
  }
}
