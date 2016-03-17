package net.hockeyapp.android.metrics;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import net.hockeyapp.android.Constants;
import net.hockeyapp.android.PrivateEventManager;
import net.hockeyapp.android.metrics.model.Data;
import net.hockeyapp.android.metrics.model.Domain;
import net.hockeyapp.android.metrics.model.EventData;
import net.hockeyapp.android.metrics.model.SessionState;
import net.hockeyapp.android.metrics.model.SessionStateData;
import net.hockeyapp.android.metrics.model.TelemetryData;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.Util;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <h3>Description</h3>
 * <p/>
 * Provides functionality to gather User Metrics, both active users, sessions,
 * and custom events.
 *
 **/
public class MetricsManager {

    private static final String TAG = "HA-MetricsManager";

    /**
     * Whether User Metrics should be globally enabled or not.
     * Includes both user/session telemetry as well as custom events.
     * Default is true.
     */
    private static boolean sUserMetricsEnabled = true;

    /**
     * The activity counter.
     */
    protected static final AtomicInteger ACTIVITY_COUNT = new AtomicInteger(0);

    /**
     * The timestamp of the last activity background event.
     */
    protected static final AtomicLong LAST_BACKGROUND = new AtomicLong(getTime());

    /**
     * Background time interval for the app after which a session gets renewed (in milliseconds).
     */
    private static final Integer SESSION_RENEWAL_INTERVAL = 20 * 1000;

    /**
     * Synchronization lock for setting static context.
     */
    private static final Object LOCK = new Object();

    /**
     * The MetricsManager singleton instance.
     */
    private static volatile MetricsManager instance;

    /**
     * Weak reference to the application necessary for capturing session events.
     */
    private static WeakReference<Application> sWeakApplication;

    /**
     * Sender instance to send data to the endpoint. Serves the goal of easy customization by the user
     * in the registration process.
     */
    private static Sender sSender;
    /**
     * Channel for collecting new events before storing and sending them.
     */
    private static Channel sChannel;
    /**
     * A telemetry context which is used to automatically add environment and meta information
     * to events.
     */
    private static TelemetryContext sTelemetryContext;
    /**
     * Flag that indicates disabled session tracking.
     * Default is false.
     */
    private volatile boolean mSessionTrackingDisabled;

    private TelemetryLifecycleCallbacks mTelemetryLifecycleCallbacks;

    /**
     * Creates and initializes a new instance of the MetricsManager class.
     * Not publicly accessible, only accessible for internal use and testing/mocking.
     *
     * @param context          Context that will be used.
     * @param telemetryContext Telemetry context, contains meta-information necessary for metrics
     *                         feature.
     * @param sender           Usually null, to be set for unit testing/dependency injection.
     * @param persistence      Included for unit testing/dependency injection.
     * @param channel          Included for unit testing/dependency injection.
     */
    protected MetricsManager(Context context, TelemetryContext telemetryContext, Sender sender,
                             Persistence persistence, Channel channel) {
        sTelemetryContext = telemetryContext;

        // Important: create sender and persistence first, wire them up and then create the channel!
        if (sender == null) {
            sender = new Sender();
        }
        sSender = sender;

        if (persistence == null) {
            persistence = new Persistence(context, sender);
        } else {
            persistence.setSender(sender);
        }

        // Link sender
        sSender.setPersistence(persistence);

        // Create the channel and wire the persistence to it.
        if (channel == null) {
            sChannel = new Channel(sTelemetryContext, persistence);
        } else {
            sChannel = channel;
        }

        // Check if any previous events are in persistence and send them
        if (persistence.hasFilesAvailable()) {
            persistence.getSender().triggerSending();
        }
    }

    /**
     * Register a new MetricsManager and collect metrics about user and session.
     * HockeyApp app identifier is read from configuration values in AndroidManifest.xml.
     *
     * @param context     The context to use. Usually your activity.
     * @param application The application which is required to get application lifecycle
     *                    callbacks.
     */
    public static void register(Context context, Application application) {
        String appIdentifier = Util.getAppIdentifier(context);
        if (appIdentifier == null || appIdentifier.length() == 0) {
            throw new IllegalArgumentException("HockeyApp app identifier was not configured correctly in manifest or build configuration.");
        }
        register(context, application, appIdentifier);
    }

    /**
     * Register a new MetricsManager and collect metrics about user and session, while
     * explicitly providing your HockeyApp app identifier.
     *
     * @param application   The application which is required to get application lifecycle
     *                      callbacks.
     * @param context       The context to use. Usually your activity.
     * @param appIdentifier Your HockeyApp App Identifier.
     */
    public static void register(Context context, Application application, String appIdentifier) {
        register(context, application, appIdentifier, null, null, null);
    }

    /**
     * Register a new MetricsManager and collect metrics information about user and session.
     * Intended to be used for unit testing only.
     *
     * @param context       The context to use. Usually your activity.
     * @param application   The application which is required to get application lifecycle
     *                      callbacks.
     * @param appIdentifier Your HockeyApp app identifier.
     * @param sender        Sender for dependency injection.
     * @param persistence   Persistence for dependency injection.
     * @param channel       Channel for dependency injection.
     */
    protected static void register(Context context, Application application, String appIdentifier,
                                   Sender sender, Persistence persistence, Channel channel) {
        MetricsManager result = instance;
        if (result == null) {
            synchronized (LOCK) {
                result = instance;        // thread may have instantiated the object
                if (result == null) {
                    Constants.loadFromContext(context);
                    result = new MetricsManager(context, new TelemetryContext(context, appIdentifier),
                            sender, persistence, channel);
                    sWeakApplication = new WeakReference<>(application);
                }
                result.mSessionTrackingDisabled = !Util.sessionTrackingSupported();
                instance = result;
                if (!result.mSessionTrackingDisabled) {
                    setSessionTrackingDisabled(false);
                }

            }

            PrivateEventManager.addEventListener(new PrivateEventManager.HockeyEventListener() {
                @Override
                public void onHockeyEvent(PrivateEventManager.Event event) {
                    if (event.getType() == PrivateEventManager.EVENT_TYPE_UNCAUGHT_EXCEPTION) {
                        sChannel.synchronize();
                    }
                }
            });
        }
    }

    /**
     * Disables User Metrics collection and transmission. Use this if your user opts out of
     * telemetry collection.
     */
    public static void disableUserMetrics() {
        setUserMetricsEnabled(false);
    }

    /**
     * Re-enables User Metrics collection and transmission. Use this if your user granted you
     * telemetry collection. User Metrics collection is enabled by default.
     */
    public static void enableUserMetrics() {
        setUserMetricsEnabled(true);
    }

    public static boolean isUserMetricsEnabled() {
        return sUserMetricsEnabled;
    }

    private static void setUserMetricsEnabled(boolean enabled) {
        sUserMetricsEnabled = enabled;
        if (sUserMetricsEnabled) {
            instance.registerTelemetryLifecycleCallbacks();
        } else {
            instance.unregisterTelemetryLifecycleCallbacks();
        }
    }

    /**
     * Determines if session tracking was enabled.
     *
     * @return YES if session tracking is enabled
     */
    public static boolean sessionTrackingEnabled() {
        return isUserMetricsEnabled() && !instance.mSessionTrackingDisabled;
    }

    /**
     * Enable and disable tracking of sessions
     *
     * @param disabled flag to indicate
     */
    public static void setSessionTrackingDisabled(Boolean disabled) {
        if (instance == null || !isUserMetricsEnabled()) {
            Log.w(TAG, "MetricsManager hasn't been registered or User Metrics has been disabled. No User Metrics will be collected!");
        } else {
            synchronized (LOCK) {
                if (Util.sessionTrackingSupported()) {
                    instance.mSessionTrackingDisabled = disabled;
                    //TODO persist this setting so the dev doesn't have to take care of this
                    //between launches?
                    if (!disabled) {
                        instance.registerTelemetryLifecycleCallbacks();
                    }
                } else {
                    instance.mSessionTrackingDisabled = true;
                    instance.unregisterTelemetryLifecycleCallbacks();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void registerTelemetryLifecycleCallbacks() {
        if (mTelemetryLifecycleCallbacks == null) {
            mTelemetryLifecycleCallbacks = new TelemetryLifecycleCallbacks();
        }
        getApplication().registerActivityLifecycleCallbacks(mTelemetryLifecycleCallbacks);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void unregisterTelemetryLifecycleCallbacks() {
        if (mTelemetryLifecycleCallbacks == null) {
            return;
        }
        getApplication().unregisterActivityLifecycleCallbacks(mTelemetryLifecycleCallbacks);
        mTelemetryLifecycleCallbacks = null;
    }

    /**
     * Set the server url if you want metrics to be sent to a custom server
     *
     * @param serverURL the URL of your custom metrics server as a String
     */
    public static void setCustomServerURL(String serverURL) {
        if (sSender != null) {
            sSender.setCustomServerURL(serverURL);
        } else {
            Log.w(TAG, "HockeyApp couldn't set the custom server url. Please register(...) the MetricsManager before setting the server URL.");
        }
    }

    /**
     * Get the reference to the Application (used for life-cycle tracking)
     *
     * @return the reference to the application that was used during initialization of the SDK
     */
    private static Application getApplication() {
        Application application = null;
        if (sWeakApplication != null) {
            application = sWeakApplication.get();
        }

        return application;
    }

    /**
     * Get the current time
     *
     * @return the current time in milliseconds
     */
    private static long getTime() {
        return new Date().getTime();
    }

    protected static Channel getChannel() {
        return sChannel;
    }

    protected void setChannel(Channel channel) {
        sChannel = channel;
    }

    protected static Sender getSender() {
        return sSender;
    }

    protected static void setSender(Sender sender) {
        sSender = sender;
    }

    protected static MetricsManager getInstance() {
        return instance;
    }

    /**
     * Updates the session. If session tracking is enabled, a new session will be started for the
     * first activity.
     * In case we have already started a session, we determine if we should renew a session.
     * This is done by comparing NOW with the last time, onPause has been called.
     */
    private void updateSession() {
        int count = this.ACTIVITY_COUNT.getAndIncrement();
        if (count == 0) {
            if (sessionTrackingEnabled()) {
                HockeyLog.debug(TAG, "Starting & tracking session");
                renewSession();
            } else {
                HockeyLog.debug(TAG, "Session management disabled by the developer");
            }
        } else {
            //we should already have a session now
            //check if the session should be renewed
            long now = this.getTime();
            long then = this.LAST_BACKGROUND.getAndSet(getTime());
            boolean shouldRenew = ((now - then) >= SESSION_RENEWAL_INTERVAL);
            HockeyLog.debug(TAG, "Checking if we have to renew a session, time difference is: " + (now - then));

            if (shouldRenew && sessionTrackingEnabled()) {
                HockeyLog.debug(TAG, "Renewing session");
                renewSession();
            }
        }
    }

    protected void renewSession() {
        String sessionId = UUID.randomUUID().toString();
        sTelemetryContext.renewSessionContext(sessionId);
        trackSessionState(SessionState.START);
    }

    /**
     * Creates and enqueues a session event for the given state.
     *
     * @param sessionState value that determines whether the session started or ended
     */
    private void trackSessionState(final SessionState sessionState) {
        try {
            AsyncTaskUtils.execute(new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    SessionStateData sessionItem = new SessionStateData();
                    sessionItem.setState(sessionState);
                    Data<Domain> data = createData(sessionItem);
                    sChannel.enqueueData(data);
                    return null;
                }
            });
        } catch (RejectedExecutionException e) {
            HockeyLog.error("Could not track session state. Executor rejected async task.", e);
        }

    }

    /**
     * Pack and forward the telemetry item to the queue.
     *
     * @param telemetryData The telemetry event to be persisted and sent
     * @return a base data object containing the telemetry data
     */
    protected static Data<Domain> createData(TelemetryData telemetryData) {
        Data<Domain> data = new Data<>();
        data.setBaseData(telemetryData);
        data.setBaseType(telemetryData.getBaseType());
        data.QualifiedName = telemetryData.getEnvelopeName();

        return data;
    }

    public static void trackEvent(final String eventName) {
        if (TextUtils.isEmpty(eventName)) {
            return;
        }
        if (!isUserMetricsEnabled()) {
            HockeyLog.warn("User Metrics is disabled. Will not track event.");
            return;
        }
        try {
            AsyncTaskUtils.execute(new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    EventData eventItem = new EventData();
                    eventItem.setName(eventName);
                    Data<Domain> data = createData(eventItem);
                    sChannel.enqueueData(data);
                    return null;
                }
            });
        } catch (RejectedExecutionException e) {
            HockeyLog.error("Could not track custom event. Executor rejected async task.", e);
        }

    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private class TelemetryLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            // unused but required to implement ActivityLifecycleCallbacks
            //NOTE:
            //This callback doesn't work for the starting
            //activity of the app because the SDK will be setup and initialized in the onCreate, so
            //we don't get the very first call to an app activity's onCreate.
        }

        @Override
        public void onActivityStarted(Activity activity) {
            // unused but required to implement ActivityLifecycleCallbacks
        }

        @Override
        public void onActivityResumed(Activity activity) {
            updateSession();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            //set the timestamp when the app was last send to the background. This will be continuously
            //updated when the user navigates through the app.
            LAST_BACKGROUND.set(getTime());
        }

        @Override
        public void onActivityStopped(Activity activity) {
            // unused but required to implement ActivityLifecycleCallbacks
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            // unused but required to implement ActivityLifecycleCallbacks
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            // unused but required to implement ActivityLifecycleCallbacks
        }
    }

}
