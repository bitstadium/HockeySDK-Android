package net.hockeyapp.android.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Build.VERSION.class, HockeyLog.class})
public class UtilTest {

    private Context mockContext;
    private ConnectivityManager mockConnectivityManager;

    @Before
    public void setUp() {
        mockContext = mock(Context.class);
        mockConnectivityManager = mock(ConnectivityManager.class);

        mockStatic(HockeyLog.class);

        PowerMockito.when(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager);
    }

    @Test
    public void testIsConnectedToNetwork() {
        NetworkInfo mockNetworkInfo = mock(NetworkInfo.class);
        assertFalse(Util.isConnectedToNetwork(mockContext));

        when(mockNetworkInfo.isConnected()).thenReturn(true);
        when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(mockNetworkInfo);

        assertTrue(Util.isConnectedToNetwork(mockContext));

        when(mockNetworkInfo.isConnected()).thenReturn(false);
        assertFalse(Util.isConnectedToNetwork(mockContext));
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Test
    public void testNetworkAccessPermissionGranted() {
        NetworkInfo mockNetworkInfo = mock(NetworkInfo.class);

        when(mockNetworkInfo.isConnected()).thenReturn(true);
        when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(mockNetworkInfo);

        Whitebox.setInternalState(Build.VERSION.class, "SDK_INT", Build.VERSION_CODES.M);

        when(mockContext.checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)).thenReturn(PackageManager.PERMISSION_GRANTED);

        assertTrue(Util.isConnectedToNetwork(mockContext));

        verifyStatic(never());
        HockeyLog.warn(anyString());
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Test
    public void testNetworkAccessPermissionRevoked() {
        Whitebox.setInternalState(Build.VERSION.class, "SDK_INT", Build.VERSION_CODES.M);

        when(mockContext.checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)).thenReturn(PackageManager.PERMISSION_DENIED);

        assertFalse(Util.isConnectedToNetwork(mockContext));

        verifyStatic();
        HockeyLog.warn(anyString());
    }

}
