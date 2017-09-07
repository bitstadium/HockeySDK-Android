package net.hockeyapp.android.metrics;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import net.hockeyapp.android.utils.AsyncTaskUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.concurrent.Executor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(AndroidJUnit4.class)
public class SenderTests {

    private Persistence mockPersistence;
    private Sender sut;

    @Before
    public void setUp() throws Exception {
        mockPersistence = mock(PublicPersistence.class);
        when(mockPersistence.nextAvailableFileInDirectory()).thenReturn(mock(File.class), (File)null);
        when(mockPersistence.load(mock(File.class))).thenReturn("SomethingToTest");
        sut = new Sender();
        sut.setPersistence(mockPersistence);

        // Run all tasks synchronously
        AsyncTaskUtils.setCustomExecutor(new Executor() {
            @Override
            public void execute(@NonNull Runnable runnable) {
                runnable.run();
            }
        });
    }

    @After
    public void tearDown() {
        AsyncTaskUtils.setCustomExecutor(null);
    }

    @Test
    public void testInstanceInitialisation() throws Exception {
        assertNotNull(sut);
        assertEquals(0, sut.requestCount());
        assertNotNull(sut.getPersistence());
        assertNull(sut.getCustomServerURL());
    }

    @Test
    public void testCreateConnection() {
        HttpURLConnection connection = sut.createConnection();
        assertNotNull(connection);

    }

    @Test
    public void testSending() {
        sut.sendAvailableFiles();

        // Second - null check
        verify(mockPersistence, times(2)).nextAvailableFileInDirectory();

        // Should be decremented back in onResponse
        assertEquals(0, sut.requestCount());
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
