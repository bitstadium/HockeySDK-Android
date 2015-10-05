package net.hockeyapp.android;

import java.lang.ref.WeakReference;
import java.util.Date;

import net.hockeyapp.android.tasks.CheckUpdateTask;
import net.hockeyapp.android.tasks.CheckUpdateTaskWithUI;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.Util;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask.Status;
import android.text.TextUtils;

/**
 * <h3>Description</h3>
 * 
 * The update manager sends version information to HockeyApp and
 * shows an alert dialog if a new version was found.
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
 * @author Thomas Dohmke
 **/
public class UpdateManager {
  /**
   * Singleton for update task.
   */
  private static CheckUpdateTask updateTask = null;
  
  /**
   * Last listener instance.
   */
  private static UpdateManagerListener lastListener = null;

  /**
   * Registers new update manager.
   * 
   * @param activity Parent activity.
   * @param appIdentifier App ID of your app on HockeyApp.
   */
  public static void register(Activity activity, String appIdentifier) {
    register(activity, appIdentifier, true);
  }

  /**
   * Registers new update manager.
   *
   * @param activity Parent activity.
   * @param appIdentifier App ID of your app on HockeyApp.
   * @param isDialogRequired Flag to indicate if a dialog must be shown when any update is available.
   */
  public static void register(Activity activity, String appIdentifier, boolean isDialogRequired) {
    register(activity, appIdentifier, null, isDialogRequired);
  }

  /**
   * Registers new update manager.
   *
   * @param activity Parent activity.
   * @param appIdentifier App ID of your app on HockeyApp.
   * @param listener Implement for callback functions.
   */
  public static void register(Activity activity, String appIdentifier, UpdateManagerListener listener) {
    register(activity, Constants.BASE_URL, appIdentifier, listener, true);
  }

  /**
   * Registers new update manager.
   *
   * @param activity Parent activity.
   * @param appIdentifier App ID of your app on HockeyApp.
   * @param listener Implement for callback functions.
   * @param isDialogRequired Flag to indicate if a dialog must be shown when any update is available.
   */
  public static void register(Activity activity, String appIdentifier, UpdateManagerListener listener, boolean isDialogRequired) {
    register(activity, Constants.BASE_URL, appIdentifier, listener, isDialogRequired);
  }

  /**
   * Registers new update manager.
   *
   * @param activity parent activity
   * @param urlString URL of the HockeyApp server
   * @param appIdentifier App ID of your app on HockeyApp
   * @param listener implement for callback functions
   */
  public static void register(Activity activity, String urlString, String appIdentifier, UpdateManagerListener listener) {
    register(activity, urlString, appIdentifier, listener, true);
  }
  
  /**
   * Registers new update manager.
   * 
   * @param activity parent activity
   * @param urlString URL of the HockeyApp server
   * @param appIdentifier App ID of your app on HockeyApp
   * @param listener implement for callback functions
   * @param isDialogRequired if false, no alert dialog is shown
   */
  public static void register(Activity activity, String urlString, String appIdentifier, UpdateManagerListener listener, boolean isDialogRequired) {
    appIdentifier = Util.sanitizeAppIdentifier(appIdentifier);

    lastListener = listener;

    WeakReference<Activity> weakActivity = new WeakReference<Activity>(activity);
    if ((Util.fragmentsSupported()) && (dialogShown(weakActivity))) {
      return;
    }

    if ((!checkExpiryDate(weakActivity, listener)) && ((listener != null && listener.canUpdateInMarket()) || !installedFromMarket(weakActivity))) {
      startUpdateTask(weakActivity, urlString, appIdentifier, listener, isDialogRequired);
    }
  }

  /**
   * Registers new update manager.
   *
   * @param appContext Application context.
   * @param appIdentifier App ID of your app on HockeyApp.
   * @param listener Implement for callback functions.
   */
  public static void registerForBackground(Context appContext, String appIdentifier, UpdateManagerListener listener) {
    registerForBackground(appContext, Constants.BASE_URL, appIdentifier, listener);
  }

  /**
   * Registers new update manager.
   *
   * @param appContext Application context.
   * @param urlString URL of the HockeyApp server.
   * @param appIdentifier App ID of your app on HockeyApp.
   * @param listener Implement for callback functions.
   */
  public static void registerForBackground(Context appContext, String urlString, String appIdentifier, UpdateManagerListener listener) {
    appIdentifier = Util.sanitizeAppIdentifier(appIdentifier);

    lastListener = listener;

    WeakReference<Context> weakContext = new WeakReference<Context>(appContext);

    if ((!checkExpiryDateForBackground(listener)) && ((listener != null && listener.canUpdateInMarket()) || !installedFromMarket(weakContext))) {
      startUpdateTaskForBackground(weakContext, urlString, appIdentifier, listener);
    }
  }

  /**
   * Unregisters the update manager
   */
  public static void unregister() {
    if (updateTask != null) {
      updateTask.cancel(true);
      updateTask.detach();
      updateTask = null;
    }

    lastListener = null;
  }

  /**
   * Returns true if the build is expired and starts an activity if not
   * handled by the owner of the UpdateManager.  
   */
  private static boolean checkExpiryDate(WeakReference<Activity> weakActivity, UpdateManagerListener listener) {
    boolean handle = false;

    boolean hasExpired = checkExpiryDateForBackground(listener);
    if(hasExpired){
      handle = listener.onBuildExpired();
    }

    if ((hasExpired) && (handle)) {
      startExpiryInfoIntent(weakActivity);
    }
    
    return hasExpired;
  }

  /**
   * Returns true if the build is expired and starts an activity if not
   * handled by the owner of the UpdateManager.
   */
  private static boolean checkExpiryDateForBackground(UpdateManagerListener listener) {
    boolean result = false;

    if (listener != null) {
      Date expiryDate = listener.getExpiryDate();
      result = ((expiryDate != null) && (new Date().compareTo(expiryDate) > 0));
    }

    return result;
  }

  /**
   * Returns true if the build was installed through a market.
   */
  private static boolean installedFromMarket(WeakReference<? extends Context> weakContext) {
    boolean result = false;
    
    Context context = weakContext.get();
    if (context != null) {
      try {
        String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());
        result = !TextUtils.isEmpty(installer);
      }
      catch (Throwable e) {
      }
    }
    
    return result;
  }

  /**
   * Starts the ExpiryInfoActivity as a new task and finished the current 
   * activity. 
   */
  private static void startExpiryInfoIntent(WeakReference<Activity> weakActivity) {
    if (weakActivity != null) {
      Activity activity = weakActivity.get();
      if (activity != null) {
        activity.finish();
        
        Intent intent = new Intent(activity, ExpiryInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);        
      }
    }
  }

  /**
   * Starts the UpdateTask if not already running. Otherwise attaches the
   * activity to it. 
   */
  private static void startUpdateTask(WeakReference<Activity> weakActivity, String urlString, String appIdentifier, UpdateManagerListener listener, boolean isDialogRequired) {
    if ((updateTask == null) || (updateTask.getStatus() == Status.FINISHED)) {
      updateTask = new CheckUpdateTaskWithUI(weakActivity, urlString, appIdentifier, listener, isDialogRequired);
      AsyncTaskUtils.execute(updateTask);
    }
    else {
      updateTask.attach(weakActivity);
    }
  }

  /**
   * Starts the UpdateTask if not already running. Otherwise attaches the
   * activity to it.
   */
  private static void startUpdateTaskForBackground(WeakReference<Context> weakContext, String urlString, String appIdentifier, UpdateManagerListener listener) {
    if ((updateTask == null) || (updateTask.getStatus() == Status.FINISHED)) {
      updateTask = new CheckUpdateTask(weakContext, urlString, appIdentifier, listener);
      AsyncTaskUtils.execute(updateTask);
    }
    else {
      updateTask.attach(weakContext);
    }
  }

  /**
   * Returns true if the dialog is already shown (only works on Android 3.0+). 
   */
  @TargetApi(11)
  private static boolean dialogShown(WeakReference<Activity> weakActivity) {
    if (weakActivity != null) {
      Activity activity = weakActivity.get();
      if (activity != null) {
        Fragment existingFragment = activity.getFragmentManager().findFragmentByTag("hockey_update_dialog");
        return (existingFragment != null);
      }
    }
    
    return false;
  }

  /**
   * Returns the last listener which has been registered with any update manager.
   *
   * @return last update manager listener
   */
  public static UpdateManagerListener getLastListener() {
    return lastListener;
  }
}
