package net.hockeyapp.android;

import java.lang.ref.WeakReference;

import net.hockeyapp.android.utils.Util;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;

/**
 * <h4>Description</h4>
 * 
 * The FeedbackManager displays the Feedback Activity
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
 * @author Bogdan Nistor
 **/
public class FeedbackManager {
  private final static String TAG = "FeedbackManager >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>";
  private static String appIdentifier;
  
  /**
   * Last listener instance.
   */
  private static FeedbackManagerListener lastListener = null;

  /**
   * Registers new Feedback manager.
   * 
   * @param activity Parent activity.
   * @param appIdentifier App ID of your app on HockeyApp.
   */
  public static void register(WeakReference<Context> weakContext, String appIdentifier) {
    register(weakContext, appIdentifier, null);
  }
  
  /**
   * Registers new Feedback manager.
   * 
   * @param activity Parent activity.
   * @param appIdentifier App ID of your app on HockeyApp.
   * @param listener Implement for callback functions.
   */
  public static void register(WeakReference<Context> weakContext, String appIdentifier, FeedbackManagerListener listener) {
    register(weakContext, Constants.BASE_URL, appIdentifier, listener);
  }
  
  /**
   * Registers new Feedback manager.
   *
   * @param activity Parent activity.
   * @param urlString URL of the HockeyApp server.
   * @param appIdentifier App ID of your app on HockeyApp.
   * @param listener Implement for callback functions.
   */
  public static void register(WeakReference<Context> weakContext, String urlString, String appIdentifier, FeedbackManagerListener listener) {
    lastListener = listener;
    FeedbackManager.appIdentifier = appIdentifier;
    
    if (fragmentsSupported()) {
      return;
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
      Class<?> activityClass = FeedbackActivity.class;
      if (lastListener != null) {
        activityClass = lastListener.getFeedbackActivityClass();
      }
      
      Intent intent = new Intent();
      intent.setClass(context, activityClass);
      intent.putExtra("url", getURLString(context));
      context.startActivity(intent);
    }
  }

  /**
   * Populates the URL String with the appIdentifier
   * @param context {@link Context} object
   * @return
   */
  private static String getURLString(Context context) {
    Constants.loadFromContext(context);
    
    StringBuilder builder = new StringBuilder();
    builder.append(String.format(Util.URL_FEEDBACK, appIdentifier));
      
    return builder.toString();
  }

  /**
   * Returns true if the Fragment API is supported (should be on Android 3.0+).
   */
  @SuppressLint("NewApi")
  public static Boolean fragmentsSupported() {
    try {
      return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) && 
          (android.app.Fragment.class != null);
    } catch (NoClassDefFoundError e) {
      return false;
    }
  }

  /**
   * Returns true if the app runs on large or very large screens (i.e. tablets). 
   */
  public static Boolean runsOnTablet(WeakReference<Context> weakContext) {
    if (weakContext != null) {
      Context context = weakContext.get();
      if (context != null) {
        Configuration configuration = context.getResources().getConfiguration();
        
        return (((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.
            SCREENLAYOUT_SIZE_LARGE) || ((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 
            Configuration.SCREENLAYOUT_SIZE_XLARGE));       
      }
    }
    
    return false;
  }

  /**
   * Returns the last listener which has been registered with any Feedback manager.
   */
  public static FeedbackManagerListener getLastListener() {
    return lastListener;
  }
}
