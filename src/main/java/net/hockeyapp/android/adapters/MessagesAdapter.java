package net.hockeyapp.android.adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import net.hockeyapp.android.objects.FeedbackMessage;
import net.hockeyapp.android.views.FeedbackMessageView;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MessagesAdapter extends BaseAdapter {
  private Context context;
  private ArrayList<FeedbackMessage> messagesList;
  private SimpleDateFormat format;
  private SimpleDateFormat formatNew;
  private Date date;
  private TextView authorTextView;
  private TextView dateTextView;
  private TextView messageTextView;

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
        
      try {
        date = format.parse(feedbackMessage.getCreatedAt());
        dateTextView.setText(formatNew.format(date));
      } catch (ParseException e) {
        e.printStackTrace();
      }
  
      authorTextView.setText(feedbackMessage.getName());
      messageTextView.setText(feedbackMessage.getText());
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