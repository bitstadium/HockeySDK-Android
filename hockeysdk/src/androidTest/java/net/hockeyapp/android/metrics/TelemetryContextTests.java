package net.hockeyapp.android.metrics;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TelemetryContextTests {

    private PublicTelemetryContext sut;

    @Before
    public void setUp() throws Exception {
        sut = new PublicTelemetryContext(InstrumentationRegistry.getContext(),
                "a123b4567cde890abcd1e2f3ab456789");//this is a made-up app identifier.
    }

    @Test
    public void testInstanceInitialisation() {
        assertNotNull(sut);
        assertNotNull(sut.getInstrumentationKey());
        assertNotNull(sut.mContext);
        assertNotNull(sut.mDevice);
        assertNotNull(sut.mUser);
        assertNotNull(sut.mInternal);
        assertNotNull(sut.mApplication);
    }

    @Test
    public void testContextIsAccessible() {

        // Device context
        sut.setDeviceModel("Model");
        assertEquals(sut.mDevice.getModel(), sut.getDeviceModel());

        sut.setDeviceType("Type");
        assertEquals(sut.mDevice.getType(), sut.getDeviceType());

        sut.setOsVersion("OsVersion");
        assertEquals(sut.mDevice.getOsVersion(), sut.getOsVersion());

        sut.setOsName("Os");
        assertEquals(sut.mDevice.getOs(), sut.getOsName());

        sut.setDeviceId("DeviceId");
        assertEquals(sut.mDevice.getId(), sut.getDeviceId());

        sut.setOsLocale("OsLocale");
        assertEquals(sut.mDevice.getLocale(), sut.getOsLocale());

        sut.setScreenResolution("ScreenResolution");
        assertEquals(sut.mDevice.getScreenResolution(), sut.getScreenResolution());

        sut.setDeviceOemName("OemName");
        assertEquals(sut.mDevice.getOemName(), sut.getDeviceOemName());

        // Internal context
        sut.setSdkVersion("SdkVersion");
        assertEquals(sut.mInternal.getSdkVersion(), sut.getSdkVersion());

        // Application context
        sut.setAppVersion("Version");
        assertEquals(sut.mApplication.getVer(), sut.getAppVersion());

        // User context
        sut.setAnonymousUserId("AnonymousUserId");
        assertEquals(sut.mUser.getId(), sut.getAnonymousUserId());

        // Session context
        sut.setSessionId("SessionId");
        assertEquals(sut.mSession.getId(), sut.getSessionId());

        sut.setIsFirstSession("IsFirstSession");
        assertEquals(sut.mSession.getIsFirst(), sut.getIsFirstSession());

        sut.setIsNewSession("IsNewSession");
        assertEquals(sut.mSession.getIsNew(), sut.getIsNewSession());
    }
}
