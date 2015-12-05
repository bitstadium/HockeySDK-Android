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
import net.hockeyapp.android.utils.Util;

import java.util.ArrayList;

/**
 * <h3>Description</h3>
 * <p/>
 * {@link AsyncTask} that parses the Feedback JSON response
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
 * @author Patrick Eschenbach
 */
public class ParseFeedbackTask extends AsyncTask<Void, Void, FeedbackResponse> {
    public static final int NEW_ANSWER_NOTIFICATION_ID = 2;
    public static final String PREFERENCES_NAME = "net.hockeyapp.android.feedback";
    public static final String ID_LAST_MESSAGE_SEND = "idLastMessageSend";
    public static final String ID_LAST_MESSAGE_PROCESSED = "idLastMessageProcessed";

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

            bundle.putSerializable("parse_feedback_response", result);
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

                if (!eventHandled) {
                    startNotification(mContext);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void startNotification(Context context) {
        if (mUrlString == null) {
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
        intent.putExtra("url", mUrlString);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = Util.createNotification(context, pendingIntent, "HockeyApp Feedback", "A new answer to your feedback is available.", iconId);

        if (notification != null) {
            notificationManager.notify(NEW_ANSWER_NOTIFICATION_ID, notification);
        }
    }
}
