package net.hockeyapp.android.metrics;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;

import net.hockeyapp.android.BuildConfig;
import net.hockeyapp.android.Constants;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.Util;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * <h3>Description</h3>
 * <p/>
 * Class that manages the context in which telemetry items get sent.
 **/
class TelemetryContext {

    private static final String TAG = "HockeyApp Metrics";

    /**
     * Key needed to access the shared preferences of the SDK.
     */
    private static final String SHARED_PREFERENCES_KEY = "HOCKEY_APP_TELEMETRY_CONTEXT";

    /**
     * Key needed to access the anonymous user id saved in the preferences.
     */
    private static final String USER_ANOM_ID_KEY = "USER_ID";

    /**
     * Key needed to determine, whether we have a new or exisiting user.
     */
    private static final String SESSION_IS_FIRST_KEY = "SESSION_IS_FIRST";

    /**
     * Synchronization LOCK for setting static context.
     */
    private static final Object LOCK = new Object();
    /**
     * Device telemetryContext.
     */
    protected final Device device;
    /**
     * Session context.
     */
    protected final Session session;
    /**
     * User context.
     */
    protected final User user;
    /**
     * Internal context.
     */
    protected final Internal internal;
    /**
     * Application context.
     */
    protected final Application application;
    /**
     * Synchronization LOCK for setting instrumentation key.
     */
    private final Object IKEY_LOCK = new Object();
    /**
     * The application context needed to update some context values.
     */
    protected Context context;
    /**
     * The shared preferences INSTANCE for reading persistent context.
     */
    protected SharedPreferences settings;
    /**
     * Device context.
     */
    protected String instrumentationKey;
    /**
     * The app's package name.
     */
    protected String packageName;

    /**
     * Constructs a new INSTANCE of TelemetryContext.
     */
    private TelemetryContext() {
        this.device = new Device();
        this.session = new Session();
        this.user = new User();
        this.application = new Application();
        this.internal = new Internal();
    }

    /**
     * Constructs a new INSTANCE of TelemetryContext.
     *
     * @param context       the context for this telemetryContext
     * @param appIdentifier the app identifier for this application
     */
    protected TelemetryContext(Context context, String appIdentifier) {
        this();
        this.settings = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        this.context = context;
        this.instrumentationKey = Util.convertAppIdentifierToGuid(appIdentifier);

        configDeviceContext();
        configUserContext();
        configInternalContext();
        configApplicationContext();
    }

    /**
     * Updates the session context.
     *
     * @param sessionId the current session Id
     */
    protected void updateSessionContext(String sessionId) {
        configSessionContext(sessionId);
    }

    /**
     * Configure the session context.
     *
     * @param sessionId the current session Id
     */
    protected void configSessionContext(String sessionId) {
        setSessionId(sessionId);
        //normally, this should also be saved to SharedPrefs like isFirst.
        //The problem is that there are cases when committing the changes is too slow and we get
        //the wrong value. As isNew is only "true" when we start a new session, it is set in
        //TrackDataOperation directly before enqueueing the session event.
        setIsNewSession("false");

        SharedPreferences.Editor editor = this.settings.edit();
        if (!this.settings.getBoolean(SESSION_IS_FIRST_KEY, false)) {
            editor.putBoolean(SESSION_IS_FIRST_KEY, true);
            editor.apply();
            setIsFirstSession("true");
        } else {
            setIsFirstSession("false");
        }
    }

    /**
     * Sets the application telemetryContext tags.
     */
    protected void configApplicationContext() {

        // App version
        String version = "unknown";
        this.packageName = "";

        try {
            final PackageManager manager = this.context.getPackageManager();
            final PackageInfo info = manager
                    .getPackageInfo(this.context.getPackageName(), 0);

            if (info.packageName != null) {
                this.packageName = info.packageName;
            }

            String appBuild = Integer.toString(info.versionCode);
            version = String.format("%s (%S)", info.versionName, appBuild);
        } catch (PackageManager.NameNotFoundException e) {
            HockeyLog.log(Constants.TAG, "Could not collect application context");
        } finally {
            setAppVersion(version);
        }

        // Hockey SDK version
        String sdkVersionString = BuildConfig.VERSION_NAME;
        setSdkVersion("android:" + sdkVersionString);
    }

    /**
     * Loads an existing user context if existing.
     *
     * @param userId custom user id
     */
    protected void configUserContext(String userId) {
        if (userId == null) {
            // No custom user Id is given, so get this info from settings
            userId = this.settings.getString(TelemetryContext.USER_ANOM_ID_KEY, null);
            if (userId == null) {
                // No settings available, generate new user info
                userId = UUID.randomUUID().toString();

            }
        }
        setAnonymousUserId(userId);
        saveUserInfo();
    }

    /**
     * Load the user context associated with telemetry data.
     */
    protected void configUserContext() {
        loadUserInfo();
        if (user != null && user.getId() == null) {
            setAnonymousUserId(UUID.randomUUID().toString());
            saveUserInfo();
        }
    }

    /**
     * Write user information to shared preferences.
     */
    protected void saveUserInfo() {
        SharedPreferences.Editor editor = this.settings.edit();
        editor.putString(TelemetryContext.USER_ANOM_ID_KEY, getAnonymousUserId());
        editor.apply();
    }

    /**
     * Load user information to shared preferences.
     */
    protected void loadUserInfo() {
        String userId = this.settings.getString(USER_ANOM_ID_KEY, null);
        setAnonymousUserId(userId);
    }

    /**
     * Sets the device telemetryContext tags.
     */
    protected void configDeviceContext() {
        setOsVersion(Build.VERSION.RELEASE);
        setOsName("Android");
        setDeviceModel(Build.MODEL);
        setDeviceOemName(Build.MANUFACTURER);
        setOsLocale(Locale.getDefault().toString());
        setOsLanguage(Locale.getDefault().getLanguage());
        updateScreenResolution();

        // get device ID
        ContentResolver resolver = this.context.getContentResolver();
        String deviceIdentifier = Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID);
        if (deviceIdentifier != null) {
            setDeviceId(Util.tryHashStringSha256(deviceIdentifier));
        }

        // check device type
        final TelephonyManager telephonyManager = (TelephonyManager)
                this.context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE) {
            setDeviceType("Phone");
        } else {
            setDeviceType("Tablet");
        }

        // detect emulator
        if (Util.isEmulator()) {
            setDeviceModel("[Emulator]" + device.getModel());
        }
    }

    @SuppressLint({"NewApi", "Deprecation"})
    protected void updateScreenResolution() {
        String resolutionString;
        int width;
        int height;

        if (this.context != null) {
            WindowManager wm = (WindowManager) this.context.getSystemService(
                    Context.WINDOW_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Point size = new Point();
                wm.getDefaultDisplay().getRealSize(size);
                width = size.x;
                height = size.y;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                try {
                    //We have to use undocumented API here. Android 4.0 introduced soft buttons for
                    //back, home and menu, but there's no API present to get the real display size
                    //all available methods only return the size of the contentview.
                    Method mGetRawW = Display.class.getMethod("getRawWidth");
                    Method mGetRawH = Display.class.getMethod("getRawHeight");
                    Display display = wm.getDefaultDisplay();
                    width = (Integer) mGetRawW.invoke(display);
                    height = (Integer) mGetRawH.invoke(display);
                } catch (Exception ex) {
                    Point size = new Point();
                    wm.getDefaultDisplay().getSize(size);
                    width = size.x;
                    height = size.y;
                    HockeyLog.log(TAG, "Couldn't determine screen resolution: " + ex.toString());
                }

            } else {
                //Use old, and now deprecated API to get width and height of the display
                Display d = wm.getDefaultDisplay();
                width = d.getWidth();
                height = d.getHeight();
            }

            resolutionString = String.valueOf(height) + "x" + String.valueOf(width);

            setScreenResolution(resolutionString);
        }
    }

    /**
     * Sets the internal package context
     */
    protected void configInternalContext() {
        String sdkVersionString = BuildConfig.VERSION_NAME;
        setSdkVersion("android:" + sdkVersionString);
    }

    /**
     * The package name.
     */
    protected String getPackageName() {
        return this.packageName;
    }

    protected Map<String, String> getContextTags() {
        Map<String, String> contextTags = new LinkedHashMap<String, String>();

        synchronized (this.application) {
            this.application.addToHashMap(contextTags);
        }
        synchronized (this.device) {
            this.device.addToHashMap(contextTags);
        }
        synchronized (this.session) {
            this.session.addToHashMap(contextTags);
        }
        synchronized (this.user) {
            this.user.addToHashMap(contextTags);
        }
        synchronized (this.internal) {
            this.internal.addToHashMap(contextTags);
        }

        return contextTags;
    }

    public String getInstrumentationKey() {
        synchronized (IKEY_LOCK) {
            return this.instrumentationKey;
        }
    }

    public synchronized void setInstrumentationKey(String instrumentationKey) {
        synchronized (IKEY_LOCK) {
            this.instrumentationKey = instrumentationKey;
        }
    }

    public String getScreenResolution() {
        synchronized (this.application) {
            return this.device.getScreenResolution();
        }
    }

    public void setScreenResolution(String screenResolution) {
        synchronized (this.application) {
            this.device.setScreenResolution(screenResolution);
        }
    }

    public String getAppVersion() {
        synchronized (this.application) {
            return this.application.getVer();
        }
    }

    public void setAppVersion(String appVersion) {
        synchronized (this.application) {
            this.application.setVer(appVersion);
        }
    }

    public String getAnonymousUserId() {
        synchronized (this.user) {
            return this.user.getId();
        }
    }

    public void setAnonymousUserId(String userId) {
        synchronized (this.user) {
            this.user.setId(userId);
        }
    }

    public String getSdkVersion() {
        synchronized (this.internal) {
            return this.internal.getSdkVersion();
        }
    }

    public void setSdkVersion(String sdkVersion) {
        synchronized (this.internal) {
            this.internal.setSdkVersion(sdkVersion);
        }
    }

    public String getSessionId() {
        synchronized (this.session) {
            return this.session.getId();
        }
    }

    public void setSessionId(String sessionId) {
        synchronized (this.session) {
            this.session.setId(sessionId);
        }
    }

    public String getIsFirstSession() {
        synchronized (this.session) {
            return this.session.getIsFirst();
        }
    }

    public void setIsFirstSession(String isFirst) {
        synchronized (this.session) {
            this.session.setIsFirst(isFirst);
        }
    }

    public String getIsNewSession() {
        synchronized (this.session) {
            return this.session.getIsNew();
        }
    }

    public void setIsNewSession(String isFirst) {
        synchronized (this.session) {
            this.session.setIsNew(isFirst);
        }
    }

    public String getOsVersion() {
        synchronized (this.device) {
            return this.device.getOsVersion();
        }
    }

    public void setOsVersion(String osVersion) {
        synchronized (this.device) {
            this.device.setOsVersion(osVersion);
        }
    }

    public String getOsName() {
        synchronized (this.device) {
            return this.device.getOs();
        }
    }

    public void setOsName(String osName) {
        synchronized (this.device) {
            this.device.setOs(osName);
        }
    }

    public String getDeviceModel() {
        synchronized (this.device) {
            return this.device.getModel();
        }
    }

    public void setDeviceModel(String deviceModel) {
        synchronized (this.device) {
            this.device.setModel(deviceModel);
        }
    }

    public String getDeviceOemName() {
        synchronized (this.device) {
            return this.device.getOemName();
        }
    }

    public void setDeviceOemName(String deviceOemName) {
        synchronized (this.device) {
            this.device.setOemName(deviceOemName);
        }
    }

    public String getOsLocale() {
        synchronized (this.device) {
            return this.device.getLocale();
        }
    }

    public void setOsLocale(String osLocale) {
        synchronized (this.device) {
            this.device.setLocale(osLocale);
        }
    }

    public String getOSLanguage() {
        synchronized (this.device) {
            return this.device.getLanguage();
        }
    }

    public void setOsLanguage(String osLanguage) {
        synchronized (this.device) {
            this.device.setLanguage(osLanguage);
        }
    }

    public String getDeviceId() {
        return this.device.getId();
    }

    public void setDeviceId(String deviceId) {
        synchronized (this.device) {
            this.device.setId(deviceId);
        }
    }

    public String getDeviceType() {
        return this.device.getType();
    }

    public void setDeviceType(String deviceType) {
        synchronized (this.device) {
            this.device.setType(deviceType);
        }
    }
}
