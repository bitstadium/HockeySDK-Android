package net.hockeyapp.android.metrics;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(AndroidJUnit4.class)
public class PersistenceTests {

    private PublicPersistence sut;

    @Before
    public void setUp() throws Exception {
        Context context = InstrumentationRegistry.getContext();
        Sender mockSender = mock(Sender.class);
        sut = new PublicPersistence(context, mockSender);
        mockSender.setPersistence(sut);
    }

    @Test
    public void testInstanceInitialisation() {
        assertNotNull(sut);
        assertNotNull(sut.mServedFiles);
    }

    @Test
    public void testCallingPersistTriggersWriteToDisk() {
        Context context = InstrumentationRegistry.getContext();
        Sender mockSender = mock(Sender.class);
        PublicPersistence sut = new PublicPersistence(context, mockSender);

        Persistence spy = spy(sut);
        String[] testData = {"test", "data"};
        String testSerializedString = "test\ndata";

        spy.persist(testData);
        verify(spy).writeToDisk(testSerializedString);
    }

    @Test
    public void testDeleteFileWorks() {
        File mockFile = mock(File.class);
        when(mockFile.delete()).thenReturn(true);
        sut.mServedFiles = mock(ArrayList.class);

        sut.deleteFile(mockFile);

        verify(mockFile).delete();
        verify(sut.mServedFiles).remove(mockFile);
    }

    @Test
    public void testMakeAvailableUnblocksFile() {
        File mockFile = mock(File.class);
        sut.mServedFiles = mock(ArrayList.class);

        sut.makeAvailable(mockFile);

        verify(sut.mServedFiles).remove(mockFile);
        verifyNoMoreInteractions(sut.mServedFiles);
    }

    @Test
    public void testNextFileRequestReturnsUnreservedFile() {
        // Mock file system with 2 files
        File mockDirectory = mock(File.class);
        File mockFile1 = mock(File.class);
        File mockFile2 = mock(File.class);
        File[] mockFiles = {mockFile1, mockFile2};
        when(mockDirectory.listFiles()).thenReturn(mockFiles);

        sut = spy(new PublicPersistence(InstrumentationRegistry.getContext(), null));
        when(sut.getTelemetryDirectory()).thenReturn(mockDirectory);

        // Mock served list containing 1 file
        ArrayList<File> servedFiles = new ArrayList<>();
        servedFiles.add(mockFile1);
        sut.mServedFiles = servedFiles;

        // Test hasFilesAvailable
        assertTrue(sut.hasFilesAvailable());
        assertTrue(!sut.mServedFiles.contains(mockFile2));
        assertTrue(sut.hasFilesAvailable());

        // Test one unreserved file left
        File result = sut.nextAvailableFileInDirectory();
        assertEquals(mockFile2, result);
        assertTrue(sut.mServedFiles.contains(mockFile2));

        // Test all files are already in use
        result = sut.nextAvailableFileInDirectory();
        assertNull(result);
    }

    @Test
    public void testloadFileWorks() {
        //TODO: Write test after sender integration
    }
}
