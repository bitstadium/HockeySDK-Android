package net.hockeyapp.android.telemetry;

import android.content.Context;

/*
 *  Class needed to mock dependencies. Most dependencies have private modifier to be not visible to
 *  the devs. But they have to be public to be mockable in the test target. This is why we need to
 *  create this kind of subclass with the modifier `public`.
 */
public class PublicChannel extends Channel {

    /**
     * Instantiates a new INSTANCE of Channel
     *
     * @param telemetryContext
     */
    public PublicChannel(TelemetryContext telemetryContext) {
        super(telemetryContext);
    }
}
