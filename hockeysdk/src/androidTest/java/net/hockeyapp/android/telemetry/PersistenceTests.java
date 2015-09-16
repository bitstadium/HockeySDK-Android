package net.hockeyapp.android.telemetry;

import android.content.Context;
import android.test.InstrumentationTestCase;

import junit.framework.Assert;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.*;

public class PersistenceTests extends InstrumentationTestCase {

    private PublicPersistence sut;

    public void setUp() throws Exception {
        super.setUp();

        System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());
        Context mockContext = getInstrumentation().getContext();
        sut = new PublicPersistence(mockContext);
    }

    public void testNewInstanceWasInitialisedCorrectly() {
        Assert.assertNotNull(sut);
        Assert.assertNotNull(sut.servedFiles);
    }

    public void testCallingPersistWillWriteToDisk() {
        PublicPersistence spy = spy(sut);
        String[] testData = {"test", "data"};

        spy.persist(testData);

        verify(spy).writeToDisk(anyString());
    }


}