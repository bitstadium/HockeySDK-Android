package net.hockeyapp.android.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
  private static final DateFormat DATE_FORMAT =
          new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT);

  static {
    TimeZone timeZone = TimeZone.getTimeZone("UTC");
    DATE_FORMAT.setTimeZone(timeZone);
  }

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
      return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) && classExists("android.app.Fragment");
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

  /**
   * Converts a map of parameters to a HTML form entity.
   * @param params the parameters
   * @return an URL-encoded form string ready for use in a HTTP post
   * @throws UnsupportedEncodingException
   */
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

  /**
   * Helper method to safely check whether a class exists at runtime.
   * @param className the full-qualified class name to check for
   * @return whether the class exists
   */
  public static boolean classExists(String className) {
    try {
      return Class.forName(className) != null;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  /**
   * Checks if the Notification.Builder API is supported.
   * @return if builder API is supported
   */
  public static boolean isNotificationBuilderSupported() {
    return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) && classExists("android.app.Notification.Builder");
  }

  /**
   * Determines if Session is possible for the current user or not.
   *
   * @return YES if app runs on at least OS 4.0
   */
  public static boolean sessionTrackingSupported() {
    return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH);
  }

  /**
   * Creates a notification on API levels from 9 to 23
   * @param context the context to use, e.g. your Activity
   * @param pendingIntent the Intent to call
   * @param title the title string for the notification
   * @param text the text content for the notification
   * @param iconId the icon resource ID for the notification
   * @return
   */
  public static Notification createNotification(Context context, PendingIntent pendingIntent, String title, String text, int iconId) {
    Notification notification;
    if (Util.isNotificationBuilderSupported()) {
      notification = buildNotificationWithBuilder(context, pendingIntent, title, text, iconId);
    } else {
      notification = buildNotificationPreHoneycomb(context, pendingIntent, title, text, iconId);
    }
    return notification;
  }

  @SuppressWarnings("deprecation")
  private static Notification buildNotificationPreHoneycomb(Context context, PendingIntent pendingIntent, String title, String text, int iconId) {
    Notification notification = new Notification(iconId, "", System.currentTimeMillis());
    try {
      // try to call "setLatestEventInfo" if available
      Method m = notification.getClass().getMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
      m.invoke(notification, context, title, text, pendingIntent);
    } catch (Exception e) {
      // do nothing
    }
    return notification;
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  @SuppressWarnings("deprecation")
  private static Notification buildNotificationWithBuilder(Context context, PendingIntent pendingIntent, String title, String text, int iconId) {
    android.app.Notification.Builder builder = new android.app.Notification.Builder(context)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setSmallIcon(iconId);

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
      return builder.getNotification();
    } else {
      return builder.build();
    }
  }

  /**
   * Convert a date object to an ISO 8601 formatted string
   *
   * @param date the date object to be formatted
   * @return an ISO 8601 string representation of the date
   */
  public static String dateToISO8601(Date date) {
    Date localDate = date;
    if (localDate == null) {
      localDate = new Date();
    }

    return DATE_FORMAT.format(localDate);
  }

  /**
   * Get a SHA-256 hash of the input string if the algorithm is available. If the algorithm is
   * unavailable, return empty string.
   *
   * @param input the string to hash.
   * @return a SHA-256 hash of the input or the empty string.
   */
  public static String tryHashStringSha256(String input) {
    String salt = "oRq=MAHHHC~6CCe|JfEqRZ+gc0ESI||g2Jlb^PYjc5UYN2P 27z_+21xxd2n";
    try {
      // Get a Sha256 digest
      MessageDigest hash = MessageDigest.getInstance("SHA-256");
      hash.reset();
      hash.update(input.getBytes());
      hash.update(salt.getBytes());
      byte[] hashedBytes = hash.digest();

      char[] hexChars = new char[hashedBytes.length * 2];
      for (int j = 0; j < hashedBytes.length; j++) {
        int v = hashedBytes[j] & 0xFF;
        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
      }

      return new String(hexChars);
    } catch (NoSuchAlgorithmException e) {
      // All android devices should support SHA256, but if unavailable return ""
      return "";
    }
  }

  /**
   * Determines whether the app is running on aan emulator or on a real device.
   *
   * @return YES if the app is running on an emulator, NO if it is running on a real device
   */
  public static boolean isEmulator() {
    return Build.BRAND.equalsIgnoreCase("generic");
  }
}
