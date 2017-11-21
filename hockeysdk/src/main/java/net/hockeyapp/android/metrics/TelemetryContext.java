package net.hockeyapp.android.metrics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;

import net.hockeyapp.android.BuildConfig;
import net.hockeyapp.android.Constants;
import net.hockeyapp.android.metrics.model.*;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.Util;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * <h3>Description</h3>
 *
 * Class that manages the context in which telemetry items get sent.
 */
@SuppressWarnings("WeakerAccess")
class TelemetryContext {

    private static final String TAG = "HockeyApp-Metrics";

    /**
     * Key needed to access the shared preferences of the SDK.
     */
    private static final String SHARED_PREFERENCES_KEY = "HOCKEY_APP_TELEMETRY_CONTEXT";

    /**
     * Key needed to determine, whether we have a new or existing user.
     */
    private static final String SESSION_IS_FIRST_KEY = "SESSION_IS_FIRST";

    /**
     * Device telemetryContext.
     */
    final Device mDevice;

    /**
     * Session context.
     */
    final Session mSession;

    /**
     * User context.
     */
    final User mUser;

    /**
     * Internal context.
     */
    final Internal mInternal;

    /**
     * Application context.
     */
    final Application mApplication;

    /**
     * Synchronization LOCK for setting instrumentation key.
     */
    private final Object IKEY_LOCK = new Object();

    /**
     * A weak reference to the application context needed to update some context values.
     */
    private WeakReference<Context> mWeakContext;

    /**
     * Device context.
     */
    private String mInstrumentationKey;

    /**
     * The app's package name.
     */
    private String mPackageName;

    /**
     * Constructs a new INSTANCE of TelemetryContext.
     */
    private TelemetryContext() {
        mDevice = new Device();
        mSession = new Session();
        mUser = new User();
        mApplication = new Application();
        mInternal = new Internal();
    }

    /**
     * Constructs a new INSTANCE of TelemetryContext.
     *
     * @param context       the context for this telemetryContext
     * @param appIdentifier the app identifier for this application
     */
    @SuppressLint("StaticFieldLeak")
    TelemetryContext(Context context, String appIdentifier) {
        this();
        mWeakContext = new WeakReference<>(context);
        mInstrumentationKey = Util.convertAppIdentifierToGuid(appIdentifier);

        configDeviceContext();
        configInternalContext();
        configApplicationContext();

        // Set device identifier.
        AsyncTaskUtils.execute(new AsyncTask<Void, Object, Object>() {
            @Override
            protected Object doInBackground(Void... voids) {
                try {
                    String deviceId = Constants.getDeviceIdentifier().get();
                    setDeviceId(deviceId);
                    setAnonymousUserId(deviceId);
                } catch (InterruptedException | ExecutionException e) {
                    HockeyLog.debug("Error config device identifier", e);
                }
                return null;
            }
        });
    }

    /**
     * Updates the session context.
     *
     * @param sessionId the current session Id
     */
    void renewSessionContext(String sessionId) {
        configSessionContext(sessionId);
    }

    /**
     * Configure the session context. This is called for each new session.
     *
     * @param sessionId the current session Id
     */
    private void configSessionContext(String sessionId) {
        HockeyLog.debug(TAG, "Configuring session context");

        setSessionId(sessionId);
        HockeyLog.debug(TAG, "Setting the isNew-flag to true, as we only count new sessions");
        setIsNewSession("true");

        Context context = getContext();
        if (context == null) {
            HockeyLog.warn(TAG, "Failed to write to SharedPreferences, context is null");
            return;
        }
        SharedPreferences settings = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        if (!settings.getBoolean(SESSION_IS_FIRST_KEY, false)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(SESSION_IS_FIRST_KEY, true);
            editor.apply();
            setIsFirstSession("true");
            HockeyLog.debug(TAG, "It's our first session, writing true to SharedPreferences.");
        } else {
            setIsFirstSession("false");
            HockeyLog.debug(TAG, "It's not their first session, writing false to SharedPreferences.");
        }
    }

    /**
     * Sets the application telemetryContext tags.
     */
    private void configApplicationContext() {
        HockeyLog.debug(TAG, "Configuring application context");

        // App version
        String version;
        mPackageName = "";
        if (Constants.APP_PACKAGE != null) {
            mPackageName = Constants.APP_PACKAGE;
        }
        version = String.format("%s (%S)", Constants.APP_VERSION_NAME, Constants.APP_VERSION);
        setAppVersion(version);

        // Hockey SDK version
        String sdkVersionString = BuildConfig.VERSION_NAME;
        setSdkVersion("android:" + sdkVersionString);
    }

    /**
     * Sets the device telemetryContext tags.
     */
    private void configDeviceContext() {
        HockeyLog.debug(TAG, "Configuring device context");
        setOsVersion(Build.VERSION.RELEASE);
        setOsName("Android");
        setDeviceModel(Build.MODEL);
        setDeviceOemName(Build.MANUFACTURER);
        setOsLocale(Locale.getDefault().toString());
        setOsLanguage(Locale.getDefault().getLanguage());
        updateScreenResolution();

        // check device type
        Context context = getContext();
        final TelephonyManager telephonyManager = context != null
                ? (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)
                : null;
        if (telephonyManager == null || telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE) {
            setDeviceType("Phone");
        } else {
            setDeviceType("Tablet");
        }

        // detect emulator
        if (Util.isEmulator()) {
            setDeviceModel("[Emulator]" + mDevice.getModel());
        }
    }

    void updateScreenResolution() {
        String resolutionString;
        int width;
        int height;

        Context context = getContext();
        if (context != null) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Point size = new Point();
                Display d = wm.getDefaultDisplay();
                if (d != null) {
                    d.getRealSize(size);
                    width = size.x;
                    height = size.y;
                } else {
                    width = 0;
                    height = 0;
                }
            } else {
                try {
                    // We have to use undocumented API here. Android 4.0 introduced soft buttons for
                    // back, home and menu, but there's no API present to get the real display size
                    // all available methods only return the size of the contentView.
                    Method mGetRawW = Display.class.getMethod("getRawWidth");
                    Method mGetRawH = Display.class.getMethod("getRawHeight");
                    Display display = wm.getDefaultDisplay();
                    width = (Integer) mGetRawW.invoke(display);
                    height = (Integer) mGetRawH.invoke(display);
                } catch (Exception ex) {
                    Point size = new Point();
                    Display d = wm.getDefaultDisplay();
                    if (d != null) {
                        d.getSize(size);
                        width = size.x;
                        height = size.y;
                    } else {
                        width = 0;
                        height = 0;
                    }
                    HockeyLog.debug(TAG, "Couldn't determine screen resolution: " + ex.toString());
                }
            }
            resolutionString = String.valueOf(height) + "x" + String.valueOf(width);
            setScreenResolution(resolutionString);
        }
    }

    /**
     * Sets the internal package context.
     */
    void configInternalContext() {
        String sdkVersionString = BuildConfig.VERSION_NAME;
        setSdkVersion("android:" + sdkVersionString);
    }

    /**
     * Retrieves the context from the weak reference.
     *
     * @return The context object for this instance.
     */
    private Context getContext() {
        return mWeakContext != null ? mWeakContext.get() : null;
    }

    /**
     * The package name.
     */
    protected String getPackageName() {
        return mPackageName;
    }

    protected Map<String, String> getContextTags() {
        Map<String, String> contextTags = new LinkedHashMap<>();

        synchronized (mApplication) {
            mApplication.addToHashMap(contextTags);
        }
        synchronized (mDevice) {
            mDevice.addToHashMap(contextTags);
        }
        synchronized (mSession) {
            mSession.addToHashMap(contextTags);
        }
        synchronized (mUser) {
            mUser.addToHashMap(contextTags);
        }
        synchronized (mInternal) {
            mInternal.addToHashMap(contextTags);
        }

        return contextTags;
    }

    public String getInstrumentationKey() {
        synchronized (IKEY_LOCK) {
            return mInstrumentationKey;
        }
    }

    public synchronized void setInstrumentationKey(String instrumentationKey) {
        synchronized (IKEY_LOCK) {
            mInstrumentationKey = instrumentationKey;
        }
    }

    public String getScreenResolution() {
        synchronized (mDevice) {
            return mDevice.getScreenResolution();
        }
    }

    public void setScreenResolution(String screenResolution) {
        synchronized (mDevice) {
            mDevice.setScreenResolution(screenResolution);
        }
    }

    public String getAppVersion() {
        synchronized (mApplication) {
            return mApplication.getVer();
        }
    }

    public void setAppVersion(String appVersion) {
        synchronized (mApplication) {
            mApplication.setVer(appVersion);
        }
    }

    public String getAnonymousUserId() {
        synchronized (mUser) {
            return mUser.getId();
        }
    }

    public void setAnonymousUserId(String userId) {
        synchronized (mUser) {
            mUser.setId(userId);
        }
    }

    public String getSdkVersion() {
        synchronized (mInternal) {
            return mInternal.getSdkVersion();
        }
    }

    public void setSdkVersion(String sdkVersion) {
        synchronized (mInternal) {
            mInternal.setSdkVersion(sdkVersion);
        }
    }

    public String getSessionId() {
        synchronized (mSession) {
            return mSession.getId();
        }
    }

    public void setSessionId(String sessionId) {
        synchronized (mSession) {
            mSession.setId(sessionId);
        }
    }

    public String getIsFirstSession() {
        synchronized (mSession) {
            return mSession.getIsFirst();
        }
    }

    public void setIsFirstSession(String isFirst) {
        synchronized (mSession) {
            mSession.setIsFirst(isFirst);
        }
    }

    public String getIsNewSession() {
        synchronized (mSession) {
            return mSession.getIsNew();
        }
    }

    public void setIsNewSession(String isNewSession) {
        synchronized (mSession) {
            mSession.setIsNew(isNewSession);
        }
    }

    public String getOsVersion() {
        synchronized (mDevice) {
            return mDevice.getOsVersion();
        }
    }

    public void setOsVersion(String osVersion) {
        synchronized (mDevice) {
            mDevice.setOsVersion(osVersion);
        }
    }

    public String getOsName() {
        synchronized (mDevice) {
            return mDevice.getOs();
        }
    }

    public void setOsName(String osName) {
        synchronized (mDevice) {
            mDevice.setOs(osName);
        }
    }

    public String getDeviceModel() {
        synchronized (mDevice) {
            return mDevice.getModel();
        }
    }

    public void setDeviceModel(String deviceModel) {
        synchronized (mDevice) {
            mDevice.setModel(deviceModel);
        }
    }

    public String getDeviceOemName() {
        synchronized (mDevice) {
            return mDevice.getOemName();
        }
    }

    public void setDeviceOemName(String deviceOemName) {
        synchronized (mDevice) {
            mDevice.setOemName(deviceOemName);
        }
    }

    public String getOsLocale() {
        synchronized (mDevice) {
            return mDevice.getLocale();
        }
    }

    public void setOsLocale(String osLocale) {
        synchronized (mDevice) {
            mDevice.setLocale(osLocale);
        }
    }

    public String getOSLanguage() {
        synchronized (mDevice) {
            return mDevice.getLanguage();
        }
    }

    public void setOsLanguage(String osLanguage) {
        synchronized (mDevice) {
            mDevice.setLanguage(osLanguage);
        }
    }

    public String getDeviceId() {
        return mDevice.getId();
    }

    public void setDeviceId(String deviceId) {
        synchronized (mDevice) {
            mDevice.setId(deviceId);
        }
    }

    public String getDeviceType() {
        return mDevice.getType();
    }

    public void setDeviceType(String deviceType) {
        synchronized (mDevice) {
            mDevice.setType(deviceType);
        }
    }
}
