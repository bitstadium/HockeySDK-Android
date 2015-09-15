/*
 * Generated from SessionStateData.bond (https://github.com/Microsoft/bond)
*/
package net.hockeyapp.android.telemetry;
/**
 * Enum SessionState.
 */
public enum SessionState
{
    START(0), END(1);

    private final int value;

    private SessionState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
