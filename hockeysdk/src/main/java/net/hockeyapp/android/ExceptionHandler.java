package net.hockeyapp.android;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.UUID;

/**
 * <h3>Description</h3>
 *
 * Helper class to catch exceptions. Saves the stack trace
 * as a file and executes callback methods to ask the app for
 * additional information and meta data (see CrashManagerListener).
 *
 * <h3>License</h3>
 *
 * <pre>
 * Copyright (c) 2009 nullwire aps
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
 * @author Mads Kristiansen
 * @author Glen Humphrey
 * @author Evan Charlton
 * @author Peter Hewitt
 * @author Thomas Dohmke
 * @author Matthias Wenz
 * @author Benjamin Reimold
 **/
public class ExceptionHandler implements UncaughtExceptionHandler {
    private boolean mIgnoreDefaultHandler = false;
    private CrashManagerListener mCrashManagerListener;
    private UncaughtExceptionHandler mDefaultExceptionHandler;

    public ExceptionHandler(UncaughtExceptionHandler defaultExceptionHandler, CrashManagerListener listener, boolean ignoreDefaultHandler) {
        mDefaultExceptionHandler = defaultExceptionHandler;
        mIgnoreDefaultHandler = ignoreDefaultHandler;
        mCrashManagerListener = listener;
    }

    public void setListener(CrashManagerListener listener) {
        mCrashManagerListener = listener;
    }

    /**
     * Save a caught exception to disk.
     * @deprecated in 3.7.0-beta.2. Use saveException(Throwable exception, Thread thread,
     * CrashManagerListener listener) instead.
     *
     * @param exception Exception to save.
     * @param listener  Custom CrashManager listener instance.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public static void saveException(Throwable exception, CrashManagerListener listener) {
        saveException(exception, null, listener);
    }

    /**
     * Save a caught exception to disk.
     * @param exception Exception to save.
     * @param thread    Thread that crashed.
     * @param listener  Custom CrashManager listener instance.
     */
    public static void saveException(Throwable exception, Thread thread, CrashManagerListener listener) {
        final Date now = new Date();
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        BufferedWriter writer = null;
        exception.printStackTrace(printWriter);

        try {
            // Create filename from a random uuid
            String filename = UUID.randomUUID().toString();
            String path = Constants.FILES_PATH + "/" + filename + ".stacktrace";
            Log.d(Constants.TAG, "Writing unhandled exception to: " + path);

            // Write the stacktrace to disk
            writer = new BufferedWriter(new FileWriter(path));

            // HockeyApp expects the package name in the first line!
            writer.write("Package: " + Constants.APP_PACKAGE + "\n");
            writer.write("Version Code: " + Constants.APP_VERSION + "\n");
            writer.write("Version Name: " + Constants.APP_VERSION_NAME + "\n");

            if ((listener == null) || (listener.includeDeviceData())) {
                writer.write("Android: " + Constants.ANDROID_VERSION + "\n");
                writer.write("Manufacturer: " + Constants.PHONE_MANUFACTURER + "\n");
                writer.write("Model: " + Constants.PHONE_MODEL + "\n");
            }

            if (thread != null && ((listener == null) || (listener.includeThreadDetails()))) {
                writer.write("Thread: " + thread.getName() + "-" + thread.getId() + "\n");
            }

            if (Constants.CRASH_IDENTIFIER != null && (listener == null || listener.includeDeviceIdentifier())) {
                writer.write("CrashReporter Key: " + Constants.CRASH_IDENTIFIER + "\n");
            }

            writer.write("Date: " + now + "\n");
            writer.write("\n");
            writer.write(result.toString());
            writer.flush();

            if (listener != null) {
                writeValueToFile(limitedString(listener.getUserID()), filename + ".user");
                writeValueToFile(limitedString(listener.getContact()), filename + ".contact");
                writeValueToFile(listener.getDescription(), filename + ".description");
            }
        } catch (IOException another) {
            Log.e(Constants.TAG, "Error saving exception stacktrace!\n", another);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                Log.e(Constants.TAG, "Error saving exception stacktrace!\n", e);
                e.printStackTrace();
            }
        }
    }

    public void uncaughtException(Thread thread, Throwable exception) {
        if (Constants.FILES_PATH == null) {
            // If the files path is null, the exception can't be stored
            // Always call the default handler instead
            mDefaultExceptionHandler.uncaughtException(thread, exception);
        } else {
            saveException(exception, thread, mCrashManagerListener);

            if (!mIgnoreDefaultHandler) {
                mDefaultExceptionHandler.uncaughtException(thread, exception);
            } else {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(10);
            }
        }
    }

    private static void writeValueToFile(String value, String filename) throws IOException {
        BufferedWriter writer = null;
        try {
            String path = Constants.FILES_PATH + "/" + filename;
            if (value.trim().length() > 0) {
                writer = new BufferedWriter(new FileWriter(path));
                writer.write(value);
                writer.flush();
            }
        } catch (IOException e) {
            // TODO: Handle exception here
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static String limitedString(String string) {
        if ((string != null) && (string.length() > 255)) {
            string = string.substring(0, 255);
        }
        return string;
    }
}
