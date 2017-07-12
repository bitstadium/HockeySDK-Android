package net.hockeyapp.android.listeners;

import net.hockeyapp.android.tasks.SendFeedbackTask;

/**
 * <h3>Description</h3>
 *
 * Abstract class for callbacks to be invoked from the {@link SendFeedbackTask}.
 *
 **/
public abstract class SendFeedbackListener {

    public void feedbackSuccessful(SendFeedbackTask task) {
    }

    public void feedbackFailed(SendFeedbackTask task, Boolean userWantsRetry) {
    }
}
