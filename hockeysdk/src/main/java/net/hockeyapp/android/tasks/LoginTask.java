package net.hockeyapp.android.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import net.hockeyapp.android.Constants;
import net.hockeyapp.android.LoginManager;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.HttpURLConnectionBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
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

    /**
     * Key for login success in the returend bundle
     */
    public static final String BUNDLE_SUCCESS = "success";

    private Context mContext;
    private Handler mHandler;
    private ProgressDialog mProgressDialog;
    private boolean mShowProgressDialog;

    private final int mMode;
    private final String mUrlString;
    private final Map<String, String> mParams;

    /**
     * Send feedback {@link AsyncTask}.
     * If the class is intended to send a simple feedback message, the a POST is made with the specific data
     * If the class is intended to fetch the messages by providing a token, a GET is made
     *
     * @param context   {@link Context} object
     * @param handler   Handler object to send data back to the activity
     * @param urlString URL for Identity Check
     * @param mode      LoginManager.LOGIN_MODE_ANONYMOUS, LoginManager.LOGIN_MODE_EMAIL_ONLY,
     *                  LoginManager.LOGIN_MODE_EMAIL_PASSWORD, or LoginManager.LOGIN_MODE_VALIDATE
     * @param params    a map for all key value params.
     */
    public LoginTask(Context context, Handler handler, String urlString, int mode, Map<String, String> params) {
        this.mContext = context;
        this.mHandler = handler;
        this.mUrlString = urlString;
        this.mMode = mode;
        this.mParams = params;
        this.mShowProgressDialog = true;

        if (context != null) {
            Constants.loadFromContext(context);
        }
    }

    public void setShowProgressDialog(boolean showProgressDialog) {
        this.mShowProgressDialog = showProgressDialog;
    }

    public void attach(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
    }

    public void detach() {
        mContext = null;
        mHandler = null;
        mProgressDialog = null;
    }

    @Override
    protected void onPreExecute() {
        if ((mProgressDialog == null || !mProgressDialog.isShowing()) && mShowProgressDialog) {
            mProgressDialog = ProgressDialog.show(mContext, "", "Please wait...", true, false);
        }
    }

    @Override
    protected Boolean doInBackground(Void... args) {
        HttpURLConnection connection = null;
        try {

            connection = makeRequest(mMode, mParams);
            connection.connect();

            if (connection.getResponseCode() == 200) {
                String responseStr = getStringFromConnection(connection);

                if (!TextUtils.isEmpty(responseStr)) {
                    return handleResponse(responseStr);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (mProgressDialog != null) {
            try {
                mProgressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /** If the Handler object is not NULL, send a message to the Activity with the result */
        if (mHandler != null) {
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putBoolean(BUNDLE_SUCCESS, success);

            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }

    private HttpURLConnection makeRequest(int mode, Map<String, String> params) throws IOException {
        if (mode == LoginManager.LOGIN_MODE_EMAIL_ONLY) {

            return new HttpURLConnectionBuilder(mUrlString)
                    .setRequestMethod("POST")
                    .writeFormFields(params)
                    .build();
        } else if (mode == LoginManager.LOGIN_MODE_EMAIL_PASSWORD) {

            return new HttpURLConnectionBuilder(mUrlString)
                    .setRequestMethod("POST")
                    .setBasicAuthorization(params.get("email"), params.get("password"))
                    .build();
        } else if (mode == LoginManager.LOGIN_MODE_VALIDATE) {
            String type = params.get("type");
            String id = params.get("id");
            String paramUrl = mUrlString + "?" + type + "=" + id;

            return new HttpURLConnectionBuilder(paramUrl)
                    .build();
        } else {
            throw new IllegalArgumentException("Login mode " + mode + " not supported.");
        }
    }

    private boolean handleResponse(String responseStr) {
        SharedPreferences prefs = mContext.getSharedPreferences("net.hockeyapp.android.login", 0);

        try {
            JSONObject response = new JSONObject(responseStr);
            String status = response.getString("status");

            if (TextUtils.isEmpty(status)) {
                return false;
            }
            HockeyLog.verbose("HockeyAuth", "Status is: " + status);

            if (mMode == LoginManager.LOGIN_MODE_EMAIL_ONLY) {
                if (status.equals("identified")) {
                    HockeyLog.verbose("HockeyAuth", "Identified!");
                    String iuid = response.getString("iuid");
                    if (!TextUtils.isEmpty(iuid)) {
                        HockeyLog.verbose("HockeyAuth", "Saving iuid");

                        prefs.edit()
                                .putString("iuid", iuid)
                                .apply();
                        return true;
                    }
                }
            } else if (mMode == LoginManager.LOGIN_MODE_EMAIL_PASSWORD) {
                if (status.equals("authorized")) {
                    String auid = response.getString("auid");
                    HockeyLog.verbose("HockeyAuth", "Authorized");

                    if (!TextUtils.isEmpty(auid)) {
                        HockeyLog.verbose("HockeyAuth", "Saving auid");
                        prefs.edit()
                                .putString("auid", auid)
                                .apply();
                        return true;
                    }
                }
            } else if (mMode == LoginManager.LOGIN_MODE_VALIDATE) {
                if (status.equals("validated")) {
                    HockeyLog.verbose("HockeyAuth", "Validated");
                    return true;
                } else {
                    prefs.edit()
                            .remove("iuid")
                            .remove("auid")
                            .apply();
                }
            } else {
                throw new IllegalArgumentException("Login mode " + mMode + " not supported.");
            }

            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
}
