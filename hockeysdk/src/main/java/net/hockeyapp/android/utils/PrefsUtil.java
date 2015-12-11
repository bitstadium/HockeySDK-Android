package net.hockeyapp.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * <h3>Description</h3>
 *
 * {@link SharedPreferences} helper class
 *
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
public class PrefsUtil {
    private SharedPreferences mFeedbackTokenPrefs;
    private SharedPreferences.Editor mFeedbackTokenPrefsEditor;
    private SharedPreferences mNameEmailSubjectPrefs;
    private SharedPreferences.Editor mNameEmailSubjectPrefsEditor;

    /**
     * Private constructor prevents instantiation from other classes
     */
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
     *
     * @return the singleton
     */
    public static PrefsUtil getInstance() {
        return PrefsUtilHolder.INSTANCE;
    }

    /**
     * Save feedback token to {@link SharedPreferences}
     *
     * @param context the context to use
     * @param token   the feedback token
     */
    public void saveFeedbackTokenToPrefs(Context context, String token) {
        if (context != null) {
            mFeedbackTokenPrefs = context.getSharedPreferences(Util.PREFS_FEEDBACK_TOKEN, 0);
            if (mFeedbackTokenPrefs != null) {
                mFeedbackTokenPrefsEditor = mFeedbackTokenPrefs.edit();
                mFeedbackTokenPrefsEditor.putString(Util.PREFS_KEY_FEEDBACK_TOKEN, token);
                mFeedbackTokenPrefsEditor.apply();
            }
        }
    }

    /**
     * Retrieves the feedback token from {@link SharedPreferences}
     *
     * @param context the context to use
     * @return the feedback token
     */
    public String getFeedbackTokenFromPrefs(Context context) {
        if (context == null) {
            return null;
        }

        mFeedbackTokenPrefs = context.getSharedPreferences(Util.PREFS_FEEDBACK_TOKEN, 0);
        if (mFeedbackTokenPrefs == null) {
            return null;
        }

        return mFeedbackTokenPrefs.getString(Util.PREFS_KEY_FEEDBACK_TOKEN, null);
    }

    /**
     * Save name and email to {@link SharedPreferences}
     *
     * @param context the context to use
     * @param name    the user's name
     * @param email   the user's email
     * @param subject the message subject
     */
    public void saveNameEmailSubjectToPrefs(Context context, String name, String email, String subject) {
        if (context != null) {
            mNameEmailSubjectPrefs = context.getSharedPreferences(Util.PREFS_NAME_EMAIL_SUBJECT, 0);
            if (mNameEmailSubjectPrefs != null) {
                mNameEmailSubjectPrefsEditor = mNameEmailSubjectPrefs.edit();
                if (name == null || email == null || subject == null) {
                    mNameEmailSubjectPrefsEditor.putString(Util.PREFS_KEY_NAME_EMAIL_SUBJECT, null);
                } else {
                    mNameEmailSubjectPrefsEditor.putString(Util.PREFS_KEY_NAME_EMAIL_SUBJECT, String.format("%s|%s|%s",
                            name, email, subject));
                }

                mNameEmailSubjectPrefsEditor.apply();
            }
        }
    }

    /**
     * Retrieves the name and email from {@link SharedPreferences}
     *
     * @param context the context to use
     * @return a string with name, email, and subject
     */
    public String getNameEmailFromPrefs(Context context) {
        if (context == null) {
            return null;
        }

        mNameEmailSubjectPrefs = context.getSharedPreferences(Util.PREFS_NAME_EMAIL_SUBJECT, 0);
        if (mNameEmailSubjectPrefs == null) {
            return null;
        }

        return mNameEmailSubjectPrefs.getString(Util.PREFS _KEY_NAME_EMAIL_SUBJECT, null);
    }

}
