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
    public static final String HOCKEY_TAG = "HockeyApp";

    private static int sLogLevel = Log.ERROR;

    /**
     * Get the loglevel to find out how much data the HockeySDK spews into LogCat. The Default will be
     * LOG_LEVEL.ERROR so only errors show up in LogCat.
     *
     * @return the log level
     */
    public static int getLogLevel() {
        return sLogLevel;
    }

    /**
     * Set the log level to determine the amount of info the HockeySDK spews info into LogCat.
     *
     * @param hockeyLogLevel The log level for hockeySDK logging
     */
    public static void setLogLevel(int hockeyLogLevel) {
        sLogLevel = hockeyLogLevel;
    }


    /**
     * Log a message with level VERBOSE with the default tag
     *
     * @param message the log message
     */
    public static void verbose(String message) {
        verbose(null, message);
    }

    /**
     * Log a message with level VERBOSE
     *
     * @param tag     the log tag for your message
     * @param message the log message
     */
    public static void verbose(String tag, String message) {
        tag = sanitizeTag(tag);
        if (sLogLevel <= Log.VERBOSE) {
            Log.v(tag, message);
        }
    }

    /**
     * Log a message with level VERBOSE with the default tag
     *
     * @param message   the log message
     * @param throwable the throwable you want to log
     */
    public static void verbose(String message, Throwable throwable) {
        verbose(null, message, throwable);
    }

    /**
     * Log a message with level VERBOSE
     *
     * @param tag       the log tag for your message
     * @param message   the log message
     * @param throwable the throwable you want to log
     */
    public static void verbose(String tag, String message, Throwable throwable) {
        tag = sanitizeTag(tag);
        if (sLogLevel <= Log.VERBOSE) {
            Log.v(tag, message, throwable);
        }
    }

    /**
     * Log a message with level DEBUG with the default tag
     *
     * @param message the log message
     */
    public static void debug(String message) {
        debug(null, message);
    }

    /**
     * Log a message with level DEBUG
     *
     * @param tag     the log tag for your message
     * @param message the log message
     */
    public static void debug(String tag, String message) {
        tag = sanitizeTag(tag);
        if (sLogLevel <= Log.DEBUG) {
            Log.d(tag, message);
        }
    }

    /**
     * Log a message with level DEBUG with the default tag
     *
     * @param message   the log message
     * @param throwable the throwable you want to log
     */
    public static void debug(String message, Throwable throwable) {
        debug(null, message, throwable);
    }

    /**
     * Log a message with level DEBUG
     *
     * @param tag       the log tag for your message
     * @param message   the log message
     * @param throwable the throwable you want to log
     */
    public static void debug(String tag, String message, Throwable throwable) {
        tag = sanitizeTag(tag);
        if (sLogLevel <= Log.DEBUG) {
            Log.d(tag, message, throwable);
        }
    }

    /**
     * Log a message with level INFO with the default tag
     *
     * @param message the log message
     */
    public static void info(String message) {
        info(null, message);
    }

    /**
     * Log a message with level INFO
     *
     * @param tag     the log tag for your message
     * @param message the log message
     */
    public static void info(String tag, String message) {
        tag = sanitizeTag(tag);
        if (sLogLevel <= Log.INFO) {
            Log.i(tag, message);
        }
    }

    /**
     * Log a message with level INFO with the default tag
     *
     * @param message   the log message
     * @param throwable the throwable you want to log
     */
    public static void info(String message, Throwable throwable) {
        info(message, throwable);
    }

    /**
     * Log a message with level INFO
     *
     * @param tag       the log tag for your message
     * @param message   the log message
     * @param throwable the throwable you want to log
     */
    public static void info(String tag, String message, Throwable throwable) {
        tag = sanitizeTag(tag);
        if (sLogLevel <= Log.INFO) {
            Log.i(tag, message, throwable);
        }
    }

    /**
     * Log a message with level WARN with the default tag
     *
     * @param message the log message
     */
    public static void warn(String message) {
        warn(null, message);
    }

    /**
     * Log a message with level WARN
     *
     * @param tag     the TAG
     * @param message the log message
     */
    public static void warn(String tag, String message) {
        tag = sanitizeTag(tag);
        if (sLogLevel <= Log.WARN) {
            Log.w(tag, message);
        }
    }

    /**
     * Log a message with level WARN with the default tag
     *
     * @param message   the log message
     * @param throwable the throwable you want to log
     */
    public static void warn(String message, Throwable throwable) {
        warn(null, message, throwable);
    }

    /**
     * Log a message with level WARN
     *
     * @param tag       the log tag for your message
     * @param message   the log message
     * @param throwable the throwable you want to log
     */
    public static void warn(String tag, String message, Throwable throwable) {
        tag = sanitizeTag(tag);
        if (sLogLevel <= Log.WARN) {
            Log.w(tag, message, throwable);
        }
    }

    /**
     * Log a message with level ERROR with the default tag
     *
     * @param message the log message
     */
    public static void error(String message) {
        error(null, message);
    }

    /**
     * Log a message with level ERROR
     *
     * @param tag     the log tag for your message
     * @param message the log message
     */
    public static void error(String tag, String message) {
        tag = sanitizeTag(tag);
        if (sLogLevel <= Log.ERROR) {
            Log.e(tag, message);
        }
    }

    /**
     * Log a message with level ERROR with the default tag
     *
     * @param message   the log message
     * @param throwable the throwable you want to log
     */
    public static void error(String message, Throwable throwable) {
        error(null, message, throwable);
    }

    /**
     * Log a message with level ERROR
     *
     * @param tag       the log tag for your message
     * @param message   the log message
     * @param throwable the throwable you want to log
     */
    public static void error(String tag, String message, Throwable throwable) {
        tag = sanitizeTag(tag);
        if (sLogLevel <= Log.ERROR) {
            Log.e(tag, message, throwable);
        }
    }

    /**
     * Sanitize a TAG string
     *
     * @param tag the log tag for your message for the logging
     * @return a sanitized TAG, defaults to 'HockeyApp' in case the log tag for your message is null, empty or longer than
     * 23 characters.
     */
    static String sanitizeTag(String tag) {
        if ((tag == null) || (tag.length() == 0) || (tag.length() > 23)) {
            tag = HOCKEY_TAG;
        }

        return tag;
    }

}
