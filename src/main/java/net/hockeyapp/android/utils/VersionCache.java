package net.hockeyapp.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * <h4>Description</h4>
 * 
 * Internal helper class to cache version data.
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
public class VersionCache {
  private static String VERSION_INFO_KEY = "versionInfo";
  
  public static void setVersionInfo(Context context, String json) {
    if (context != null) {
      SharedPreferences preferences = context.getSharedPreferences("HockeyApp", Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = preferences.edit();
      editor.putString(VERSION_INFO_KEY, json);
      PrefsUtil.applyChanges(editor);
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
