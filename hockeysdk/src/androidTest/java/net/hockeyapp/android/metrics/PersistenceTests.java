package net.hockeyapp.android.metrics;

import android.content.Context;
import android.test.InstrumentationTestCase;

import junit.framework.Assert;

import java.io.File;
import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class PersistenceTests extends InstrumentationTestCase {

    private PublicPersistence sut;

    public void setUp() throws Exception {
        super.setUp();

        System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());
        Context mockContext = getInstrumentation().getContext();
        Sender mockSender = mock(Sender.class);
        sut = new PublicPersistence(mockContext, mockSender);
        mockSender.setPersistence(sut);
    }

    public void testInstanceInitialisation() {
        Assert.assertNotNull(sut);
        Assert.assertNotNull(sut.mServedFiles);
    }

    public void testTelemetryDirectoryGetsCreated() {
        File spy = spy(new File("/my/test/directory/"));

        sut = new PublicPersistence(getInstrumentation().getContext(), spy, null);

        verify(spy).mkdirs();
    }

    public void testCallingPersistTriggersWriteToDisk() {
        PublicPersistence spy = spy(sut);
        String[] testData = {"test", "data"};
        String testSerializedString = "test\ndata";

        spy.persist(testData);

        verify(spy).writeToDisk(testSerializedString);
    }

    public void testDeleteFileWorks() {
        File mockFile = mock(File.class);
        when(mockFile.delete()).thenReturn(true);
        sut.mServedFiles = mock(ArrayList.class);

        sut.deleteFile(mockFile);

        verify(mockFile).delete();
        verify(sut.mServedFiles).remove(mockFile);
    }

    public void testMakeAvailableUnblocksFile() {
        File mockFile = mock(File.class);
        sut.mServedFiles = mock(ArrayList.class);

        sut.makeAvailable(mockFile);

        verify(sut.mServedFiles).remove(mockFile);
        verifyNoMoreInteractions(sut.mServedFiles);
    }

    public void testNextFileRequestReturnsUnreservedFile() {
        // Mock file system with 2 files
        File mockDirectory = mock(File.class);
        File mockFile1 = mock(File.class);
        File mockFile2 = mock(File.class);
        File[] mockFiles = {mockFile1, mockFile2};
        sut = new PublicPersistence(getInstrumentation().getContext(), mockDirectory, null);
        when(mockDirectory.listFiles()).thenReturn(mockFiles);

        // Mock served list containing 1 file
        ArrayList<File> servedFiles = new ArrayList<File>();
        servedFiles.add(mockFile1);
        sut.mServedFiles = servedFiles;

        // Test one unreserved file left
        File result = sut.nextAvailableFileInDirectory();
        Assert.assertEquals(mockFile2, result);
        Assert.assertTrue(sut.mServedFiles.contains(mockFile2));

        // Test all files are already in use
        result = sut.nextAvailableFileInDirectory();
        Assert.assertNull(result);
    }

    public void testloadFileWorks() {
        //TODO: Write test after sender integration
    }
}