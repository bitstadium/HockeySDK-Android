package net.hockeyapp.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import net.hockeyapp.android.objects.CrashDetails;
import net.hockeyapp.android.objects.CrashManagerUserInput;
import net.hockeyapp.android.objects.CrashMetaData;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.HttpURLConnectionBuilder;
import net.hockeyapp.android.utils.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * <h3>Description</h3>
 *
 * The crash manager sets an exception handler to catch all unhandled
 * exceptions. The handler writes the stack trace and additional meta data to
 * a file. If it finds one or more of these files at the next start, it shows
 * an alert dialog to ask the user if he want the send the crash data to
 * HockeyApp.
 *
 **/
public class CrashManager {
    /**
     * App identifier from HockeyApp.
     */
    private static String identifier = null;

    /**
     * Weak reference to the context.
     */
    static WeakReference<Context> weakContext;

    /**
     * URL of HockeyApp service.
     */
    private static String urlString = null;

    private static long initializeTimestamp;

    private static boolean didCrashInLastSession = false;

    /**
     * Lock used to wait last session crash info.
     */
    static CountDownLatch latch = new CountDownLatch(1);

    /**
     * Shared preferences key for always send dialog button.
     */
    private static final String ALWAYS_SEND_KEY = "always_send_crash_reports";

    private static final String PREFERENCES_NAME = "HockeySDK";
    private static final String CONFIRMED_FILENAMES_KEY = "ConfirmedFilenames";

    private static final int STACK_TRACES_FOUND_NONE = 0;
    private static final int STACK_TRACES_FOUND_NEW = 1;
    private static final int STACK_TRACES_FOUND_CONFIRMED = 2;

    /**
     * Registers new crash manager and handles existing crash logs.
     * HockeyApp App Identifier is read from configuration values in AndroidManifest.xml
     *
     * @param context The context to use. Usually your Activity object. If
     *                context is not an instance of Activity (or a subclass of it),
     *                crashes will be sent automatically.
     */
    public static void register(Context context) {
        String appIdentifier = Util.getAppIdentifier(context);
        register(context, appIdentifier);
    }

    /**
     * Registers new crash manager and handles existing crash logs.
     * HockeyApp App Identifier is read from configuration values in AndroidManifest.xml
     *
     * @param context   The context to use. Usually your Activity object. If
     *                  context is not an instance of Activity (or a subclass of it),
     *                  crashes will be sent automatically.
     * @param listener  Implement for callback functions.
     */
    @SuppressWarnings("unused")
    public static void register(Context context, CrashManagerListener listener) {
        String appIdentifier = Util.getAppIdentifier(context);
        register(context, appIdentifier, listener);
    }

    /**
     * Registers new crash manager and handles existing crash logs. If
     * context is not an instance of Activity (or a subclass of it),
     * crashes will be sent automatically.
     *
     * @param context       The context to use. Usually your Activity object.
     * @param appIdentifier App ID of your app on HockeyApp.
     */
    @SuppressWarnings("WeakerAccess")
    public static void register(Context context, String appIdentifier) {
        register(context, Constants.BASE_URL, appIdentifier, null);
    }

    /**
     * Registers new crash manager and handles existing crash logs. If
     * context is not an instance of Activity (or a subclass of it),
     * crashes will be sent automatically.
     *
     * @param context       The context to use. Usually your Activity object.
     * @param appIdentifier App ID of your app on HockeyApp.
     * @param listener      Implement for callback functions.
     */
    @SuppressWarnings("WeakerAccess")
    public static void register(Context context, String appIdentifier, CrashManagerListener listener) {
        register(context, Constants.BASE_URL, appIdentifier, listener);
    }

    /**
     * Registers new crash manager and handles existing crash logs. If
     * context is not an instance of Activity (or a subclass of it),
     * crashes will be sent automatically.
     *
     * @param context       The context to use. Usually your Activity object.
     * @param urlString     URL of the HockeyApp server.
     * @param appIdentifier App ID of your app on HockeyApp.
     * @param listener      Implement for callback functions.
     */
    @SuppressWarnings("WeakerAccess")
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
     * @param context       The context to use. Usually your Activity object.
     * @param appIdentifier App ID of your app on HockeyApp.
     * @param listener      Implement for callback functions.
     */
    @SuppressWarnings("unused")
    public static void initialize(Context context, String appIdentifier, CrashManagerListener listener) {
        initialize(context, Constants.BASE_URL, appIdentifier, listener, true);
    }

    /**
     * Initializes the crash manager, but does not handle crash log. Use this
     * method only if you want to split the process into two parts, i.e. when
     * your app has multiple entry points. You need to call the method 'execute'
     * at some point after this method.
     *
     * @param context       The context to use. Usually your Activity object.
     * @param urlString     URL of the HockeyApp server.
     * @param appIdentifier App ID of your app on HockeyApp.
     * @param listener      Implement for callback functions.
     */
    @SuppressWarnings("unused")
    public static void initialize(Context context, String urlString, String appIdentifier, CrashManagerListener listener) {
        initialize(context, urlString, appIdentifier, listener, true);
    }

    /**
     * Executes the crash manager. You need to call this method if you have used
     * the method 'initialize' before. If context is not an instance of Activity
     * (or a subclass of it), crashes will be sent automatically.
     *
     * @param context  The context to use. Usually your Activity object.
     * @param listener Implement for callback functions.
     */
    public static void execute(Context context, final CrashManagerListener listener) {
        final WeakReference<Context> weakContext = new WeakReference<>(context);
        AsyncTaskUtils.execute(new AsyncTask<Void, Object, Integer>() {
            private boolean autoSend = false;

            @Override
            protected Integer doInBackground(Void... voids) {
                Context context = weakContext.get();
                if (context != null) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    autoSend |= prefs.getBoolean(ALWAYS_SEND_KEY, false);
                }
                int foundOrSend = hasStackTraces(weakContext);
                didCrashInLastSession = foundOrSend == STACK_TRACES_FOUND_NEW;
                latch.countDown();
                return foundOrSend;
            }

            @Override
            protected void onPostExecute(Integer foundOrSend) {
                boolean autoSend = this.autoSend;
                boolean ignoreDefaultHandler = (listener != null) && (listener.ignoreDefaultHandler());

                if (foundOrSend == STACK_TRACES_FOUND_NEW) {
                    if (listener != null) {
                        autoSend |= listener.shouldAutoUploadCrashes();
                        listener.onNewCrashesFound();
                    }

                    if (autoSend || !showDialog(weakContext, listener, ignoreDefaultHandler)) {
                        sendCrashes(weakContext, listener, ignoreDefaultHandler, null);
                    }
                } else if (foundOrSend == STACK_TRACES_FOUND_CONFIRMED) {
                    if (listener != null) {
                        listener.onConfirmedCrashesFound();
                    }

                    sendCrashes(weakContext, listener, ignoreDefaultHandler, null);
                } else if (foundOrSend == STACK_TRACES_FOUND_NONE) {
                    if (listener != null) {
                        listener.onNoCrashesFound();
                    }

                    registerHandler(listener, ignoreDefaultHandler);
                }
            }
        });
    }

    /**
     * Checks if there are any saved stack traces in the files dir.
     *
     * @param weakContext The context to use. Usually your Activity object.
     * @return STACK_TRACES_FOUND_NONE if there are no stack traces,
     * STACK_TRACES_FOUND_NEW if there are any new stack traces,
     * STACK_TRACES_FOUND_CONFIRMED if there only are confirmed stack traces.
     */
    @SuppressWarnings("WeakerAccess")
    public static int hasStackTraces(final WeakReference<Context> weakContext) {
        String[] filenames = searchForStackTraces(weakContext);
        List<String> confirmedFilenames = null;
        int result = STACK_TRACES_FOUND_NONE;
        if ((filenames != null) && (filenames.length > 0)) {
            try {
                Context context = weakContext != null ? weakContext.get() : null;
                if (context != null) {
                    SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
                    confirmedFilenames = Arrays.asList(preferences.getString(CONFIRMED_FILENAMES_KEY, "").split("\\|"));
                }
            } catch (Exception ignored) {
            }

            if (confirmedFilenames != null) {
                result = STACK_TRACES_FOUND_CONFIRMED;

                for (String filename : filenames) {
                    if (!confirmedFilenames.contains(filename)) {
                        result = STACK_TRACES_FOUND_NEW;
                        break;
                    }
                }
            } else {
                result = STACK_TRACES_FOUND_NEW;
            }
        }

        return result;
    }

    @SuppressWarnings("WeakerAccess")
    public static Future<Boolean> didCrashInLastSession() {
        return AsyncTaskUtils.execute(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                latch.await();
                return didCrashInLastSession;
            }
        });
    }

    @SuppressWarnings("WeakerAccess")
    public static Future<CrashDetails> getLastCrashDetails(final Context context) {
        final WeakReference<Context> weakContext = new WeakReference<>(context);
        return AsyncTaskUtils.execute(new Callable<CrashDetails>() {

            @Override
            public CrashDetails call() throws Exception {
                latch.await();
                if (!didCrashInLastSession) {
                    return null;
                }

                Context context = weakContext.get();
                if (context == null) {
                    return null;
                }

                File dir = context.getFilesDir();
                if (dir == null) {
                    return null;
                }
                File[] files = dir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.endsWith(".stacktrace");
                    }
                });

                long lastModification = 0;
                File lastModifiedFile = null;
                CrashDetails result = null;
                for (File file : files) {
                    if (file.lastModified() > lastModification) {
                        lastModification = file.lastModified();
                        lastModifiedFile = file;
                    }
                }

                if (lastModifiedFile != null && lastModifiedFile.exists()) {
                    try {
                        result = CrashDetails.fromFile(lastModifiedFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                return result;
            }
        });
    }

    /**
     * Submits all stack traces in the files dir to HockeyApp.
     *
     * @param weakContext The context to use. Usually your Activity object.
     * @param listener Implement for callback functions.
     */
    @SuppressWarnings("unused")
    public static void submitStackTraces(final WeakReference<Context> weakContext, CrashManagerListener listener) {
        submitStackTraces(weakContext, listener, null);
    }

    /**
     * Submits all stack traces in the files dir to HockeyApp.
     *
     * @param weakContext The context to use. Usually your Activity object.
     * @param listener      Implement for callback functions.
     * @param crashMetaData The crashMetaData, provided by the user.
     */
    @SuppressWarnings("WeakerAccess")
    public static synchronized void submitStackTraces(final WeakReference<Context> weakContext, CrashManagerListener listener, CrashMetaData crashMetaData) {
        String[] list = searchForStackTraces(weakContext);
        if (list != null && list.length > 0) {
            HockeyLog.debug("Found " + list.length + " stacktrace(s).");
            for (String file : list) {
                submitStackTrace(weakContext, file, listener, crashMetaData);
            }
        }
    }

    private static void submitStackTrace(final WeakReference<Context> weakContext, String filename, CrashManagerListener listener, CrashMetaData crashMetaData) {
        Boolean successful = false;
        HttpURLConnection urlConnection = null;
        try {
            String stacktrace = contentsOfFile(weakContext, filename);
            if (stacktrace.length() > 0) {
                // Transmit stack trace with POST request

                HockeyLog.debug("Transmitting crash data: \n" + stacktrace);

                // Retrieve user ID and contact information if given
                String userID = contentsOfFile(weakContext, filename.replace(".stacktrace", ".user"));
                String contact = contentsOfFile(weakContext, filename.replace(".stacktrace", ".contact"));

                if (crashMetaData != null) {
                    final String crashMetaDataUserID = crashMetaData.getUserID();
                    if (!TextUtils.isEmpty(crashMetaDataUserID)) {
                        userID = crashMetaDataUserID;
                    }
                    final String crashMetaDataContact = crashMetaData.getUserEmail();
                    if (!TextUtils.isEmpty(crashMetaDataContact)) {
                        contact = crashMetaDataContact;
                    }
                }

                // Append application log to user provided description if present, if not, just send application log
                final String applicationLog = contentsOfFile(weakContext, filename.replace(".stacktrace", ".description"));
                String description = crashMetaData != null ? crashMetaData.getUserDescription() : "";
                if (!TextUtils.isEmpty(applicationLog)) {
                    if (!TextUtils.isEmpty(description)) {
                        description = String.format("%s\n\nLog:\n%s", description, applicationLog);
                    } else {
                        description = String.format("Log:\n%s", applicationLog);
                    }
                }

                Map<String, String> parameters = new HashMap<>();

                parameters.put("raw", stacktrace);
                parameters.put("userID", userID);
                parameters.put("contact", contact);
                parameters.put("description", description);
                parameters.put("sdk", Constants.SDK_NAME);
                parameters.put("sdk_version", BuildConfig.VERSION_NAME);

                urlConnection = new HttpURLConnectionBuilder(getURLString())
                        .setRequestMethod("POST")
                        .writeFormFields(parameters)
                        .build();

                int responseCode = urlConnection.getResponseCode();

                successful = (responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_CREATED);

            }
        } catch (Exception e) {
            HockeyLog.error("Failed to transmit crash data", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (successful) {
                HockeyLog.debug("Transmission succeeded");
                deleteStackTrace(weakContext, filename);

                if (listener != null) {
                    listener.onCrashesSent();
                    deleteRetryCounter(weakContext, filename);
                }
            } else {
                HockeyLog.debug("Transmission failed, will retry on next register() call");
                if (listener != null) {
                    listener.onCrashesNotSent();
                    updateRetryCounter(weakContext, filename, listener.getMaxRetryAttempts());
                }
            }
        }
    }

    /**
     * Deletes all stack traces and meta files from files dir.
     *
     * @param weakContext The context to use. Usually your Activity object.
     */
    @SuppressWarnings("WeakerAccess")
    public static void deleteStackTraces(final WeakReference<Context> weakContext) {
        String[] list = searchForStackTraces(weakContext);
        if (list != null && list.length > 0) {
            HockeyLog.debug("Found " + list.length + " stacktrace(s).");

            for (String file : list) {
                try {
                    Context context;
                    if (weakContext != null) {
                        HockeyLog.debug("Delete stacktrace " + file + ".");
                        deleteStackTrace(weakContext, file);

                        context = weakContext.get();
                        if (context != null) {
                            context.deleteFile(file);
                        }
                    }
                } catch (Exception e) {
                    HockeyLog.error("Failed to delete stacktrace", e);
                }
            }
        }
    }

    /**
     * Provides an interface to pass user input from a custom alert to a crash report
     *
     * @param userInput            Defines the users action whether to send, always send, or not to send the crash report.
     * @param userProvidedMetaData The content of this optional CrashMetaData instance will be attached to the crash report
     *                             and allows to ask the user for e.g. additional comments or info.
     * @param listener             an optional crash manager listener to use.
     * @param weakContext          The context to use. Usually your Activity object.
     * @param ignoreDefaultHandler whether to ignore the default exception handler.
     * @return true if the input is a valid option and successfully triggered further processing of the crash report.
     * @see CrashManagerUserInput
     * @see CrashMetaData
     * @see CrashManagerListener
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean handleUserInput(final CrashManagerUserInput userInput,
                                          final CrashMetaData userProvidedMetaData, final CrashManagerListener listener,
                                          final WeakReference<Context> weakContext, final boolean ignoreDefaultHandler) {
        switch (userInput) {
            case CrashManagerUserInputDontSend:
                if (listener != null) {
                    listener.onUserDeniedCrashes();
                }

                registerHandler(listener, ignoreDefaultHandler);
                AsyncTaskUtils.execute(new AsyncTask<Void, Object, Object>() {
                    @Override
                    protected Object doInBackground(Void... voids) {
                        deleteStackTraces(weakContext);
                        return null;
                    }
                });
                return true;
            case CrashManagerUserInputAlwaysSend:
                Context context = weakContext != null ? weakContext.get() : null;
                if (context == null) {
                    return false;
                }

                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                prefs.edit().putBoolean(ALWAYS_SEND_KEY, true).apply();

                sendCrashes(weakContext, listener, ignoreDefaultHandler, userProvidedMetaData);
                return true;
            case CrashManagerUserInputSend:
                sendCrashes(weakContext, listener, ignoreDefaultHandler, userProvidedMetaData);
                return true;
            default:
                return false;
        }
    }

    /**
     * Clears the preference to always send crashes. The next time the user
     * sees a crash and restarts the app, they will see the dialog again to
     * send the crash.
     *
     * @param weakContext The context to use. Usually your Activity object.
     */
    @SuppressWarnings("unused")
    public static void resetAlwaysSend(final WeakReference<Context> weakContext) {
        Context context = weakContext != null ? weakContext.get() : null;
        if (context != null) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().remove(ALWAYS_SEND_KEY).apply();
        }
    }

    /**
     * Private method to initialize the crash manager. This method has an
     * additional parameter to decide whether to register the exception handler
     * at the end or not.
     */
    private static void initialize(Context context, String urlString, String appIdentifier, CrashManagerListener listener, boolean registerHandler) {
        if (context != null) {
            if (CrashManager.initializeTimestamp == 0) {
                CrashManager.initializeTimestamp = System.currentTimeMillis();
            }
            CrashManager.urlString = urlString;
            CrashManager.identifier = Util.sanitizeAppIdentifier(appIdentifier);
            CrashManager.didCrashInLastSession = false;
            CrashManager.weakContext = new WeakReference<>(context);

            Constants.loadFromContext(context);
            
            if (CrashManager.identifier == null) {
                CrashManager.identifier = Constants.APP_PACKAGE;
            }

            if (registerHandler) {
                boolean ignoreDefaultHandler = (listener != null) && (listener.ignoreDefaultHandler());
                registerHandler(listener, ignoreDefaultHandler);
            }
        }
    }

    /**
     * Shows a dialog to ask the user whether he wants to send crash reports to
     * HockeyApp or delete them.
     */
    private static boolean showDialog(final WeakReference<Context> weakContext, final CrashManagerListener listener, final boolean ignoreDefaultHandler) {
        if (listener != null && listener.onHandleAlertView()) {
            return true;
        }

        Context context = weakContext != null ? weakContext.get() : null;
        if (context == null || !(context instanceof Activity)) {
            return false;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String alertTitle = getAlertTitle(context);
        builder.setTitle(alertTitle);
        builder.setMessage(R.string.hockeyapp_crash_dialog_message);

        builder.setNegativeButton(R.string.hockeyapp_crash_dialog_negative_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                handleUserInput(CrashManagerUserInput.CrashManagerUserInputDontSend, null, listener, weakContext, ignoreDefaultHandler);
            }
        });

        builder.setNeutralButton(R.string.hockeyapp_crash_dialog_neutral_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                handleUserInput(CrashManagerUserInput.CrashManagerUserInputAlwaysSend, null, listener, weakContext, ignoreDefaultHandler);
            }
        });

        builder.setPositiveButton(R.string.hockeyapp_crash_dialog_positive_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                handleUserInput(CrashManagerUserInput.CrashManagerUserInputSend, null, listener, weakContext, ignoreDefaultHandler);
            }
        });

        builder.create().show();
        return true;
    }

    private static String getAlertTitle(Context context) {
        return context.getString(R.string.hockeyapp_crash_dialog_title, Util.getAppName(context));
    }

    /**
     * Starts thread to send crashes to HockeyApp, then registers the exception
     * handler.
     */
    private static void sendCrashes(final WeakReference<Context> weakContext, final CrashManagerListener listener, final boolean ignoreDefaultHandler, final CrashMetaData crashMetaData) {
        registerHandler(listener, ignoreDefaultHandler);
        Context context = weakContext != null ? weakContext.get() : null;
        final boolean isConnectedToNetwork = context != null && Util.isConnectedToNetwork(context);

        // Not connected to network, not trying to submit stack traces
        if (!isConnectedToNetwork && listener != null) {
            listener.onCrashesNotSent();
        }

        AsyncTaskUtils.execute(new AsyncTask<Void, Object, Object>() {
            @Override
            protected Object doInBackground(Void... voids) {
                final String[] list = searchForStackTraces(weakContext);
                if (list != null) {
                    saveConfirmedStackTraces(weakContext, list);
                    if (isConnectedToNetwork) {
                        for (String file : list) {
                            submitStackTrace(weakContext, file, listener, crashMetaData);
                        }
                    }
                }
                return null;
            }
        });
    }

    /**
     * Registers the exception handler.
     */
    private static void registerHandler(CrashManagerListener listener, boolean ignoreDefaultHandler) {
        if (!TextUtils.isEmpty(Constants.APP_VERSION) && !TextUtils.isEmpty(Constants.APP_PACKAGE)) {
            // Get current handler
            UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
            if (currentHandler != null) {
                HockeyLog.debug("Current handler class = " + currentHandler.getClass().getName());
            }

            // Update listener if already registered, otherwise set new handler
            if (currentHandler instanceof ExceptionHandler) {
                ((ExceptionHandler) currentHandler).setListener(listener);
            } else {
                Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(currentHandler, listener, ignoreDefaultHandler));
            }
        } else {
            HockeyLog.debug("Exception handler not set because version or package is null.");
        }
    }

    /**
     * Returns the complete URL for the HockeyApp API.
     */
    private static String getURLString() {
        return urlString + "api/2/apps/" + identifier + "/crashes/";
    }

    /**
     * Update the retry attempts count for this crash stacktrace.
     */
    private static void updateRetryCounter(final WeakReference<Context> weakContext, String filename, int maxRetryAttempts) {
        if (maxRetryAttempts == -1) {
            return;
        }

        Context context = weakContext != null ? weakContext.get() : null;
        if (context != null) {
            SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            int retryCounter = preferences.getInt("RETRY_COUNT: " + filename, 0);
            if (retryCounter >= maxRetryAttempts) {
                deleteStackTrace(weakContext, filename);
                deleteRetryCounter(weakContext, filename);
            } else {
                editor.putInt("RETRY_COUNT: " + filename, retryCounter + 1);
                editor.apply();
            }
        }
    }

    /**
     * Delete the retry counter if stacktrace is uploaded or retry limit is
     * reached.
     */
    private static void deleteRetryCounter(final WeakReference<Context> weakContext, String filename) {
        Context context = weakContext != null ? weakContext.get() : null;
        if (context != null) {
            SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove("RETRY_COUNT: " + filename);
            editor.apply();
        }
    }

    /**
     * Deletes the give filename and all corresponding files (same name,
     * different extension).
     */
    private static void deleteStackTrace(final WeakReference<Context> weakContext, String filename) {
        Context context = weakContext != null ? weakContext.get() : null;
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

    /**
     * Returns the content of a file as a string.
     */
    private static String contentsOfFile(final WeakReference<Context> weakContext, String filename) {
        Context context = weakContext != null ? weakContext.get() : null;
        if (context != null) {
            File file = context.getFileStreamPath(filename);
            if(file == null || !file.exists()) {
                return "";
            }
            StringBuilder contents = new StringBuilder();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(context.openFileInput(filename)));
                String line;
                while ((line = reader.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } catch (IOException e) {
                HockeyLog.error("Failed to read content of " + filename, e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            return contents.toString();
        }
        return "";
    }

    /**
     * Saves the list of the stack traces' file names in shared preferences.
     */
    private static void saveConfirmedStackTraces(final WeakReference<Context> weakContext, String[] stackTraces) {
        Context context = weakContext != null ? weakContext.get() : null;
        if (context != null) {
            try {
                SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
                Editor editor = preferences.edit();
                editor.putString(CONFIRMED_FILENAMES_KEY, TextUtils.join(",", stackTraces));
                editor.apply();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Searches .stacktrace files and returns them as array.
     */
    private static String[] searchForStackTraces(final WeakReference<Context> weakContext) {
        Context context = weakContext != null ? weakContext.get() : null;
        if (context != null) {
            File dir = context.getFilesDir();
            if (dir != null) {
                HockeyLog.debug("Looking for exceptions in: " + dir.getAbsolutePath());
                if (!dir.exists() && !dir.mkdir()) {
                    return new String[0];
                }

                // Filter for ".stacktrace" files
                FilenameFilter filter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".stacktrace");
                    }
                };
                return dir.list(filter);
            } else {
                HockeyLog.debug("Can't search for exception as file path is null.");
            }
        }
        return null;
    }

    @SuppressWarnings("WeakerAccess")
    public static long getInitializeTimestamp() {
        return initializeTimestamp;
    }
}
