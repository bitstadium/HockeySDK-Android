package net.hockeyapp.android.objects;

/**
 * Crash Manager status
 * @author Andreas Wörner
 */
public enum CrashManagerStatus {
    /**
     *	Crash reporting is disabled
     */
    CrashManagerStatusDisabled(0),
    /**
     *	User is asked each time before sending
     */
    CrashManagerStatusAlwaysAsk(1),
    /**
     *	Each crash report is send automatically
     */
    CrashManagerStatusAutoSend(2);

    private final int value;

    private CrashManagerStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
