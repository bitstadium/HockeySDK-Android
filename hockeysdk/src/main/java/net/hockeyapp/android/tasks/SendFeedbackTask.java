package net.hockeyapp.android.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import net.hockeyapp.android.Constants;
import net.hockeyapp.android.utils.HttpURLConnectionBuilder;
import net.hockeyapp.android.utils.Util;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h3>Description</h3>
 * <p/>
 * Internal helper class. Sends feedback to server.
 * <p/>
 * <h3>License</h3>
 * <p/>
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
 * @author Bogdan Nistor
 **/
public class SendFeedbackTask extends ConnectionTask<Void, Void, HashMap<String, String>> {
    private static final String TAG = "SendFeedbackTask";
    private Context context;
    private Handler handler;
    private String urlString;
    private String name;
    private String email;
    private String subject;
    private String text;
    private List<Uri> attachmentUris;
    private String token;
    private boolean isFetchMessages;
    private ProgressDialog progressDialog;
    private boolean showProgressDialog;
    private int lastMessageId;

    /**
     * Send feedback {@link AsyncTask}.
     * If the class is intended to send a simple feedback message, the a POST is made with the
     * specific data
     * If the class is intended to fetch the messages by providing a token, a GET is made
     *
     * @param context         {@link Context} object
     * @param urlString       URL for sending feedback/fetching messages
     * @param name            Name of the feedback sender
     * @param email           Email of the feedback sender
     * @param subject         Message subject
     * @param text            The message
     * @param attachmentUris  List of all attached files
     * @param token           Token received after sending the first feedback. This should be
     *                        stored in {@link SharedPreferences}
     * @param handler         Handler object to send data back to the activity
     * @param isFetchMessages If true, the {@link AsyncTask} will perform a GET, fetching the
     *                        messages.
     *                        If false, the {@link AsyncTask} will perform a POST, sending the
     *                        feedback message
     */
    public SendFeedbackTask(Context context, String urlString, String name, String email, String
            subject,
                            String text, List<Uri> attachmentUris, String token, Handler handler,
                            boolean isFetchMessages) {

        this.context = context;
        this.urlString = urlString;
        this.name = name;
        this.email = email;
        this.subject = subject;
        this.text = text;
        this.attachmentUris = attachmentUris;
        this.token = token;
        this.handler = handler;
        this.isFetchMessages = isFetchMessages;
        this.showProgressDialog = true;
        this.lastMessageId = -1;

        if (context != null) {
            Constants.loadFromContext(context);
        }
    }

    public void setShowProgressDialog(boolean showProgressDialog) {
        this.showProgressDialog = showProgressDialog;
    }

    public void setLastMessageId(int lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public void attach(Context context) {
        this.context = context;
    }

    public void detach() {
        context = null;
        progressDialog = null;
    }

    @Override
    protected void onPreExecute() {
        String loadingMessage = "Sending feedback..";
        if (isFetchMessages) {
            loadingMessage = "Retrieving discussions...";
        }

        if ((progressDialog == null || !progressDialog.isShowing()) && showProgressDialog) {
            progressDialog = ProgressDialog.show(context, "", loadingMessage, true, false);
        }
    }

    @Override
    protected HashMap<String, String> doInBackground(Void... args) {
        if (isFetchMessages && token != null) {
            /** If we are fetching messages then do a GET */
            return doGet();
        } else {
            /**
             * If we are sending a feedback do POST, and if we are sending a feedback
             * to an existing discussion do PUT
             */
            if (!isFetchMessages) {
                if (attachmentUris.isEmpty()) {
                    return doPostPut();
                } else {
                    HashMap<String, String> result = doPostPutWithAttachments();
                    if (result != null) {
                        clearTemporaryFolder(result);
                    }
                    return result;
                }
            } else {
                return null;
            }
        }
    }

    private void clearTemporaryFolder(HashMap<String, String> result) {
        String status = result.get("status");
        if ((status != null) && (status.startsWith("2")) && (context != null)) {
            File folder = new File(context.getCacheDir(), Constants.TAG);
            if ((folder != null) && folder.exists()) {
                for (File file : folder.listFiles()) {
                    if (file != null) {
                        Boolean success = file.delete();
                        if (!success) {
                            Log.d(TAG, "Error deleting file from temporary folder");
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onPostExecute(HashMap<String, String> result) {
        if (progressDialog != null) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /** If the Handler object is not NULL, send a message to the Activity with the result */
        if (handler != null) {
            Message msg = new Message();
            Bundle bundle = new Bundle();

            if (result != null) {
                bundle.putString("request_type", result.get("type"));
                bundle.putString("feedback_response", result.get("response"));
                bundle.putString("feedback_status", result.get("status"));
            } else {
                bundle.putString("request_type", "unknown");
            }

            msg.setData(bundle);

            handler.sendMessage(msg);
        }
    }

    /**
     * POST/PUT
     *
     * @return
     */
    private HashMap<String, String> doPostPut() {
        HashMap<String, String> result = new HashMap<String, String>();
        result.put("type", "send");

        HttpURLConnection urlConnection = null;
        try {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("name", name);
            parameters.put("email", email);
            parameters.put("subject", subject);
            parameters.put("text", text);
            parameters.put("bundle_identifier", Constants.APP_PACKAGE);
            parameters.put("bundle_short_version", Constants.APP_VERSION_NAME);
            parameters.put("bundle_version", Constants.APP_VERSION);
            parameters.put("os_version", Constants.ANDROID_VERSION);
            parameters.put("oem", Constants.PHONE_MANUFACTURER);
            parameters.put("model", Constants.PHONE_MODEL);

            if (token != null) {
                urlString += token + "/";
            }

            urlConnection = new HttpURLConnectionBuilder(urlString)
                    .setRequestMethod(token != null ? "PUT" : "POST")
                    .writeFormFields(parameters)
                    .build();

            urlConnection.connect();

            result.put("status", String.valueOf(urlConnection.getResponseCode()));
            result.put("response", getStringFromConnection(urlConnection));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return result;
    }

    /**
     * POST/PUT with attachments
     *
     * @return
     */
    private HashMap<String, String> doPostPutWithAttachments() {
        HashMap<String, String> result = new HashMap<String, String>();
        result.put("type", "send");

        HttpURLConnection urlConnection = null;
        try {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("name", name);
            parameters.put("email", email);
            parameters.put("subject", subject);
            parameters.put("text", text);
            parameters.put("bundle_identifier", Constants.APP_PACKAGE);
            parameters.put("bundle_short_version", Constants.APP_VERSION_NAME);
            parameters.put("bundle_version", Constants.APP_VERSION);
            parameters.put("os_version", Constants.ANDROID_VERSION);
            parameters.put("oem", Constants.PHONE_MANUFACTURER);
            parameters.put("model", Constants.PHONE_MODEL);

            if (token != null) {
                urlString += token + "/";
            }

            urlConnection = new HttpURLConnectionBuilder(urlString)
                    .setRequestMethod(token != null ? "PUT" : "POST")
                    .writeMultipartData(parameters, context, attachmentUris)
                    .build();

            urlConnection.connect();

            result.put("status", String.valueOf(urlConnection.getResponseCode()));
            result.put("response", getStringFromConnection(urlConnection));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return result;
    }

    /**
     * GET
     *
     * @return
     */
    private HashMap<String, String> doGet() {
        StringBuilder sb = new StringBuilder();
        sb.append(urlString + Util.encodeParam(token));

        if (lastMessageId != -1) {
            sb.append("?last_message_id=" + lastMessageId);
        }

        HashMap<String, String> result = new HashMap<String, String>();

        HttpURLConnection urlConnection = null;
        try {

            urlConnection = new HttpURLConnectionBuilder(sb.toString())
                    .build();

            result.put("type", "fetch");

            urlConnection.connect();

            result.put("status", String.valueOf(urlConnection.getResponseCode()));
            result.put("response", getStringFromConnection(urlConnection));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return result;
    }
}
