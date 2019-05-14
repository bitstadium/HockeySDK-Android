package net.hockeyapp.android.tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import net.hockeyapp.android.Constants;
import net.hockeyapp.android.objects.FeedbackAttachment;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.ImageUtils;
import net.hockeyapp.android.views.AttachmentView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * <h3>Description</h3>
 *
 * Singleton class to queue attachment downloads.
 *
 */
public class AttachmentDownloader {

    /**
     * AttachmentDownloaderHolder is loaded on the first execution of AttachmentDownloader.getInstance()
     * or the first access to FeedbackParserHolder.INSTANCE, not before.
     */
    private static class AttachmentDownloaderHolder {
        static final AttachmentDownloader INSTANCE = new AttachmentDownloader();
    }

    @SuppressWarnings("SameReturnValue")
    public static AttachmentDownloader getInstance() {
        return AttachmentDownloaderHolder.INSTANCE;
    }

    private Queue<DownloadJob> queue;
    private boolean downloadRunning;
    private final Handler downloadHandler = new DownloadHandler(this);

    private AttachmentDownloader() {
        this.queue = new LinkedList<>();
        this.downloadRunning = false;
    }

    public void download(FeedbackAttachment feedbackAttachment, AttachmentView attachmentView) {
        queue.add(new DownloadJob(feedbackAttachment, attachmentView));
        downloadNext();
    }

    private void downloadNext() {
        if (downloadRunning) {
            return;
        }

        DownloadJob downloadJob = queue.peek();
        if (downloadJob != null) {
            downloadRunning = true;
            AsyncTaskUtils.execute(new DownloadTask(downloadJob, downloadHandler));
        }
    }

    private static class DownloadHandler extends Handler
    {
        private final AttachmentDownloader downloader;

        DownloadHandler(AttachmentDownloader downloader) {
            this.downloader = downloader;
        }

        @Override
        public void handleMessage(Message msg) {
            final DownloadJob retryCandidate = downloader.queue.poll();
            if (!retryCandidate.isSuccess() && retryCandidate.consumeRetry()) {
                this.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        downloader.queue.add(retryCandidate);
                        downloader.downloadNext();
                    }
                }, 3000);
            }
            downloader.downloadRunning = false;
            downloader.downloadNext();
        }
    }

    /**
     * Holds everything needed for a download process.
     */
    private static class DownloadJob {

        private final FeedbackAttachment feedbackAttachment;
        private final AttachmentView attachmentView;
        private boolean success;
        private int remainingRetries;

        private DownloadJob(FeedbackAttachment feedbackAttachment, AttachmentView attachmentView) {
            this.feedbackAttachment = feedbackAttachment;
            this.attachmentView = attachmentView;
            this.success = false;
            this.remainingRetries = 2;
        }

        FeedbackAttachment getFeedbackAttachment() {
            return feedbackAttachment;
        }

        AttachmentView getAttachmentView() {
            return attachmentView;
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        boolean isSuccess() {
            return success;
        }

        void setSuccess(boolean success) {
            this.success = success;
        }

        boolean hasRetry() {
            return remainingRetries > 0;
        }

        boolean consumeRetry() {
            return --remainingRetries >= 0;
        }
    }

    /**
     * The AsyncTask that downloads the image and the updates the view.
     */
    @SuppressLint("StaticFieldLeak")
    private static class DownloadTask extends AsyncTask<Void, Integer, Boolean> {

        private final DownloadJob downloadJob;
        private final Handler handler;
        private final Context context;
        private Bitmap bitmap;
        private int bitmapOrientation;

        DownloadTask(DownloadJob downloadJob, Handler handler) {
            this.downloadJob = downloadJob;
            this.handler = handler;
            this.context = downloadJob.getAttachmentView().getContext();
            this.bitmap = null;
            this.bitmapOrientation = ImageUtils.ORIENTATION_PORTRAIT; // default
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... args) {
            FeedbackAttachment attachment = downloadJob.getFeedbackAttachment();
            File file = new File(Constants.getHockeyAppStorageDir(context), attachment.getCacheId());

            if (file.exists()) {
                HockeyLog.error("Cached...");
                loadImageThumbnail(file);
                return true;

            } else {
                HockeyLog.error("Downloading...");
                boolean success = downloadAttachment(attachment.getUrl(), file);
                if (success) {
                    loadImageThumbnail(file);
                }
                return success;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }

        @Override
        protected void onPostExecute(Boolean success) {
            AttachmentView attachmentView = downloadJob.getAttachmentView();
            downloadJob.setSuccess(success);

            if (success) {
                attachmentView.setImage(bitmap, bitmapOrientation);

            } else {
                if (!downloadJob.hasRetry()) {
                    attachmentView.signalImageLoadingError();
                }
            }

            handler.sendEmptyMessage(0);
        }

        private void loadImageThumbnail(File file) {
            try {
                AttachmentView attachmentView = downloadJob.getAttachmentView();
                bitmapOrientation = ImageUtils.determineOrientation(file);
                int width = bitmapOrientation == ImageUtils.ORIENTATION_LANDSCAPE ?
                        attachmentView.getWidthLandscape() : attachmentView.getWidthPortrait();
                int height = bitmapOrientation == ImageUtils.ORIENTATION_LANDSCAPE ?
                        attachmentView.getMaxHeightLandscape() : attachmentView.getMaxHeightPortrait();

                bitmap = ImageUtils.decodeSampledBitmap(file, width, height);

            } catch (IOException e) {
                HockeyLog.error("Failed to load image thumbnail", e);
                bitmap = null;
            }
        }

        private boolean downloadAttachment(String url, File file) {
            InputStream input = null;
            OutputStream output = null;
            HttpsURLConnection connection = null;
            try {
                connection = (HttpsURLConnection) createConnection(new URL(url));
                TrafficStats.setThreadStatsTag(Constants.THREAD_STATS_TAG);
                connection.connect();

                int lengthOfFile = connection.getContentLength();
                String status = connection.getHeaderField("Status");

                if (status != null) {
                    if (!status.startsWith("200")) {
                        return false;
                    }
                }

                input = new BufferedInputStream(connection.getInputStream());
                output = new FileOutputStream(file);

                byte data[] = new byte[1024];
                int count;
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress((int) (total * 100 / lengthOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                return (total > 0);

            } catch (IOException e) {
                HockeyLog.error("Failed to download attachment to " + file, e);
                return false;
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
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        private URLConnection createConnection(URL url) throws IOException {
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", Constants.SDK_USER_AGENT);
            connection.setInstanceFollowRedirects(true);
            return connection;
        }
    }
}
