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
     * @param context	{@link Context} object
     * @param token		Feedback token
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
     * @param context	{@link Context} object
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
}
