package net.hockeyapp.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import net.hockeyapp.android.tasks.LoginTask;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.PrefsUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * <h3>Description</h3>
 *
 * The LoginManager displays the auth activity.
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
 * @author Patrick Eschenbach
 **/
public class LoginManager {
  public static final int LOGIN_MODE_ANONYMOUS      = 0;
  public static final int LOGIN_MODE_EMAIL_ONLY     = 1;
  public static final int LOGIN_MODE_EMAIL_PASSWORD = 2;
  public static final int LOGIN_MODE_VALIDATE       = 3;

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
   * The Login Mode.
   */
  private static int mode;

  /**
   * Registers new LoginManager.
   *
   * @param context the context to use. Usually your Activity object.
   * @param appIdentifier the App ID of your app on HockeyApp.
   * @param appSecret the App Secret of your app on HockeyApp.
   * @param mode the login mode.
   * @param listener instance of LoginListener
   */
  public static void register(final Context context, String appIdentifier, String appSecret, int mode, LoginManagerListener listener) {
    LoginManager.listener = listener;
    register(context, appIdentifier, appSecret, mode, (Class<?>)null);
  }

  /**
   * Registers new LoginManager.
   *
   * @param context The context to use. Usually your Activity object.
   * @param appIdentifier App ID of your app on HockeyApp.
   * @param appSecret The App Secret of your app on HockeyApp.
   * @param mode The Login Mode.
   * @param activity The first activity to be started by your app.
   */
  public static void register(final Context context, String appIdentifier, String appSecret, int mode, Class<?> activity) {
    if (context != null) {
      LoginManager.identifier = appIdentifier;
      LoginManager.secret = appSecret;
      LoginManager.mode = mode;
      LoginManager.mainActivity = activity;

      if (LoginManager.validateHandler == null) {
        LoginManager.validateHandler = new Handler() {
          @Override
          public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            boolean success = bundle.getBoolean("success");

            if (!success) {
              startLoginActivity(context);
            }
          }
        };
      }

      Constants.loadFromContext(context);
    }
  }

  /**
   * Checks the authentication status. If not authenticated at all it will start the LoginActivity,
   * otherwise it will verify if the user is still allowed to use this app. Also exits the app if the
   * LoginActivity is exited with the back button.
   *
   * @param context The activity from which this method is called.
   * @param intent The intent that the activity has been created with.
   */
  public static void verifyLogin(Activity context, Intent intent) {
    // Check if application needs to be exited.
    if (intent != null) {
      if (intent.getBooleanExtra(LOGIN_EXIT_KEY, false)) {
        context.finish();
        return;
      }
    }

    if (context == null || mode == LOGIN_MODE_ANONYMOUS || mode == LOGIN_MODE_VALIDATE) {
      return;
    }

    SharedPreferences prefs = context.getSharedPreferences("net.hockeyapp.android.login", 0);
    int currentMode = prefs.getInt("mode", -1);
    if (currentMode != mode) {
      PrefsUtil.applyChanges(prefs.edit()
          .remove("auid")
          .remove("iuid")
          .putInt("mode", mode));
    }

    String auid = prefs.getString("auid", null);
    String iuid = prefs.getString("iuid", null);

    boolean notAuthenticated = auid == null && iuid == null;
    boolean auidMissing      = auid == null && mode == LOGIN_MODE_EMAIL_PASSWORD;
    boolean iuidMissing      = iuid == null && mode == LOGIN_MODE_EMAIL_ONLY;

    if (notAuthenticated || auidMissing || iuidMissing) {
      startLoginActivity(context);
      return;
    }

    Map<String, String> params = new HashMap<String, String>();
    if (auid != null) {
      params.put("type", "auid");
      params.put("id", auid);
    }
    else if (iuid != null) {
      params.put("type", "iuid");
      params.put("id", iuid);
    }

    LoginTask verifyTask = new LoginTask(context, validateHandler, getURLString(LOGIN_MODE_VALIDATE), LOGIN_MODE_VALIDATE, params);
    verifyTask.setShowProgressDialog(false);
    AsyncTaskUtils.execute(verifyTask);
  }

  private static void startLoginActivity(Context context) {
    Intent intent = new Intent();
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
    intent.setClass(context, LoginActivity.class);
    intent.putExtra("url", getURLString(mode));
    intent.putExtra("mode", mode);
    intent.putExtra("secret", secret);
    context.startActivity(intent);
  }

  private static String getURLString(int mode) {
    String suffix = "";
    if (mode == LOGIN_MODE_EMAIL_PASSWORD) {
      suffix = "authorize";
    }
    else if (mode == LOGIN_MODE_EMAIL_ONLY) {
      suffix = "check";
    }
    else if (mode == LOGIN_MODE_VALIDATE) {
      suffix = "validate";
    }

    return Constants.BASE_URL + "api/3/apps/" + identifier + "/identity/" + suffix;
  }
}
