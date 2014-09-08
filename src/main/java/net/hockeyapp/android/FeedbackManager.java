package net.hockeyapp.android;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import net.hockeyapp.android.tasks.ParseFeedbackTask;
import net.hockeyapp.android.tasks.SendFeedbackTask;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.PrefsUtil;

import java.io.File;
import java.io.FileOutputStream;

/**
 * <h3>Description</h3>
 * 
 * The FeedbackManager displays the feedback currentActivity.
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
 * @author Bogdan Nistor
 **/
public class FeedbackManager {
  /**
   * The id of the notification to take a screenshot.
   */
  private static final int SCREENSHOT_NOTIFICATION_ID = 1;

  /**
   * The request code for the broadcast.
   */
  private static final int BROADCAST_REQUEST_CODE = 1;

  /**
   * Broadcast action for intent filter.
   */
  private static final String BROADCAST_ACTION = "net.hockeyapp.android.SCREENSHOT";

  /**
   * The BroadcastReceiver instance to listen to the screenshot notification broadcast.
   */
  private static BroadcastReceiver receiver = null;

  /**
   * Used to hold a reference to the currently visible currentActivity of this app.
   */
  private static Activity currentActivity;

  /**
   * Tells if a notification has been created and is visible to the user.
   */
  private static boolean notificationActive = false;

  /**
   * App identifier from HockeyApp.
   */
  private static String identifier = null;
  
  /**
   * URL of HockeyApp service.
   */
  private static String urlString = null;
  
  /**
   * Last listener instance.
   */
  private static FeedbackManagerListener lastListener = null;

  /**
   * Registers new Feedback manager.
   * 
   * @param context The context to use. Usually your Activity object.
   * @param appIdentifier App ID of your app on HockeyApp.
   */
  public static void register(Context context, String appIdentifier) {
    register(context, appIdentifier, null);
  }
  
  /**
   * Registers new Feedback manager.
   * 
   * @param context The context to use. Usually your Activity object.
   * @param appIdentifier App ID of your app on HockeyApp.
   * @param listener Implement for callback functions.
   */
  public static void register(Context context, String appIdentifier, FeedbackManagerListener listener) {
    register(context, Constants.BASE_URL, appIdentifier, listener);
  }
  
  /**
   * Registers new Feedback manager.
   *
   * @param context The context to use. Usually your Activity object.
   * @param urlString URL of the HockeyApp server.
   * @param appIdentifier App ID of your app on HockeyApp.
   * @param listener Implement for callback functions.
   */
  public static void register(Context context, String urlString, String appIdentifier, FeedbackManagerListener listener) {
    if (context != null) {
      FeedbackManager.identifier = appIdentifier;
      FeedbackManager.urlString = urlString;
      FeedbackManager.lastListener = listener;
    
      Constants.loadFromContext(context);
    }
  }

  /**
   * Unregisters the update manager
   */
  public static void unregister() {
    lastListener = null;
  }
  
  /**
   * Starts the {@link FeedbackActivity}
   * @param context {@link Context} object
   */
  public static void showFeedbackActivity(Context context) {
    if (context != null) {
      Class<?> activityClass = null;
      if (lastListener != null) {
        activityClass = lastListener.getFeedbackActivityClass();
      }
      if (activityClass == null) {
        activityClass = FeedbackActivity.class;
      }
      
      Intent intent = new Intent();
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setClass(context, activityClass);
      intent.putExtra("url", getURLString(context));
      context.startActivity(intent);
    }
  }

  /**
   * Checks if an answer to the feedback is available and if yes notifies the listener or
   * creates a system notification.
   *
   * @param context the context to use
   */
  public static void checkForAnswersAndNotify(final Context context) {
    String token = PrefsUtil.getInstance().getFeedbackTokenFromPrefs(context);
    if (token == null) {
      return;
    }

    int lastMessageId = context.getSharedPreferences(ParseFeedbackTask.PREFERENCES_NAME, 0)
        .getInt(ParseFeedbackTask.ID_LAST_MESSAGE_SEND, -1);

    SendFeedbackTask sendFeedbackTask = new SendFeedbackTask(context, getURLString(context), null, null, null, null, null, token, new Handler() {
      @Override
      public void handleMessage(Message msg) {
        Bundle bundle = msg.getData();
        String responseString = bundle.getString("feedback_response");

        if (responseString != null) {
          ParseFeedbackTask task = new ParseFeedbackTask(context, responseString, null, "fetch");
          task.setUrlString(getURLString(context));
          AsyncTaskUtils.execute(task);
        }
      }
    }, true);
    sendFeedbackTask.setShowProgressDialog(false);
    sendFeedbackTask.setLastMessageId(lastMessageId);
    AsyncTaskUtils.execute(sendFeedbackTask);
  }

  /**
   * Returns the last listener which has been registered with any Feedback manager.
   *
   * @return last used feedback listener
   */
  public static FeedbackManagerListener getLastListener() {
    return lastListener;
  }

  /**
   * Populates the URL String with the appIdentifier
   * @param context {@link Context} object
   * @return
   */
  private static String getURLString(Context context) {
    return urlString + "api/2/apps/" + identifier + "/feedback/";
  }

  /**
   * Stores a reference to the given activity to be used for taking a screenshot of it.
   * Reference is cleared only when method unsetCurrentActivityForScreenshot is called.
   *
   * @param activity {@link Activity} object
   */
  public static void setActivityForScreenshot(Activity activity) {
    currentActivity = activity;

    if (!notificationActive) {
      startNotification();
    }
  }

  /**
   * Clears the reference to the activity that was set before by setActivityForScreenshot.
   *
   * @param activity activity for screenshot
   */
  public static void unsetCurrentActivityForScreenshot(Activity activity) {
    if (currentActivity == null || currentActivity != activity) {
      return;
    }

    endNotification();
    currentActivity = null;
  }

  /**
   * Takes a screenshot of the currently set activity and stores it in the HockeyApp folder on the
   * external storage also publishing it to the Android gallery.
   *
   * @param context toast messages will be displayed using this context
   */
  public static void takeScreenshot(Context context) {
    View view = currentActivity.getWindow().getDecorView();
    view.setDrawingCacheEnabled(true);
    Bitmap bitmap = view.getDrawingCache();

    String filename = currentActivity.getLocalClassName();
    File dir = Constants.getHockeyAppStorageDir();
    File result = new File(dir, filename  + ".jpg");
    int suffix = 1;
    while (result.exists()) {
      result = new File(dir, filename + "_" + suffix + ".jpg");
      suffix++;
    }

    try {
      FileOutputStream out = new FileOutputStream(result);
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
      out.close();

    } catch (Exception e) {
      Log.e(Constants.TAG, "Could not save screenshot.", e);
      Toast.makeText(context, "Screenshot could not be created. Sorry.", 2000).show();
    }

    /* Publish to gallery. */
    MediaScannerClient client = new MediaScannerClient(result.getAbsolutePath());
    MediaScannerConnection connection = new MediaScannerConnection(currentActivity, client);
    client.setConnection(connection);
    connection.connect();

    Toast.makeText(context, "Screenshot '" + result.getName() + "' is available in gallery.", 2000).show();
  }

  @SuppressWarnings("deprecation")
  private static void startNotification() {
    notificationActive = true;

    NotificationManager notificationManager = (NotificationManager) currentActivity.getSystemService(Context.NOTIFICATION_SERVICE);

    int iconId = currentActivity.getResources().getIdentifier("ic_menu_camera", "drawable", "android");
    Notification notification = new Notification(iconId, "", System.currentTimeMillis());

    Intent intent =  new Intent();
    intent.setAction(BROADCAST_ACTION);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(currentActivity, BROADCAST_REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT);
    notification.setLatestEventInfo(currentActivity, "HockeyApp Feedback", "Take a screenshot for your feedback.", pendingIntent);
    notificationManager.notify(SCREENSHOT_NOTIFICATION_ID, notification);

    if (receiver == null) {
      receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          FeedbackManager.takeScreenshot(context);
        }
      };
    }
    currentActivity.registerReceiver(receiver, new IntentFilter(BROADCAST_ACTION));
  }

  private static void endNotification() {
    notificationActive = false;

    currentActivity.unregisterReceiver(receiver);
    NotificationManager notificationManager = (NotificationManager) currentActivity.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(SCREENSHOT_NOTIFICATION_ID);
  }

  /**
   * Provides a callback for when the media scanner is connected and an image can be scanned.
   */
  private static class MediaScannerClient implements MediaScannerConnection.MediaScannerConnectionClient {

    private MediaScannerConnection connection;

    private String path;

    private MediaScannerClient(String path) {
      this.connection = null;
      this.path = path;
    }

    public void setConnection(MediaScannerConnection connection) {
      this.connection = connection;
    }

    @Override
    public void onMediaScannerConnected() {
      if (connection != null) {
        connection.scanFile(path, null);
      }
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
      Log.i(Constants.TAG, String.format("Scanned path %s -> URI = %s", path, uri.toString()));
      connection.disconnect();
    }
  }
}
