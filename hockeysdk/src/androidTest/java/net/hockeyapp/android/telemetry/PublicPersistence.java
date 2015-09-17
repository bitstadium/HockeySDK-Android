package net.hockeyapp.android.telemetry;

import android.content.Context;

import java.io.File;

/*
 *  Class needed to mock dependencies. Most dependencies have private modifier to be not visible to
 *  the devs. But they have to be public to be mockable in the test target. This is why we need to
 *  create this kind of subclass with the modifier `public`.
 */
public class PublicPersistence extends Persistence {

    protected PublicPersistence(Context context, File telemetryDirectory) {
        super(context, telemetryDirectory);
    }

    public PublicPersistence(Context context) {
        super(context);
    }
}
