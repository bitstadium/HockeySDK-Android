package net.hockeyapp.android.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import net.hockeyapp.android.R;
import net.hockeyapp.android.objects.FeedbackAttachment;
import net.hockeyapp.android.objects.FeedbackMessage;
import net.hockeyapp.android.tasks.AttachmentDownloader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <h3>Description</h3>
 * <p/>
 * Internal helper class to draw the content view of a Feedback message row
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
public class FeedbackMessageView extends LinearLayout {

  @SuppressLint("SimpleDateFormat")
  private final static SimpleDateFormat DATE_FORMAT_IN = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
  @SuppressLint("SimpleDateFormat")
  private final static SimpleDateFormat DATE_FORMAT_OUT = new SimpleDateFormat("d MMM h:mm a");

  private TextView authorTextView;
  private TextView dateTextView;
  private TextView messageTextView;
  private AttachmentListView attachmentListView;

  private FeedbackMessage mFeedbackMessage;

  private final Context mContext;

  @SuppressWarnings("unused")
  @Deprecated
  private boolean ownMessage;//TODO why surpress this?! Intended for future use?

  public FeedbackMessageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;

    LayoutInflater.from(context).inflate(R.layout.view_feedback_message, this);

    authorTextView = (TextView) findViewById(R.id.label_author);
    dateTextView = (TextView) findViewById(R.id.label_date);
    messageTextView = (TextView) findViewById(R.id.label_text);
    attachmentListView = (AttachmentListView) findViewById(R.id.list_attachments);

  }

  public void setFeedbackMessage(FeedbackMessage feedbackMessage) {
    mFeedbackMessage = feedbackMessage;

    try {
      Date date = DATE_FORMAT_IN.parse(feedbackMessage.getCreatedAt());
      dateTextView.setText(DATE_FORMAT_OUT.format(date));
    } catch (ParseException e) {
      e.printStackTrace();
    }

    authorTextView.setText(feedbackMessage.getName());
    messageTextView.setText(feedbackMessage.getText());

    attachmentListView.removeAllViews();
    for (FeedbackAttachment feedbackAttachment : feedbackMessage.getFeedbackAttachments()) {
      AttachmentView attachmentView = new AttachmentView(mContext, attachmentListView, feedbackAttachment, false);
      AttachmentDownloader.getInstance().download(feedbackAttachment, attachmentView);
      attachmentListView.addView(attachmentView);
    }
  }


  /**
   * Sets the background for the entire {@link FeedbackMessageView} and for the text colors used
   *
   * @param index index of the message view in it's parent view
   */
  @SuppressWarnings("deprecation")
  public void setIndex(int index) {
    if (index % 2 == 0) {

      setBackgroundColor(getResources().getColor(R.color.background_light));
      authorTextView.setTextColor(getResources().getColor(R.color.text_white));
      dateTextView.setTextColor(getResources().getColor(R.color.text_white));

    } else {

      setBackgroundColor(getResources().getColor(R.color.background_white));
      authorTextView.setTextColor(getResources().getColor(R.color.text_light));
      dateTextView.setTextColor(getResources().getColor(R.color.text_light));

    }
    messageTextView.setTextColor(getResources().getColor(R.color.text_black));
  }


}