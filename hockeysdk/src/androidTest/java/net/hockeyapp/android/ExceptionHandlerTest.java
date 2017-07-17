package net.hockeyapp.android;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import net.hockeyapp.android.util.StacktraceFilenameFilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(AndroidJUnit4.class)
public class ExceptionHandlerTest extends InstrumentationTestCase {

    private File filesDirectory;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        injectInstrumentation(InstrumentationRegistry.getInstrumentation());

        Constants.loadFromContext(getInstrumentation().getTargetContext());
        CrashManagerHelper.reset(getInstrumentation().getTargetContext());
        filesDirectory = CrashManagerHelper.cleanFiles(getInstrumentation().getTargetContext());
    }

    @SuppressWarnings("ThrowableInstanceNeverThrown")
    @Test
    public void saveExceptionTest() {

        fakeCrashReport();

        File[] files = filesDirectory.listFiles(new StacktraceFilenameFilter());
        assertEquals(1, files.length);

        fakeCrashReport();
        fakeCrashReport();

        files = filesDirectory.listFiles(new StacktraceFilenameFilter());
        assertEquals(3, files.length);
    }

    @SuppressWarnings("ThrowableInstanceNeverThrown")
    @Test
    public void saveExceptionCustomListenerTest() {

        Throwable tr = new RuntimeException("Just a test exception");

        ExceptionHandler.saveException(tr, null, new CrashManagerListener() {
        });

        File[] files = filesDirectory.listFiles(new StacktraceFilenameFilter());
        assertEquals(1, files.length);
    }

    private static void fakeCrashReport() {
        Throwable tr = new RuntimeException("Just a test exception");
        ExceptionHandler.saveException(tr, Thread.currentThread(), null);
    }

}
