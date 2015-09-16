package net.hockeyapp.android.telemetry;

import android.content.Context;
import android.test.InstrumentationTestCase;

public class PersistenceTests extends InstrumentationTestCase {

    private PublicPersistence sut;

    public void setUp() throws Exception {
        super.setUp();

        System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());
        Context mockContext = getInstrumentation().getContext();
        sut = new PublicPersistence(mockContext);
    }
}