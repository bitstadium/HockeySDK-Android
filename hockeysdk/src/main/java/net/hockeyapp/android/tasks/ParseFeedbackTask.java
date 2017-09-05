package net.hockeyapp.android.tasks;

import android.app.Notification;
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
import net.hockeyapp.android.R;
import net.hockeyapp.android.objects.Feedback;
import net.hockeyapp.android.objects.FeedbackMessage;
import net.hockeyapp.android.objects.FeedbackResponse;
import net.hockeyapp.android.utils.FeedbackParser;
import net.hockeyapp.android.utils.Util;

import java.util.ArrayList;

/**
 * <h3>Description</h3>
 *
 * {@link AsyncTask} that parses the Feedback JSON response
 *
 */
public class ParseFeedbackTask extends AsyncTask<Void, Void, FeedbackResponse> {

    public static final String PREFERENCES_NAME = "net.hockeyapp.android.feedback";
    public static final String ID_LAST_MESSAGE_SEND = "idLastMessageSend";
    public static final String ID_LAST_MESSAGE_PROCESSED = "idLastMessageProcessed";
    public static final String BUNDLE_PARSE_FEEDBACK_RESPONSE = "parse_feedback_response";

    private Context mContext;
    private String mFeedbackResponse;
    private Handler mHandler;
    private String mRequestType;
    private String mUrlString;

    public ParseFeedbackTask(Context context, String feedbackResponse, Handler handler, String requestType) {
        this.mContext = context;
        this.mFeedbackResponse = feedbackResponse;
        this.mHandler = handler;
        this.mRequestType = requestType;
        this.mUrlString = null;
    }

    public void setUrlString(String urlString) {
        this.mUrlString = urlString;
    }

    @Override
    protected FeedbackResponse doInBackground(Void... params) {
        if (mContext != null && mFeedbackResponse != null) {
            FeedbackResponse response = FeedbackParser.getInstance().parseFeedbackResponse(mFeedbackResponse);

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
        if (result != null && mHandler != null) {
            Message msg = new Message();
            Bundle bundle = new Bundle();

            bundle.putSerializable(BUNDLE_PARSE_FEEDBACK_RESPONSE, result);
            msg.setData(bundle);

            mHandler.sendMessage(msg);
        }
    }

    private void checkForNewAnswers(ArrayList<FeedbackMessage> messages) {
        FeedbackMessage latestMessage = messages.get(messages.size() - 1);
        int idLatestMessage = latestMessage.getId();

        SharedPreferences preferences = mContext.getSharedPreferences(PREFERENCES_NAME, 0);

        if (mRequestType.equals("send")) {
            preferences.edit()
                    .putInt(ID_LAST_MESSAGE_SEND, idLatestMessage)
                    .putInt(ID_LAST_MESSAGE_PROCESSED, idLatestMessage)
                    .apply();
        } else if (mRequestType.equals("fetch")) {
            int idLastMessageSend = preferences.getInt(ID_LAST_MESSAGE_SEND, -1);
            int idLastMessageProcessed = preferences.getInt(ID_LAST_MESSAGE_PROCESSED, -1);

            if (idLatestMessage != idLastMessageSend && idLatestMessage != idLastMessageProcessed) {
                // We have a new answer here.
                preferences.edit()
                        .putInt(ID_LAST_MESSAGE_PROCESSED, idLatestMessage)
                        .apply();
                boolean eventHandled = false;

                FeedbackManagerListener listener = FeedbackManager.getLastListener();
                if (listener != null) {
                    eventHandled = listener.feedbackAnswered(latestMessage);
                }

                if (!eventHandled && mUrlString != null) {
                    showNewAnswerNotification(mContext, mUrlString);
                }
            }
        }
    }

    private static void showNewAnswerNotification(Context context, String urlString) {
        Class<?> activityClass = null;
        if (FeedbackManager.getLastListener() != null) {
            activityClass = FeedbackManager.getLastListener().getFeedbackActivityClass();
        }
        if (activityClass == null) {
            activityClass = FeedbackActivity.class;
        }
        int iconId = context.getResources().getIdentifier("ic_menu_refresh", "drawable", "android");
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setClass(context, activityClass);
        intent.putExtra("url", urlString);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Notification notification = Util.createNotification(context, pendingIntent,
                context.getString(R.string.hockeyapp_feedback_notification_title),
                context.getString(R.string.hockeyapp_feedback_new_answer_notification_message), iconId, FeedbackManager.NOTIFICATION_CHANNEL_ID);
        Util.sendNotification(context, FeedbackManager.NEW_ANSWER_NOTIFICATION_ID, notification, FeedbackManager.NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.hockeyapp_feedback_notification_channel));
    }
}
