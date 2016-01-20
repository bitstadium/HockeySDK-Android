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

    public void testInstanceInitialisation() {
        Assert.assertNotNull(sut);
        Assert.assertNotNull(sut.getInstrumentationKey());
        Assert.assertNotNull(sut.mContext);
        Assert.assertNotNull(sut.mDevice);
        Assert.assertNotNull(sut.mUser);
        Assert.assertNotNull(sut.mInternal);
        Assert.assertNotNull(sut.mApplication);
    }

    public void testContextIsAccessible() {

        // Device context
        sut.setDeviceModel("Model");
        Assert.assertEquals(sut.mDevice.getModel(), sut.getDeviceModel());

        sut.setDeviceType("Type");
        Assert.assertEquals(sut.mDevice.getType(), sut.getDeviceType());

        sut.setOsVersion("OsVersion");
        Assert.assertEquals(sut.mDevice.getOsVersion(), sut.getOsVersion());

        sut.setOsName("Os");
        Assert.assertEquals(sut.mDevice.getOs(), sut.getOsName());

        sut.setDeviceId("DeviceId");
        Assert.assertEquals(sut.mDevice.getId(), sut.getDeviceId());

        sut.setOsLocale("OsLocale");
        Assert.assertEquals(sut.mDevice.getLocale(), sut.getOsLocale());

        sut.setScreenResolution("ScreenResolution");
        Assert.assertEquals(sut.mDevice.getScreenResolution(), sut.getScreenResolution());

        sut.setDeviceOemName("OemName");
        Assert.assertEquals(sut.mDevice.getOemName(), sut.getDeviceOemName());

        // Internal context
        sut.setSdkVersion("SdkVersion");
        Assert.assertEquals(sut.mInternal.getSdkVersion(), sut.getSdkVersion());

        // Application context
        sut.setAppVersion("Version");
        Assert.assertEquals(sut.mApplication.getVer(), sut.getAppVersion());

        // User context
        sut.setAnonymousUserId("AnonymousUserId");
        Assert.assertEquals(sut.mUser.getId(), sut.getAnonymousUserId());

        // Session context
        sut.setSessionId("SessionId");
        Assert.assertEquals(sut.mSession.getId(), sut.getSessionId());

        sut.setIsFirstSession("IsFirstSession");
        Assert.assertEquals(sut.mSession.getIsFirst(), sut.getIsFirstSession());

        sut.setIsNewSession("IsNewSession");
        Assert.assertEquals(sut.mSession.getIsNew(), sut.getIsNewSession());
    }

}