package net.hockeyapp.android.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import net.hockeyapp.android.Constants;
import net.hockeyapp.android.utils.ConnectionManager;
import net.hockeyapp.android.utils.SimpleMultipartEntity;
import net.hockeyapp.android.utils.Util;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <h3>Description</h3>
 * 
 * Internal helper class. Sends feedback to server.
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
 * @author Bogdan Nistor
 **/
public class SendFeedbackTask extends AsyncTask<Void, Void, HashMap<String, String>> {
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
   * If the class is intended to send a simple feedback message, the a POST is made with the specific data
   * If the class is intended to fetch the messages by providing a token, a GET is made
   * 
   * @param context         {@link Context} object
   * @param urlString       URL for sending feedback/fetching messages
   * @param name            Name of the feedback sender
   * @param email           Email of the feedback sender
   * @param subject         Message subject
   * @param text            The message
   * @param attachmentUris  List of all attached files
   * @param token           Token received after sending the first feedback. This should be stored in {@link SharedPreferences}
   * @param handler         Handler object to send data back to the activity
   * @param isFetchMessages If true, the {@link AsyncTask} will perform a GET, fetching the messages. 
   *                        If false, the {@link AsyncTask} will perform a POST, sending the feedback message
   */
  public SendFeedbackTask(Context context, String urlString, String name, String email, String subject, 
      String text, List<Uri> attachmentUris, String token, Handler handler, boolean isFetchMessages) {
    
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
    if (context == null) return null;
    HttpClient httpclient = ConnectionManager.getInstance(context).getHttpClient();

    if (isFetchMessages && token != null) {
      /** If we are fetching messages then do a GET */
      return doGet(httpclient);
    } 
    else if (!isFetchMessages) {
      /** 
       * If we are sending a feedback do POST, and if we are sending a feedback
       * to an existing discussion do PUT
       */
      if (attachmentUris.isEmpty()) {
        return doPostPut(httpclient);
      } 
      else {
        HashMap<String, String> result = doPostPutWithAttachments(httpclient);

        /** Clear temporary folder */
        String status = result.get("status");
        if ((status != null) && (status.startsWith("2")) && (context != null)) {
          File folder = new File(context.getCacheDir(), Constants.TAG);
          if (folder.exists()) {
            for (File file : folder.listFiles()) {
              file.delete();
            }
          }
        }

        return result;
      }
    }
    
    return null;
  }

  @Override
  protected void onPostExecute(HashMap<String, String> result) {
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
      
      if (result != null) {
	      bundle.putString("request_type", (String)result.get("type"));
	      bundle.putString("feedback_response", (String)result.get("response"));
	      bundle.putString("feedback_status", (String)result.get("status"));
      }
      else {
        bundle.putString("request_type", "unknown");
      }

      msg.setData(bundle);

      handler.sendMessage(msg);
    }
  }
  
  /**
   * POST/PUT
   * @param httpClient
   * @return
   */
  private HashMap<String, String> doPostPut(HttpClient httpClient) {
    HashMap<String, String> result = new HashMap<String, String>();
    result.put("type", "send");

    try {
      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
      nameValuePairs.add(new BasicNameValuePair("name", name));
      nameValuePairs.add(new BasicNameValuePair("email", email));
      nameValuePairs.add(new BasicNameValuePair("subject", subject));
      nameValuePairs.add(new BasicNameValuePair("text", text));
      nameValuePairs.add(new BasicNameValuePair("bundle_identifier", Constants.APP_PACKAGE));
      nameValuePairs.add(new BasicNameValuePair("bundle_short_version", Constants.APP_VERSION_NAME));
      nameValuePairs.add(new BasicNameValuePair("bundle_version", Constants.APP_VERSION));
      nameValuePairs.add(new BasicNameValuePair("os_version", Constants.ANDROID_VERSION));
      nameValuePairs.add(new BasicNameValuePair("oem", Constants.PHONE_MANUFACTURER));
      nameValuePairs.add(new BasicNameValuePair("model", Constants.PHONE_MODEL));

      UrlEncodedFormEntity form = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
      form.setContentEncoding(HTTP.UTF_8);

      HttpPost httpPost = null;
      HttpPut httpPut = null;
      if (token != null) {
        urlString += token + "/";
        httpPut = new HttpPut(urlString);
      } 
      else {
        httpPost = new HttpPost(urlString);
      }
      
      HttpResponse response = null;
      if (httpPut != null) {
        httpPut.setEntity(form);
        response = (HttpResponse) httpClient.execute(httpPut);
      } 
      else if (httpPost != null) {
        httpPost.setEntity(form);
        response = (HttpResponse) httpClient.execute(httpPost);
      }
      
      if (response != null) {
        HttpEntity resEntity = response.getEntity();  
        result.put("response", EntityUtils.toString(resEntity));
        result.put("status", "" + response.getStatusLine().getStatusCode());
      }
    } 
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } 
    catch (ClientProtocolException e) {
      e.printStackTrace();
    } 
    catch (IOException e) {
      e.printStackTrace();
    }
    
    return result;
  }

  /**
   * POST/PUT with attachments
   * @param httpClient
   * @return
   */
  private HashMap<String, String> doPostPutWithAttachments(HttpClient httpClient) {
    HashMap<String, String> result = new HashMap<String, String>();
    result.put("type", "send");

    try {
      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
      nameValuePairs.add(new BasicNameValuePair("name", name));
      nameValuePairs.add(new BasicNameValuePair("email", email));
      nameValuePairs.add(new BasicNameValuePair("subject", subject));
      nameValuePairs.add(new BasicNameValuePair("text", text));
      nameValuePairs.add(new BasicNameValuePair("bundle_identifier", Constants.APP_PACKAGE));
      nameValuePairs.add(new BasicNameValuePair("bundle_short_version", Constants.APP_VERSION_NAME));
      nameValuePairs.add(new BasicNameValuePair("bundle_version", Constants.APP_VERSION));
      nameValuePairs.add(new BasicNameValuePair("os_version", Constants.ANDROID_VERSION));
      nameValuePairs.add(new BasicNameValuePair("oem", Constants.PHONE_MANUFACTURER));
      nameValuePairs.add(new BasicNameValuePair("model", Constants.PHONE_MODEL));

      SimpleMultipartEntity entity = new SimpleMultipartEntity();
      entity.writeFirstBoundaryIfNeeds();

      /** Write form data */
      for (NameValuePair pair : nameValuePairs) {
        entity.addPart(pair.getName(), pair.getValue());
      }

      /** Write files */
      for (int i = 0; i < attachmentUris.size(); i++) {
        Uri attachmentUri = attachmentUris.get(i);
        boolean lastFile = (i == attachmentUris.size() - 1);

        InputStream input = context.getContentResolver().openInputStream(attachmentUri);
        String filename = attachmentUri.getLastPathSegment();
        entity.addPart("attachment" + i, filename, input, lastFile);
      }
      entity.writeLastBoundaryIfNeeds();

      HttpPost httpPost = null;
      HttpPut httpPut = null;
      if (token != null) {
        urlString += token + "/";
        httpPut = new HttpPut(urlString);
      }
      else {
        httpPost = new HttpPost(urlString);
      }

      HttpResponse response = null;
      if (httpPut != null) {
        httpPut.setHeader("Content-type", "multipart/form-data; boundary=" + entity.getBoundary());
        httpPut.setEntity(entity);
        response = (HttpResponse) httpClient.execute(httpPut);
      }
      else if (httpPost != null) {
        httpPost.setHeader("Content-type", "multipart/form-data; boundary=" + entity.getBoundary());
        httpPost.setEntity(entity);
        response = (HttpResponse) httpClient.execute(httpPost);
      }

      if (response != null) {
        HttpEntity resEntity = response.getEntity();
        result.put("response", EntityUtils.toString(resEntity));
        result.put("status", "" + response.getStatusLine().getStatusCode());
      }
    }
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    catch (ClientProtocolException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    return result;
  }
  
  /**
   * GET
   * @param httpClient
   * @return
   */
  private HashMap<String, String> doGet(HttpClient httpClient) {
    StringBuilder sb = new StringBuilder();
    sb.append(urlString + Util.encodeParam(token));

    if (lastMessageId != -1) {
      sb.append("?last_message_id=" + lastMessageId);
    }
    
    HttpGet httpGet = new HttpGet(sb.toString());

    HashMap<String, String> result = new HashMap<String, String>();
    result.put("type", "fetch");

    /** Execute HTTP Post Request */
    try {
      HttpResponse response = (HttpResponse) httpClient.execute(httpGet);
      HttpEntity responseEntity = response.getEntity();
      
      result.put("response", EntityUtils.toString(responseEntity));
      result.put("status", "" + response.getStatusLine().getStatusCode());
    } 
    catch (ClientProtocolException e) {
      e.printStackTrace();
    } 
    catch (IllegalStateException e) {
      e.printStackTrace();
    } 
    catch (IOException e) {
      e.printStackTrace();
    }
    
    return result;
  }
}
