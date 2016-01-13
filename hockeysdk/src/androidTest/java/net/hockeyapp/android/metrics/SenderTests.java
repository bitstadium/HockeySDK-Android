package net.hockeyapp.android.metrics;

import android.test.InstrumentationTestCase;

import junit.framework.Assert;

import java.io.File;
import java.net.HttpURLConnection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SenderTests extends InstrumentationTestCase {

    private Sender sut;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext()
                .getCacheDir().getPath());

        Persistence mockPersistence = mock(PublicPersistence.class);
        when(mockPersistence.nextAvailableFileInDirectory()).thenReturn(mock(File.class));
        when(mockPersistence.load(mock(File.class))).thenReturn("SomethingToTest");
        sut = new Sender();
        sut.setPersistence(mockPersistence);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testInitialisationWorks() throws Exception {
        Assert.assertNotNull(sut);
        Assert.assertEquals(0, sut.requestCount());
        Assert.assertNotNull(sut.getPersistence());
        assertNull(sut.getCustomServerURL());
    }

    public void testCreateConnection() {
        HttpURLConnection connection = sut.createConnection();
        assertNotNull(connection);

    }

    public void testSending() {
        HttpURLConnection connection1 = sut.createConnection();
        File mockFile1 = mock(File.class);
        Persistence publicPersistence = new PublicPersistence(getInstrumentation().getContext(), mock
                (File.class), null);
        sut.setPersistence(publicPersistence);
        sut.triggerSendingForTesting(connection1, mockFile1, "test1");
        Assert.assertEquals(1, sut.requestCount());
        spy(publicPersistence).nextAvailableFileInDirectory();
        spy(publicPersistence).load(mockFile1);

        File mockFile2 = mock(File.class);
        HttpURLConnection connection2 = sut.createConnection();
        sut.triggerSendingForTesting(connection2, mockFile2, "test2");
        Assert.assertEquals(2, sut.requestCount());
        spy(sut.getPersistence()).nextAvailableFileInDirectory();
        spy(sut.getPersistence()).load(mockFile2);
    }

    public void testResponseCodeHandling() {
        int[] recoverableCodes = new int[]{408, 429, 500, 503, 511};
        int[] successCodes = new int[]{200, 201, 202, 203};
        int[] errorsAndWhatNot = new int[]{100, 400, 403, 405};

        for (int code : recoverableCodes) {
            assertTrue(sut.isRecoverableError(code));
        }
        for (int code : successCodes) {
            assertFalse(sut.isRecoverableError(code));
        }
        for (int code : errorsAndWhatNot) {
            assertFalse(sut.isRecoverableError(code));
        }

        for (int code : successCodes) {
            assertTrue(sut.isExpected(code));
        }
        for (int code : recoverableCodes) {
            assertFalse(sut.isExpected(code));
        }
        for (int code : errorsAndWhatNot) {
            assertFalse(sut.isExpected(code));
        }
    }

    public void testFilesGetDeletedAfterUnrecoverable() {
        File mockFile1 = mock(File.class);
        sut.onResponse(sut.createConnection(), 501, "test", mockFile1);
        verify(sut.getPersistence()).deleteFile(mockFile1);
    }

    public void testFilesGetUnblockedForRecoverableError() {
        File mockFile = mock(File.class);
        sut.onResponse(sut.createConnection(), 500, "test", mockFile);
        verify(sut.getPersistence()).makeAvailable(mockFile);
    }

}
