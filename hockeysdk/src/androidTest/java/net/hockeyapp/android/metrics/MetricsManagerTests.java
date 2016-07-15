package net.hockeyapp.android.metrics;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MetricsManagerTests extends InstrumentationTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());

    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void initializationWorks() {
        PublicTelemetryContext mockTelemetryContext = mock(PublicTelemetryContext.class);
        PublicChannel mockChannel = mock(PublicChannel.class);
        PublicSender mockSender = mock(PublicSender.class);
        PublicPersistence mockPersistence = mock(PublicPersistence.class);
        MetricsManager sut = new MetricsManager(getInstrumentation().getContext(),
                mockTelemetryContext, mockSender, mockPersistence, mockChannel);
        verify(mockSender).setPersistence(mockPersistence);
        verify(mockPersistence).setSender(mockSender);
        assertNotNull(sut);
        assertNotNull(sut.getSender());
        assertNotNull(sut.getChannel());
    }

    @Test
    public void registerWorks() {
        Persistence mockPersistence = mock(PublicPersistence.class);
        Channel mockChannel = mock(PublicChannel.class);
        Sender mockSender = mock(PublicSender.class);
        Application mockApplication = mock(Application.class);
        when(mockApplication.getApplicationContext()).thenReturn(getInstrumentation().getContext());

        MetricsManager.register(getInstrumentation().getContext(), mockApplication, "12345678901234567890123456789032",
                mockSender, mockPersistence, mockChannel);
        assertNotNull(MetricsManager.getSender());
        assertNotNull(MetricsManager.getChannel());
    }

}
