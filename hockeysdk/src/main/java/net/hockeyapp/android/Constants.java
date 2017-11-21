package net.hockeyapp.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.CompletedFuture;
import net.hockeyapp.android.utils.HockeyLog;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * <h3>Description</h3>
 *
 * Various constants and meta information loaded from the context.
 **/
public class Constants {

    /**
     * HockeyApp API URL.
     */
    public static final String BASE_URL = "https://sdk.hockeyapp.net/";
    /**
     * Name of this SDK.
     */
    public static final String SDK_NAME = "HockeySDK";
    /**
     * Version of the SDK - retrieved from the build configuration.
     */
    public static final String SDK_VERSION = BuildConfig.VERSION_NAME;

    public static final String FILES_DIRECTORY_NAME = "HockeyApp";

    /**
     * The user agent string the SDK will send with every HockeyApp API request.
     */
    public static final String SDK_USER_AGENT = "HockeySDK/Android " + BuildConfig.VERSION_NAME;

    /**
     * Permissions request for the update task.
     */
    public static final int UPDATE_PERMISSIONS_REQUEST = 1;
    private static final String BUNDLE_BUILD_NUMBER = "buildNumber";

    /**
     * The app's version code.
     */
    public static String APP_VERSION = null;
    /**
     * The app's version name.
     */
    public static String APP_VERSION_NAME = null;
    /**
     * The app's package name.
     */
    public static String APP_PACKAGE = null;
    /**
     * The device's OS version.
     */
    public static String ANDROID_VERSION = null;
    /**
     * The device's OS build.
     */
    public static String ANDROID_BUILD = null;

    /**
     * The device's model name.
     */
    public static String PHONE_MODEL = null;
    /**
     * The device's model manufacturer name.
     */
    public static String PHONE_MANUFACTURER = null;

    /**
     * Unique identifier for device, not dependent on package or device.
     */
    static String DEVICE_IDENTIFIER = null;

    /**
     * Lock used to wait for loading of identifiers.
     */
    static CountDownLatch LOADING_LATCH = new CountDownLatch(1);

    public static Future<String> getDeviceIdentifier() {
        if (LOADING_LATCH.getCount() == 0) {
            return new CompletedFuture<>(DEVICE_IDENTIFIER);
        }
        return AsyncTaskUtils.execute(new Callable<String>() {

            @Override
            public String call() throws Exception {
                LOADING_LATCH.await();
                return DEVICE_IDENTIFIER;
            }
        });
    }

    /**
     * Initializes constants from the given context. The context is used to set
     * the package name, version code, and the files path.
     *
     * @param context The context to use. Usually your Activity object.
     */
    public static void loadFromContext(Context context) {
        ANDROID_VERSION = android.os.Build.VERSION.RELEASE;
        ANDROID_BUILD = android.os.Build.DISPLAY;
        PHONE_MODEL = android.os.Build.MODEL;
        PHONE_MANUFACTURER = android.os.Build.MANUFACTURER;

        loadPackageData(context);
        loadIdentifiers(context);
    }

    /**
     * Returns a file representing the folder in which screenshots are stored.
     *
     * @return A file representing the screenshot folder.
     */
    public static File getHockeyAppStorageDir(Context context) {
        File dir = new File(context.getExternalFilesDir(null), FILES_DIRECTORY_NAME);
        boolean success = dir.exists() || dir.mkdirs();
        if (!success) {
            HockeyLog.warn("Couldn't create HockeyApp Storage dir");
        }
        return dir;
    }

    /**
     * Helper method to set the package name and version code. If an exception
     * occurs, these values will be null!
     *
     * @param context The context to use. Usually your Activity object.
     */
    private static void loadPackageData(Context context) {
        if (context != null) {
            try {
                PackageManager packageManager = context.getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
                APP_PACKAGE = packageInfo.packageName;
                APP_VERSION = "" + packageInfo.versionCode;
                APP_VERSION_NAME = packageInfo.versionName;

                int buildNumber = loadBuildNumber(context, packageManager);
                if ((buildNumber != 0) && (buildNumber > packageInfo.versionCode)) {
                    APP_VERSION = "" + buildNumber;
                }
            } catch (PackageManager.NameNotFoundException e) {
                HockeyLog.error("Exception thrown when accessing the package info", e);
            }
        }
    }

    /**
     * Helper method to load the build number from the AndroidManifest.
     *
     * @param context        the context to use. Usually your Activity object.
     * @param packageManager an instance of PackageManager
     */
    private static int loadBuildNumber(Context context, PackageManager packageManager) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = appInfo.metaData;
            if (metaData != null) {
                return metaData.getInt(BUNDLE_BUILD_NUMBER, 0);
            }
        } catch (PackageManager.NameNotFoundException e) {
            HockeyLog.error("Exception thrown when accessing the application info", e);
        }

        return 0;
    }

    /**
     * Helper method to load the identifiers.
     *
     * @param context the context to use. Usually your Activity object.
     */
    @SuppressLint("StaticFieldLeak")
    private static void loadIdentifiers(final Context context) {
        if (DEVICE_IDENTIFIER != null) {
            return;
        }
        AsyncTaskUtils.execute(new AsyncTask<Void, Object, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                final SharedPreferences preferences = context.getSharedPreferences("HockeyApp", Context.MODE_PRIVATE);
                String deviceIdentifier = preferences.getString("deviceIdentifier", null);
                if (deviceIdentifier == null) {
                    deviceIdentifier = UUID.randomUUID().toString();
                    preferences.edit().putString("deviceIdentifier", deviceIdentifier).apply();
                }
                return deviceIdentifier;
            }

            @Override
            protected void onPostExecute(String deviceIdentifier) {
                DEVICE_IDENTIFIER = deviceIdentifier;
                LOADING_LATCH.countDown();
            }
        });
    }
}
