package net.hockeyapp.android.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import net.hockeyapp.android.UpdateInfoListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <h4>Description</h4>
 * 
 * Internal helper class. Provides helper methods to parse the
 * version JSON and create the release notes as HTML. 
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
public class VersionHelper {
  ArrayList<JSONObject> sortedVersions;
  JSONObject newest;
  UpdateInfoListener listener;
  
  public VersionHelper(String infoJSON, UpdateInfoListener listener) {
    this.listener = listener;

    loadVersions(infoJSON);
    sortVersions();
  }
  
  private void loadVersions(String infoJSON) {
    this.newest = new JSONObject();

    try {
      JSONArray versions = new JSONArray(infoJSON);
      this.sortedVersions = new ArrayList<JSONObject>();
      
      int versionCode = listener.getCurrentVersionCode();
      for (int index = 0; index < versions.length(); index++) {
        JSONObject entry = versions.getJSONObject(index);
        if (entry.getInt("version") > versionCode) {
          newest = entry;
          versionCode = entry.getInt("version");
        }
        sortedVersions.add(entry);
      }
    }
    catch (JSONException je) {
    }
    catch (NullPointerException ne) {
    }
  }

  private void sortVersions() {
    Collections.sort(sortedVersions, new Comparator<JSONObject>() {
      public int compare(JSONObject object1, JSONObject object2) {
        try {
          if (object1.getInt("version") > object2.getInt("version")) {
            return 0;
          }
        }
        catch (JSONException e) {
        }

        return 0;
      }
    });
  }

  public String getVersionString() {
    return failSafeGetStringFromJSON(newest, "shortversion", "") + " (" + failSafeGetStringFromJSON(newest, "version", "") + ")";
  }
  
  public String getFileInfoString() {
    int appSize = failSafeGetIntFromJSON(newest, "appsize", 0);
    long timestamp = failSafeGetIntFromJSON(newest, "timestamp", 0);
    Date date = new Date(timestamp * 1000);
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    return dateFormat.format(date) + " - " + String.format("%.2f", appSize / 1024F / 1024F) + " MB";
  }
  
  private static String failSafeGetStringFromJSON(JSONObject json, String name, String defaultValue) {
    try {
      return json.getString(name);
    }
    catch (JSONException e) {
      return defaultValue;
    }
  }
  
  private static int failSafeGetIntFromJSON(JSONObject json, String name, int defaultValue) {
    try {
      return json.getInt(name);
    }
    catch (JSONException e) {
      return defaultValue;
    }
  }
  
  public String getReleaseNotes() {
    StringBuilder result = new StringBuilder();
    result.append("<html>");
    result.append("<body style='padding: 0px 0px 10px 0px'>");
    
    int count = 0;
    for (JSONObject version : sortedVersions) {
      result.append(getVersionLine(count, version));
      result.append(getVersionNotes(count, version));
      count++;
    }

    result.append("</body>");
    result.append("</html>");

    return result.toString();
  }

  private Object getVersionLine(int count, JSONObject version) {
    StringBuilder result = new StringBuilder();

    int versionCode = 0;
    String versionName= "";
    try { 
      versionCode = version.getInt("version");
      versionName = version.getString("shortversion");
    }
    catch (JSONException e) {
    }
    
    result.append("<div style='padding: 20px 10px 10px;'><strong>");
    if (count == 0) {
      result.append("Release Notes:");
    }
    else {
      int currentVersionCode = listener.getCurrentVersionCode();
      result.append("Version " + versionName + " (" + versionCode + "): " + (versionCode == currentVersionCode ? "[INSTALLED]" : ""));
    }
    result.append("</strong></div>");
    
    return result.toString();
  }

  private Object getVersionNotes(int count, JSONObject version) {
    StringBuilder result = new StringBuilder();

    String notes = failSafeGetStringFromJSON(version, "notes", "");
    result.append("<div style='padding: 0px 10px;'>");
    if (notes.trim().length() == 0) {
      result.append("<em>No information.</em>");
    }
    else {
      result.append(notes);
    }
    result.append("</div>");

    return result.toString();
  }
}
