package net.hockeyapp.android;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * <h3>Description</h3>
 *
 * This class provides testing for the environment features, such as the SDK wide constants.
 */
@RunWith(AndroidJUnit4.class)
public class EnvironmentTest extends TestCase {

    /**
     * Test to verify basic creation of the external storage directory works.
     */
    @Test
    public void basicStorageDirTest() {
        File storageDir = Constants.getHockeyAppStorageDir(InstrumentationRegistry.getTargetContext());

        assertNotNull(storageDir);
        assertTrue(storageDir.exists());
    }
}
