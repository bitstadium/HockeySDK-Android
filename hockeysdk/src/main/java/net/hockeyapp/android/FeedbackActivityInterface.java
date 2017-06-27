package net.hockeyapp.android;

import android.view.View;

/**
 * <h3>Description</h3>
 *
 * Interface definition for callbacks to be invoked from the {@link FeedbackActivity}.
 *
 **/
public interface FeedbackActivityInterface {
    /**
     * Return an instance of View to show the update information.
     *
     * @return Instance of View
     */
    View getLayoutView();
}
