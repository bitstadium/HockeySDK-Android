package net.hockeyapp.android;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CrashManagerTest extends InstrumentationTestCase {

    public static final String DUMMY_APP_IDENTIFIER = "12345678901234567890123456789012";

    @Before
    public void setUp() throws Exception {
        super.setUp();

        injectInstrumentation(InstrumentationRegistry.getInstrumentation());

        if (Constants.FILES_PATH == null) {
            Constants.loadFromContext(getInstrumentation().getTargetContext());
        }
    }

    @Test
    public void registerCrashManagerWorks() {
        // verify that registering the Crash Manager works (e.g. it's not throwing any exception)
        CrashManager.register(getInstrumentation().getTargetContext(), DUMMY_APP_IDENTIFIER);

        // verify that there were no crashes in the last session
        assertFalse(CrashManager.didCrashInLastSession());
    }

    @Test
    public void crashInLastSessionRecognized() {
        Throwable tr = new RuntimeException("Just a test exception");
        ExceptionHandler.saveException(tr, Thread.currentThread(), null);
        assertNotNull(Constants.FILES_PATH);

        CrashManager.register(getInstrumentation().getTargetContext(), DUMMY_APP_IDENTIFIER);

        assertTrue(CrashManager.didCrashInLastSession());
    }


}
