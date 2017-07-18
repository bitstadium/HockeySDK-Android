package net.hockeyapp.android.util;


import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import net.hockeyapp.android.Constants;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ConstantsTest {

    @Test
    public void testLoadingConstantsWorks() {
        Constants.loadFromContext(InstrumentationRegistry.getContext());
        assertNotNull(Constants.BASE_URL);
        assertEquals("https://sdk.hockeyapp.net/", Constants.BASE_URL);
        assertNotNull(Constants.SDK_NAME);
        assertEquals("HockeySDK", Constants.SDK_NAME);
        assertNotNull(Constants.FILES_DIRECTORY_NAME);
        assertEquals("HockeyApp", Constants.FILES_DIRECTORY_NAME);
        assertEquals(1, Constants.UPDATE_PERMISSIONS_REQUEST);
        assertEquals("HockeyApp", Constants.FILES_DIRECTORY_NAME);
        assertNotNull(Constants.ANDROID_VERSION);
        assertNotNull(Constants.ANDROID_BUILD);
        assertNotNull(Constants.PHONE_MODEL);
        assertNotNull(Constants.PHONE_MANUFACTURER);
        assertNotNull(Constants.CRASH_IDENTIFIER);
        assertNotNull(Constants.DEVICE_IDENTIFIER);

        //TODO add tests for other constants, too.
    }
}
