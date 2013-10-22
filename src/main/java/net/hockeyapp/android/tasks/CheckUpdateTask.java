package net.hockeyapp.android.tasks;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Locale;

import android.content.Context;
import net.hockeyapp.android.Constants;
import net.hockeyapp.android.Tracking;
import net.hockeyapp.android.UpdateManagerListener;
import net.hockeyapp.android.utils.VersionCache;
import net.hockeyapp.android.utils.VersionHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;

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

	protected static final String APK = "apk";
	protected static final String INTENT_EXTRA_URL = "url";
	protected static final String INTENT_EXTRA_JSON = "json";

	protected String urlString = null;
  protected String appIdentifier = null;

  private Context context = null;
  protected Boolean mandatory = false;
  protected UpdateManagerListener listener;
  private long usageTime = 0;
  
  public CheckUpdateTask(WeakReference<? extends Context> weakContext, String urlString) {
   this(weakContext, urlString, null);
  }
  
  public CheckUpdateTask(WeakReference<? extends Context> weakContext, String urlString, String appIdentifier) {
   this(weakContext, urlString, appIdentifier, null);
  }

  public CheckUpdateTask(WeakReference<? extends Context> weakContext, String urlString, String appIdentifier, UpdateManagerListener listener) {
    this.appIdentifier = appIdentifier;
    this.urlString = urlString;
    this.listener = listener;

    if (weakContext != null) {
      context = weakContext.get();
    }

    if (context != null) {
      this.usageTime = Tracking.getUsageTime(context);
      Constants.loadFromContext(context);
    }
  }

  public void attach(WeakReference<? extends Context> weakContext) {
    if (weakContext != null) {
      context = weakContext.get();
    }
    
    if (context != null) {
      Constants.loadFromContext(context);
    }
  }
  
  public void detach() {
    context = null;
  }

  protected int getVersionCode() {
    return Integer.parseInt(Constants.APP_VERSION);
  }
  
  @Override
  protected JSONArray doInBackground(String... args) {
    try {
      int versionCode = getVersionCode();
      
      JSONArray json = new JSONArray(VersionCache.getVersionInfo(context));
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
    connection.addRequestProperty("User-Agent", "HockeySDK/Android");
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
        if ((entry.getInt("version") > versionCode) &&
            (VersionHelper.compareVersionStrings(entry.getString("minimum_os_version"), Build.VERSION.RELEASE) <= 0)) {
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
        listener.onUpdateAvailable(updateInfo, getURLString(APK));
      }
    }
    else {
      if (listener != null) {
        listener.onNoUpdateAvailable();
      }
    }
  }
  
  protected void cleanUp() {
    urlString = null;
    appIdentifier = null;
  }

  protected String getURLString(String format) {
    StringBuilder builder = new StringBuilder();
    builder.append(urlString);
    builder.append("api/2/apps/");
    builder.append((this.appIdentifier != null ? this.appIdentifier : context.getPackageName()));
    builder.append("?format=" + format);

    String deviceIdentifier = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    if (deviceIdentifier != null) {
      builder.append("&udid=" + encodeParam(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)));
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