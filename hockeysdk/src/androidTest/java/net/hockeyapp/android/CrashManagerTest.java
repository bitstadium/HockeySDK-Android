package net.hockeyapp.android;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;
import net.hockeyapp.android.objects.CrashDetails;
import net.hockeyapp.android.util.StacktraceFilenameFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(AndroidJUnit4.class)
public class CrashManagerTest extends InstrumentationTestCase {

    private static final String DUMMY_APP_IDENTIFIER = "12345678901234567890123456789012";

    private static final StacktraceFilenameFilter STACK_TRACE_FILTER = new StacktraceFilenameFilter();

    private static void cleanupReportsDir() {
        assertNotNull(Constants.FILES_PATH);
        File reportsDir = new File(Constants.FILES_PATH);
        for (File f : reportsDir.listFiles(STACK_TRACE_FILTER)) {
            f.delete();
        }
    }

    private static void fakeCrashReport() {
        Throwable tr = new RuntimeException("Just a test exception");
        ExceptionHandler.saveException(tr, Thread.currentThread(), null);
    }

    private static void fakeXamarinCrashReport() {
        Throwable tr = new RuntimeException("That's the Java exception");
        Throwable xamaTr = new RuntimeException("Outer Exception", new RuntimeException("Inner Exception"));
        ExceptionHandler.saveXamarinException(tr, Thread.currentThread(), xamaTr, null);
    }

    private static File lastCrashReportFile() {
        assertNotNull(Constants.FILES_PATH);
        File reportsDir = new File(Constants.FILES_PATH);

        long modifiedReference = 0;
        File lastReportsFile = null;
        for (File f : reportsDir.listFiles(STACK_TRACE_FILTER)) {
            if (f.lastModified() > modifiedReference) {
                modifiedReference = f.lastModified();
                lastReportsFile = f;
            }
        }
        return lastReportsFile;
    }

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
        fakeCrashReport();
        assertNotNull(Constants.FILES_PATH);

        CrashManager.register(getInstrumentation().getTargetContext(), DUMMY_APP_IDENTIFIER);

        assertTrue(CrashManager.didCrashInLastSession());
        assertNotNull(CrashManager.getLastCrashDetails());
    }

    @Test
    public void crashDetailsInLastSessionCorrect() {
        assertNotNull(Constants.FILES_PATH);

        cleanupReportsDir();
        fakeCrashReport();

        CrashManager.register(getInstrumentation().getTargetContext(), DUMMY_APP_IDENTIFIER);

        CrashDetails crashDetails = CrashManager.getLastCrashDetails();

        assertNotNull(crashDetails);

        File lastStackTrace = lastCrashReportFile();
        assertNotNull(lastStackTrace);

        assertEquals(lastStackTrace.getName().substring(0, lastStackTrace.getName().indexOf(".stacktrace")), crashDetails.getCrashIdentifier());
        assertEquals(Constants.CRASH_IDENTIFIER, crashDetails.getReporterKey());

        fakeCrashReport();
        fakeCrashReport();
        fakeCrashReport();

        crashDetails = CrashManager.getLastCrashDetails();

        assertNotNull(crashDetails);

        lastStackTrace = lastCrashReportFile();
        assertNotNull(lastStackTrace);

        assertEquals(lastStackTrace.getName().substring(0, lastStackTrace.getName().indexOf(".stacktrace")), crashDetails.getCrashIdentifier());
    }

    @Test
    public void xamarinCrashCorrect() {
        cleanupReportsDir();

        fakeXamarinCrashReport();

        CrashManager.register(getInstrumentation().getTargetContext(), DUMMY_APP_IDENTIFIER);

        fakeXamarinCrashReport();

        CrashDetails crashDetails = CrashManager.getLastCrashDetails();
        assertNotNull(crashDetails);
        assertEquals(crashDetails.getFormat(), "Xamarin");
        String throwableStackTrace = crashDetails.getThrowableStackTrace();
        Boolean containsCausedByXamarin = throwableStackTrace.contains("Xamarin caused by:");
        assertTrue(containsCausedByXamarin);
    }
}
