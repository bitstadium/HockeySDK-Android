package net.hockeyapp.android.metrics;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import junit.framework.Assert;

import net.hockeyapp.android.metrics.model.Data;
import net.hockeyapp.android.metrics.model.Domain;
import net.hockeyapp.android.metrics.model.Envelope;
import net.hockeyapp.android.metrics.model.SessionState;
import net.hockeyapp.android.metrics.model.SessionStateData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class ChannelTests extends InstrumentationTestCase {

    // Helper
    private static final String MOCK_APP_ID = "appId";
    private static final String MOCK_APP_VER = "appVer";
    private static final String MOCK_IKEY = "iKey";
    private static final String MOCK_OS_VER = "osVer";
    private static final String MOCK_OS = "os";
    private static final String MOCK_TAGS_KEY = "tagsKey";
    private static final String MOCK_TAGS_VALUE = "tagsValue";
    private PublicChannel sut;
    private PublicTelemetryContext mockTelemetryContext;
    private PublicPersistence mockPersistence;

    private static PublicTelemetryContext getMockTelemetryContext() {
        HashMap<String, String> tags = new HashMap<>();
        tags.put(MOCK_TAGS_KEY, MOCK_TAGS_VALUE);

        PublicTelemetryContext mockContext = mock(PublicTelemetryContext.class);
        when(mockContext.getPackageName()).thenReturn(MOCK_APP_ID);
        when(mockContext.getContextTags()).thenReturn(tags);
        when(mockContext.getAppVersion()).thenReturn(MOCK_APP_VER);
        when(mockContext.getInstrumentationKey()).thenReturn(MOCK_IKEY);
        when(mockContext.getOsVersion()).thenReturn(MOCK_OS_VER);
        when(mockContext.getOsName()).thenReturn(MOCK_OS);

        return mockContext;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        injectInstrumentation(InstrumentationRegistry.getInstrumentation());

        mockTelemetryContext = getMockTelemetryContext();
        mockPersistence = mock(PublicPersistence.class);
        sut = new PublicChannel(mockTelemetryContext, mockPersistence);
    }

    @Test
    public void testInstanceInitialisation() {
        Assert.assertNotNull(sut);
        Assert.assertNotNull(sut.mTelemetryContext);
        Assert.assertEquals(mockTelemetryContext, sut.mTelemetryContext);
        Assert.assertNotNull(sut.mQueue);
        Assert.assertEquals(0, sut.mQueue.size());
    }

    @Test
    public void testLoggingItemAddsToQueue() {
        Data<Domain> data = new Data<>();
        Assert.assertEquals(0, sut.mQueue.size());

        sut.enqueueData(data);
        Assert.assertEquals(1, sut.mQueue.size());
    }

    @Test
    public void testQueueFlushesWhenMaxBatchCountReached() {
        Assert.assertEquals(0, sut.mQueue.size());

        for (int i = 1; i < Channel.getMaxBatchCount(); i++) {
            sut.enqueueData(new Data<>());
            Assert.assertEquals(i, sut.mQueue.size());
        }

        sut.enqueueData(new Data<>());
        Assert.assertEquals(0, sut.mQueue.size());

        verify(mockPersistence).persist(any(String[].class));
    }

    @Test
    public void testCreateEnvelopeForTelemetryData() {
        SessionStateData sessionStateData = new SessionStateData();
        sessionStateData.setState(SessionState.START);
        Data<Domain> testData = new Data<>();
        testData.setBaseData(sessionStateData);
        testData.setBaseType(sessionStateData.getBaseType());
        testData.QualifiedName = sessionStateData.getEnvelopeName();

        Envelope result = sut.createEnvelope(testData);

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getTime());
        Assert.assertEquals(MOCK_IKEY, result.getIKey());
        Assert.assertNotNull(result.getTags());
        Assert.assertEquals(1, result.getTags().size());
        Assert.assertTrue(result.getTags().containsKey(MOCK_TAGS_KEY));
        Assert.assertEquals(MOCK_TAGS_VALUE, result.getTags().get(MOCK_TAGS_KEY));
        Assert.assertNotNull(result.getData());
        SessionState actualState = ((SessionStateData) ((Data<Domain>) result.getData()).getBaseData()).getState();
        Assert.assertEquals(SessionState.START, actualState);
        String actualBaseType = result.getData().getBaseType();
        Assert.assertEquals(new SessionStateData().getBaseType(), actualBaseType);
    }
}