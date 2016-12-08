package net.hockeyapp.android;

import net.hockeyapp.android.objects.FeedbackMessage;

/**
 * <h3>Description</h3>
 *
 * Abstract class for callbacks to be invoked from the {@link FeedbackManager}
 *
 **/
public abstract class FeedbackManagerListener {
    /**
     * Return your own subclass of FeedbackActivity for customization.
     *
     * @return subclass of FeedbackActivity
     */
    public Class<? extends FeedbackActivity> getFeedbackActivityClass() {
        return FeedbackActivity.class;
    }

    /**
     * Called when an answer to a feedback is available.
     *
     * @param latestMessage the last message
     * @return true if this event has been properly handled by this method
     * and false if not and a notification should be fired.
     */
    public abstract boolean feedbackAnswered(FeedbackMessage latestMessage);

    /**
     * Called when posting a new feedback message.
     * @return Whether a new feedback thread should be created or not. Defaults to false.
     */
    public boolean shouldCreateNewFeedbackThread() {
        return false;
    }
}
