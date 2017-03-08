package net.hockeyapp.android;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.lang.ref.WeakReference;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Build.VERSION.class, TextUtils.class})
public class UpdateManagerTest {

    private Context mockContext;
    private PackageManager mockPackageManager;

    private WeakReference<Context> contextWeakReference;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Reset Build version code
        Whitebox.setInternalState(Build.VERSION.class, "SDK_INT", Build.VERSION_CODES.BASE);

        mockContext = mock(Context.class);
        mockPackageManager = mock(PackageManager.class);

        mockStatic(TextUtils.class);
        PowerMockito.when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence a = (CharSequence) invocation.getArguments()[0];
                return !(a != null && a.length() > 0);
            }
        });

        PowerMockito.when(TextUtils.equals(any(CharSequence.class), any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence a = (CharSequence) invocation.getArguments()[0];
                CharSequence b = (CharSequence) invocation.getArguments()[1];
                return a.equals(b);
            }
        });

        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);

        contextWeakReference = new WeakReference<>(mockContext);
    }

    @Test
    public void testInstalledViaADBDefault() {
        when(mockPackageManager.getInstallerPackageName(any(String.class))).thenReturn(null);

        // Typically, the installer string for ADB is null
        assertFalse(UpdateManager.installedFromMarket(contextWeakReference));

        // or an empty string
        when(mockPackageManager.getInstallerPackageName(any(String.class))).thenReturn("");
        assertFalse(UpdateManager.installedFromMarket(contextWeakReference));

        // Verify this also works on Android Nougat
        Whitebox.setInternalState(Build.VERSION.class, "SDK_INT", Build.VERSION_CODES.N);
        assertFalse(UpdateManager.installedFromMarket(contextWeakReference));
    }

    @Test
    public void testInstalledViaGooglePlay() {
        // Set typical identifier for Google Play and check
        when(mockPackageManager.getInstallerPackageName(any(String.class))).thenReturn("com.google.play");

        assertTrue(UpdateManager.installedFromMarket(contextWeakReference));

        Whitebox.setInternalState(Build.VERSION.class, "SDK_INT", Build.VERSION_CODES.N);
        assertTrue(UpdateManager.installedFromMarket(contextWeakReference));
    }

    @Test
    public void testInstalledViaADBXiaomi() {
        // On some devices, e.g. Xiaomi devices, launching via USB will return "adb" for the installer
        when(mockPackageManager.getInstallerPackageName(any(String.class))).thenReturn("adb");

        assertFalse(UpdateManager.installedFromMarket(contextWeakReference));

        Whitebox.setInternalState(Build.VERSION.class, "SDK_INT", Build.VERSION_CODES.N);
        assertFalse(UpdateManager.installedFromMarket(contextWeakReference));
    }

    @Test
    public void testInstalledViaPackageManagerNougat() {
        // On Android Nougat, installing packages using HockeyApp (using the package manager) will list the following installer identifier
        when(mockPackageManager.getInstallerPackageName(any(String.class))).thenReturn("com.google.android.packageinstaller");
        // When not on Android Nougat this is considered "store"
        assertTrue(UpdateManager.installedFromMarket(contextWeakReference));

        // Test desired behavior on Android Nougat
        Whitebox.setInternalState(Build.VERSION.class, "SDK_INT", Build.VERSION_CODES.N);
        assertFalse(UpdateManager.installedFromMarket(contextWeakReference));
    }

    @Test
    public void testInstalledViaPackageManagerNougat2() {
        // On Android Nougat, installing packages using HockeyApp (using the package manager) will list the following installer identifier
        when(mockPackageManager.getInstallerPackageName(any(String.class))).thenReturn("com.android.packageinstaller");
        // When not on Android Nougat this is considered "store"
        assertTrue(UpdateManager.installedFromMarket(contextWeakReference));

        // Test desired behavior on Android Nougat
        Whitebox.setInternalState(Build.VERSION.class, "SDK_INT", Build.VERSION_CODES.N);
        assertFalse(UpdateManager.installedFromMarket(contextWeakReference));
    }
}
