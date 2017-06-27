package net.hockeyapp.android.objects;

/**
 * <h3>Description</h3>
 *
 * Activity to show the feedback form.
 *
 */
public enum FeedbackUserDataElement {

    DONT_SHOW(0), OPTIONAL(1), REQUIRED(2);

    private final int mValue;

    FeedbackUserDataElement(int value) {
        this.mValue = value;
    }

    public int getValue() {
        return mValue;
    }

}
