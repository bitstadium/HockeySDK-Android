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

    public enum LogLevel {
        VERBOSE(Log.VERBOSE), DEBUG(Log.DEBUG), INFO(Log.INFO), WARN(Log.WARN), ERROR(Log.ERROR), ASSERT(Log.ASSERT);
        final int systemLogLevel;

        LogLevel(final int systemLogLevel) {
            this.systemLogLevel = systemLogLevel;
        }

        public int getStystemLogLevel() {
            return systemLogLevel;
        }
    }

    private static LogLevel sLogLevel = LogLevel.ERROR;


    /**
     * Get the loglevel to find out how much data the HockeySDK spews into LogCat. The Default will be
     * LOG_LEVEL.ERROR so only errors show up in LogCat.
     * @return the log level
     */
    public static LogLevel getHockeyLogLevel() {
        return sLogLevel;
    }

    /**
     * Set the log level to determine the amount of info the HockeySDK spews info into LogCat.
     *
     * @param hockeyLogLevel The log level for hockeySDK logging
     */
    public static void setHockeyLogLevel(LogLevel hockeyLogLevel) {
        sLogLevel = hockeyLogLevel;
    }

    public static void verbose(String message) {
        verbose(null, message);
    }

    public static void verbose(String tag, String message) {
        tag = sanitizeTag(tag);
        if (sLogLevel.systemLogLevel <= Log.VERBOSE) {
            Log.v(tag, message);
        }
    }

    public static void verbose(String tag, String message, Throwable throwable) {
        tag = sanitizeTag(tag);
        if (sLogLevel.systemLogLevel <= Log.VERBOSE) {
            Log.v(tag, message, throwable);
        }
    }

    public static void debug(String message) {
        debug(null, message);
    }

    public static void debug(String tag, String message) {
        tag = sanitizeTag(tag);
        if (sLogLevel.systemLogLevel <= Log.DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void debug(String tag, String message, Throwable throwable) {
        tag = sanitizeTag(tag);
        if (sLogLevel.systemLogLevel <= Log.DEBUG) {
            Log.d(tag, message, throwable);
        }
    }

    public static void info(String message) {
        info(null, message);
    }

    public static void info(String tag, String message) {
        tag = sanitizeTag(tag);
        if (sLogLevel.systemLogLevel <= Log.INFO) {
            Log.i(tag, message);
        }
    }

    public static void info(String tag, String message, Throwable throwable) {
        tag = sanitizeTag(tag);
        if (sLogLevel.systemLogLevel <= Log.INFO) {
            Log.i(tag, message, throwable);
        }
    }

    public static void warn(String message) {
        warn(null, message);
    }

    public static void warn(String tag, String message) {
        tag = sanitizeTag(tag);
        if (sLogLevel.systemLogLevel <= Log.WARN) {
            Log.w(tag, message);
        }
    }

    public static void warn(String tag, String message, Throwable throwable) {
        tag = sanitizeTag(tag);
        if (sLogLevel.systemLogLevel <= Log.WARN) {
            Log.w(tag, message, throwable);
        }
    }

    public static void error(String message) {
        error(null, message);
    }

    public static void error(String tag, String message) {
        tag = sanitizeTag(tag);
        if (sLogLevel.systemLogLevel <= Log.ERROR) {
            Log.e(tag, message);
        }
    }

    public static void error(String tag, String message, Throwable throwable) {
        tag = sanitizeTag(tag);
        if (sLogLevel.systemLogLevel <= Log.ERROR) {
            Log.e(tag, message, throwable);
        }
    }

    private static String sanitizeTag(String tag) {
        if((tag == null) || (tag.length() == 0) || (tag.length() > 23) ) {
            tag = HOCKEY_TAG;
        }

        return tag;
    }

}
