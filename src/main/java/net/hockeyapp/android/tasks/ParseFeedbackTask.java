package net.hockeyapp.android.tasks;

import net.hockeyapp.android.objects.FeedbackResponse;
import net.hockeyapp.android.utils.FeedbackParser;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * {@link AsyncTask} that parses the Feedback JSON response
 * @author Bogdan Nistor
 *
 */
public class ParseFeedbackTask extends AsyncTask<Void, Void, FeedbackResponse> {
  private Context context;
  private String feedbackResponse;
  private Handler handler;
  
  public ParseFeedbackTask(Context context, String feedbackResponse, Handler handler) {
    this.context = context;
    this.feedbackResponse = feedbackResponse;
    this.handler = handler;
  }
  
  @Override
  protected FeedbackResponse doInBackground(Void... params) {
    if (context != null && feedbackResponse != null) {
      return FeedbackParser.getInstance().parseFeedbackResponse(feedbackResponse);
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
}
