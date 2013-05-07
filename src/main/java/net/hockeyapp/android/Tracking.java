package net.hockeyapp.android;

import net.hockeyapp.android.utils.PrefsUtil;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * <h4>Description</h4>
 * 
 * This class defines methods to track the app's usage time.  
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
 * @author Thomas Dohmke
 **/
public class Tracking {
  /**
   * Key for shared preferences to store a start time.
   */
  private static final String START_TIME_KEY = "startTime";

  /**
   * Key for shared preferences to store a usage time.
   */
  private static final String USAGE_TIME_KEY = "usageTime";
  
  /**
   * Starts tracking of the usage time for the given activity. The current
   * time is saved in shared preferences with a unique key.
   * 
   * @param activity Instance of activity
   */
  public static void startUsage(Activity activity) {
    long now = System.currentTimeMillis();

    if (activity == null) {
      return;
    }
    
    SharedPreferences.Editor editor = getPreferences(activity).edit();
    editor.putLong(START_TIME_KEY + activity.hashCode(), now);
    PrefsUtil.applyChanges(editor);
  }

  /**
   * Stops tracking of the usage time for the given activity. Reads the
   * start time which was stored by startUsage and calculates the 
   * difference. This difference is then added to the total usage time
   * for the current version.   
   * 
   * @param activity Instance of activity
   */
  public static void stopUsage(Activity activity) {
    long now = System.currentTimeMillis();
    
    if (activity == null) {
      return;
    }

    if (!checkVersion(activity)) {
      return;
    }

    SharedPreferences preferences = getPreferences(activity);
    long start = preferences.getLong(START_TIME_KEY + activity.hashCode(), 0);
    long sum = preferences.getLong(USAGE_TIME_KEY + Constants.APP_VERSION, 0);
    
    if (start > 0) {
      long duration = now - start;
      
      SharedPreferences.Editor editor = preferences.edit();
      editor.putLong(USAGE_TIME_KEY + Constants.APP_VERSION, sum + duration);
      PrefsUtil.applyChanges(editor);
    }
  }

  /**
   * Returns the usage time of the current version in seconds.
   * 
   * @param context Context to access shared preference.
   * @return Usage time in seconds.
   */
  public static long getUsageTime(Context context) {
    if (!checkVersion(context)) {
      return 0;
    }

    SharedPreferences preferences = getPreferences(context);
    long sum = preferences.getLong(USAGE_TIME_KEY + Constants.APP_VERSION, 0);
    return sum / 1000;
  }
  
  /**
   * Checks if the versionCode was set. If not, try to load it. Returns false
   * if it is still null.
   */
  private static boolean checkVersion(Context context) {
    if (Constants.APP_VERSION == null) {
      Constants.loadFromContext(context);

      if (Constants.APP_VERSION == null) {
        return false;
      }
    }
    
    return true;
  }

  /**
   * Helper method to return an instance of SharedPreferences. 
   */
  private static SharedPreferences getPreferences(Context context) {
    return context.getSharedPreferences("HockeyApp", Context.MODE_PRIVATE);
  }
}
