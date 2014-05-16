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
 * Internal helper class. Downloads an .apk from HockeyApp and stores
 * it on external storage. If the download was successful, the file 
 * is then opened to trigger the installation. 
 * 
 * <h4>License</h4>
 * 
 * <pre>
 * Copyright (c) 2011-2013 Bit Stadium GmbH
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
 * @author Thomas Dohmke
 **/
public class DownloadFileTask extends AsyncTask<Void, Integer, Integer> {
  protected static final int MAX_REDIRECTS = 6;

  protected Context context;
  protected DownloadFileListener notifier;
  protected String urlString;
  protected String filename;
  protected String filePath;
  protected ProgressDialog progressDialog;

  public DownloadFileTask(Context context, String urlString, DownloadFileListener notifier) {
    this.context = context;
    this.urlString = urlString;
    this.filename = UUID.randomUUID() + ".apk";
    this.filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download";
    this.notifier = notifier;
  }

  public void attach(Context context) {
    this.context = context;
  }

  public void detach() {
    context = null;
    progressDialog = null;
  }

  @Override
  protected Integer doInBackground(Void... args) {
    try {
      URL url = new URL(getURLString());
      URLConnection connection = createConnection(url, MAX_REDIRECTS);
      connection.connect();

      int lenghtOfFile = connection.getContentLength();

      File dir = new File(this.filePath);
      boolean result = dir.mkdirs();
      if (!result && !dir.exists()) {
        throw new IOException("Could not create the dir(s):" + dir.getAbsolutePath());
      }
      File file = new File(dir, this.filename);

      InputStream input = new BufferedInputStream(connection.getInputStream());
      OutputStream output = new FileOutputStream(file);

      byte data[] = new byte[1024];
      int count = 0;
      int total = 0;
      while ((count = input.read(data)) != -1) {
        total += count;
        publishProgress(Math.round(total * 100.0f / lenghtOfFile));
        output.write(data, 0, count);
      }

      output.flush();
      output.close();
      input.close();

      return total;
    } 
    catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }

  protected void setConnectionProperties(HttpURLConnection connection) {
    connection.addRequestProperty("User-Agent", "HockeySDK/Android");
    connection.setInstanceFollowRedirects(true);

    // connection bug workaround for SDK<=2.x
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD) {
      connection.setRequestProperty("connection", "close");
    }
  }

  /**
   * Recursive method for resolving redirects. Resolves at most MAX_REDIRECTS times.
   */
  protected URLConnection createConnection(URL url, int remainingRedirects) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    setConnectionProperties(connection);

    int code = connection.getResponseCode();
    if (code == HttpURLConnection.HTTP_MOVED_PERM ||
        code == HttpURLConnection.HTTP_MOVED_TEMP ||
        code == HttpURLConnection.HTTP_SEE_OTHER) {

      if (remainingRedirects == 0) {
        // Stop redirecting.
        return connection;
      }

      URL movedUrl = new URL(connection.getHeaderField("Location"));
      if (!url.getProtocol().equals(movedUrl.getProtocol())) {
        // HttpURLConnection doesn't handle redirects across schemes, so handle it manually, see
        // http://code.google.com/p/android/issues/detail?id=41651
        connection.disconnect();
        return createConnection(movedUrl, --remainingRedirects); // Recursion
      }
    }
    return connection;
  }

  @Override
  protected void onProgressUpdate(Integer... args) {
    try {
      if (progressDialog == null) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
      }
      progressDialog.setProgress(args[0]);
    }
    catch (Exception e) {
      // Ignore all exceptions
    }
  }

  @Override
  protected void onPostExecute(Integer result) {
    if (progressDialog != null) {
      try {
        progressDialog.dismiss();
      }
      catch (Exception e) {
        // Ignore all exceptions
      }
    }

    if (result > 0) {
      notifier.downloadSuccessful(this);

      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setDataAndType(Uri.fromFile(new File(this.filePath, this.filename)), "application/vnd.android.package-archive");
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent);
    }
    else {
      try {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(Strings.get(notifier, Strings.DOWNLOAD_FAILED_DIALOG_TITLE_ID));
        builder.setMessage(Strings.get(notifier, Strings.DOWNLOAD_FAILED_DIALOG_MESSAGE_ID));

        builder.setNegativeButton(Strings.get(notifier, Strings.DOWNLOAD_FAILED_DIALOG_NEGATIVE_BUTTON_ID), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            notifier.downloadFailed(DownloadFileTask.this, false);
          } 
        });

        builder.setPositiveButton(Strings.get(notifier, Strings.DOWNLOAD_FAILED_DIALOG_POSITIVE_BUTTON_ID), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            notifier.downloadFailed(DownloadFileTask.this, true);
          } 
        });

        builder.create().show();
      }
      catch (Exception e) {
        // Ignore all exceptions
      }
    }
  }

  protected String getURLString() {
    return urlString + "&type=apk";      
  }
}
