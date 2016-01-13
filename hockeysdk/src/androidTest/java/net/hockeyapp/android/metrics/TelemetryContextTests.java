package net.hockeyapp.android.metrics;

import android.test.InstrumentationTestCase;

import junit.framework.Assert;

public class TelemetryContextTests extends InstrumentationTestCase {

  private PublicTelemetryContext sut;

  public void setUp() throws Exception {
    super.setUp();
    System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir()
      .getPath());
    sut = new PublicTelemetryContext(getInstrumentation().getContext(),
      "a123b4567cde890abcd1e2f3ab456789");//this is a made-up app identifier.
  }

  public void testNewInstanceWasInitialisedCorrectly() {
    Assert.assertNotNull(sut);
    Assert.assertNotNull(sut.getInstrumentationKey());
    Assert.assertNotNull(sut.context);
    Assert.assertNotNull(sut.device);
    Assert.assertNotNull(sut.user);
    Assert.assertNotNull(sut.internal);
    Assert.assertNotNull(sut.application);
  }

  public void testContextIsAccessible() {

    // Device context
    sut.setDeviceModel("Model");
    Assert.assertEquals(sut.device.getModel(), sut.getDeviceModel());

    sut.setDeviceType("Type");
    Assert.assertEquals(sut.device.getType(), sut.getDeviceType());

    sut.setOsVersion("OsVersion");
    Assert.assertEquals(sut.device.getOsVersion(), sut.getOsVersion());

    sut.setOsName("Os");
    Assert.assertEquals(sut.device.getOs(), sut.getOsName());

    sut.setDeviceId("DeviceId");
    Assert.assertEquals(sut.device.getId(), sut.getDeviceId());

    sut.setOsLocale("OsLocale");
    Assert.assertEquals(sut.device.getLocale(), sut.getOsLocale());

    sut.setScreenResolution("ScreenResolution");
    Assert.assertEquals(sut.device.getScreenResolution(), sut.getScreenResolution());

    sut.setDeviceOemName("OemName");
    Assert.assertEquals(sut.device.getOemName(), sut.getDeviceOemName());

    // Internal context
    sut.setSdkVersion("SdkVersion");
    Assert.assertEquals(sut.internal.getSdkVersion(), sut.getSdkVersion());

    // Application context
    sut.setAppVersion("Version");
    Assert.assertEquals(sut.application.getVer(), sut.getAppVersion());

    // User context
    sut.setAnonymousUserId("AnonymousUserId");
    Assert.assertEquals(sut.user.getId(), sut.getAnonymousUserId());

    // Session context
    sut.setSessionId("SessionId");
    Assert.assertEquals(sut.session.getId(), sut.getSessionId());

    sut.setIsFirstSession("IsFirstSession");
    Assert.assertEquals(sut.session.getIsFirst(), sut.getIsFirstSession());

    sut.setIsNewSession("IsNewSession");
    Assert.assertEquals(sut.session.getIsNew(), sut.getIsNewSession());
  }

}