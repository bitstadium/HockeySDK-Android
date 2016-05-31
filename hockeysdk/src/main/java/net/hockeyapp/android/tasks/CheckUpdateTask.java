package net.hockeyapp.android.tasks;

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
import net.hockeyapp.android.utils.VersionCache;
import net.hockeyapp.android.utils.VersionHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
            String jsonString = convertStreamToString(inputStream);
            inputStream.close();

            json = new JSONArray(jsonString);
            if (findNewVersion(json, versionCode)) {
                json = limitResponseSize(json);
                return json;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected URLConnection createConnection(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.addRequestProperty("User-Agent", Constants.SDK_USER_AGENT);
        // connection bug workaround for SDK<=2.x
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD) {
            connection.setRequestProperty("connection", "close");
        }
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
            } catch (JSONException e) {
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
        builder.append("?format=" + format);

        String deviceIdentifier = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (!TextUtils.isEmpty(deviceIdentifier)) {
            builder.append("&udid=" + encodeParam(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)));
        }

        SharedPreferences prefs = context.getSharedPreferences("net.hockeyapp.android.login", 0);
        String auid = prefs.getString("auid", null);
        if (!TextUtils.isEmpty(auid)) {
            builder.append("&auid=" + encodeParam(auid));
        }

        String iuid = prefs.getString("iuid", null);
        if(!TextUtils.isEmpty(iuid)) {
            builder.append("&iuid=" + encodeParam(iuid));
        }

        builder.append("&os=Android");
        builder.append("&os_version=" + encodeParam(Constants.ANDROID_VERSION));
        builder.append("&device=" + encodeParam(Constants.PHONE_MODEL));
        builder.append("&oem=" + encodeParam(Constants.PHONE_MANUFACTURER));
        builder.append("&app_version=" + encodeParam(Constants.APP_VERSION));
        builder.append("&sdk=" + encodeParam(Constants.SDK_NAME));
        builder.append("&sdk_version=" + encodeParam(BuildConfig.VERSION_NAME));
        builder.append("&lang=" + encodeParam(Locale.getDefault().getLanguage()));
        builder.append("&usage_time=" + usageTime);

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

    /*
    MIT Mobile for Android is open source software, created, maintained, and shared under the MIT license by Information Services & Technology at the Massachusetts Institute of Technology. The project includes components from other open source projects which remain under their existing licenses, detailed in their respective source files. The open source license does not apply to media depicting people and places at MIT which are included in the source. Said media may not be duplicated without MIT's consent.

    Copyright (c) 2010 Massachusetts Institute of Technology

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
    */
    private static String convertStreamToString(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), 1024);
        StringBuilder stringBuilder = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

}