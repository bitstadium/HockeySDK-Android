package net.hockeyapp.android.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Scanner;

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
    this.sortedVersions = new ArrayList<JSONObject>();

    try {
      JSONArray versions = new JSONArray(infoJSON);
      
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
        catch (JSONException je) {
        }
        catch (NullPointerException ne) {
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
  
  public String getReleaseNotes(boolean showRestore) {
    StringBuilder result = new StringBuilder();
    result.append("<html>");
    result.append("<body style='padding: 0px 0px 20px 0px'>");
    
    int count = 0;
    for (JSONObject version : sortedVersions) {
      if (count > 0) {
        result.append(getSeparator());
        if (showRestore) { 
          result.append(getRestoreButton(count, version));
        }
      }
      result.append(getVersionLine(count, version));
      result.append(getVersionNotes(count, version));
      count++;
    }

    result.append("</body>");
    result.append("</html>");

    return result.toString();
  }

  private Object getSeparator() {
    return "<hr style='border-top: 1px solid #c8c8c8; border-bottom: 0px; margin: 40px 10px 0px 10px;' />";
  }

  private String getRestoreButton(int count, JSONObject version) {
    StringBuilder result = new StringBuilder();

    String versionID = getVersionID(version);
    if (versionID.length() > 0) {
      result.append("<a href='restore:" + versionID + "'  style='background: #c8c8c8; color: #000; display: block; float: right; padding: 7px; margin: 0px 10px 10px; text-decoration: none;'>Restore</a>");
    }

    return result.toString();
  }

  private String getVersionID(JSONObject version) {
    String versionID = "";
    try { 
      versionID = version.getString("id");
    }
    catch (JSONException e) {
    }
    return versionID;
  }

  private String getVersionLine(int count, JSONObject version) {
    StringBuilder result = new StringBuilder();

    int versionCode = getVersionCode(version);
    String versionName = getVersionName(version);
    
    result.append("<div style='padding: 20px 10px 10px;'><strong>");
    if (count == 0) {
      result.append("Newest version:");
    }
    else {
      int currentVersionCode = listener.getCurrentVersionCode();
      result.append("Version " + versionName + " (" + versionCode + "): " + (versionCode == currentVersionCode ? "[INSTALLED]" : ""));
    }
    result.append("</strong></div>");
    
    return result.toString();
  }
  
  private int getVersionCode(JSONObject version) {
    int versionCode = 0;
    try { 
      versionCode = version.getInt("version");
    }
    catch (JSONException e) {
    }
    return versionCode;
  }
  
  private String getVersionName(JSONObject version) {
    String versionName = "";
    try { 
      versionName = version.getString("shortversion");
    }
    catch (JSONException e) {
    }
    return versionName;
  }

  private String getVersionNotes(int count, JSONObject version) {
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
  
  /**
   * Compare two versions strings with each other by splitting at the . 
   * and comparing the integer values. Additional string like "-update1"
   * are ignored, i.e. "2.2" is considered equal to "2.2-update1". 
   * 
   * @param left A version string, e.g. "2.1".
   * @param right A version string, e.g. "4.2.2".
   * @return 0 if the versions are equal. 
   *         1 if the left side is bigger.
   *         -1 if the right side is bigger.
   */
  public static int compareVersionStrings(String left, String right) {
    // If either side is null, we consider the versions equal
    if ((left == null) || (right == null)) {
      return 0;
    }

    try {
      // Strip out any "-update1" stuff, then build a scanner for the strings
      Scanner leftScanner = new Scanner(left.replaceAll("\\-.*", ""));
      Scanner rightScanner = new Scanner(right.replaceAll("\\-.*", ""));
      leftScanner.useDelimiter("\\.");
      rightScanner.useDelimiter("\\.");

      // Compare the parts
      while ((leftScanner.hasNextInt()) && (rightScanner.hasNextInt())) {
        int leftValue = leftScanner.nextInt();
        int rightValue = rightScanner.nextInt();
        if (leftValue < rightValue) {
          return -1;
        } 
        else if (leftValue > rightValue) {
          return 1;
        }
      }

      // Left side has more parts, so consider it bigger
      if (leftScanner.hasNextInt()) {
        return 1;
      } 
      // Right side has more parts, so consider it bigger
      else if (rightScanner.hasNextInt()) {
        return -1;
      } 
      // Ok, they are equal
      else {
        return 0;
      }
    }
    catch (Exception e) {
      // If any exceptions happen, return zero
      return 0;
    }
  }
}
