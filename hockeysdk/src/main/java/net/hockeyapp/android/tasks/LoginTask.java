package net.hockeyapp.android.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import net.hockeyapp.android.Constants;
import net.hockeyapp.android.LoginManager;
import net.hockeyapp.android.utils.HttpURLConnectionBuilder;
import net.hockeyapp.android.utils.PrefsUtil;
import net.hockeyapp.android.utils.Util;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * <h3>Description</h3>
 *
 * Perform the authentication process.
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
public class LoginTask extends ConnectionTask<Void, Void, Boolean> {
  private Context context;
  private Handler handler;
  private ProgressDialog progressDialog;
  private boolean showProgressDialog;

  private final int mode;
  private final String urlString;
  private final Map<String, String> params;

  /**
   * Send feedback {@link AsyncTask}.
   * If the class is intended to send a simple feedback message, the a POST is made with the specific data
   * If the class is intended to fetch the messages by providing a token, a GET is made
   *
   * @param context     {@link Context} object
   * @param handler     Handler object to send data back to the activity
   * @param urlString   URL for Identity Check
   * @param mode        LoginManager.LOGIN_MODE_ANONYMOUS, LoginManager.LOGIN_MODE_EMAIL_ONLY, 
   *                    LoginManager.LOGIN_MODE_EMAIL_PASSWORD, or LoginManager.LOGIN_MODE_VALIDATE
   * @param params      a map for all key value params.
   */
  public LoginTask(Context context, Handler handler, String urlString, int mode, Map<String, String> params) {
    this.context = context;
    this.handler = handler;
    this.urlString = urlString;
    this.mode = mode;
    this.params = params;
    this.showProgressDialog = true;

    if (context != null) {
      Constants.loadFromContext(context);
    }
  }

  public void setShowProgressDialog(boolean showProgressDialog) {
    this.showProgressDialog = showProgressDialog;
  }

  public void attach(Context context, Handler handler) {
    this.context = context;
    this.handler = handler;
  }

  public void detach() {
    context = null;
    handler = null;
    progressDialog = null;
  }

  @Override
  protected void onPreExecute() {
    if ((progressDialog == null || !progressDialog.isShowing()) && showProgressDialog) {
      progressDialog = ProgressDialog.show(context, "", "Please wait...", true, false);
    }
  }

  @Override
  protected Boolean doInBackground(Void... args) {
    HttpURLConnection connection = null;
    try {

      connection = makeRequest(mode, params);
      connection.connect();

      if (connection.getResponseCode() == 200) {
        String responseStr = getStringFromConnection(connection);

        if (!TextUtils.isEmpty(responseStr)) {
          return handleResponse(responseStr);
        }
      }
    }
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      if (connection != null) {
        connection.disconnect();
      }
    }

    return false;
  }

  @Override
  protected void onPostExecute(Boolean success) {
    if (progressDialog != null) {
      try {
        progressDialog.dismiss();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

    /** If the Handler object is not NULL, send a message to the Activity with the result */
    if (handler != null) {
      Message msg = new Message();
      Bundle bundle = new Bundle();
      bundle.putBoolean("success", success);

      msg.setData(bundle);
      handler.sendMessage(msg);
    }
  }

  private HttpURLConnection makeRequest(int mode, Map<String, String> params) throws IOException {
    if (mode == LoginManager.LOGIN_MODE_EMAIL_ONLY) {

      return new HttpURLConnectionBuilder(urlString)
              .setRequestMethod("POST")
              .writeFormFields(params)
              .build();
    }
    else if (mode == LoginManager.LOGIN_MODE_EMAIL_PASSWORD) {

      return new HttpURLConnectionBuilder(urlString)
              .setRequestMethod("POST")
              .setBasicAuthorization(params.get("email"), params.get("password"))
              .build();
    }
    else if (mode == LoginManager.LOGIN_MODE_VALIDATE) {
      String type = params.get("type");
      String id   = params.get("id");
      String paramUrl = urlString + "?" + type + "=" + id;

      return new HttpURLConnectionBuilder(paramUrl)
              .build();
    }
    else {
      throw new IllegalArgumentException("Login mode " + mode + " not supported.");
    }
  }

  private boolean handleResponse(String responseStr) {
    SharedPreferences prefs = context.getSharedPreferences("net.hockeyapp.android.login", 0);

    try {
      JSONObject response = new JSONObject(responseStr);
      String status = response.getString("status");

      if (TextUtils.isEmpty(status)) {
        return false;
      }

      if (mode == LoginManager.LOGIN_MODE_EMAIL_ONLY) {
        if (status.equals("identified")) {
          String iuid = response.getString("iuid");
          if (!TextUtils.isEmpty(iuid)) {
            prefs.edit()
                    .putString("iuid", iuid)
                    .apply();
            return true;
          }
        }
      }
      else if (mode == LoginManager.LOGIN_MODE_EMAIL_PASSWORD) {
        if (status.equals("authorized")) {
          String auid = response.getString("auid");
          if (!TextUtils.isEmpty(auid)) {
            prefs.edit()
                    .putString("auid", auid)
                    .apply();
            return true;
          }
        }
      }
      else if (mode == LoginManager.LOGIN_MODE_VALIDATE) {
        if (status.equals("validated")) {
          return true;
        }
        else {
          prefs.edit()
                  .remove("iuid")
                  .remove("auid")
                  .apply();
        }
      }
      else {
        throw new IllegalArgumentException("Login mode " + mode + " not supported.");
      }

      return false;
    }
    catch (JSONException e) {
      e.printStackTrace();
      return false;
    }
  }
}
