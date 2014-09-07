package net.hockeyapp.android.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;

/**
 * Utility class
 * @author Bogdan Nistor
 *
 */
public class Util {
  public static final String PREFS_FEEDBACK_TOKEN = "net.hockeyapp.android.prefs_feedback_token";
  public static final String PREFS_KEY_FEEDBACK_TOKEN = "net.hockeyapp.android.prefs_key_feedback_token";

  public static final String PREFS_NAME_EMAIL_SUBJECT = "net.hockeyapp.android.prefs_name_email";
  public static final String PREFS_KEY_NAME_EMAIL_SUBJECT = "net.hockeyapp.android.prefs_key_name_email";

  public static String encodeParam(String param) {
    try {
      return URLEncoder.encode(param, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      // UTF-8 should be available, so just in case
      e.printStackTrace();
      return "";
    }
  }
  
  @TargetApi(Build.VERSION_CODES.FROYO)
  public final static boolean isValidEmail(CharSequence target) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
      return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
    else {
      return !TextUtils.isEmpty(target);
    }
  }
  
}
