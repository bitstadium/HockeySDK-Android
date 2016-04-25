package net.hockeyapp.android.objects;

import java.io.Serializable;

/**
 * <h3>Description</h3>
 *
 * Model for feedback responses.
 *
 */
public class FeedbackResponse implements Serializable {
    private static final long serialVersionUID = -1093570359639034766L;

    private String mStatus;
    private Feedback mFeedback;
    private String mToken;

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        this.mStatus = status;
    }

    public Feedback getFeedback() {
        return mFeedback;
    }

    public void setFeedback(Feedback feedback) {
        this.mFeedback = feedback;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        this.mToken = token;
    }
}
