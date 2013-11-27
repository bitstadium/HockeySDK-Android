package net.hockeyapp.android;

import java.io.File;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

/**
 * <h4>Description</h4>
 * 
 * Various constants and meta information loaded from the context.
 * 
 * <h4>License</h4>
 * 
 * <pre>
 * Copyright (c) 2009 nullwire aps
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
 * @author Mads Kristiansen
 * @author Glen Humphrey
 * @author Evan Charlton
 * @author Peter Hewitt
 * @author Thomas Dohmke
 **/
public class Constants {
  /**
   * Path where crash logs and temporary files are stored.
   */
  public static String FILES_PATH = null;
  
  /**
   * The app's version code.
   */
  public static String APP_VERSION = null;
  
  /**
   * The app's version name.
   */
  public static String APP_VERSION_NAME = null;

  /**
   * The app's package name.
   */
  public static String APP_PACKAGE = null;
  
  /**
   * The device's OS version.
   */
  public static String ANDROID_VERSION  = null;

  /**
   * The device's model name.
   */
  public static String PHONE_MODEL = null;

  /**
   * The device's model manufacturer name.
   */
  public static String PHONE_MANUFACTURER = null;

  /**
   * Tag for internal logging statements.
   */
  public static final String TAG = "HockeyApp";
  
  /**
   * HockeyApp API URL.
   */
  public static final String BASE_URL = "https://sdk.hockeyapp.net/";
  
  /**
   * Name of this SDK.
   */
  public static final String SDK_NAME = "HockeySDK";
  
  /**
   * Version of this SDK.
   */
  public static final String SDK_VERSION = "3.0.2-SNAPSHOT";

  /**
   * Initializes constants from the given context. The context is used to set 
   * the package name, version code, and the files path.
   *
   * @param context The context to use. Usually your Activity object.
   */
  public static void loadFromContext(Context context) {
    Constants.ANDROID_VERSION = android.os.Build.VERSION.RELEASE;
    Constants.PHONE_MODEL = android.os.Build.MODEL;
    Constants.PHONE_MANUFACTURER = android.os.Build.MANUFACTURER;

    loadFilesPath(context);
    loadPackageData(context);
  }

  /**
   * Helper method to set the files path. If an exception occurs, the files 
   * path will be null! 
   * 
   * @param context The context to use. Usually your Activity object.
   */
  private static void loadFilesPath(Context context) {
    if (context != null) {
      try {
        File file = context.getFilesDir();

        // The file shouldn't be null, but apparently it still can happen, see
        // http://code.google.com/p/android/issues/detail?id=8886
        if (file != null) {
          Constants.FILES_PATH = file.getAbsolutePath();
        }
      } 
      catch (Exception e) {
        Log.e(TAG, "Exception thrown when accessing the files dir:");
        e.printStackTrace();
      }
    }
  }
  
  /**
   * Helper method to set the package name and version code. If an exception 
   * occurs, these values will be null! 
   * 
   * @param context The context to use. Usually your Activity object.
   */
  private static void loadPackageData(Context context) {
    if (context != null) {
      try {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        Constants.APP_PACKAGE = packageInfo.packageName;
        Constants.APP_VERSION = "" + packageInfo.versionCode;
        Constants.APP_VERSION_NAME = packageInfo.versionName;
        
        int buildNumber = loadBuildNumber(context, packageManager);
        if ((buildNumber != 0) && (buildNumber > packageInfo.versionCode)) {
          Constants.APP_VERSION = "" + buildNumber;
        }
      } 
      catch (Exception e) {
        Log.e(TAG, "Exception thrown when accessing the package info:");
        e.printStackTrace();
      }
    }
  }

  private static int loadBuildNumber(Context context, PackageManager packageManager) {
    try {
      ApplicationInfo appInfo = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
      Bundle metaData = appInfo.metaData;
      if (metaData != null) {
        return metaData.getInt("buildNumber", 0);
      }
    } 
    catch (Exception e) {
      Log.e(TAG, "Exception thrown when accessing the application info:");
      e.printStackTrace();
    }
    
    return 0;
  }
}