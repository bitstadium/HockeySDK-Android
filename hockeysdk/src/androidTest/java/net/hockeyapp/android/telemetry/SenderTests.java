package net.hockeyapp.android.telemetry;

import android.test.InstrumentationTestCase;

import junit.framework.Assert;

import java.io.File;
import java.net.HttpURLConnection;

import static org.mockito.Mockito.mock;
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
    sut.triggerSendingForTesting(connection1, mockFile1, "test1");
    Assert.assertEquals(1, sut.requestCount());
    verify(sut.getPersistence()).nextAvailableFileInDirectory();
    verify(sut.getPersistence()).load(mockFile1);

    File mockFile2 = mock(File.class);
    HttpURLConnection connection2 = sut.createConnection();
    sut.triggerSendingForTesting(connection2, mockFile2, "test2");
    Assert.assertEquals(2, sut.requestCount());
    verify(sut.getPersistence()).nextAvailableFileInDirectory();
    verify(sut.getPersistence()).load(mockFile2);
  }
}
