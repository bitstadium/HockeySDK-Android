package net.hockeyapp.android.telemetry;

import android.content.Context;

/*
 *  Class needed to mock dependencies. Most dependencies have private modifier to be not visible to
 *  the devs. But they have to be public to be mockable in the test target. This is why we need to
 *  create this kind of subclass with the modifier `public`.
 */
public class PublicTelemetryContext extends TelemetryContext {

    /**
     * Constructs a new INSTANCE of the Telemetry telemetryContext tag keys
     *
     * @param context            the context for this telemetryContext
     * @param instrumentationKey the instrumentationkey for this application
     */
    protected PublicTelemetryContext(Context context, String instrumentationKey) {
        super(context, instrumentationKey);
    }
}
