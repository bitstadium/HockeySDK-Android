package net.hockeyapp.android.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.*;
import android.util.Log;
import net.hockeyapp.android.Constants;
import net.hockeyapp.android.objects.FeedbackAttachment;
import net.hockeyapp.android.views.AttachmentView;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Singleton class to queue attachment downloads.
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

  public static File getAttachmentStorageDir() {
    File externalStorage = Environment.getExternalStorageDirectory();

    File targetDir = new File(externalStorage.getAbsolutePath() + "/" + Constants.TAG);
    targetDir.mkdirs();
    return targetDir;
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
          queue.poll();
          downloadRunning = false;
          downloadNext();
        }
      });
      downloadRunning = true;
      downloadTask.execute();
    }
  }

  /**
   * Holds everything needed for a download process.
   */
  private static class DownloadJob {

    private final FeedbackAttachment feedbackAttachment;
    private final AttachmentView attachmentView;

    private DownloadJob(FeedbackAttachment feedbackAttachment, AttachmentView attachmentView) {
      this.feedbackAttachment = feedbackAttachment;
      this.attachmentView = attachmentView;
    }

    public FeedbackAttachment getFeedbackAttachment() {
      return feedbackAttachment;
    }

    public AttachmentView getAttachmentView() {
      return attachmentView;
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

    public DownloadTask(DownloadJob downloadJob, Handler handler) {
      this.downloadJob = downloadJob;
      this.handler = handler;
      this.dropFolder = AttachmentDownloader.getAttachmentStorageDir();
      this.bitmap = null;
    }

    @Override
    protected void onPreExecute() {
      // Todo make loading downloadJob.getAttachmentView()
    }

    @Override
    protected Boolean doInBackground(Void... args) {
      FeedbackAttachment attachment = downloadJob.getFeedbackAttachment();
      Log.e(Constants.TAG, "Loading " + attachment.getCacheId());

      if (attachment.isAvailableInCache()) {
        Log.e(Constants.TAG, "Cached...");
        loadImageThumbnail();
        return true;

      } else {
        Log.e(Constants.TAG, "Downloading...");
        boolean success = downloadAttachment(attachment.getUrl(), attachment.getCacheId());
        if (success) {
          loadImageThumbnail();
        }
        return success;
      }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
      // TODO update progress
    }

    @Override
    protected void onPostExecute(Boolean success) {
      AttachmentView attachmentView = downloadJob.getAttachmentView();
      // TODO check if correct instance and valid

      if (success) {
        attachmentView.setImage(bitmap);

      } else {
        attachmentView.signalImageLoadingError();
      }

      handler.sendEmptyMessage(0);
    }

    private void loadImageThumbnail() {
      try {
        /* Create image thumbnail */
        AttachmentView attachmentView = downloadJob.getAttachmentView();
        int width = attachmentView.getThumbnailWidth();
        int height = attachmentView.getThumbnailHeight();

        String filename = downloadJob.getFeedbackAttachment().getCacheId();

        Bitmap temp = BitmapFactory.decodeStream(new FileInputStream(new File(dropFolder, filename)));
        if (temp != null) {
          bitmap = Bitmap.createScaledBitmap(temp, width, height, false);
        } else {
          Log.e(Constants.TAG, "Could not load thumbnail. Is null.");
        }

      } catch(FileNotFoundException e) {
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

        File file = new File(dropFolder, filename);
        InputStream input = new BufferedInputStream(connection.getInputStream());
        OutputStream output = new FileOutputStream(file);

        byte data[] = new byte[1024];
        int count = 0;
        long total = 0;
        while ((count = input.read(data)) != -1) {
          total += count;
          publishProgress((int)(total * 100 / lengthOfFile));
          output.write(data, 0, count);
        }

        output.flush();
        output.close();
        input.close();
        return (total > 0);

      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }
    }

    private URLConnection createConnection(URL url) throws IOException {
      HttpURLConnection connection = (HttpURLConnection)url.openConnection();
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
