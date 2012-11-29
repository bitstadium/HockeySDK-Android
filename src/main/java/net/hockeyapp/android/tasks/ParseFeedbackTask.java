package net.hockeyapp.android.tasks;

import net.hockeyapp.android.jsonParseUtils.FeedbackParser;
import net.hockeyapp.android.objects.FeedbackResponse;
import android.app.ProgressDialog;
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
	private ProgressDialog progressDialog;
	
	public ParseFeedbackTask(Context context, String feedbackResponse, Handler handler) {
		this.context = context;
		this.feedbackResponse = feedbackResponse;
		this.handler = handler;
	}
	
	@Override
	protected void onPreExecute() {
		if (context != null && (progressDialog == null || !progressDialog.isShowing())) {
			progressDialog = ProgressDialog.show(context, "", "Loading responses...", true, false);
		}
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
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		
		if (result != null && handler != null) {
			Message msg = new Message();
			Bundle bundle = new Bundle();
			
			bundle.putSerializable("parse_feedback_response", result);
			msg.setData(bundle);
			
			handler.sendMessage(msg);
		}
	}
}
