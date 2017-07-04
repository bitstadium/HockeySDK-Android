package net.hockeyapp.android.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;

import java.util.ArrayList;

public class PermissionsUtil {

    public static int[] permissionsState(@NonNull Context context, String... permissions) {
        if (permissions == null) {
            return null;
        }
        int[] state = new int[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            state[i] = context.checkCallingOrSelfPermission(permissions[i]);
        }
        return state;
    }

    public static boolean permissionsAreGranted(int[] permissionsState) {
        for (int permissionState : permissionsState) {
            if (permissionState != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static String[] deniedPermissions(String[] permissions, int[] permissionsState) {
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (permissionsState[i] != PackageManager.PERMISSION_GRANTED) {
                result.add(permissions[i]);
            }
        }
        return result.toArray(new String[0]);
    }

    /**
     * Checks if Unknown Sources is enabled
     */
    public static boolean isUnknownSourcesEnabled(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return context.getPackageManager().canRequestPackageInstalls();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return "1".equals(Settings.Global.getString(context.getContentResolver(), Settings.Global.INSTALL_NON_MARKET_APPS));
        } else {
            return "1".equals(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS));
        }
    }
}
