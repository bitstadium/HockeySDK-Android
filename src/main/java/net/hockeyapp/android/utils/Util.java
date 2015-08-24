package net.hockeyapp.android.utils;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.text.TextUtils;

/**
 * <h3>License</h3>
 * 
 * <pre>
 * Copyright (c) 2011-2014 Bit Stadium GmbH
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
 * @author Bogdan Nistor
 */
public class Util {
  public static final String PREFS_FEEDBACK_TOKEN = "net.hockeyapp.android.prefs_feedback_token";
  public static final String PREFS_KEY_FEEDBACK_TOKEN = "net.hockeyapp.android.prefs_key_feedback_token";

  public static final String PREFS_NAME_EMAIL_SUBJECT = "net.hockeyapp.android.prefs_name_email";
  public static final String PREFS_KEY_NAME_EMAIL_SUBJECT = "net.hockeyapp.android.prefs_key_name_email";
  public static final String APP_IDENTIFIER_PATTERN = "[0-9a-f]+";
  public static final int APP_IDENTIFIER_LENGTH = 32;

  public static final String LOG_IDENTIFIER = "HockeyApp";

  private static final Pattern appIdentifierPattern = Pattern.compile(APP_IDENTIFIER_PATTERN, Pattern.CASE_INSENSITIVE);

  /**
   * Returns the given param URL-encoded.
   *
   * @param param a string to encode
   * @return the encoded param
   */
  public static String encodeParam(String param) {
    try {
      return URLEncoder.encode(param, "UTF-8");
    } 
    catch (UnsupportedEncodingException e) {
      // UTF-8 should be available, so just in case
      e.printStackTrace();
      return "";
    }
  }
  
  /**
   * Returns true if value is a valid email.
   *
   * @param value a string
   * @return true if value is a valid email
   */
  @TargetApi(Build.VERSION_CODES.FROYO)
  public final static boolean isValidEmail(String value) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
      return !TextUtils.isEmpty(value) && android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches();
    }
    else {
      return !TextUtils.isEmpty(value);
    }
  }

  /**
   * Returns true if the Fragment API is supported (should be on Android 3.0+).
   *
   * @return true if the Fragment API is supported
   */
  @SuppressLint("NewApi")
  public static Boolean fragmentsSupported() {
    try {
      return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) && (android.app.Fragment.class != null);
    }
    catch (NoClassDefFoundError e) {
      return false;
    }
  }

  /**
   * Returns true if the app runs on large or very large screens (i.e. tablets). 
   *
   * @param weakActivity the context to use
   * @return true if the app runs on large or very large screens
   */
  public static Boolean runsOnTablet(WeakReference<Activity> weakActivity) {
    if (weakActivity != null) {
      Activity activity = weakActivity.get();
      if (activity != null) {
        Configuration configuration = activity.getResources().getConfiguration();
        
        return (((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) || 
                ((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE));
      }
    }
    
    return false;
  }

  /**
   * Sanitizes an app identifier or throws an exception if it can't be sanitized.
   * @param appIdentifier the app identifier to sanitize
   * @return the sanitized app identifier
   * @throws java.lang.IllegalArgumentException if the app identifier can't be sanitized because of unrecoverable input character errors
   */
  public static String sanitizeAppIdentifier(String appIdentifier) throws IllegalArgumentException {

    if (appIdentifier == null) {
      throw new IllegalArgumentException("App ID must not be null.");
    }

    String sAppIdentifier = appIdentifier.trim();

    Matcher matcher = appIdentifierPattern.matcher(sAppIdentifier);

    if (sAppIdentifier.length() != APP_IDENTIFIER_LENGTH) {
      throw new IllegalArgumentException("App ID length must be " + APP_IDENTIFIER_LENGTH + " characters.");
    } else if (!matcher.matches()) {
      throw new IllegalArgumentException("App ID must match regex pattern /" + APP_IDENTIFIER_PATTERN + "/i");
    }

    return sAppIdentifier;
  }

  public static String getFormString(Map<String, String> params) throws UnsupportedEncodingException {
      List<String> protoList = new ArrayList<String>();
      for (String key : params.keySet()) {
          String value = params.get(key);
          key = URLEncoder.encode(key, "UTF-8");
          value = URLEncoder.encode(value, "UTF-8");
          protoList.add(key + "=" + value);
      }
      return TextUtils.join("&", protoList);
  }
}
