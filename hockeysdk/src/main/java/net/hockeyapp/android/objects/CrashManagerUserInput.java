package net.hockeyapp.android.objects;

/**
 * Crash Manager user input
 *
 * @author Andreas Wörner
 */
public enum CrashManagerUserInput {
    /**
     * User chose not to send the crash report
     */
    CrashManagerUserInputDontSend(0),
    /**
     * User wants the crash report to be sent
     */
    CrashManagerUserInputSend(1),
    /**
     * User chose to always send crash reports
     */
    CrashManagerUserInputAlwaysSend(2);

    private final int value;

    CrashManagerUserInput(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
