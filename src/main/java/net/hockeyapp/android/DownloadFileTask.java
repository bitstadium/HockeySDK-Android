package net.hockeyapp.android;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

public class DownloadFileTask extends AsyncTask<String, Integer, Boolean>{
  private Context context;
  private DownloadFileListener notifier;
  private String urlString;
  private String filename;
  private String filePath;
  private ProgressDialog progressDialog;

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
  protected Boolean doInBackground(String... args) {
    try {
      URL url = new URL(getURLString());
      URLConnection connection = url.openConnection();
      connection.setRequestProperty("connection", "close");
      connection.connect();

      int lenghtOfFile = connection.getContentLength();

      File dir = new File(this.filePath);
      dir.mkdirs();
      File file = new File(dir, this.filename);

      InputStream input = new BufferedInputStream(connection.getInputStream());
      OutputStream output = new FileOutputStream(file);

      byte data[] = new byte[1024];
      int count = 0;
      long total = 0;
      while ((count = input.read(data)) != -1) {
        total += count;
        publishProgress((int)(total * 100 / lenghtOfFile));
        output.write(data, 0, count);
      }

      output.flush();
      output.close();
      input.close();
      
      return (total > 0);
    } 
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

   @Override
   protected void onProgressUpdate(Integer... args){
     if (progressDialog == null) {
       progressDialog = new ProgressDialog(context);
       progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
       progressDialog.setMessage("Loading...");
       progressDialog.setCancelable(false);
       progressDialog.show();
     }
     progressDialog.setProgress(args[0]);
   }
   
   @Override
   protected void onPostExecute(Boolean result) {
     if (progressDialog != null) {
       progressDialog.dismiss();
     }
     
     if (result) {
       notifier.downloadSuccessful(this);
       
       Intent intent = new Intent(Intent.ACTION_VIEW);
       intent.setDataAndType(Uri.fromFile(new File(this.filePath, this.filename)), "application/vnd.android.package-archive");
       intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       context.startActivity(intent);
     }
     else {
       try {
         AlertDialog.Builder builder = new AlertDialog.Builder(context);
         builder.setTitle(R.string.download_failed_dialog_title);
         builder.setMessage(R.string.download_failed_dialog_message);
  
         builder.setNegativeButton(R.string.download_failed_dialog_negative_button, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int which) {
             notifier.downloadFailed(DownloadFileTask.this, false);
           } 
         });
  
         builder.setPositiveButton(R.string.download_failed_dialog_positive_button, new DialogInterface.OnClickListener() {
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

   private String getURLString() {
     return urlString + "&type=apk";      
   }
}
