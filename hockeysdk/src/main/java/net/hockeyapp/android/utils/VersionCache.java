package net.hockeyapp.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * <h3>Description</h3>
 *
 * Internal helper class to cache version data.
 */
public class VersionCache {
    private static String PREF_VERSION_INFO_KEY = "versionInfo";

    public static void setVersionInfo(Context context, String json) {
        if (context != null) {
            SharedPreferences preferences = context.getSharedPreferences("HockeyApp", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PREF_VERSION_INFO_KEY, json);
            editor.apply();
        }
    }

    public static String getVersionInfo(Context context) {
        if (context != null) {
            SharedPreferences preferences = context.getSharedPreferences("HockeyApp", Context.MODE_PRIVATE);
            return preferences.getString(PREF_VERSION_INFO_KEY, "[]");
        } else {
            return "[]";
        }
    }
}
