package net.hockeyapp.android;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class CheckUpdateTask extends AsyncTask<String, String, JSONArray>{
  protected String urlString = null;
  protected String appIdentifier = null;
  
  private Activity activity = null;
  private Boolean mandatory = false;
  private UpdateManagerListener listener;
  
  public CheckUpdateTask(Activity activity, String urlString) {
    this.appIdentifier = null;
    this.activity = activity;
    this.urlString = urlString;
    
    Constants.loadFromContext(activity);
  }
  
  public CheckUpdateTask(Activity activity, String urlString, String appIdentifier) {
    this.appIdentifier = appIdentifier;
    this.activity = activity;
    this.urlString = urlString;

    Constants.loadFromContext(activity);
  }
  
  public CheckUpdateTask(Activity activity, String urlString, String appIdentifier, UpdateManagerListener listener) {
    this.appIdentifier = appIdentifier;
    this.activity = activity;
    this.urlString = urlString;
    this.listener = listener;

    Constants.loadFromContext(activity);
  }

  public void attach(Activity activity) {
    this.activity = activity;

    Constants.loadFromContext(activity);
  }
  
  public void detach() {
    activity = null;
  }

  protected int getVersionCode() {
    try {
      return activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_META_DATA).versionCode;
    }
    catch (NameNotFoundException e) {
      return 0;
    }
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
      URLConnection connection = url.openConnection();
      connection.addRequestProperty("User-Agent", "Hockey/Android");
      connection.setRequestProperty("connection", "close");
      connection.connect();

      InputStream inputStream = new BufferedInputStream(connection.getInputStream());
      String jsonString = convertStreamToString(inputStream);
      inputStream.close();
      
      json = new JSONArray(jsonString);
      if (findNewVersion(json, versionCode)) {
        return json;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return null;
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
      builder.append("&udid=" + URLEncoder.encode(Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID)));
    }
    
    builder.append("&os=Android");
    builder.append("&os_version=" + URLEncoder.encode(Constants.ANDROID_VERSION));
    builder.append("&device=" + URLEncoder.encode(Constants.PHONE_MODEL));
    builder.append("&oem=" + URLEncoder.encode(Constants.PHONE_MANUFACTURER));
    builder.append("&app_version=" + URLEncoder.encode(Constants.APP_VERSION));
    
    return builder.toString();
  }
  
  private void showDialog(final JSONArray updateInfo) {
    if (getCachingEnabled()) {
      VersionCache.setVersionInfo(activity, updateInfo.toString());
    }
    
    if ((activity == null) || (activity.isFinishing())) {
      return;
    }
    
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setTitle(R.string.update_dialog_title);
    
    if (!mandatory) {
      builder.setMessage(R.string.update_dialog_message);
  
      builder.setNegativeButton(R.string.update_dialog_negative_button, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          cleanUp();
        } 
      });
      
      builder.setPositiveButton(R.string.update_dialog_positive_button, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          if (getCachingEnabled()) {
            VersionCache.setVersionInfo(activity, "[]");
          }
          
          if ((UpdateManager.fragmentsSupported()) && (UpdateManager.runsOnTablet(activity))) {
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
      Toast.makeText(activity, R.string.update_mandatory_toast, Toast.LENGTH_LONG).show();
      startUpdateIntent(updateInfo, true);
    }
  }
  
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

  private void showUpdateFragment(final JSONArray updateInfo) {
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