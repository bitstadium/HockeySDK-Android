package net.hockeyapp.android;

/**
 * LICENSE INFORMATION
 * 
 * Copyright (c) 2009 nullwire aps
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
 *
 * Contributors:
 * Mads Kristiansen, mads.kristiansen@nullwire.com
 * Glen Humphrey
 * Evan Charlton
 * Peter Hewitt
 * Thomas Dohmke, thomas@dohmke.de
 **/

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.UUID;

import android.util.Log;

public class ExceptionHandler implements UncaughtExceptionHandler {
  private boolean ignoreDefaultHandler = false;
  private CrashManagerListener listener;
  private UncaughtExceptionHandler defaultExceptionHandler;

  public ExceptionHandler(UncaughtExceptionHandler defaultExceptionHandler, CrashManagerListener listener, boolean ignoreDefaultHandler) {
    this.defaultExceptionHandler = defaultExceptionHandler;
    this.ignoreDefaultHandler = ignoreDefaultHandler;
    this.listener = listener;
  }
  
  public static void saveException(Throwable exception, CrashManagerListener listener) {
    final Date now = new Date();
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);

    exception.printStackTrace(printWriter);

    try {
      // Create filename from a random uuid
      String filename = UUID.randomUUID().toString();
      String path = Constants.FILES_PATH + "/" + filename + ".stacktrace";
      Log.d(Constants.TAG, "Writing unhandled exception to: " + path);
      
      // Write the stacktrace to disk
      BufferedWriter write = new BufferedWriter(new FileWriter(path));
      write.write("Package: " + Constants.APP_PACKAGE + "\n");
      write.write("Version: " + Constants.APP_VERSION + "\n");
      write.write("Android: " + Constants.ANDROID_VERSION + "\n");
      write.write("Manufacturer: " + Constants.PHONE_MANUFACTURER + "\n");
      write.write("Model: " + Constants.PHONE_MODEL + "\n");
      write.write("Date: " + now + "\n");
      write.write("\n");
      write.write(result.toString());
      write.flush();
      write.close();
      
      if (listener != null) {
        writeValueToFile(limitedString(listener.getUserID()), filename + ".user");
        writeValueToFile(limitedString(listener.getContact()), filename + ".contact");
        writeValueToFile(listener.getDescription(), filename + ".description");
      }
      
    } 
    catch (Exception another) {
      Log.e(Constants.TAG, "Error saving exception stacktrace!\n", another);
    }
  }
  
  public void uncaughtException(Thread thread, Throwable exception) {
    saveException(exception, listener);

    if (!ignoreDefaultHandler) {
      defaultExceptionHandler.uncaughtException(thread, exception);
    }
    else {
      android.os.Process.killProcess(android.os.Process.myPid());
      System.exit(10);
    }
  }

  private static void writeValueToFile(String value, String filename) {
    try {
      String path = Constants.FILES_PATH + "/" + filename;
      if (value.trim().length() > 0) {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(value);
        writer.flush();
        writer.close();
      }
    }
    catch (Exception e) {
    }
  }

  private static String limitedString(String string) {
    if ((string != null) && (string.length() > 255)) {
      string = string.substring(0, 255);
    }
    return string;
  }
}