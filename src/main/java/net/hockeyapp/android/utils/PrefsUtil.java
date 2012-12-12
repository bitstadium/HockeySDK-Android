package net.hockeyapp.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * {@link SharedPreferences} helper class
 * @author Bogdan Nistor
 *
 */
public class PrefsUtil {
  private SharedPreferences feedbackTokenPrefs;
  private SharedPreferences.Editor feedbackTokenPrefsEditor;
  private SharedPreferences nameEmailSubjectPrefs;
  private SharedPreferences.Editor nameEmailSubjectPrefsEditor;
  
  /** Private constructor prevents instantiation from other classes */
  private PrefsUtil() { 
  }

  /**
  * PrefsUtilHolder is loaded on the first execution of WbUtil.getInstance() 
  * or the first access to PrefsUtilHolder.INSTANCE, not before.
  */
  private static class PrefsUtilHolder { 
    public static final PrefsUtil INSTANCE = new PrefsUtil();
  }

  public static PrefsUtil getInstance() {
    return PrefsUtilHolder.INSTANCE;
  }
    
  /**
   * Save feedback token to {@link SharedPreferences}
   * @param context {@link Context} object
   * @param token   Feedback token
   */
  public void saveFeedbackTokenToPrefs(Context context, String token) {
    if (context != null) {
      feedbackTokenPrefs = context.getSharedPreferences(Util.PREFS_FEEDBACK_TOKEN, 0);
      if (feedbackTokenPrefs != null) {
        feedbackTokenPrefsEditor = feedbackTokenPrefs.edit();
        feedbackTokenPrefsEditor.putString(Util.PREFS_KEY_FEEDBACK_TOKEN, token);
        feedbackTokenPrefsEditor.commit();
      }
    }
  }
    
  /**
   * Retrieves the feedback token from {@link SharedPreferences}
   * @param context {@link Context} object
   * @return
   */
  public String getFeedbackTokenFromPrefs(Context context) {
    if (context == null) {
      return null;
    }
    
    feedbackTokenPrefs = context.getSharedPreferences(Util.PREFS_FEEDBACK_TOKEN, 0);
    if (feedbackTokenPrefs == null) {
      return null;
    }
    
    return feedbackTokenPrefs.getString(Util.PREFS_KEY_FEEDBACK_TOKEN, null);
  }

  /**
   * Save name and email to {@link SharedPreferences}
   * @param context {@link Context} object
   * @param name    Name
   * @param email   Email
   * @param subject Subject
   */
  public void saveNameEmailSubjectToPrefs(Context context, String name, String email, String subject) {
    if (context != null) {
      nameEmailSubjectPrefs = context.getSharedPreferences(Util.PREFS_NAME_EMAIL_SUBJECT, 0);
      if (nameEmailSubjectPrefs != null) {
        nameEmailSubjectPrefsEditor = nameEmailSubjectPrefs.edit();
        if (name == null || email == null || subject == null) {
          nameEmailSubjectPrefsEditor.putString(Util.PREFS_KEY_NAME_EMAIL_SUBJECT, null); 
        } else {
          nameEmailSubjectPrefsEditor.putString(Util.PREFS_KEY_NAME_EMAIL_SUBJECT, String.format("%s|%s|%s", 
              name, email, subject));
        }
        
        nameEmailSubjectPrefsEditor.commit();
      }
    }
  }
  
  /**
   * Retrieves the name and email from {@link SharedPreferences}
   * @param context {@link Context} object
   * @return
   */
  public String getNameEmailFromPrefs(Context context) {
    if (context == null) {
      return null;
    }
    
    nameEmailSubjectPrefs = context.getSharedPreferences(Util.PREFS_NAME_EMAIL_SUBJECT, 0);
    if (nameEmailSubjectPrefs == null) {
      return null;
    }
    
    return nameEmailSubjectPrefs.getString(Util.PREFS_KEY_NAME_EMAIL_SUBJECT, null);
  }
}
