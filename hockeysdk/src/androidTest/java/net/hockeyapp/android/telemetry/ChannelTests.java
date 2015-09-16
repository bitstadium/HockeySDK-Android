package net.hockeyapp.android.telemetry;

import android.test.InstrumentationTestCase;

import junit.framework.Assert;

import net.hockeyapp.android.utils.Util;

import java.util.HashMap;
import java.util.Map;

import static  org.mockito.Mockito.*;

public class ChannelTests extends InstrumentationTestCase {

    private PublicChannel sut;
    private PublicTelemetryContext mockTelemetryContext;
    private PublicPersistence mockPersistence;

    public void setUp() throws Exception {
        super.setUp();

        System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());
        mockTelemetryContext = getMockTelemetryContext();
        mockPersistence = mock(PublicPersistence.class);
        sut = new PublicChannel(mockTelemetryContext, mockPersistence);
    }

    public void testNewInstanceWasInitialisedCorrectly() {
        Assert.assertNotNull(sut);
        Assert.assertNotNull(sut.telemetryContext);
        Assert.assertEquals(mockTelemetryContext, sut.telemetryContext);
        Assert.assertNotNull(sut.queue);
        Assert.assertEquals(0, sut.queue.size());
    }

    public void testLoggingItemAddsItToQueue() {
        Data<Domain> data = new Data<Domain>();
        Channel.MAX_BATCH_COUNT = 3;
        Assert.assertEquals(0, sut.queue.size());

        sut.log(data);
        Assert.assertEquals(1, sut.queue.size());
    }

    public void testQueueFlushesWhenMaxBatchCountReached() {
        PublicChannel.MAX_BATCH_COUNT = 3;
        Assert.assertEquals(0, sut.queue.size());

        sut.log(new Data<Domain>());
        Assert.assertEquals(1, sut.queue.size());

        sut.log(new Data<Domain>());
        Assert.assertEquals(2, sut.queue.size());

        sut.log(new Data<Domain>());
        Assert.assertEquals(0, sut.queue.size());

        verify(mockPersistence).persist(any(String[].class));
    }

    public void testCreateEnvelopeForTelemetryDataWorks() {
        SessionStateData sessionStateData = new SessionStateData();
        sessionStateData.setState(SessionState.START);
        Data<Domain> testData = new Data<Domain>();
        testData.setBaseData(sessionStateData);
        testData.setBaseType(sessionStateData.getBaseType());
        testData.QualifiedName = sessionStateData.getEnvelopeName();

        Envelope result = sut.createEnvelope(testData);

        Assert.assertNotNull(result);
        Assert.assertEquals(MOCK_APP_ID, result.getAppId());
        Assert.assertEquals(MOCK_APP_VER, result.getAppVer());
        Assert.assertNotNull(result.getTime());
        Assert.assertEquals(MOCK_IKEY, result.getIKey());
        Assert.assertEquals(MOCK_OS_VER, result.getOsVer());
        Assert.assertEquals(MOCK_OS, result.getOs());
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

    // Helper
    private static final String MOCK_APP_ID = "appId";
    private static final String MOCK_APP_VER = "appVer";
    private static final String MOCK_IKEY = "iKey";
    private static final String MOCK_OS_VER = "osVer";
    private static final String MOCK_OS = "os";
    private static final String MOCK_TAGS_KEY = "tagsKey";
    private static final String MOCK_TAGS_VALUE = "tagsValue";

    private static PublicTelemetryContext getMockTelemetryContext() {
        HashMap<String, String> tags = new HashMap<String, String>();
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
}