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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.view.View;
import android.widget.Toast;

import net.hockeyapp.android.objects.FeedbackUserDataElement;
import net.hockeyapp.android.tasks.ParseFeedbackTask;
import net.hockeyapp.android.tasks.SendFeedbackTask;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.PrefsUtil;
import net.hockeyapp.android.utils.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * <h3>Description</h3>
 *
 * The FeedbackManager displays the feedback currentActivity.
 *
 **/
@SuppressWarnings({"unused", "WeakerAccess"})
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
     * Used to hold a reference to the currently visible activity of this app.
     */
    private static WeakReference<Activity> weakActivity;

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
     * Whether the user's name is required.
     */
    private static FeedbackUserDataElement requireUserName = FeedbackUserDataElement.REQUIRED;

    /**
     * Whether the user's email is required.
     */
    private static FeedbackUserDataElement requireUserEmail = FeedbackUserDataElement.REQUIRED;

    private static String userName;

    private static String userEmail;

    /**
     * Last listener instance.
     */
    private static FeedbackManagerListener lastListener = null;

    /**
     * Registers new Feedback manager.
     * HockeyApp App Identifier is read from configuration values in AndroidManifest.xml
     *
     * @param context The context to use. Usually your Activity object.
     */
    public static void register(Context context) {
        String appIdentifier = Util.getAppIdentifier(context);
        register(context, appIdentifier);
    }

    /**
     * Registers new Feedback manager.
     * HockeyApp App Identifier is read from configuration values in AndroidManifest.xml
     *
     * @param context   The context to use. Usually your Activity object.
     * @param listener  Implement for callback functions.
     */
    public static void register(Context context, FeedbackManagerListener listener) {
        String appIdentifier = Util.getAppIdentifier(context);
        register(context, appIdentifier, listener);
    }

    /**
     * Registers new Feedback manager.
     *
     * @param context       The context to use. Usually your Activity object.
     * @param appIdentifier App ID of your app on HockeyApp.
     */
    public static void register(Context context, String appIdentifier) {
        register(context, appIdentifier, null);
    }

    /**
     * Registers new Feedback manager.
     *
     * @param context       The context to use. Usually your Activity object.
     * @param appIdentifier App ID of your app on HockeyApp.
     * @param listener      Implement for callback functions.
     */
    public static void register(Context context, String appIdentifier, FeedbackManagerListener listener) {
        register(context, Constants.BASE_URL, appIdentifier, listener);
    }

    /**
     * Registers new Feedback manager.
     *
     * @param context       The context to use. Usually your Activity object.
     * @param urlString     URL of the HockeyApp server.
     * @param appIdentifier App ID of your app on HockeyApp.
     * @param listener      Implement for callback functions.
     */
    public static void register(Context context, String urlString, String appIdentifier, FeedbackManagerListener listener) {
        if (context != null) {
            FeedbackManager.identifier = Util.sanitizeAppIdentifier(appIdentifier);
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
     *
     * @param context     {@link Context} object
     * @param attachments the optional attachment {@link Uri}s
     */
    public static void showFeedbackActivity(Context context, Uri... attachments) {
        showFeedbackActivity(context, null, attachments);
    }

    /**
     * Starts the {@link FeedbackActivity}
     *
     * @param context     {@link Context} object
     * @param attachments the optional attachment {@link Uri}s
     * @param extras      a bundle to be added to the Intent that starts the FeedbackActivity instance
     */
    public static void showFeedbackActivity(final Context context, final Bundle extras, final Uri... attachments) {
        if (context != null) {
            final Class<?> activityClass = lastListener != null ? lastListener.getFeedbackActivityClass() : null;
            final boolean forceNewThread = lastListener != null && lastListener.shouldCreateNewFeedbackThread();

            AsyncTaskUtils.execute(new AsyncTask<Void, Object, Intent>() {
                @Override
                protected Intent doInBackground(Void... voids) {
                    Intent intent = new Intent();
                    if (extras != null && !extras.isEmpty()) {
                        intent.putExtras(extras);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(context, activityClass != null ? activityClass : FeedbackActivity.class);
                    intent.putExtra(FeedbackActivity.EXTRA_URL, getURLString(context));
                    String token = !forceNewThread ? PrefsUtil.getInstance().getFeedbackTokenFromPrefs(context) : null;
                    intent.putExtra(FeedbackActivity.EXTRA_TOKEN, token);
                    intent.putExtra(FeedbackActivity.EXTRA_FORCE_NEW_THREAD, forceNewThread);
                    String userName = FeedbackManager.userName;
                    String userEmail = FeedbackManager.userEmail;
                    String userSubject = null;
                    String nameEmailSubject = PrefsUtil.getInstance().getNameEmailFromPrefs(context);
                    if (nameEmailSubject != null) {
                        // Prepopulate the appropriate fields
                        String[] nameEmailSubjectArray = nameEmailSubject.split("\\|");
                        if (nameEmailSubjectArray != null && nameEmailSubjectArray.length >= 2) {
                            userName = nameEmailSubjectArray[0];
                            userEmail = nameEmailSubjectArray[1];
                            if (!forceNewThread && nameEmailSubjectArray.length >= 3) {
                                userSubject = nameEmailSubjectArray[2];
                            }
                        }
                    }
                    intent.putExtra(FeedbackActivity.EXTRA_INITIAL_USER_NAME, userName);
                    intent.putExtra(FeedbackActivity.EXTRA_INITIAL_USER_EMAIL, userEmail);
                    intent.putExtra(FeedbackActivity.EXTRA_INITIAL_USER_EMAIL, userSubject);
                    intent.putExtra(FeedbackActivity.EXTRA_INITIAL_ATTACHMENTS, attachments);
                    return intent;
                }

                @Override
                protected void onPostExecute(Intent intent) {
                    context.startActivity(intent);
                }
            });
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
                String responseString = bundle.getString(SendFeedbackTask.BUNDLE_FEEDBACK_RESPONSE);

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
     *
     * @param context {@link Context} object
     * @return URL String with the appIdentifier
     */
    private static String getURLString(Context context) {
        return urlString + "api/2/apps/" + identifier + "/feedback/";
    }

    /**
     * Returns the required setting for the user name property.
     *
     * @return required setting for name
     */
    public static FeedbackUserDataElement getRequireUserName() {
        return requireUserName;
    }

    /**
     * Sets the required setting for the user name property
     *
     * @param requireUserName whether the user name property should be required
     */
    public static void setRequireUserName(FeedbackUserDataElement requireUserName) {
        FeedbackManager.requireUserName = requireUserName;
    }

    /**
     * Returns the required setting for the user email property.
     *
     * @return required setting for email
     */
    public static FeedbackUserDataElement getRequireUserEmail() {
        return requireUserEmail;
    }

    /**
     * Sets the required setting for the user email property
     *
     * @param requireUserEmail whether the user email property should be required
     */
    public static void setRequireUserEmail(FeedbackUserDataElement requireUserEmail) {
        FeedbackManager.requireUserEmail = requireUserEmail;
    }

    /**
     * Sets the user's name to pre-fill the feedback form with
     *
     * @param userName user's name
     */
    public static void setUserName(String userName) {
        FeedbackManager.userName = userName;
    }

    /**
     * Sets the user's e-mail to pre-fill the feedback form with
     *
     * @param userEmail user's e-mail
     */
    public static void setUserEmail(String userEmail) {
        FeedbackManager.userEmail = userEmail;
    }

    /**
     * Stores a reference to the given activity to be used for taking a screenshot of it.
     * Reference is cleared only when method unsetCurrentActivityForScreenshot is called.
     *
     * @param activity {@link Activity} object
     */
    public static void setActivityForScreenshot(Activity activity) {
        weakActivity = new WeakReference<>(activity);

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
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null || currentActivity != activity) {
            return;
        }

        endNotification();
        weakActivity = null;
    }

    /**
     * Takes a screenshot of the currently set activity and stores it in the HockeyApp folder on the
     * external storage also publishing it to the Android gallery.
     *
     * @param context toast messages will be displayed using this context
     */
    @UiThread
    public static void takeScreenshot(final Context context) {
        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return;
        }

        View view = currentActivity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        final Bitmap bitmap = view.getDrawingCache();
        final String filename = currentActivity.getLocalClassName();

        AsyncTaskUtils.execute(new AsyncTask<Void, Void, Boolean>() {
            File result;

            @Override
            protected Boolean doInBackground(Void... args) {
                File dir = Constants.getHockeyAppStorageDir(context);
                result = new File(dir, filename + ".jpg");
                int suffix = 1;
                while (result.exists()) {
                    result = new File(dir, filename + "_" + suffix + ".jpg");
                    suffix++;
                }
                try {
                    FileOutputStream out = new FileOutputStream(result);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.close();
                    return true;
                } catch (IOException e) {
                    HockeyLog.error("Could not save screenshot.", e);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {

                    /* Publish to gallery. */
                    MediaScannerClient client = new MediaScannerClient(result.getAbsolutePath());
                    MediaScannerConnection connection = new MediaScannerConnection(currentActivity, client);
                    client.setConnection(connection);
                    connection.connect();

                    Toast.makeText(context, "Screenshot '" + result.getName() + "' is available in gallery.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Screenshot could not be created. Sorry.", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    private static void startNotification() {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return;
        }
        notificationActive = true;
        NotificationManager notificationManager = (NotificationManager) currentActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        int iconId = currentActivity.getResources().getIdentifier("ic_menu_camera", "drawable", "android");

        Intent intent = new Intent();
        intent.setAction(BROADCAST_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(currentActivity, BROADCAST_REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = Util.createNotification(currentActivity, pendingIntent, "HockeyApp Feedback", "Take a screenshot for your feedback.", iconId);

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
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return;
        }

        notificationActive = false;
        currentActivity.unregisterReceiver(receiver);
        NotificationManager notificationManager = (NotificationManager) currentActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(SCREENSHOT_NOTIFICATION_ID);
    }

    private static Activity getCurrentActivity() {
        return weakActivity != null ? weakActivity.get() : null;
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
            HockeyLog.verbose(String.format("Scanned path %s -> URI = %s", path, uri.toString()));
            connection.disconnect();
        }
    }
}
