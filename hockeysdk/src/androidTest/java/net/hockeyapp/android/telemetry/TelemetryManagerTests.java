package net.hockeyapp.android.telemetry;

import android.app.Application;
import android.test.InstrumentationTestCase;

import static org.mockito.Mockito.mock;

public class TelemetryManagerTests extends InstrumentationTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext()
                .getCacheDir()
                .getPath());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testInitializationWorks() {
        TelemetryContext mockTelemetryContext = mock(PublicTelemetryContext.class);
        Persistence mockPersistence = mock(PublicPersistence.class);
        Channel mockChannel = mock(PublicChannel.class);
        Sender mockSender = mock(PublicSender.class);
        TelemetryManager sut = new TelemetryManager(getInstrumentation().getContext(),
                mockTelemetryContext, mockSender, mockPersistence, mockChannel);
        assertNotNull(sut);
        assertNotNull(sut.getSender());
        assertNotNull(sut.getChannel());
        assertNotNull(sut.getSender().getPersistence());
    }

    public void testRegisterWorks() {
        Persistence mockPersistence = mock(PublicPersistence.class);
        Channel mockChannel = mock(PublicChannel.class);
        Sender mockSender = mock(PublicSender.class);
        Application mockApplication = mock(Application.class);

        TelemetryManager.register(getInstrumentation().getContext(), mockApplication, "12345678901234567890123456789032",
                mockSender, mockPersistence, mockChannel);

        assertNotNull(TelemetryManager.getSender());
        assertNotNull(TelemetryManager.getChannel());
        assertNotNull(TelemetryManager.getSender().getPersistence());
    }

}
