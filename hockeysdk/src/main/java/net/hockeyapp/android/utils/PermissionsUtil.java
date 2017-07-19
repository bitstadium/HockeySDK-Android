package net.hockeyapp.android.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import java.util.ArrayList;

public class PermissionsUtil {

    public static int[] permissionsState(Context context, String... permissions) {
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
    public static boolean isUnknownSourcesEnabled(Context context) {

        /*
         * On Android 8 with applications targeting lower versions,
         * it's impossible to check unknown sources enabled: using old APIs will always return true
         * and using the new one will always return false,
         * so in order to avoid a stuck dialog that can't be bypassed we will assume true.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.O || context.getPackageManager().canRequestPackageInstalls();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            return "1".equals(Settings.Global.getString(context.getContentResolver(), Settings.Global.INSTALL_NON_MARKET_APPS));
        } else {
            //noinspection deprecation
            return "1".equals(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS));
        }
    }
}
