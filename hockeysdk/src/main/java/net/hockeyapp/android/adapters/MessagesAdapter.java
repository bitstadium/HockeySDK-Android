package net.hockeyapp.android.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import net.hockeyapp.android.objects.FeedbackMessage;
import net.hockeyapp.android.views.FeedbackMessageView;

import java.util.ArrayList;

/**
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
 */
public class MessagesAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<FeedbackMessage> messagesList;

    public MessagesAdapter(Context context, ArrayList<FeedbackMessage> messagesList) {
        this.context = context;
        this.messagesList = messagesList;
    }

    public int getCount() {
        return this.messagesList.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final FeedbackMessage feedbackMessage = messagesList.get(position);
        FeedbackMessageView view;

        if (convertView == null) {
            view = new FeedbackMessageView(context, null);
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
