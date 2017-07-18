package net.hockeyapp.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.WorkerThread;

import net.hockeyapp.android.tasks.LoginTask;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.Util;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * <h3>Description</h3>
 *
 * The LoginManager displays the auth activity.
 *
 **/
public class LoginManager {
    /**
     * The default mode for authorization. User's won't be authorized.
     */
    public static final int LOGIN_MODE_ANONYMOUS = 0;

    /**
     * Testers/users need a HockeyApp account and have to provide their email address to use the app.
     */
    public static final int LOGIN_MODE_EMAIL_ONLY = 1;

    /**
     * Testers/users need a HockeyApp account and have to provide their email address and password to use the app.
     */
    public static final int LOGIN_MODE_EMAIL_PASSWORD = 2;

    /**
     * Same as LOGIN_MODE_EMAIL_PASSWORD and HockeySDK will check if the user has access to the app.
     */
    public static final int LOGIN_MODE_VALIDATE = 3;

    /**
     * The key for the intent of the main activity.
     */
    static final String LOGIN_EXIT_KEY = "net.hockeyapp.android.EXIT";

    /**
     * The entry activity of this app.
     */
    static Class<?> mainActivity;

    /**
     * Optional listener to handler callbacks.
     */
    static LoginManagerListener listener;

    /**
     * App identifier from HockeyApp.
     */
    private static String identifier = null;

    /**
     * App secret from HockeyApp.
     */
    private static String secret = null;

    /**
     * Handler for uid validation.
     */
    private static Handler validateHandler = null;

    /**
     * URL of HockeyApp service
     */
    private static String urlString = null;

    /**
     * The Login Mode.
     */
    private static int mode;

    /**
     * Registers new login manager.
     * HockeyApp App Identifier is read from configuration values in AndroidManifest.xml.
     *
     * @param context   The context to use. Usually your Activity object. Has to be
     *                  of class Activity or subclass for interactive login.
     * @param appSecret The App Secret of your app on HockeyApp.
     * @param mode      The login mode to use.
     */
    @SuppressWarnings("unused")
    public static void register(final Context context, String appSecret, int mode) {
        String appIdentifier = Util.getAppIdentifier(context);
        register(context, appIdentifier, appSecret, mode, (Class<?>) null);
    }

    /**
     * Registers new login manager.
     * HockeyApp App Identifier is read from configuration values in AndroidManifest.xml.
     *
     * @param context   The context to use. Usually your Activity object. Has to be
     *                  of class Activity or subclass for interactive login.
     * @param appSecret The App Secret of your app on HockeyApp.
     * @param mode      The login mode to use.
     * @param listener  Instance of LoginListener
     */
    @SuppressWarnings("unused")
    public static void register(final Context context, String appSecret, int mode, LoginManagerListener listener) {
        String appIdentifier = Util.getAppIdentifier(context);
        register(context, appIdentifier, appSecret, mode, listener);
    }

    /**
     * Registers new LoginManager.
     *
     * @param context       The context to use. Usually your Activity object.
     * @param appIdentifier The App ID of your app on HockeyApp.
     * @param appSecret     The App Secret of your app on HockeyApp.
     * @param mode          The login mode.
     * @param listener      Instance of LoginListener
     */
    public static void register(final Context context, String appIdentifier, String appSecret, int mode, LoginManagerListener listener) {
        LoginManager.listener = listener;
        register(context, appIdentifier, appSecret, mode, (Class<?>) null);
    }

    /**
     * Registers new LoginManager.
     *
     * @param context       The context to use. Usually your Activity object.
     * @param appIdentifier App ID of your app on HockeyApp.
     * @param appSecret     The App Secret of your app on HockeyApp.
     * @param mode          The Login Mode.
     * @param activity      The first activity to be started by your app.
     */
    @SuppressWarnings("WeakerAccess")
    public static void register(final Context context, String appIdentifier, String appSecret, int mode, Class<?> activity) {
        register(context, appIdentifier, appSecret, Constants.BASE_URL, mode, activity);
    }

    /**
     * Registers new LoginManager.
     *
     * @param context       The context to use. Usually your Activity object.
     * @param appIdentifier App ID of your app on HockeyApp.
     * @param appSecret     The App Secret of your app on HockeyApp.
     * @param urlString     The URL of the HockeyApp service
     * @param mode          The Login Mode.
     * @param activity      The first activity to be started by your app.
     */
    @SuppressWarnings("WeakerAccess")
    public static void register(final Context context, String appIdentifier, String appSecret, String urlString, int mode, Class<?> activity) {
        if (context != null) {
            LoginManager.identifier = Util.sanitizeAppIdentifier(appIdentifier);
            LoginManager.secret = appSecret;
            LoginManager.urlString = urlString;
            LoginManager.mode = mode;
            LoginManager.mainActivity = activity;

            if (LoginManager.validateHandler == null) {
                LoginManager.validateHandler = new LoginHandler(context);
            }

            Constants.loadFromContext(context);
        }
    }

    /**
     * Checks the authentication status. If not authenticated at all it will start the LoginActivity.
     * If the user has authenticated before, the SDK will not check for authorization again or validate the user's
     * access to the app. In case of LOGIN_MODE_VALIDATE, it will verify if the user is still allowed to use this app.
     * In case the user tries to navigate back from the login dialog (LoginActivity), it \aAlso exits the app.
     *
     * @param context The activity from which this method is called.
     * @param intent  The intent that the activity has been created with.
     */
    public static void verifyLogin(final Activity context, Intent intent) {
        // Check if application needs to be exited.
        if (intent != null) {
            if (intent.getBooleanExtra(LOGIN_EXIT_KEY, false)) {
                context.finish();
                return;
            }
        }

        //Don't verify anything if we're in LOGIN_MODE_ANONYMOUS
        if (context == null || mode == LOGIN_MODE_ANONYMOUS) {
            return;
        }

        AsyncTaskUtils.execute(new AsyncTask<Void, Object, Object>() {
            private String auid;
            private String iuid;

            @Override
            protected Object doInBackground(Void... voids) {
                //Check if the LOGIN_MODE has changed. Delete IUID and AUID if it has changed.
                //This requires re-authentication.
                SharedPreferences prefs = context.getSharedPreferences("net.hockeyapp.android.login", 0);
                int currentMode = prefs.getInt("mode", -1);
                if (currentMode != mode) {
                    HockeyLog.verbose("HockeyAuth", "Mode has changed, require re-auth.");
                    prefs.edit()
                            .remove("auid")
                            .remove("iuid")
                            .putInt("mode", mode)
                            .apply();
                }

                //Get auth ids and check if we're successfully authenticated.
                auid = prefs.getString("auid", null);
                iuid = prefs.getString("iuid", null);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                boolean notAuthenticated = (auid == null) && (iuid == null);
                boolean auidMissing = (auid == null) && ((mode == LOGIN_MODE_EMAIL_PASSWORD) || mode == LOGIN_MODE_VALIDATE);
                boolean iuidMissing = (iuid == null) && (mode == LOGIN_MODE_EMAIL_ONLY);

                if (notAuthenticated || auidMissing || iuidMissing) {
                    HockeyLog.verbose("HockeyAuth", "Not authenticated or correct ID missing, re-authenticate.");
                    startLoginActivity(context);
                    return;
                }

                //Validate the user's auth data in case LOGIN_MODE_AUTH is set.
                if (mode == LOGIN_MODE_VALIDATE) {
                    HockeyLog.verbose("HockeyAuth", "LOGIN_MODE_VALIDATE, Validate the user's info!");

                    Map<String, String> params = new HashMap<>();
                    if (auid != null) {
                        params.put("type", "auid");
                        params.put("id", auid);
                    } else if (iuid != null) {
                        params.put("type", "iuid");
                        params.put("id", iuid);
                    }

                    LoginTask verifyTask = new LoginTask(context, validateHandler, getURLString(LOGIN_MODE_VALIDATE), LOGIN_MODE_VALIDATE, params);
                    verifyTask.setShowProgressDialog(false);
                    AsyncTaskUtils.execute(verifyTask);
                }
            }
        });
    }

    /**
     * Retrieves the email address that was used for logging in. Returns null if there has not been
     * a successful login.
     *
     * @param context The context to use. Usually your Activity object.
     * @return Email address or null.
     */
    @SuppressWarnings("unused")
    @WorkerThread
    public static String getLoginEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("net.hockeyapp.android.login", 0);
        return prefs.getString("email", null);
    }

    private static void startLoginActivity(Context context) {
        Intent intent = new Intent();
        //In case of LOGIN_MODE_VALIDATE, we have to authenticate with username and password first.
        //So we override the mode variable with LOGIN_MODE_EMAIL_PASSWORD
        Boolean isLoginModeValidate = mode == LOGIN_MODE_VALIDATE;
        int tempMode = (isLoginModeValidate) ? LOGIN_MODE_EMAIL_PASSWORD : mode;
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setClass(context, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_URL, getURLString(tempMode));
        intent.putExtra(LoginActivity.EXTRA_MODE, tempMode);
        intent.putExtra(LoginActivity.EXTRA_SECRET, secret);
        context.startActivity(intent);
    }

    private static String getURLString(int mode) {
        String suffix = "";
        if (mode == LOGIN_MODE_EMAIL_PASSWORD) {
            suffix = "authorize";
        } else if (mode == LOGIN_MODE_EMAIL_ONLY) {
            suffix = "check";
        } else if (mode == LOGIN_MODE_VALIDATE) {
            suffix = "validate";
        }

        return urlString + "api/3/apps/" + identifier + "/identity/" + suffix;
    }

    private static class LoginHandler extends Handler {

        private final WeakReference<Context> mWeakContext;

        LoginHandler(Context context) {
            mWeakContext = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            boolean success = bundle.getBoolean(LoginTask.BUNDLE_SUCCESS);

            Context context = mWeakContext.get();
            if (context == null) {
                return;
            }

            if (!success) {
                startLoginActivity(context);
                //TODO should show a message that user didn't have enough rights?
            }
            else {
                HockeyLog.verbose("HockeyAuth", "We authenticated or verified successfully");
            }
        }
    }
}
