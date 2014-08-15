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

import java.util.ArrayList;

/**
 * {@link AsyncTask} that parses the Feedback JSON response
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
    Notification notification = new Notification(iconId, "New Answer to Your Feedback.", System.currentTimeMillis());

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
    notification.setLatestEventInfo(context, "HockeyApp Feedback", "A new answer to your feedback is available.", pendingIntent);
    notificationManager.notify(NEW_ANSWER_NOTIFICATION_ID, notification);
  }
}
