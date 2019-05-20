package net.hockeyapp.android.tasks;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;

import net.hockeyapp.android.Constants;
import net.hockeyapp.android.R;
import net.hockeyapp.android.listeners.DownloadFileListener;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.Util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

/**
 * <h3>Description</h3>
 *
 * Internal helper class. Downloads an .apk from HockeyApp and stores
 * it on external storage. If the download was successful, the file
 * is then opened to trigger the installation.
 **/
@SuppressLint("StaticFieldLeak")
public class DownloadFileTask extends AsyncTask<Void, Integer, Long> {
    protected static final int MAX_REDIRECTS = 6;
    protected static final int TIMEOUT = 60000;

    protected Context mContext;
    protected DownloadFileListener mNotifier;
    protected String mUrlString;
    protected String mFilename;
    protected File mDirectory;
    protected ProgressDialog mProgressDialog;
    private String mDownloadErrorMessage;

    public DownloadFileTask(Context context, String urlString, DownloadFileListener notifier) {
        this.mContext = context;
        this.mUrlString = urlString;
        this.mFilename = UUID.randomUUID() + ".apk";
        this.mDirectory = new File(context.getExternalFilesDir(null), "Download");
        this.mNotifier = notifier;
        this.mDownloadErrorMessage = null;
    }

    public void attach(Context context) {
        this.mContext = context;
    }

    public void detach() {
        mContext = null;
        mProgressDialog = null;
    }

    @Override
    protected Long doInBackground(Void... args) {
        InputStream input = null;
        OutputStream output = null;

        try {
            URL url = new URL(getURLString());
            TrafficStats.setThreadStatsTag(Constants.THREAD_STATS_TAG);
            URLConnection connection = createConnection(url, MAX_REDIRECTS);
            connection.connect();

            int lengthOfFile = connection.getContentLength();
            String contentType = connection.getContentType();

            if (contentType != null && contentType.contains("text")) {
                // This is not the expected APK file. Maybe the redirect could not be resolved.
                mDownloadErrorMessage = "The requested download does not appear to be a file.";
                return 0L;
            }

            boolean result = mDirectory.mkdirs();
            if (!result && !mDirectory.exists()) {
                throw new IOException("Could not create the dir(s):" + mDirectory.getAbsolutePath());
            }
            File file = new File(mDirectory, this.mFilename);

            input = new BufferedInputStream(connection.getInputStream());
            output = new FileOutputStream(file);

            byte data[] = new byte[1024];
            int count;
            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress(Math.round(total * 100.0f / lengthOfFile));
                output.write(data, 0, count);
            }

            output.flush();

            return total;
        } catch (IOException e) {
            HockeyLog.error("Failed to download " + mUrlString, e);
            return 0L;
        } finally {
            TrafficStats.clearThreadStatsTag();
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    protected void setConnectionProperties(HttpsURLConnection connection) {
        connection.addRequestProperty("User-Agent", Constants.SDK_USER_AGENT);
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
    }

    /**
     * Recursive method for resolving redirects. Resolves at most MAX_REDIRECTS times.
     *
     * @param url                a URL
     * @param remainingRedirects loop counter
     * @return instance of URLConnection
     * @throws IOException if connection fails
     */
    protected URLConnection createConnection(URL url, int remainingRedirects) throws IOException {
        HttpsURLConnection connection = Util.openHttpsConnection(url);
        setConnectionProperties(connection);

        int code = connection.getResponseCode();
        if (code == HttpsURLConnection.HTTP_MOVED_PERM ||
                code == HttpsURLConnection.HTTP_MOVED_TEMP ||
                code == HttpsURLConnection.HTTP_SEE_OTHER) {

            if (remainingRedirects == 0) {
                // Stop redirecting.
                return connection;
            }

            URL movedUrl = new URL(connection.getHeaderField("Location"));
            if (!url.getProtocol().equals(movedUrl.getProtocol())) {
                // HttpsURLConnection doesn't handle redirects across schemes, so handle it manually, see
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
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(mContext);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setMessage(mContext.getString(R.string.hockeyapp_update_loading));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            mProgressDialog.setProgress(args[0]);
        } catch (Exception e) {
            // Ignore all exceptions
        }
    }

    @Override
    protected void onPostExecute(Long result) {
        if (mProgressDialog != null) {
            try {
                mProgressDialog.dismiss();
            } catch (Exception e) {
                // Ignore all exceptions
            }
        }

        if (result > 0L) {
            mNotifier.downloadSuccessful(this);
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setDataAndType(Uri.fromFile(new File(this.mDirectory, this.mFilename)),
                    "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            StrictMode.VmPolicy oldVmPolicy = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                oldVmPolicy = StrictMode.getVmPolicy();

                StrictMode.VmPolicy policy = new StrictMode.VmPolicy.Builder()
                        .penaltyLog()
                        .build();

                StrictMode.setVmPolicy(policy);
            }

            mContext.startActivity(intent);

            if (oldVmPolicy != null) {
                StrictMode.setVmPolicy(oldVmPolicy);
            }

        } else {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.hockeyapp_download_failed_dialog_title);

                String message;
                if (mDownloadErrorMessage == null) {
                    message = mContext.getString(R.string.hockeyapp_download_failed_dialog_message);
                } else {
                    message = mDownloadErrorMessage;
                }
                builder.setMessage(message);

                builder.setNegativeButton(R.string.hockeyapp_download_failed_dialog_negative_button, new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mNotifier.downloadFailed(DownloadFileTask.this, false);
                            }
                        });

                builder.setPositiveButton(R.string.hockeyapp_download_failed_dialog_positive_button, new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mNotifier.downloadFailed(DownloadFileTask.this, true);
                            }
                        });

                builder.create().show();
            } catch (Exception e) {
                // Ignore all exceptions
            }
        }
    }

    protected String getURLString() {
        return mUrlString + "&type=apk";
    }
}
