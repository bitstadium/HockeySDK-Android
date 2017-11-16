package net.hockeyapp.android;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import net.hockeyapp.android.util.StacktraceFilenameFilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.lang.ref.WeakReference;

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

    @Test
    public void maxFilesTest() {
        WeakReference<Context> weakContext = new WeakReference<>(InstrumentationRegistry.getTargetContext());
        String[] files;
        for (int i = 0; i < CrashManager.MAX_NUMBER_OF_CRASHFILES; i++) {
            files = CrashManager.searchForStackTraces(weakContext);
            assertNotNull(files);
            assertEquals(i, files.length);
            assertEquals(i, CrashManager.stackTracesCount);

            fakeCrashReport();
        }

        files = CrashManager.searchForStackTraces(weakContext);
        assertNotNull(files);
        assertEquals(CrashManager.MAX_NUMBER_OF_CRASHFILES, files.length);
        assertEquals(CrashManager.MAX_NUMBER_OF_CRASHFILES, CrashManager.stackTracesCount);

        fakeCrashReport();

        files = CrashManager.searchForStackTraces(weakContext);
        assertNotNull(files);
        assertEquals(CrashManager.MAX_NUMBER_OF_CRASHFILES, files.length);
        assertEquals(CrashManager.MAX_NUMBER_OF_CRASHFILES, CrashManager.stackTracesCount);

        fakeCrashReport();
        fakeCrashReport();

        files = CrashManager.searchForStackTraces(weakContext);
        assertNotNull(files);
        assertEquals(CrashManager.MAX_NUMBER_OF_CRASHFILES, files.length);
        assertEquals(CrashManager.MAX_NUMBER_OF_CRASHFILES, CrashManager.stackTracesCount);
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
