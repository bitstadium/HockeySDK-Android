package net.hockeyapp.android.tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;

import android.text.TextUtils;
import net.hockeyapp.android.BuildConfig;
import net.hockeyapp.android.Constants;
import net.hockeyapp.android.Tracking;
import net.hockeyapp.android.UpdateManagerListener;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.Util;
import net.hockeyapp.android.utils.VersionCache;
import net.hockeyapp.android.utils.VersionHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * <h3>Description</h3>
 *
 * Internal helper class. Checks if a new update is available by
 * fetching version data from Hockeyapp.
 **/
public class CheckUpdateTask extends AsyncTask<Void, String, JSONArray> {
    private static final int MAX_NUMBER_OF_VERSIONS = 25;

    protected static final String APK = "apk";

    protected String urlString = null;
    protected String appIdentifier = null;

    private Context context = null;
    protected Boolean mandatory = false;
    protected UpdateManagerListener listener;
    private long usageTime = 0;

    public CheckUpdateTask(WeakReference<? extends Context> weakContext, String urlString) {
        this(weakContext, urlString, null);
    }

    public CheckUpdateTask(WeakReference<? extends Context> weakContext, String urlString, String appIdentifier) {
        this(weakContext, urlString, appIdentifier, null);
    }

    public CheckUpdateTask(WeakReference<? extends Context> weakContext, String urlString, String appIdentifier, UpdateManagerListener listener) {
        this.appIdentifier = appIdentifier;
        this.urlString = urlString;
        this.listener = listener;

        Context ctx = null;
        if (weakContext != null) {
            ctx = weakContext.get();
        }

        if (ctx != null) {
            this.context = ctx.getApplicationContext();
            this.usageTime = Tracking.getUsageTime(ctx);
            Constants.loadFromContext(ctx);
        }
    }

    public void attach(WeakReference<? extends Context> weakContext) {
        Context ctx = null;
        if (weakContext != null) {
            ctx = weakContext.get();
        }

        if (ctx != null) {
            this.context = ctx.getApplicationContext();
            Constants.loadFromContext(ctx);
        }
    }

    public void detach() {
        context = null;
    }

    protected int getVersionCode() {
        return Integer.parseInt(Constants.APP_VERSION);
    }

    @Override
    protected JSONArray doInBackground(Void... args) {
        try {
            int versionCode = getVersionCode();

            JSONArray json = new JSONArray(VersionCache.getVersionInfo(context));

            if ((getCachingEnabled()) && (findNewVersion(json, versionCode))) {
                HockeyLog.verbose("HockeyUpdate", "Returning cached JSON");
                return json;
            }

            URL url = new URL(getURLString("json"));
            URLConnection connection = createConnection(url);
            connection.connect();

            InputStream inputStream = new BufferedInputStream(connection.getInputStream());
            String jsonString = Util.convertStreamToString(inputStream);
            inputStream.close();

            json = new JSONArray(jsonString);
            if (findNewVersion(json, versionCode)) {
                json = limitResponseSize(json);
                return json;
            }
        } catch (IOException | JSONException e) {
            if(this.context != null && Util.isConnectedToNetwork(this.context)) {
                HockeyLog.error("HockeyUpdate", "Could not fetch updates although connected to internet");
                e.printStackTrace();
            }
        }

        return null;
    }

    protected URLConnection createConnection(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.addRequestProperty("User-Agent", Constants.SDK_USER_AGENT);
        return connection;
    }

    private boolean findNewVersion(JSONArray json, int versionCode) {
        try {
            boolean newerVersionFound = false;

            for (int index = 0; index < json.length(); index++) {
                JSONObject entry = json.getJSONObject(index);

                boolean largerVersionCode = (entry.getInt("version") > versionCode);
                boolean newerApkFile = ((entry.getInt("version") == versionCode) && VersionHelper.isNewerThanLastUpdateTime(context, entry.getLong("timestamp")));
                boolean minRequirementsMet = VersionHelper.compareVersionStrings(entry.getString("minimum_os_version"), VersionHelper.mapGoogleVersion(Build.VERSION.RELEASE)) <= 0;

                if ((largerVersionCode || newerApkFile) && minRequirementsMet) {
                    if (entry.has("mandatory")) {
                        mandatory |= entry.getBoolean("mandatory");
                    }
                    newerVersionFound = true;
                }
            }

            return newerVersionFound;
        } catch (JSONException e) {
            return false;
        }
    }

    private JSONArray limitResponseSize(JSONArray json) {
        JSONArray result = new JSONArray();
        for (int index = 0; index < Math.min(json.length(), MAX_NUMBER_OF_VERSIONS); index++) {
            try {
                result.put(json.get(index));
            } catch (JSONException ignored) {
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(JSONArray updateInfo) {
        if (updateInfo != null) {
            HockeyLog.verbose("HockeyUpdate", "Received Update Info");

            if (listener != null) {
                listener.onUpdateAvailable(updateInfo, getURLString(APK));
            }
        } else {
            HockeyLog.verbose("HockeyUpdate", "No Update Info available");

            if (listener != null) {
                listener.onNoUpdateAvailable();
            }
        }
    }

    protected void cleanUp() {
        urlString = null;
        appIdentifier = null;
    }

    protected String getURLString(String format) {
        StringBuilder builder = new StringBuilder();
        builder.append(urlString);
        builder.append("api/2/apps/");
        builder.append((this.appIdentifier != null ? this.appIdentifier : context.getPackageName()));
        builder.append("?format=").append(format);

        @SuppressLint("HardwareIds") String deviceIdentifier = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (!TextUtils.isEmpty(deviceIdentifier)) {
            builder.append("&udid=").append(encodeParam(deviceIdentifier));
        }

        SharedPreferences prefs = context.getSharedPreferences("net.hockeyapp.android.login", 0);
        String auid = prefs.getString("auid", null);
        if (!TextUtils.isEmpty(auid)) {
            builder.append("&auid=").append(encodeParam(auid));
        }

        String iuid = prefs.getString("iuid", null);
        if(!TextUtils.isEmpty(iuid)) {
            builder.append("&iuid=").append(encodeParam(iuid));
        }

        builder.append("&os=Android");
        builder.append("&os_version=").append(encodeParam(Constants.ANDROID_VERSION));
        builder.append("&device=").append(encodeParam(Constants.PHONE_MODEL));
        builder.append("&oem=").append(encodeParam(Constants.PHONE_MANUFACTURER));
        builder.append("&app_version=").append(encodeParam(Constants.APP_VERSION));
        builder.append("&sdk=").append(encodeParam(Constants.SDK_NAME));
        builder.append("&sdk_version=").append(encodeParam(BuildConfig.VERSION_NAME));
        builder.append("&lang=").append(encodeParam(Locale.getDefault().getLanguage()));
        builder.append("&usage_time=").append(usageTime);

        return builder.toString();
    }

    private String encodeParam(String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // UTF-8 should be available, so just in case
            return "";
        }
    }

    protected boolean getCachingEnabled() {
        return true;
    }
}
