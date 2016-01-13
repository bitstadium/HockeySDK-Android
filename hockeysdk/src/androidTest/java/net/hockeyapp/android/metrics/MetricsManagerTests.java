package net.hockeyapp.android.metrics;

import android.app.Application;
import android.test.InstrumentationTestCase;

import static org.mockito.Mockito.mock;

public class MetricsManagerTests extends InstrumentationTestCase {

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
        MetricsManager sut = new MetricsManager(getInstrumentation().getContext(),
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

        MetricsManager.register(getInstrumentation().getContext(), mockApplication, "12345678901234567890123456789032",
                mockSender, mockPersistence, mockChannel);

        assertNotNull(MetricsManager.getSender());
        assertNotNull(MetricsManager.getChannel());
        assertNotNull(MetricsManager.getSender().getPersistence());
    }

}
