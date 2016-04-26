package net.hockeyapp.android.metrics;

import android.content.Context;

/*
 *  Class needed to mock dependencies. Most dependencies have private modifier to be not visible to
 *  the devs. But they have to be public to be mockable in the test target. This is why we need to
 *  create this kind of subclass with the modifier `public`.
 */
public class PublicTelemetryContext extends TelemetryContext {

    /**
     * Constructs a new INSTANCE of TelemetryContext.
     *
     * @param context       the context for this telemetryContext
     * @param appIdentifier the app identifier for this application
     */
    protected PublicTelemetryContext(Context context, String appIdentifier) {
        super(context, appIdentifier);
    }
}
