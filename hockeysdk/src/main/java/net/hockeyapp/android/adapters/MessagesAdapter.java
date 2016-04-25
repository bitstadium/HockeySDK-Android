package net.hockeyapp.android.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import net.hockeyapp.android.objects.FeedbackMessage;
import net.hockeyapp.android.views.FeedbackMessageView;

import java.util.ArrayList;

public class MessagesAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<FeedbackMessage> mMessagesList;

    public MessagesAdapter(Context context, ArrayList<FeedbackMessage> messagesList) {
        this.mContext = context;
        this.mMessagesList = messagesList;
    }

    public int getCount() {
        return this.mMessagesList.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final FeedbackMessage feedbackMessage = mMessagesList.get(position);
        FeedbackMessageView view;

        if (convertView == null) {
            view = new FeedbackMessageView(mContext, null);
        } else {
            view = (FeedbackMessageView) convertView;
        }

        if (feedbackMessage != null) {
            view.setFeedbackMessage(feedbackMessage);
        }

        view.setIndex(position);

        return view;
    }

    @Override
    public Object getItem(int position) {
        return mMessagesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        if (mMessagesList != null) {
            mMessagesList.clear();
        }
    }

    public void add(FeedbackMessage message) {
        if (message != null && mMessagesList != null) {
            mMessagesList.add(message);
        }
    }
}
