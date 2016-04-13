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
        String fakeManagedExceptionString = "System.Runtime.ExceptionServices.ExceptionDispatchInfo.Throw() Android.Runtime.JNIEnv.CallVoidMethod(IntPtr jobject, IntPtr jmethod, JValue* parms) Com.Microsoft.AI.Xamarinexample.ExampleClass.ForceAppCrash(Activity p0) XamarinTest.Droid.DummyLibraryAndroid.TriggerExceptionCrash() XamarinTest.DummyLibrary.TriggerExceptionCrash() XamarinTest.XamarinTestMasterView.TrackTelemetryData(TelemetryType type) XamarinTest.XamarinTestMasterView.<XamarinTestMasterView>m__3() at Xamarin.Forms.Command+<>c__DisplayClass2.<.ctor>b__0 (System.Object o) <0x9b13fb68 + 0x00014> in <filename unknown>:0 Xamarin.Forms.Command.Execute(object parameter) Xamarin.Forms.TextCell.OnTapped() Xamarin.Forms.TableView.TableSectionModel.OnRowSelected(object item) Xamarin.Forms.TableModel.RowSelected(object item) Xamarin.Forms.TableModel.RowSelected(int section, int row) Xamarin.Forms.Platform.Android.TableViewModelRenderer.HandleItemClick(AdapterView parent, View nview, int position, long id) Xamarin.Forms.Platform.Android.CellAdapter.OnItemClick(AdapterView parent, View view, int position, long id) Android.Widget.AdapterView.IOnItemClickListenerInvoker.n_OnItemClick_Landroid_widget_AdapterView_Landroid_view_View_IJ(IntPtr jnienv, IntPtr native__this, IntPtr native_parent, IntPtr native_view, int position, long id) at (wrapper dynamic-method) System.Object:ab525826-8008-474b-a02c-b5ae8ba471a3 (intptr,intptr,intptr,intptr,int,long)";
        ExceptionHandler.saveException(tr, Thread.currentThread(), fakeManagedExceptionString, null);
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
}
