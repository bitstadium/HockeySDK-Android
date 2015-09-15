package net.hockeyapp.android.telemetry;

import android.test.InstrumentationTestCase;

import static  org.mockito.Mockito.*;

public class ChannelTests extends InstrumentationTestCase {

    private Channel sut;
    private PublicTelemetryContext mockTelemetryContext;

    public void setUp() throws Exception {
        super.setUp();

        System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());

        mockTelemetryContext = mock(PublicTelemetryContext.class);
        sut = new Channel(mockTelemetryContext);
    }

    public void testMockito(){
        Data mockData = mock(Data.class);
        sut.createEnvelope(mockData);
        verify(mockTelemetryContext).updateScreenResolution();
    }

}