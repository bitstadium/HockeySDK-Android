package net.hockeyapp.android.telemetry;

import android.content.Context;
import android.test.InstrumentationTestCase;

import junit.framework.Assert;

import java.io.File;
import java.util.ArrayList;

import static org.mockito.Mockito.*;

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

    public void testNewInstanceWasInitialisedCorrectly() {
        Assert.assertNotNull(sut);
        Assert.assertNotNull(sut.servedFiles);
    }

    public void testTelemetryDirIsGetsCreated (){
        File spy = spy(new File("/my/test/directory/"));

        sut = new PublicPersistence(getInstrumentation().getContext(), spy, null);

        verify(spy).mkdirs();
    }

    public void testCallingPersistWillWriteToDisk() {
        PublicPersistence spy = spy(sut);
        String[] testData = {"test", "data"};
        String testSerializedString = "test\ndata";

        spy.persist(testData);

        verify(spy).writeToDisk(testSerializedString);
    }

    public void testDeleteFileWorks() {
        File mockFile = mock(File.class);
        when(mockFile.delete()).thenReturn(true);
        sut.servedFiles = mock(ArrayList.class);

        sut.deleteFile(mockFile);

        verify(mockFile).delete();
        verify(sut.servedFiles).remove(mockFile);
    }

    public void testMakeAvailableUnblockesFile() {
        File mockFile = mock(File.class);
        sut.servedFiles = mock(ArrayList.class);

        sut.makeAvailable(mockFile);

        verify(sut.servedFiles).remove(mockFile);
        verifyNoMoreInteractions(sut.servedFiles);
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
        sut.servedFiles = servedFiles;

        // Test one unreserved file left
        File result = sut.nextAvailableFileInDirectory();
        Assert.assertEquals(mockFile2, result);
        Assert.assertTrue(sut.servedFiles.contains(mockFile2));

        // Test all files are already in use
        result = sut.nextAvailableFileInDirectory();
        Assert.assertNull(result);
    }

    public void testloadFileWorks() {
        //TODO: Write test after sender integration
    }
}