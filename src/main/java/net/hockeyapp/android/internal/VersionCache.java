package net.hockeyapp.android.internal;

import android.content.Context;
import android.content.SharedPreferences;

public class VersionCache {
  private static String VERSION_INFO_KEY = "versionInfo";
  
  public static void setVersionInfo(Context context, String json) {
    if (context != null) {
      SharedPreferences preferences = context.getSharedPreferences("HockeyApp", Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = preferences.edit();
      editor.putString(VERSION_INFO_KEY, json);
      editor.commit();
    }
  }
  
  public static String getVersionInfo(Context context) {
    if (context != null) {
      SharedPreferences preferences = context.getSharedPreferences("HockeyApp", Context.MODE_PRIVATE);
      return preferences.getString(VERSION_INFO_KEY, "[]");
    }
    else {
      return "[]";
    }
  }
}
