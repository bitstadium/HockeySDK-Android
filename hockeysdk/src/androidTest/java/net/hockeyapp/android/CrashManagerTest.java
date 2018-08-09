package net.hockeyapp.android;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import net.hockeyapp.android.objects.CrashDetails;
import net.hockeyapp.android.util.StacktraceFilenameFilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class CrashManagerTest {

    private static final String DUMMY_APP_IDENTIFIER = "12345678901234567890123456789012";

    private File filesDirectory;

    private static void fakeCrashReport() {
        Throwable tr = new RuntimeException("Just a test exception");
        ExceptionHandler.saveException(tr, Thread.currentThread(), null);
    }

    private static void fakeXamarinCrashReport() {
        Throwable tr = new RuntimeException("That's the Java exception");
        Throwable xamaTr = new RuntimeException("Outer Exception", new RuntimeException("Inner Exception"));
        ExceptionHandler.saveNativeException(tr, xamaTr.toString(), Thread.currentThread(), null);
    }

    private File lastCrashReportFile() {
        long modifiedReference = 0;
        File lastReportsFile = null;
        for (File f : filesDirectory.listFiles(new StacktraceFilenameFilter())) {
            if (f.lastModified() > modifiedReference) {
                modifiedReference = f.lastModified();
                lastReportsFile = f;
            }
        }
        return lastReportsFile;
    }

    @Before
    public void setUp() throws Exception {
        CrashManagerHelper.loadConstants(InstrumentationRegistry.getTargetContext());
        CrashManagerHelper.reset(InstrumentationRegistry.getTargetContext());
        filesDirectory = CrashManagerHelper.cleanFiles(InstrumentationRegistry.getTargetContext());
    }

    @Test
    public void registerCrashManagerWorks() throws Exception {
        // verify that registering the Crash Manager works (e.g. it's not throwing any exception)
        CrashManager.register(InstrumentationRegistry.getTargetContext(), DUMMY_APP_IDENTIFIER);

        // verify that there were no crashes in the last session
        assertFalse(CrashManager.didCrashInLastSession().get());
        assertEquals(0, CrashManager.stackTracesCount);
    }

    @Test
    public void crashInLastSessionRecognized() throws Exception {
        fakeCrashReport();

        CrashManager.register(InstrumentationRegistry.getTargetContext(), DUMMY_APP_IDENTIFIER);

        assertTrue(CrashManager.didCrashInLastSession().get());
        assertNotNull(CrashManager.getLastCrashDetails(InstrumentationRegistry.getTargetContext()).get());
        assertEquals(1, CrashManager.stackTracesCount);
    }

    @Test
    public void crashDetailsInLastSessionCorrect() throws Exception {
        fakeCrashReport();

        CrashManager.register(InstrumentationRegistry.getTargetContext(), DUMMY_APP_IDENTIFIER);

        CrashDetails crashDetails = CrashManager.getLastCrashDetails(InstrumentationRegistry.getTargetContext()).get();
        assertNotNull(crashDetails);
        assertEquals(1, CrashManager.stackTracesCount);

        File lastStackTrace = lastCrashReportFile();
        assertNotNull(lastStackTrace);

        assertEquals(lastStackTrace.getName().substring(0, lastStackTrace.getName().indexOf(".stacktrace")), crashDetails.getCrashIdentifier());
        assertEquals(Constants.getDeviceIdentifier().get(), crashDetails.getReporterKey());

        fakeCrashReport();
        fakeCrashReport();
        fakeCrashReport();

        crashDetails = CrashManager.getLastCrashDetails(InstrumentationRegistry.getTargetContext()).get();
        assertNotNull(crashDetails);
        assertEquals(4, CrashManager.stackTracesCount);

        lastStackTrace = lastCrashReportFile();
        assertNotNull(lastStackTrace);

        assertEquals(lastStackTrace.getName().substring(0, lastStackTrace.getName().indexOf(".stacktrace")), crashDetails.getCrashIdentifier());
    }

    @Test
    public void xamarinCrashCorrect() throws Exception {
        fakeXamarinCrashReport();

        CrashManager.register(InstrumentationRegistry.getTargetContext(), DUMMY_APP_IDENTIFIER);

        fakeXamarinCrashReport();

        CrashDetails crashDetails = CrashManager.getLastCrashDetails(InstrumentationRegistry.getTargetContext()).get();
        assertNotNull(crashDetails);
        assertEquals(2, CrashManager.stackTracesCount);
        assertEquals(crashDetails.getFormat(), "Xamarin");
        String throwableStackTrace = crashDetails.getThrowableStackTrace();
        Boolean containsCausedByXamarin = throwableStackTrace.contains("Xamarin caused by:");
        assertTrue(containsCausedByXamarin);
    }

    @Test
    public void invalidStackTrace() throws Exception {
        File file = new File(filesDirectory, UUID.randomUUID().toString() + ".stacktrace");
        file.createNewFile();

        CrashManagerListener listener = mock(CrashManagerListener.class);
        WeakReference<Context> weakContext = new WeakReference<>(InstrumentationRegistry.getTargetContext());

        CrashManager.submitStackTraces(weakContext, listener);

        verify(listener).onCrashesNotSent();
        assertEquals(0, CrashManager.stackTracesCount);
    }

    @Test
    public void largeStackTrace() throws Exception {
        String stackTrace = "java.lang.OutOfMemoryError: Failed to allocate a 37657308 byte allocation with 16776928 free bytes and 27MB until OOM\n" +
                "\tat java.lang.String.<init>(String.java:400)\n" +
                "\tat java.lang.AbstractStringBuilder.toString(AbstractStringBuilder.java:633)\n" +
                "\tat java.lang.StringBuilder.toString(StringBuilder.java:663)\n" +
                "\tat net.hockeyapp.android.CrashManager.contentsOfFile(CrashManager.java:772)\n" +
                "\tat net.hockeyapp.android.CrashManager.submitStackTrace(CrashManager.java:379)\n" +
                "\tat net.hockeyapp.android.CrashManager.access$500(CrashManager.java:47)\n" +
                "\tat net.hockeyapp.android.CrashManager$8.doInBackground(CrashManager.java:647)\n" +
                "\tat net.hockeyapp.android.CrashManager$8.doInBackground(CrashManager.java:639)\n";

        String filename = UUID.randomUUID().toString() + ".stacktrace";
        File file = new File(filesDirectory, filename);
        PrintWriter writer = new PrintWriter(file);
        writer.print(stackTrace);
        writer.flush();
        writer.close();

        WeakReference<Context> weakContext = new WeakReference<>(InstrumentationRegistry.getTargetContext());
        String result = CrashManager.contentsOfFile(weakContext, filename, 500);
        assertTrue(result.endsWith("submitStackTrace(CrashManager.java:379)\n"));
    }
}
