package net.hockeyapp.android;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import net.hockeyapp.android.util.StacktraceFilenameFilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ExceptionHandlerTest {

    private File filesDirectory;

    @Before
    public void setUp() throws Exception {
        CrashManagerHelper.loadConstants(InstrumentationRegistry.getTargetContext());
        CrashManagerHelper.reset(InstrumentationRegistry.getTargetContext());
        filesDirectory = CrashManagerHelper.cleanFiles(InstrumentationRegistry.getTargetContext());
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
