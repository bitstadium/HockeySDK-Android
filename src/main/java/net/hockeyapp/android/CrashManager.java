package net.hockeyapp.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import net.hockeyapp.android.utils.ConnectionManager;
import net.hockeyapp.android.utils.PrefsUtil;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * <h4>Description</h4>
 * 
 * The crash manager sets an exception handler to catch all unhandled 
 * exceptions. The handler writes the stack trace and additional meta data to 
 * a file. If it finds one or more of these files at the next start, it shows 
 * an alert dialog to ask the user if he want the send the crash data to 
 * HockeyApp. 
 * 
 * <h4>License</h4>
 * 
 * <pre>
 * Copyright (c) 2011-2013 Bit Stadium GmbH
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
 * @author Thomas Dohmke
 **/
public class CrashManager {
  /**
   * App identifier from HockeyApp.
   */
  private static String identifier = null;
  
  /**
   * URL of HockeyApp service.
   */
  private static String urlString = null;

  /**
   * Registers new crash manager and handles existing crash logs.
   * 
   * @param context The context to use. Usually your Activity object.
   * @param appIdentifier App ID of your app on HockeyApp.
   */
  public static void register(Context context, String appIdentifier) {
    register(context, Constants.BASE_URL, appIdentifier, null);
  }

  /**
   * Registers new crash manager and handles existing crash logs.
   * 
   * @param context The context to use. Usually your Activity object.
   * @param appIdentifier App ID of your app on HockeyApp.
   * @param listener Implement for callback functions.
   */
  public static void register(Context context, String appIdentifier, CrashManagerListener listener) {
    register(context, Constants.BASE_URL, appIdentifier, listener);
  }

  /**
   * Registers new crash manager and handles existing crash logs.
   * 
   * @param context The context to use. Usually your Activity object.
   * @param urlString URL of the HockeyApp server.
   * @param appIdentifier App ID of your app on HockeyApp.
   * @param listener Implement for callback functions.
   */
  public static void register(Context context, String urlString, String appIdentifier, CrashManagerListener listener) {
    initialize(context, urlString, appIdentifier, listener, false);
    execute(context, listener);
  }

  /**
   * Initializes the crash manager, but does not handle crash log. Use this 
   * method only if you want to split the process into two parts, i.e. when
   * your app has multiple entry points. You need to call the method 'execute' 
   * at some point after this method. 
   * 
   * @param context The context to use. Usually your Activity object.
   * @param appIdentifier App ID of your app on HockeyApp.
   * @param listener Implement for callback functions.
   */
  public static void initialize(Context context, String appIdentifier, CrashManagerListener listener) {
    initialize(context, Constants.BASE_URL, appIdentifier, listener, true);
  }

  /**
   * Initializes the crash manager, but does not handle crash log. Use this 
   * method only if you want to split the process into two parts, i.e. when
   * your app has multiple entry points. You need to call the method 'execute' 
   * at some point after this method. 
   * 
   * @param context The context to use. Usually your Activity object.
   * @param urlString URL of the HockeyApp server.
   * @param appIdentifier App ID of your app on HockeyApp.
   * @param listener Implement for callback functions.
   */
  public static void initialize(Context context, String urlString, String appIdentifier, CrashManagerListener listener) {
    initialize(context, urlString, appIdentifier, listener, true);
  }

  /**
   * Executes the crash manager. You need to call this method if you have used
   * the method 'initialize' before.
   * 
   * @param context The context to use. Usually your Activity object.
   * @param listener Implement for callback functions.
   */
  @SuppressWarnings("deprecation")
  public static void execute(Context context, CrashManagerListener listener) {
    Boolean ignoreDefaultHandler = (listener != null) && (listener.ignoreDefaultHandler());
    WeakReference<Context> weakContext = new WeakReference<Context>(context);
    
    int foundOrSend = hasStackTraces(weakContext);
    if (foundOrSend == 1) {
      Boolean autoSend = false;
      if (listener != null) {
        autoSend |= listener.shouldAutoUploadCrashes();
        autoSend |= listener.onCrashesFound();
        
        listener.onNewCrashesFound();
      }
      
      if (!autoSend) {
        showDialog(weakContext, listener, ignoreDefaultHandler);
      }
      else {
        sendCrashes(weakContext, listener, ignoreDefaultHandler);
      }
    }
    else if (foundOrSend == 2) {
      if (listener != null) {
        listener.onConfirmedCrashesFound();
      }
      
      sendCrashes(weakContext, listener, ignoreDefaultHandler);
    }
    else {
      registerHandler(weakContext, listener, ignoreDefaultHandler);
    }
  }
  
  /**
   * Checks if there are any saved stack traces in the files dir.
   * 
   * @param context The context to use. Usually your Activity object.
   * @return 0 if there are no stack traces,
   *         1 if there are any new stack traces, 
   *         2 if there are confirmed stack traces
   */
  public static int hasStackTraces(WeakReference<Context> weakContext) {
    String[] filenames = searchForStackTraces();
    List<String> confirmedFilenames = null;
    int result = 0;
    if ((filenames != null) && (filenames.length > 0)) {
      try {
        Context context = null;
        if (weakContext != null) {
          context = weakContext.get();
          if (context != null) {
            SharedPreferences preferences = context.getSharedPreferences("HockeySDK", Context.MODE_PRIVATE);
            confirmedFilenames = Arrays.asList(preferences.getString("ConfirmedFilenames", "").split("\\|"));    
          }
        }
        
      }
      catch (Exception e) {
        // Just in case, we catch all exceptions here
      }

      if (confirmedFilenames != null) {
        result = 2;
        
        for (String filename : filenames) {
          if (!confirmedFilenames.contains(filename)) {
            result = 1;
            break;
          }
        }
      }
      else {
        result = 1;
      }
    }

    return result;
  }

  /**
   * Submits all stack traces in the files dir to HockeyApp.
   * 
   * @param context The context to use. Usually your Activity object.
   * @param listener Implement for callback functions.
   */
  public static void submitStackTraces(WeakReference<Context> weakContext, CrashManagerListener listener) {
    String[] list = searchForStackTraces();
    Boolean successful = false;

    if ((list != null) && (list.length > 0)) {
      Log.d(Constants.TAG, "Found " + list.length + " stacktrace(s).");

      for (int index = 0; index < list.length; index++) {
        try {
          // Read contents of stack trace
          String filename = list[index];
          String stacktrace = contentsOfFile(weakContext, filename);
          if (stacktrace.length() > 0) {
            // Transmit stack trace with POST request
            Log.d(Constants.TAG, "Transmitting crash data: \n" + stacktrace);
            DefaultHttpClient httpClient = (DefaultHttpClient)ConnectionManager.getInstance().getHttpClient();
            HttpPost httpPost = new HttpPost(getURLString());
            
            List <NameValuePair> parameters = new ArrayList <NameValuePair>(); 
            parameters.add(new BasicNameValuePair("raw", stacktrace));
            parameters.add(new BasicNameValuePair("userID", contentsOfFile(weakContext, filename.replace(".stacktrace", ".user"))));
            parameters.add(new BasicNameValuePair("contact", contentsOfFile(weakContext, filename.replace(".stacktrace", ".contact"))));
            parameters.add(new BasicNameValuePair("description", contentsOfFile(weakContext, filename.replace(".stacktrace", ".description"))));
            parameters.add(new BasicNameValuePair("sdk", Constants.SDK_NAME));
            parameters.add(new BasicNameValuePair("sdk_version", Constants.SDK_VERSION));
            
            httpPost.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
            
            httpClient.execute(httpPost);   
            successful = true;
          }
        }
        catch (Exception e) {
          e.printStackTrace();
        } 
        finally {
          if (successful) {
            deleteStackTrace(weakContext, list[index]);

            if (listener != null) {
              listener.onCrashesSent();
            }
          }
          else {
            if (listener != null) {
              listener.onCrashesNotSent();
            }
          }
        }
      }
    }
  } 

  /**
   * Deletes all stack traces and meta files from files dir.
   * 
   * @param context The context to use. Usually your Activity object.
   */
  public static void deleteStackTraces(WeakReference<Context> weakContext) {
    String[] list = searchForStackTraces();

    if ((list != null) && (list.length > 0)) {
      Log.d(Constants.TAG, "Found " + list.length + " stacktrace(s).");

      for (int index = 0; index < list.length; index++) {
        try {
          Context context = null;
          if (weakContext != null) {
            Log.d(Constants.TAG, "Delete stacktrace " + list[index] + ".");
            deleteStackTrace(weakContext, list[index]);
            
            context = weakContext.get();
            if (context != null) {
              context.deleteFile(list[index]);              
            }
          }
        } 
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
  
  /**
   * Private method to initialize the crash manager. This method has an 
   * additional parameter to decide whether to register the exception handler
   * at the end or not.
   */
  private static void initialize(Context context, String urlString, String appIdentifier, CrashManagerListener listener, boolean registerHandler) {
    if (context != null) {
      CrashManager.urlString = urlString;
      CrashManager.identifier = appIdentifier;
  
      Constants.loadFromContext(context);
      
      if (CrashManager.identifier == null) {
        CrashManager.identifier = Constants.APP_PACKAGE;
      }
      
      if (registerHandler) {
        Boolean ignoreDefaultHandler = (listener != null) && (listener.ignoreDefaultHandler());
        WeakReference<Context> weakContext = new WeakReference<Context>(context); 
        registerHandler(weakContext, listener, ignoreDefaultHandler);
      }
    }
  }

  /**
   * Shows a dialog to ask the user whether he wants to send crash reports to 
   * HockeyApp or delete them.
   */
  private static void showDialog(final WeakReference<Context> weakContext, final CrashManagerListener listener, final boolean ignoreDefaultHandler) {
    Context context = null;
    if (weakContext != null) {
      context = weakContext.get();
    }
    
    if (context == null) {
      return;
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle(Strings.get(listener, Strings.CRASH_DIALOG_TITLE_ID));
    builder.setMessage(Strings.get(listener, Strings.CRASH_DIALOG_MESSAGE_ID));

    builder.setNegativeButton(Strings.get(listener, Strings.CRASH_DIALOG_NEGATIVE_BUTTON_ID), new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        if (listener != null) {
          listener.onUserDeniedCrashes();
        }
        
        deleteStackTraces(weakContext);
        registerHandler(weakContext, listener, ignoreDefaultHandler);
      } 
    });

    builder.setPositiveButton(Strings.get(listener, Strings.CRASH_DIALOG_POSITIVE_BUTTON_ID), new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        sendCrashes(weakContext, listener, ignoreDefaultHandler);
      } 
    });

    builder.create().show();
  }
  
  /**
   * Starts thread to send crashes to HockeyApp, then registers the exception 
   * handler. 
   */
  private static void sendCrashes(final WeakReference<Context> weakContext, final CrashManagerListener listener, final boolean ignoreDefaultHandler) {
    saveConfirmedStackTraces(weakContext);
    
    new Thread() {
      @Override
      public void run() {
        submitStackTraces(weakContext, listener);
        registerHandler(weakContext, listener, ignoreDefaultHandler);
      }
    }.start();
  }

  /**
   * Registers the exception handler. 
   */
  private static void registerHandler(WeakReference<Context> weakContext, CrashManagerListener listener, boolean ignoreDefaultHandler) {
    if ((Constants.APP_VERSION != null) && (Constants.APP_PACKAGE != null)) {
      // Get current handler
      UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
      if (currentHandler != null) {
        Log.d(Constants.TAG, "Current handler class = " + currentHandler.getClass().getName());
      }
  
      // Update listener if already registered, otherwise set new handler 
      if (currentHandler instanceof ExceptionHandler) {
        ((ExceptionHandler)currentHandler).setListener(listener);
      }
      else {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(currentHandler, listener, ignoreDefaultHandler));
      }
    }
    else {
      Log.d(Constants.TAG, "Exception handler not set because version or package is null.");
    }
  }

  /**
   * Returns the complete URL for the HockeyApp API. 
   */
  private static String getURLString() {
    return urlString + "api/2/apps/" + identifier + "/crashes/";      
  }
  
  /**
   * Deletes the give filename and all corresponding files (same name, 
   * different extension).
   */
  private static void deleteStackTrace(WeakReference<Context> weakContext, String filename) {
    Context context = null;
    if (weakContext != null) {
      context = weakContext.get();
      if (context != null) {
        context.deleteFile(filename);
        
        String user = filename.replace(".stacktrace", ".user");
        context.deleteFile(user);
        
        String contact = filename.replace(".stacktrace", ".contact");
        context.deleteFile(contact);
        
        String description = filename.replace(".stacktrace", ".description");
        context.deleteFile(description);        
      }
    }
  }

  /**
   * Returns the content of a file as a string. 
   */
  private static String contentsOfFile(WeakReference<Context> weakContext, String filename) {
    Context context = null;
    if (weakContext != null) {
      context = weakContext.get();
      if (context != null) {
        StringBuilder contents = new StringBuilder();
        BufferedReader reader = null;
        try {
          reader = new BufferedReader(new InputStreamReader(context.openFileInput(filename)));
          String line = null;
          while ((line = reader.readLine()) != null) {
            contents.append(line);
            contents.append(System.getProperty("line.separator"));
          }
        }
        catch (FileNotFoundException e) {
        }
        catch (IOException e) {
          e.printStackTrace();
        }
        finally {
          if (reader != null) {
            try { 
              reader.close(); 
            } 
            catch (IOException ignored) { 
            }
          }
        }
        
        return contents.toString();        
      }
    }
    
    return null;
  }
  
  /**
   * Saves the list of the stack traces' file names in shared preferences.  
   */
  private static void saveConfirmedStackTraces(WeakReference<Context> weakContext) {
    Context context = null;
    if (weakContext != null) {
      context = weakContext.get();
      if (context != null) {
        try {
          String[] filenames = searchForStackTraces();
          SharedPreferences preferences = context.getSharedPreferences("HockeySDK", Context.MODE_PRIVATE);
          Editor editor = preferences.edit();
          editor.putString("ConfirmedFilenames", joinArray(filenames, "|"));
          PrefsUtil.applyChanges(editor);
        }
        catch (Exception e) {
          // Just in case, we catch all exceptions here
        }
      }
    }
  }
  
  /**
   * Returns a string created by each element of the array, separated by 
   * delimiter. 
   */
  private static String joinArray(String[] array, String delimiter) {
    StringBuffer buffer = new StringBuffer();
    for (int index = 0; index < array.length; index++) {
      buffer.append(array[index]);
      if (index < array.length - 1) {
        buffer.append(delimiter);
      }
    }
    return buffer.toString();
  }

  /**
   * Searches .stacktrace files and returns then as array. 
   */
  private static String[] searchForStackTraces() {
    if (Constants.FILES_PATH != null) {
      Log.d(Constants.TAG, "Looking for exceptions in: " + Constants.FILES_PATH);
  
      // Try to create the files folder if it doesn't exist
      File dir = new File(Constants.FILES_PATH + "/");
      boolean created = dir.mkdir();
      if (!created && !dir.exists()) {
        return new String[0];
      }
  
      // Filter for ".stacktrace" files
      FilenameFilter filter = new FilenameFilter() { 
        public boolean accept(File dir, String name) {
          return name.endsWith(".stacktrace"); 
        } 
      }; 
      return dir.list(filter);
    }
    else {
      Log.d(Constants.TAG, "Can't search for exception as file path is null.");
      return null;
    }
  }
}