package net.hockeyapp.android.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import net.hockeyapp.android.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    private static final String APP_IDENTIFIER_PATTERN = "[0-9a-f]+";
    private static final int APP_IDENTIFIER_LENGTH = 32;
    private static final String APP_IDENTIFIER_KEY = "net.hockeyapp.android.appIdentifier";
    private static final String APP_SECRET_KEY = "net.hockeyapp.android.appSecret";
    private static final Pattern appIdentifierPattern = Pattern.compile(APP_IDENTIFIER_PATTERN, Pattern.CASE_INSENSITIVE);

    private static final ThreadLocal<DateFormat> DATE_FORMAT_THREAD_LOCAL = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat;
        }
    };

    /**
     * Returns the given param URL-encoded.
     *
     * @param param a string to encode
     * @return the encoded param
     */
    public static String encodeParam(String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // UTF-8 should be available, so just in case
            HockeyLog.error("Failed to encode param " + param, e);
            return "";
        }
    }

    /**
     * Returns true if value is a valid email.
     *
     * @param value a string
     * @return true if value is a valid email
     */
    public static boolean isValidEmail(String value) {
        return !TextUtils.isEmpty(value) && android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches();
    }

    /**
     * Returns true if the app runs on large or very large screens (i.e. tablets).
     *
     * @param context the context to use
     * @return true if the app runs on large or very large screens
     */
    public static Boolean runsOnTablet(Context context) {
        if (context != null) {
            Configuration configuration = context.getResources().getConfiguration();
            return (((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) ||
                    ((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE));
        }
        return false;
    }

    /**
     * Sanitizes an app identifier or throws an exception if it can't be sanitized.
     *
     * @param appIdentifier the app identifier to sanitize
     * @return the sanitized app identifier
     * @throws java.lang.IllegalArgumentException if the app identifier can't be sanitized because of unrecoverable input character errors
     */
    public static String sanitizeAppIdentifier(String appIdentifier) throws IllegalArgumentException {

        if (appIdentifier == null) {
            throw new IllegalArgumentException("App ID must not be null.");
        }

        String sAppIdentifier = appIdentifier.trim();

        Matcher matcher = appIdentifierPattern.matcher(sAppIdentifier);

        if (sAppIdentifier.length() != APP_IDENTIFIER_LENGTH) {
            throw new IllegalArgumentException("App ID length must be " + APP_IDENTIFIER_LENGTH + " characters.");
        } else if (!matcher.matches()) {
            throw new IllegalArgumentException("App ID must match regex pattern /" + APP_IDENTIFIER_PATTERN + "/i");
        }

        return sAppIdentifier;
    }

    /**
     * Converts a map of parameters to a HTML form entity.
     *
     * @param params the parameters
     * @return an URL-encoded form string ready for use in a HTTP post
     * @throws UnsupportedEncodingException when your system does not know how to handle the UTF-8 charset
     */
    public static String getFormString(Map<String, String> params) throws UnsupportedEncodingException {
        List<String> protoList = new ArrayList<>();
        for (String key : params.keySet()) {
            String value = params.get(key);
            key = URLEncoder.encode(key, "UTF-8");
            value = URLEncoder.encode(value, "UTF-8");
            protoList.add(key + "=" + value);
        }
        return TextUtils.join("&", protoList);
    }

    /**
     * Helper method to safely check whether a class exists at runtime.
     *
     * @param className the full-qualified class name to check for
     * @return whether the class exists
     */
    public static boolean classExists(String className) {
        try {
            return Class.forName(className) != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Creates a notification on API levels from 9 to 23
     *
     * @param context       the context to use, e.g. your Activity
     * @param pendingIntent the Intent to call
     * @param title         the title string for the notification
     * @param text          the text content for the notification
     * @param iconId        the icon resource ID for the notification
     * @return the created notification
     */
    @SuppressWarnings("deprecation")
    public static Notification createNotification(Context context, PendingIntent pendingIntent, String title, String text, int iconId, String channelId) {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, channelId);
        } else {
            builder = new Notification.Builder(context);
        }
        builder.setContentTitle(title)
               .setContentText(text)
               .setContentIntent(pendingIntent)
               .setSmallIcon(iconId);
        return builder.build();
    }

    public static void sendNotification(Context context, int id, Notification notification, String channelId, CharSequence channelName) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(id, notification);
    }

    public static void cancelNotification(Context context, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    public static void announceForAccessibility(View view, CharSequence text) {
        final AccessibilityManager manager = (AccessibilityManager) view.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (!manager.isEnabled()) {
            return;
        }
         final AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT);
        event.getText().add(text);
        event.setSource(view);
        event.setEnabled(view.isEnabled());
        event.setClassName(view.getClass().getName());
        event.setPackageName(view.getContext().getPackageName());
        manager.sendAccessibilityEvent(event);
    }

    /**
     * Retrieve the HockeyApp AppIdentifier from the Manifest
     *
     * @param context usually your Activity
     * @return the HockeyApp AppIdentifier
     */
    public static String getAppIdentifier(Context context) {
        String appIdentifier = getManifestString(context, APP_IDENTIFIER_KEY);
        if (TextUtils.isEmpty(appIdentifier)) {
            throw new IllegalArgumentException("HockeyApp app identifier was not configured correctly in manifest or build configuration.");
        }
        return appIdentifier;
    }

    /**
     * Retrieve the HockeyApp appSecret from the Manifest
     *
     * @param context usually your Activity
     * @return the HockeyApp appSecret
     */
    public static String getAppSecret(Context context) {
        return getManifestString(context, APP_SECRET_KEY);
    }

    public static String getManifestString(Context context, String key) {
        return getBundle(context).getString(key);
    }

    private static Bundle getBundle(Context context) {
        Bundle bundle;
        try {
            bundle = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        return bundle;
    }

    public static boolean isConnectedToNetwork(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnected();
            }
        } catch (Exception e) {
            HockeyLog.error("Exception thrown when check network is connected", e);
        }
        return false;
    }

    public static String getAppName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
        } catch (final PackageManager.NameNotFoundException ignored) {
        }
        return applicationInfo != null ? (String) packageManager.getApplicationLabel(applicationInfo)
                : context.getString(R.string.hockeyapp_crash_dialog_app_name_fallback);
    }

    /**
     * Sanitizes an app identifier and adds dashes to it so that it conforms to the instrumentation
     * key format of Application Insights.
     *
     * @param appIdentifier the app identifier to sanitize and convert
     * @return the converted appIdentifier
     * @throws java.lang.IllegalArgumentException if the app identifier can't be converted because
     *                                            of unrecoverable input character errors
     */
    public static String convertAppIdentifierToGuid(String appIdentifier) throws IllegalArgumentException {
        String sanitizedAppIdentifier= sanitizeAppIdentifier(appIdentifier);
        String guid = null;

        if (sanitizedAppIdentifier != null) {
            StringBuilder idBuf = new StringBuilder(sanitizedAppIdentifier);
            idBuf.insert(20, '-');
            idBuf.insert(16, '-');
            idBuf.insert(12, '-');
            idBuf.insert(8, '-');
            guid = idBuf.toString();
        }
        return guid;
    }

    /**
     * Determines whether the app is running on aan emulator or on a real device.
     *
     * @return YES if the app is running on an emulator, NO if it is running on a real device
     */
    public static boolean isEmulator() {
        return Build.BRAND.equalsIgnoreCase("generic");
    }

    /**
     * Convert a date object to an ISO 8601 formatted string
     *
     * @param date the date object to be formatted
     * @return an ISO 8601 string representation of the date
     */
    public static String dateToISO8601(Date date) {
        Date localDate = date;
        if (localDate == null) {
            localDate = new Date();
        }
        return DATE_FORMAT_THREAD_LOCAL.get().format(localDate);
    }

    /**
     * Determines if a debugger is currently attached.
     *
     * @return YES if debugger is attached, otherwise NO.
     */
    public static boolean isDebuggerConnected(){
        return Debug.isDebuggerConnected();
    }

    public static String convertStreamToString(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), 1024);
        StringBuilder stringBuilder = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        } catch (IOException e) {
            HockeyLog.error("Failed to convert stream to string", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
        return stringBuilder.toString();
    }

    public static byte[] hash(final byte[] bytes, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        digest.update(bytes);
        return digest.digest();
    }

    /**
     * Helper method to convert a byte array to the hex string.
     *
     * @param bytes a byte array
     */
    public static String bytesToHex(final byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte aMessageDigest : bytes) {
            String h = Integer.toHexString(0xFF & aMessageDigest);
            while (h.length() < 2)
                h = "0" + h;
            hexString.append(h);
        }
        return hexString.toString();
    }

    /**
     * Returns a file's display name from its Uri.
     *
     * @param context Context trying to resolve the file's display name.
     * @param uri Uri of the file.
     * @return the file's display name.
     */
    public static String getFileName(Context context, Uri uri) {
        String scheme = uri.getScheme();
        String result = null;
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            result = uri.getLastPathSegment();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            String[] projection = { OpenableColumns.DISPLAY_NAME };
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        return result != null ? result : uri.toString();
    }
}
