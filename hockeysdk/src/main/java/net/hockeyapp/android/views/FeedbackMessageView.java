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
 *
 * Internal helper class to draw the content view of a Feedback message row
 *
 **/
public class FeedbackMessageView extends LinearLayout {

    @SuppressLint("SimpleDateFormat")
    private final static SimpleDateFormat DATE_FORMAT_IN = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    @SuppressLint("SimpleDateFormat")
    private final static SimpleDateFormat DATE_FORMAT_OUT = new SimpleDateFormat("d MMM h:mm a");

    private TextView mAuthorTextView;
    private TextView mDateTextView;
    private TextView mMessageTextView;
    private AttachmentListView mAttachmentListView;

    private FeedbackMessage mFeedbackMessage;

    private final Context mContext;

    @SuppressWarnings("unused")
    @Deprecated
    private boolean ownMessage;//TODO why surpress this?! Intended for future use?

    public FeedbackMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        LayoutInflater.from(context).inflate(R.layout.hockeyapp_view_feedback_message, this);

        mAuthorTextView = (TextView) findViewById(R.id.label_author);
        mDateTextView = (TextView) findViewById(R.id.label_date);
        mMessageTextView = (TextView) findViewById(R.id.label_text);
        mAttachmentListView = (AttachmentListView) findViewById(R.id.list_attachments);

    }

    public void setFeedbackMessage(FeedbackMessage feedbackMessage) {
        mFeedbackMessage = feedbackMessage;

        try {
            Date date = DATE_FORMAT_IN.parse(mFeedbackMessage.getCreatedAt());
            mDateTextView.setText(DATE_FORMAT_OUT.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mAuthorTextView.setText(mFeedbackMessage.getName());
        mMessageTextView.setText(mFeedbackMessage.getText());

        mAttachmentListView.removeAllViews();
        for (FeedbackAttachment feedbackAttachment : mFeedbackMessage.getFeedbackAttachments()) {
            AttachmentView attachmentView = new AttachmentView(mContext, mAttachmentListView, feedbackAttachment, false);
            AttachmentDownloader.getInstance().download(feedbackAttachment, attachmentView);
            mAttachmentListView.addView(attachmentView);
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

            setBackgroundColor(getResources().getColor(R.color.hockeyapp_background_light));
            mAuthorTextView.setTextColor(getResources().getColor(R.color.hockeyapp_text_white));
            mDateTextView.setTextColor(getResources().getColor(R.color.hockeyapp_text_white));

        } else {

            setBackgroundColor(getResources().getColor(R.color.hockeyapp_background_white));
            mAuthorTextView.setTextColor(getResources().getColor(R.color.hockeyapp_text_light));
            mDateTextView.setTextColor(getResources().getColor(R.color.hockeyapp_text_light));

        }
        mMessageTextView.setTextColor(getResources().getColor(R.color.hockeyapp_text_black));
    }


}