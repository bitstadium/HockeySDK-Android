package net.hockeyapp.android.tasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import net.hockeyapp.android.FeedbackActivity;
import net.hockeyapp.android.FeedbackManager;
import net.hockeyapp.android.FeedbackManagerListener;
import net.hockeyapp.android.objects.Feedback;
import net.hockeyapp.android.objects.FeedbackMessage;
import net.hockeyapp.android.objects.FeedbackResponse;
import net.hockeyapp.android.utils.FeedbackParser;
import net.hockeyapp.android.utils.PrefsUtil;
import net.hockeyapp.android.utils.Util;

import java.util.ArrayList;

/**
 * <h3>Description</h3>
 * 
 * {@link AsyncTask} that parses the Feedback JSON response
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
 * @author Patrick Eschenbach
 */
public class ParseFeedbackTask extends AsyncTask<Void, Void, FeedbackResponse> {
  public static final int NEW_ANSWER_NOTIFICATION_ID   = 2;
  public static final String PREFERENCES_NAME          = "net.hockeyapp.android.feedback";
  public static final String ID_LAST_MESSAGE_SEND      = "idLastMessageSend";
  public static final String ID_LAST_MESSAGE_PROCESSED = "idLastMessageProcessed";

  private Context context;
  private String feedbackResponse;
  private Handler handler;
  private String requestType;
  private String urlString;
  
  public ParseFeedbackTask(Context context, String feedbackResponse, Handler handler, String requestType) {
    this.context = context;
    this.feedbackResponse = feedbackResponse;
    this.handler = handler;
    this.requestType = requestType;
    this.urlString = null;
  }

  public void setUrlString(String urlString) {
    this.urlString = urlString;
  }
  
  @Override
  protected FeedbackResponse doInBackground(Void... params) {
    if (context != null && feedbackResponse != null) {
      FeedbackResponse response = FeedbackParser.getInstance().parseFeedbackResponse(feedbackResponse);

      if (response != null) {
        Feedback feedback = response.getFeedback();
        if (feedback != null) {
          ArrayList<FeedbackMessage> messages = response.getFeedback().getMessages();
          if (messages != null && !messages.isEmpty()) {
            checkForNewAnswers(messages);
          }
        }
      }

      return response;
    }
    
    return null;
  }
  
  @Override
  protected void onPostExecute(FeedbackResponse result) {
    if (result != null && handler != null) {
      Message msg = new Message();
      Bundle bundle = new Bundle();
      
      bundle.putSerializable("parse_feedback_response", result);
      msg.setData(bundle);
      
      handler.sendMessage(msg);
    }
  }

  private void checkForNewAnswers(ArrayList<FeedbackMessage> messages) {
    FeedbackMessage latestMessage = messages.get(messages.size() - 1);
    int idLatestMessage = latestMessage.getId();

    SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, 0);

    if (requestType.equals("send")) {
      PrefsUtil.applyChanges(preferences.edit()
          .putInt(ID_LAST_MESSAGE_SEND, idLatestMessage)
          .putInt(ID_LAST_MESSAGE_PROCESSED, idLatestMessage));
    }
    else if (requestType.equals("fetch")) {
      int idLastMessageSend = preferences.getInt(ID_LAST_MESSAGE_SEND, -1);
      int idLastMessageProcessed = preferences.getInt(ID_LAST_MESSAGE_PROCESSED, -1);

      if (idLatestMessage != idLastMessageSend && idLatestMessage != idLastMessageProcessed) {
        // We have a new answer here.
        PrefsUtil.applyChanges(preferences.edit().putInt(ID_LAST_MESSAGE_PROCESSED, idLatestMessage));
        boolean eventHandled = false;

        FeedbackManagerListener listener = FeedbackManager.getLastListener();
        if (listener != null) {
          eventHandled = listener.feedbackAnswered(latestMessage);
        }

        if (!eventHandled) {
          startNotification(context);
        }
      }
    }
  }

  @SuppressWarnings("deprecation")
  private void startNotification(Context context) {
    if (urlString == null) {
      return;
    }

    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    int iconId = context.getResources().getIdentifier("ic_menu_refresh", "drawable", "android");

    Class<?> activityClass = null;
    if (FeedbackManager.getLastListener() != null) {
      activityClass = FeedbackManager.getLastListener().getFeedbackActivityClass();
    }
    if (activityClass == null) {
      activityClass = FeedbackActivity.class;
    }

    Intent intent = new Intent();
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    intent.setClass(context, activityClass);
    intent.putExtra("url", urlString);

    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

    Notification notification = Util.createNotification(context, pendingIntent, "HockeyApp Feedback", "A new answer to your feedback is available.", iconId);

    if (notification != null) {
      notificationManager.notify(NEW_ANSWER_NOTIFICATION_ID, notification);
    }
  }
}
