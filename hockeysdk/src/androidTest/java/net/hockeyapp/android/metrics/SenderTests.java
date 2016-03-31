package net.hockeyapp.android.metrics;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.HttpURLConnection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class SenderTests extends InstrumentationTestCase {

    private Sender sut;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        injectInstrumentation(InstrumentationRegistry.getInstrumentation());

        Persistence mockPersistence = mock(PublicPersistence.class);
        when(mockPersistence.nextAvailableFileInDirectory()).thenReturn(mock(File.class));
        when(mockPersistence.load(mock(File.class))).thenReturn("SomethingToTest");
        sut = new Sender();
        sut.setPersistence(mockPersistence);
    }

    @Test
    public void testInstanceInitialisation() throws Exception {
        Assert.assertNotNull(sut);
        Assert.assertEquals(0, sut.requestCount());
        Assert.assertNotNull(sut.getPersistence());
        assertNull(sut.getCustomServerURL());
    }

    @Test
    public void testCreateConnection() {
        HttpURLConnection connection = sut.createConnection();
        assertNotNull(connection);

    }

    @Test
    public void testSending() {
//        Sender sut = new Sender();
        HttpURLConnection connection1 = sut.createConnection();
        File mockFile1 = mock(File.class);

        PublicPersistence persistenceMock = mock(PublicPersistence.class);
        when(persistenceMock.nextAvailableFileInDirectory()).thenReturn(mock(File.class));

        sut.setPersistence(persistenceMock);
        sut.triggerSendingForTesting(connection1, mockFile1, "test1");
        Assert.assertEquals(1, sut.requestCount());

        sut.send();
        verify(persistenceMock).nextAvailableFileInDirectory();
    }



    @Test
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

    @Test
    public void testFilesGetDeletedAfterUnrecoverable() {
        File mockFile1 = mock(File.class);
        sut.onResponse(sut.createConnection(), 501, "test", mockFile1);
        verify(sut.getPersistence()).deleteFile(mockFile1);
    }

    @Test
    public void testFilesGetUnblockedForRecoverableError() {
        File mockFile = mock(File.class);
        sut.onResponse(sut.createConnection(), 500, "test", mockFile);
        verify(sut.getPersistence()).makeAvailable(mockFile);
    }

}
