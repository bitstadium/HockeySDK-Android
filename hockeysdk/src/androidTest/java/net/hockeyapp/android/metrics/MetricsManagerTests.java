package net.hockeyapp.android.metrics;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import net.hockeyapp.android.util.DummyExecutor;
import net.hockeyapp.android.utils.AsyncTaskUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Executor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(AndroidJUnit4.class)
public class MetricsManagerTests {

    @Test
    public void initializationWorks() {
        PublicTelemetryContext mockTelemetryContext = mock(PublicTelemetryContext.class);
        PublicChannel mockChannel = mock(PublicChannel.class);
        PublicSender mockSender = mock(PublicSender.class);
        PublicPersistence mockPersistence = mock(PublicPersistence.class);
        MetricsManager sut = new MetricsManager(InstrumentationRegistry.getContext(),
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
        when(mockApplication.getApplicationContext()).thenReturn(InstrumentationRegistry.getContext());

        MetricsManager.register(mockApplication, "12345678901234567890123456789032",
                mockSender, mockPersistence, mockChannel);
        assertNotNull(MetricsManager.getSender());
        assertNotNull(MetricsManager.getChannel());
    }

    @Test
    public void disableUserMetricsWorks() {
        Executor dummyExecutor = mock(DummyExecutor.class);
        AsyncTaskUtils.setCustomExecutor(dummyExecutor);

        MetricsManager.disableUserMetrics();

        assertFalse(MetricsManager.isUserMetricsEnabled());
        assertFalse(MetricsManager.sessionTrackingEnabled());

        verifyNoMoreInteractions(dummyExecutor);

        MetricsManager.trackEvent("Test event");

        AsyncTaskUtils.setCustomExecutor(null);
    }

}
