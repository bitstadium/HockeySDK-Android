package net.hockeyapp.android;

import net.hockeyapp.android.internal.CheckUpdateTask;
import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.AsyncTask.Status;

/**
 * <h4>Description</h4>
 * 
 * The update manager sends version information to HockeyApp and
 * shows an alert dialog if a new version was found.
 * 
 * <h4>License</h4>
 * 
 * <pre>
 * Copyright (c) 2012 Codenauts UG
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
    register(activity, appIdentifier, null);
  }
  
  /**
   * Registers new update manager.
   * 
   * @param activity Parent activity.
   * @param appIdentifier App ID of your app on HockeyApp.
   * @param listener Implement for callback functions.
   */
  public static void register(Activity activity, String appIdentifier, UpdateManagerListener listener) {
    register(activity, Constants.BASE_URL, appIdentifier, listener);
  }
  
  /**
   * Registers new update manager.
   * 
   * @param activity Parent activity.
   * @param urlString URL of the HockeyApp server.
   * @param appIdentifier App ID of your app on HockeyApp.
   * @param listener Implement for callback functions.
   */
  public static void register(Activity activity, String urlString, String appIdentifier, UpdateManagerListener listener) {
    lastListener = listener;
    
    if ((fragmentsSupported()) && (dialogShown(activity))) {
      return;
    }
    
    if ((updateTask == null) || (updateTask.getStatus() == Status.FINISHED)) {
      updateTask = new CheckUpdateTask(activity, urlString, appIdentifier, listener);
      updateTask.execute();
    }
    else {
      updateTask.attach(activity);
    }
  }

  /**
   * Returns true if the dialog is already shown (only works on Android 3.0+). 
   */
  private static boolean dialogShown(Activity activity) {
    Fragment existingFragment = activity.getFragmentManager().findFragmentByTag("hockey_update_dialog");
    return (existingFragment != null);
  }

  /**
   * Returns true if the Fragment API is supported (should be on Android 3.0+).
   */
  public static Boolean fragmentsSupported() {
    try {
      return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) && (android.app.Fragment.class != null);
    }
    catch (NoClassDefFoundError e) {
      return false;
    }
  }

  /**
   * Returns true if the app runs on large or very large screens (i.e. tablets). 
   */
  public static Boolean runsOnTablet(Activity activity) {
    Configuration configuration = activity.getResources().getConfiguration();
    return (((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) || ((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE));
  }

  /**
   * Returns the last listener which has been registered with any update manager.
   */
  public static UpdateManagerListener getLastListener() {
    return lastListener;
  }
}
