package net.hockeyapp.android;

import android.content.Context;
import android.content.Intent;

/**
 * <h4>Description</h4>
 * 
 * The FeedbackManager displays the feedback activity.
 * 
 * <h4>License</h4>
 * 
 * <pre>
 * Copyright (c) 2011-2013 Bit Stadium GmbH
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
   * Populates the URL String with the appIdentifier
   * @param context {@link Context} object
   * @return
   */
  private static String getURLString(Context context) {
    return urlString + "api/2/apps/" + identifier + "/feedback/";      
  }

  /**
   * Returns the last listener which has been registered with any Feedback manager.
   */
  public static FeedbackManagerListener getLastListener() {
    return lastListener;
  }
}
