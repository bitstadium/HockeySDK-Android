package net.hockeyapp.android.tasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * <h3>Description</h3>
 *
 * Singleton class to queue attachment downloads.
 *
 * <h3>License</h3>
 *
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
 * @author Patrick Eschenbach
 */
public class AttachmentDownloader {

    /**
     * AttachmentDownloaderHolder is loaded on the first execution of AttachmentDownloader.getInstance()
     * or the first access to FeedbackParserHolder.INSTANCE, not before.
     */
    private static class AttachmentDownloaderHolder {
        public static final AttachmentDownloader INSTANCE = new AttachmentDownloader();
    }

    public static AttachmentDownloader getInstance() {
        return AttachmentDownloaderHolder.INSTANCE;
    }

    private Queue<DownloadJob> queue;

    private boolean downloadRunning;

    private AttachmentDownloader() {
        this.queue = new LinkedList<DownloadJob>();
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
            DownloadTask downloadTask = new DownloadTask(downloadJob, new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    final DownloadJob retryCandidate = queue.poll();
                    if (!retryCandidate.isSuccess() && retryCandidate.consumeRetry()) {
                        this.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                queue.add(retryCandidate);
                                downloadNext();
                            }
                        }, 3000);
                    }
                    downloadRunning = false;
                    downloadNext();
                }
            });
            downloadRunning = true;
            AsyncTaskUtils.execute(downloadTask);
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

        public FeedbackAttachment getFeedbackAttachment() {
            return feedbackAttachment;
        }

        public AttachmentView getAttachmentView() {
            return attachmentView;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public boolean hasRetry() {
            return remainingRetries > 0;
        }

        public boolean consumeRetry() {
            return --remainingRetries < 0 ? false : true;
        }
    }

    /**
     * The AsyncTask that downloads the image and the updates the view.
     */
    private static class DownloadTask extends AsyncTask<Void, Integer, Boolean> {

        private final DownloadJob downloadJob;

        private final Handler handler;

        private File dropFolder;

        private Bitmap bitmap;

        private int bitmapOrientation;

        public DownloadTask(DownloadJob downloadJob, Handler handler) {
            this.downloadJob = downloadJob;
            this.handler = handler;
            this.dropFolder = Constants.getHockeyAppStorageDir();
            this.bitmap = null;
            this.bitmapOrientation = ImageUtils.ORIENTATION_PORTRAIT; // default
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... args) {
            FeedbackAttachment attachment = downloadJob.getFeedbackAttachment();

            if (attachment.isAvailableInCache()) {
                Log.e(HockeyLog.TAG, "Cached...");
                loadImageThumbnail();
                return true;

            } else {
                Log.e(HockeyLog.TAG, "Downloading...");
                boolean success = downloadAttachment(attachment.getUrl(), attachment.getCacheId());
                if (success) {
                    loadImageThumbnail();
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

        private void loadImageThumbnail() {
            try {
                String filename = downloadJob.getFeedbackAttachment().getCacheId();
                AttachmentView attachmentView = downloadJob.getAttachmentView();

                bitmapOrientation = ImageUtils.determineOrientation(new File(dropFolder, filename));
                int width = bitmapOrientation == ImageUtils.ORIENTATION_LANDSCAPE ?
                        attachmentView.getWidthLandscape() : attachmentView.getWidthPortrait();
                int height = bitmapOrientation == ImageUtils.ORIENTATION_LANDSCAPE ?
                        attachmentView.getMaxHeightLandscape() : attachmentView.getMaxHeightPortrait();

                bitmap = ImageUtils.decodeSampledBitmap(new File(dropFolder, filename), width, height);

            } catch (IOException e) {
                e.printStackTrace();
                bitmap = null;
            }
        }

        private boolean downloadAttachment(String urlString, String filename) {
            try {
                URL url = new URL(urlString);
                URLConnection connection = createConnection(url);
                connection.connect();

                int lengthOfFile = connection.getContentLength();
                String status = connection.getHeaderField("Status");

                if (status != null) {
                    if (!status.startsWith("200")) {
                        return false;
                    }
                }

                File file = new File(dropFolder, filename);
                InputStream input = new BufferedInputStream(connection.getInputStream());
                OutputStream output = new FileOutputStream(file);

                byte data[] = new byte[1024];
                int count = 0;
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress((int) (total * 100 / lengthOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
                return (total > 0);

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        private URLConnection createConnection(URL url) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", "HockeySDK/Android");
            connection.setInstanceFollowRedirects(true);
      /* connection bug workaround for SDK<=2.x */
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD) {
                connection.setRequestProperty("connection", "close");
            }
            return connection;
        }
    }
}
