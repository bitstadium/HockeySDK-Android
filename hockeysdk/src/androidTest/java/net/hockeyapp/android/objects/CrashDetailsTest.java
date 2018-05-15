package net.hockeyapp.android.objects;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;

import net.hockeyapp.android.Constants;
import net.hockeyapp.android.CrashManagerHelper;
import net.hockeyapp.android.ExceptionHandler;
import net.hockeyapp.android.util.StacktraceFilenameFilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@RunWith(AndroidJUnit4.class)
public class CrashDetailsTest {

    private File filesDirectory;

    @Before
    public void setUp() throws Exception {
        CrashManagerHelper.loadConstants(InstrumentationRegistry.getTargetContext());
        CrashManagerHelper.reset(InstrumentationRegistry.getTargetContext());
        filesDirectory = CrashManagerHelper.cleanFiles(InstrumentationRegistry.getTargetContext());

        // Create some fake data
        Constants.APP_VERSION = "1";
        Constants.APP_VERSION_NAME = "1.0";
    }

    @Test
    public void testCrashDetailParsing() throws Exception {
        Throwable tr = new RuntimeException("Just a test exception");
        ExceptionHandler.saveException(tr, Thread.currentThread(), null);

        File[] stackTraceFiles = filesDirectory.listFiles(new StacktraceFilenameFilter());
        assertEquals(1, stackTraceFiles.length);

        File stackTraceFile = stackTraceFiles[0];
        String crashIdentifier = stackTraceFile.getName().substring(0, stackTraceFile.getName().indexOf(".stacktrace"));

        CrashDetails details = CrashDetails.fromFile(stackTraceFile);
        assertNotNull(details);

        assertFalse(TextUtils.isEmpty(details.getCrashIdentifier()));

        assertEquals(crashIdentifier, details.getCrashIdentifier());
        assertEquals(Constants.getDeviceIdentifier().get(), details.getReporterKey());
        assertEquals(Constants.ANDROID_VERSION, details.getOsVersion());
        assertEquals(Constants.ANDROID_BUILD, details.getOsBuild());
        assertEquals(Constants.PHONE_MANUFACTURER, details.getDeviceManufacturer());
        assertEquals(Constants.PHONE_MODEL, details.getDeviceModel());
        assertEquals(Constants.APP_VERSION_NAME, details.getAppVersionName());
        assertEquals(Constants.APP_VERSION, details.getAppVersionCode());
        assertEquals(Thread.currentThread().getName() + "-" + Thread.currentThread().getId(), details.getThreadName());

        assertNotNull(details.getAppStartDate());
        assertNotNull(details.getAppCrashDate());
        assertTrue("Crash date must be later than initialization date", details.getAppCrashDate().compareTo(details.getAppStartDate()) > 0);
    }

    @Test
    public void testCrashStacktraceIsTruncated() throws Exception {
        Throwable tr = new RuntimeException("Just a test exception");
        Throwable throwableSpy = Mockito.spy(tr);
        doAnswer(new Answer<Void>() {
           public Void answer(InvocationOnMock invocation) {
               PrintWriter writer = (PrintWriter) invocation.getArguments()[0];
               char [] stackTraceBuffer = new char[CrashDetails.STACKTRACE_MAX_LENGTH + 1];
               Arrays.fill(stackTraceBuffer, 0, CrashDetails.STACKTRACE_MAX_LENGTH - 1,'0');
               int randomIndex = new Random().nextInt(CrashDetails.STACKTRACE_MAX_LENGTH);
               stackTraceBuffer[randomIndex] = '\n';
               writer.write(stackTraceBuffer);
               return null;
           }
        }).when(throwableSpy).printStackTrace(any(PrintWriter.class));
        ExceptionHandler.saveException(throwableSpy, Thread.currentThread(), null);

        File[] stackTraceFiles = filesDirectory.listFiles(new StacktraceFilenameFilter());
        assertEquals(1, stackTraceFiles.length);

        CrashDetails details = CrashDetails.fromFile(stackTraceFiles[0]);
        assertNotNull(details);

        assertTrue("Crash stacktrace was not truncated", details.getThrowableStackTrace().length() < CrashDetails.STACKTRACE_MAX_LENGTH);
    }
}
