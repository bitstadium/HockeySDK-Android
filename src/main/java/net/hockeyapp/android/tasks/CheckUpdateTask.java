package net.hockeyapp.android.tasks;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Locale;

import net.hockeyapp.android.Constants;
import net.hockeyapp.android.Strings;
import net.hockeyapp.android.Tracking;
import net.hockeyapp.android.UpdateActivity;
import net.hockeyapp.android.UpdateFragment;
import net.hockeyapp.android.UpdateManager;
import net.hockeyapp.android.UpdateManagerListener;
import net.hockeyapp.android.utils.VersionCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/**
 * <h4>Description</h4>
 * 
 * Internal helper class. Checks if a new update is available by 
 * fetching version data from Hockeyapp. 
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
public class CheckUpdateTask extends AsyncTask<String, String, JSONArray>{
  private static final int MAX_NUMBER_OF_VERSIONS = 25;
  
  protected String urlString = null;
  protected String appIdentifier = null;
  
  private Activity activity = null;
  private Boolean mandatory = false;
  private UpdateManagerListener listener;
  private long usageTime = 0;
  
  public CheckUpdateTask(WeakReference<Activity> weakActivity, String urlString) {
    this.appIdentifier = null;
    this.urlString = urlString;
    
    if (weakActivity != null) {
      activity = weakActivity.get();
    }
    
    if (activity != null) {
      this.usageTime = Tracking.getUsageTime(activity);
      Constants.loadFromContext(activity);
    }
  }
  
  public CheckUpdateTask(WeakReference<Activity> weakActivity, String urlString, String appIdentifier) {
    this.appIdentifier = appIdentifier;
    this.urlString = urlString;

    if (weakActivity != null) {
      activity = weakActivity.get();
    }
    
    if (activity != null) {
      this.usageTime = Tracking.getUsageTime(activity);
      Constants.loadFromContext(activity);
    }
  }
  
  public CheckUpdateTask(WeakReference<Activity> weakActivity, String urlString, String appIdentifier, UpdateManagerListener listener) {
    this.appIdentifier = appIdentifier;
    this.urlString = urlString;
    this.listener = listener;

    if (weakActivity != null) {
      activity = weakActivity.get();
    }
    
    if (activity != null) {
      this.usageTime = Tracking.getUsageTime(activity);
      Constants.loadFromContext(activity);
    }
  }

  public void attach(WeakReference<Activity> weakActivity) {
    if (weakActivity != null) {
      activity = weakActivity.get();
    }
    
    if (activity != null) {
      Constants.loadFromContext(activity);
    }
  }
  
  public void detach() {
    activity = null;
  }

  protected int getVersionCode() {
    if (activity != null) {
      try {
        return activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_META_DATA).versionCode;
      }
      catch (NameNotFoundException e) {
        return 0;
      }
    }
    
    return 0;
  }
  
  @Override
  protected JSONArray doInBackground(String... args) {
    try {
      int versionCode = getVersionCode();
      
      JSONArray json = new JSONArray(VersionCache.getVersionInfo(activity));
      if ((getCachingEnabled()) && (findNewVersion(json, versionCode))) {
        return json;
      }
      
      URL url = new URL(getURLString("json"));
      URLConnection connection = createConnection(url);
      connection.connect();

      InputStream inputStream = new BufferedInputStream(connection.getInputStream());
      String jsonString = convertStreamToString(inputStream);
      inputStream.close();
      
      json = new JSONArray(jsonString);
      if (findNewVersion(json, versionCode)) {
        json = limitResponseSize(json);
        return json;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return null;
  }

  protected URLConnection createConnection(URL url) throws IOException {
    URLConnection connection = url.openConnection();
    connection.addRequestProperty("User-Agent", "Hockey/Android");
    // connection bug workaround for SDK<=2.x
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD) {
      connection.setRequestProperty("connection", "close");
    }
    return connection;
  }

  private boolean findNewVersion(JSONArray json, int versionCode) {
    try {
      for (int index = 0; index < json.length(); index++) {
        JSONObject entry = json.getJSONObject(index);
        if (entry.getInt("version") > versionCode) {
          if (entry.has("mandatory")) {
            mandatory = entry.getBoolean("mandatory");
          }
          return true;
        }
      }
      
      return false;
    }
    catch (JSONException e) {
      return false;
    }
  }

  private JSONArray limitResponseSize(JSONArray json) {
    JSONArray result = new JSONArray();
    for (int index = 0; index < Math.min(json.length(), MAX_NUMBER_OF_VERSIONS); index++) {
      try {
        result.put(json.get(index));
      }
      catch (JSONException e) {
      }
    }
    return result;
  }

  @Override
  protected void onPostExecute(JSONArray updateInfo) {
    if (updateInfo != null) {
      if (listener != null) {
        listener.onUpdateAvailable();
      }

      showDialog(updateInfo);
    }
    else {
      if (listener != null) {
        listener.onNoUpdateAvailable();
      }
    }
  }
  
  private void cleanUp() {
    activity = null;
    urlString = null;
    appIdentifier = null;
  }

  protected String getURLString(String format) {
    StringBuilder builder = new StringBuilder();
    builder.append(urlString);
    builder.append("api/2/apps/");
    builder.append((this.appIdentifier != null ? this.appIdentifier : activity.getPackageName()));
    builder.append("?format=" + format);

    String deviceIdentifier = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
    if (deviceIdentifier != null) {
      builder.append("&udid=" + encodeParam(Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID)));
    }
    
    builder.append("&os=Android");
    builder.append("&os_version=" + encodeParam(Constants.ANDROID_VERSION));
    builder.append("&device=" + encodeParam(Constants.PHONE_MODEL));
    builder.append("&oem=" + encodeParam(Constants.PHONE_MANUFACTURER));
    builder.append("&app_version=" + encodeParam(Constants.APP_VERSION));
    builder.append("&sdk=" + encodeParam(Constants.SDK_NAME));
    builder.append("&sdk_version=" + encodeParam(Constants.SDK_VERSION));
    builder.append("&lang=" + encodeParam(Locale.getDefault().getLanguage()));
    builder.append("&usage_time=" + usageTime);
    
    return builder.toString();
  }
  
  private String encodeParam(String param) {
    try {
      return URLEncoder.encode(param, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      // UTF-8 should be available, so just in case
      return "";
    }
  }
  
  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private void showDialog(final JSONArray updateInfo) {
    if (getCachingEnabled()) {
      VersionCache.setVersionInfo(activity, updateInfo.toString());
    }
    
    if ((activity == null) || (activity.isFinishing())) {
      return;
    }
    
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setTitle(Strings.get(listener, Strings.UPDATE_DIALOG_TITLE_ID));
    
    if (!mandatory) {
      builder.setMessage(Strings.get(listener, Strings.UPDATE_DIALOG_MESSAGE_ID));
  
      builder.setNegativeButton(Strings.get(listener, Strings.UPDATE_DIALOG_NEGATIVE_BUTTON_ID), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          cleanUp();
        } 
      });
      
      builder.setPositiveButton(Strings.get(listener, Strings.UPDATE_DIALOG_POSITIVE_BUTTON_ID), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          if (getCachingEnabled()) {
            VersionCache.setVersionInfo(activity, "[]");
          }
          
          WeakReference<Activity> weakActivity = new WeakReference<Activity>(activity);
          if ((UpdateManager.fragmentsSupported()) && (UpdateManager.runsOnTablet(weakActivity))) {
            showUpdateFragment(updateInfo);
          }
          else {
            startUpdateIntent(updateInfo, false);
          }
        } 
      });

      builder.create().show();
    }
    else {
      Toast.makeText(activity, Strings.get(listener, Strings.UPDATE_MANDATORY_TOAST_ID), Toast.LENGTH_LONG).show();
      startUpdateIntent(updateInfo, true);
    }
  }
  
  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private void startUpdateIntent(final JSONArray updateInfo, Boolean finish) {
    Class<?> activityClass = UpdateActivity.class;
    if (listener != null) {
      activityClass = listener.getUpdateActivityClass();
    }
    
    Intent intent = new Intent();
    intent.setClass(activity, activityClass);
    intent.putExtra("json", updateInfo.toString());
    intent.putExtra("url", getURLString("apk"));
    activity.startActivity(intent);
    
    if (finish) {
      activity.finish();
    }
    
    cleanUp();
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private void showUpdateFragment(final JSONArray updateInfo) {
    if (activity != null) {
      FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
      fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
      
      Fragment existingFragment = activity.getFragmentManager().findFragmentByTag("hockey_update_dialog");
      if (existingFragment != null) {
        fragmentTransaction.remove(existingFragment);
      }
      fragmentTransaction.addToBackStack(null);
  
      // Create and show the dialog
      Class<? extends UpdateFragment> fragmentClass = UpdateFragment.class;
      if (listener != null) {
        fragmentClass = listener.getUpdateFragmentClass();
      }
      
      try {
        Method method = fragmentClass.getMethod("newInstance", JSONArray.class, String.class);
        DialogFragment updateFragment = (DialogFragment)method.invoke(null, updateInfo, getURLString("apk"));
        updateFragment.show(fragmentTransaction, "hockey_update_dialog");
      }
      catch (Exception e) {
        Log.d(Constants.TAG, "An exception happened while showing the update fragment:");
        e.printStackTrace();
        Log.d(Constants.TAG, "Showing update activity instead.");
        startUpdateIntent(updateInfo, false);
      }
    }
  }
  
  private static String convertStreamToString(InputStream inputStream) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), 1024);
    StringBuilder stringBuilder = new StringBuilder();

    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line + "\n");
      }
    } 
    catch (IOException e) {
      e.printStackTrace();
    } 
    finally {
      try {
        inputStream.close();
      } 
      catch (IOException e) {
        e.printStackTrace();
      }
    }
    return stringBuilder.toString();
  }

  protected boolean getCachingEnabled() {
    return true;
  }
}