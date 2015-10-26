package net.hockeyapp.android.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import net.hockeyapp.android.objects.FeedbackAttachment;
import net.hockeyapp.android.objects.FeedbackMessage;
import net.hockeyapp.android.tasks.AttachmentDownloader;
import net.hockeyapp.android.views.AttachmentListView;
import net.hockeyapp.android.views.AttachmentView;
import net.hockeyapp.android.views.FeedbackMessageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
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
 */
public class MessagesAdapter extends BaseAdapter {
  private Context context;
  private ArrayList<FeedbackMessage> messagesList;
  private SimpleDateFormat format;
  private SimpleDateFormat formatNew;
  private Date date;
  private TextView authorTextView;
  private TextView dateTextView;
  private TextView messageTextView;
  private AttachmentListView attachmentListView;

  @SuppressLint("SimpleDateFormat")
  public MessagesAdapter(Context context, ArrayList<FeedbackMessage> messagesList) {
    this.context = context;
    this.messagesList = messagesList;
    
    format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    formatNew = new SimpleDateFormat("d MMM h:mm a");
  }

  public int getCount() {
    return this.messagesList.size();
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    final FeedbackMessage feedbackMessage = messagesList.get(position);
    FeedbackMessageView view;
  
    if (convertView == null) {
      view = new FeedbackMessageView(context);
    } 
    else {
      view = (FeedbackMessageView) convertView;
    }
  
    if (feedbackMessage != null) {
      authorTextView = (TextView) view.findViewById(FeedbackMessageView.AUTHOR_TEXT_VIEW_ID);
      dateTextView = (TextView) view.findViewById(FeedbackMessageView.DATE_TEXT_VIEW_ID);
      messageTextView = (TextView) view.findViewById(FeedbackMessageView.MESSAGE_TEXT_VIEW_ID);
      attachmentListView = (AttachmentListView) view.findViewById(FeedbackMessageView.ATTACHMENT_LIST_VIEW_ID);

      try {
        date = format.parse(feedbackMessage.getCreatedAt());
        dateTextView.setText(formatNew.format(date));
      } catch (ParseException e) {
        e.printStackTrace();
      }
  
      authorTextView.setText(feedbackMessage.getName());
      messageTextView.setText(feedbackMessage.getText());

      attachmentListView.removeAllViews();
      for (FeedbackAttachment feedbackAttachment : feedbackMessage.getFeedbackAttachments()) {
        AttachmentView attachmentView = new AttachmentView(context, attachmentListView, feedbackAttachment, false);
        AttachmentDownloader.getInstance().download(feedbackAttachment, attachmentView);
        attachmentListView.addView(attachmentView);
      }
    }

    view.setFeedbackMessageViewBgAndTextColor(position % 2 == 0 ? 0 : 1);
  
    return view;
  }

  @Override
  public Object getItem(int position) {
    return messagesList.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }
  
  public void clear() {
    if (messagesList != null) {
      messagesList.clear();
    }
  }
  
  public void add(FeedbackMessage message) {
    if (message != null && messagesList != null) {
      messagesList.add(message);
    }
  }
}
