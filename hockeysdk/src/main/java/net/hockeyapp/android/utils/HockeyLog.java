package net.hockeyapp.android.utils;


import android.util.Log;

/**
 * <h3>License</h3>
 * <p/>
 * <pre>
 * Copyright (c) 2011-2015 Bit Stadium GmbH
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
 * @author Benjamin Reimold
 */
public class HockeyLog {
    public static final String LOG_IDENTIFIER = "HockeyApp";

    private static boolean mDebugLogEnabled = false;

    /**
     * Check if debug logging has been enabled.
     *
     * @return true/falls to indicate if debug logging has been enabled.
     */
    public static boolean isDebugLogEnabled() {
        return mDebugLogEnabled;
    }

    /**
     * Enable additional output to Log.d(...) by the SDK.
     *
     * @param debugLogEnabled
     */
    public static void setDebugLogEnabled(boolean debugLogEnabled) {
        mDebugLogEnabled = debugLogEnabled;
    }

    /**
     * Log a message to Log.d(TAG, message) if debug logging has been enabled.
     *
     * @param message the message to log to Log.d, if it is null, nothing will be logged.
     */
    public static void log(String message) {
        log(null, message);
    }

    /**
     * Log a message to Log.d(TAG, message) if debug logging has been enabled.
     *
     * @param tag,    the tag used for logging. If null, it will default to HockeyApp
     * @param message the message to log to Log.d, if it is null, nothing will be logged.
     */
    public static void log(String tag, String message) {
        if (isDebugLogEnabled() && (message != null)) {
            if (tag == null) {
                tag = LOG_IDENTIFIER;
            }

            Log.d(tag, message);
        }
    }
}
