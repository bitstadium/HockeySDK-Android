package net.hockeyapp.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

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

  /**
   * Return the singleton.
   * @return
   */
  public static PrefsUtil getInstance() {
    return PrefsUtilHolder.INSTANCE;
  }
    
  /**
   * Save feedback token to {@link SharedPreferences}
   */
  public void saveFeedbackTokenToPrefs(Context context, String token) {
    if (context != null) {
      feedbackTokenPrefs = context.getSharedPreferences(Util.PREFS_FEEDBACK_TOKEN, 0);
      if (feedbackTokenPrefs != null) {
        feedbackTokenPrefsEditor = feedbackTokenPrefs.edit();
        feedbackTokenPrefsEditor.putString(Util.PREFS_KEY_FEEDBACK_TOKEN, token);
        applyChanges(feedbackTokenPrefsEditor);
      }
    }
  }
    
  /**
   * Retrieves the feedback token from {@link SharedPreferences}
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
        
        applyChanges(nameEmailSubjectPrefsEditor);
      }
    }
  }
  
  /**
   * Retrieves the name and email from {@link SharedPreferences}
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
  
  /**
   * Apply SharedPreferences.Editor changes. If the code runs on API level 9 or higher,
   * the asynchronous method apply is used, otherwise commit. 
   */
  public static void applyChanges(Editor editor) {
    if (applySupported()) {
      editor.apply();
    }
    else {
      editor.commit();
    }
  }
  
  /**
   * Returns true if SharedPreferences.Editor.apply is supported.
   */
  public static Boolean applySupported() {
    try {
      return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD);
    }
    catch (NoClassDefFoundError e) {
      return false;
    }
  }
}
