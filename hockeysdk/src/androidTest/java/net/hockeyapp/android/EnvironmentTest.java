package net.hockeyapp.android;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * <h3>Description</h3>
 *
 * This class provides testing for the environment features, such as the SDK wide constants.
 */
@RunWith(AndroidJUnit4.class)
public class EnvironmentTest {

    /*
     * This test is disabled since it is always failing on emulators with API under 18. The problem
     * is that emulators start with external storage being in removed state and necessary directory
     * can not be created.
     *
     * TODO: Uncomment test in case emulators with API under 18 start supporting external storage
     */
    /**
     * Test to verify basic creation of the external storage directory works.
     */
    //@Test
    public void basicStorageDirTest() {
        File storageDir = Constants.getHockeyAppStorageDir(InstrumentationRegistry.getTargetContext());

        assertNotNull(storageDir);
        assertTrue(storageDir.exists());
    }

    @Test
    public void dummyTestMethod() {}
}
