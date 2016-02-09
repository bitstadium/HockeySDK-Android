package net.hockeyapp.android.metrics;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import net.hockeyapp.android.Constants;
import net.hockeyapp.android.metrics.model.Data;
import net.hockeyapp.android.metrics.model.Domain;
import net.hockeyapp.android.metrics.model.SessionState;
import net.hockeyapp.android.metrics.model.SessionStateData;
import net.hockeyapp.android.metrics.model.TelemetryData;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.Util;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <h3>Description</h3>
 * <p>
 * The MetricsManager provides functionality to gather metrics about your users and session.
 * </p>
 * <h3>License</h3>
 * <p/>
 * <pre>
 * Copyright (c) 2011-2015 Bit Stadium GmbH
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * </pre>
 *
 * @author Benjamin Reimold
 **/
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MetricsManager implements Application.ActivityLifecycleCallbacks {

    /**
     * The activity counter
     */
    protected static final AtomicInteger ACTIVITY_COUNT = new AtomicInteger(0);

    /**
     * The timestamp of the last activity
     */
    protected static final AtomicLong LAST_BACKGROUND = new AtomicLong(getTime());

    private static final String TAG = "HA-MetricsManager";
    /**
     * Background time of the app after which a session gets renewed (in milliseconds).
     */
    private static final Integer SESSION_RENEWAL_INTERVAL = 20 * 1000;

    /**
     * Synchronization LOCK for setting static context
     */
    private static final Object LOCK = new Object();

    /**
     * The only MetricsManager instance.
     */
    private static volatile MetricsManager instance;

    /**
     * The application needed for auto collecting session data
     */
    private static WeakReference<Application> sWeakApplication;

    /**
     * A sender who's responsible to send telemetry to the server
     * MetricsManager holds a reference to it because we want the user to easily set the server
     * url.
     */
    private static Sender sSender;
    /**
     * A channel for collecting new events before storing and sending them.
     */
    private static Channel sChannel;
    /**
     * A telemetry context which is used to add meta info to events, before they're sent out.
     */
    private static TelemetryContext sTelemetryContext;
    /**
     * Flag that indicates disabled session tracking.
     * Default is false.
     */
    private volatile boolean mSessionTrackingDisabled;

    /**
     * Restrict access to the default constructor
     * Create a new INSTANCE of the MetricsManager class
     * Contains params for unit testing/mocking
     *
     * @param context          the context that will be used for the SDK
     * @param telemetryContext telemetry context, contains meta-information necessary for metrics
     *                         feature of the SDK
     * @param sender           usually null, included for unit testing/dependency injection
     * @param persistence,     included for unit testing/dependency injection
     * @param channel,         included for unit testing/dependency injection
     */
    protected MetricsManager(Context context, TelemetryContext telemetryContext, Sender sender,
                             Persistence persistence, Channel channel) {
        sTelemetryContext = telemetryContext;

        //Important: create sender and persistence first, wire them up and then create the channel!
        if (sender == null) {
            sender = new Sender();
        }
        sSender = sender;

        if (persistence == null) {
            persistence = new Persistence(context, sender);
        }

        //Link sender
        this.sSender.setPersistence(persistence);

        //create the channel and wire the persistence to it.
        if (channel == null) {
            sChannel = new Channel(sTelemetryContext, persistence);
        } else {
            sChannel = channel;
        }

    }

    /**
     * Register a new MetricsManager and collect metrics about user and session.
     * HockeyApp App Identifier is read from configuration values in AndroidManifest.xml
     *
     * @param context     The context to use. Usually your Activity object.
     * @param application the Application object which is required to get application lifecycle
     *                    callbacks
     */
    public static void register(Context context, Application application) {
        String appIdentifier = Util.getAppIdentifier(context);
        if (appIdentifier == null || appIdentifier.length() == 0) {
            throw new IllegalArgumentException("HockeyApp app identifier was not configured correctly in manifest or build configuration.");
        }
        register(context, application, appIdentifier);
    }

    /**
     * Register a new MetricsManager and collect metrics about user and session.
     *
     * @param application   the Application object which is required to get application lifecycle
     *                      callbacks
     * @param context       The context to use. Usually your Activity object.
     * @param appIdentifier your HockeyApp App Identifier.
     */
    public static void register(Context context, Application application, String appIdentifier) {
        register(context, application, appIdentifier, null, null, null);
    }

    /**
     * Register a new MetricsManager and collect metrics information about user and session
     * Intended to be used for unit testing only, shouldn't be visible outside the SDK   *
     *
     * @param context       The context to use. Usually your Activity object.
     * @param application   the Application object which is required to get application lifecycle
     *                      callbacks
     * @param appIdentifier your HockeyApp App Identifier.
     * @param sender        sender for dependency injection
     * @param persistence   persistence for dependency injection
     * @param channel       channel for dependency injection
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
        }
    }

    /**
     * Determines if session tracking was enabled.
     *
     * @return YES if session tracking is enabled
     */
    public static boolean sessionTrackingEnabled() {
        return !instance.mSessionTrackingDisabled;
    }

    /**
     * Enable and disable tracking of sessions
     *
     * @param disabled flag to indicate
     */
    public static void setSessionTrackingDisabled(Boolean disabled) {
        if (instance == null) {
            Log.w(TAG, "MetricsManager hasn't been registered. No Metrics will be collected!");
        } else {
            synchronized (LOCK) {
                if (Util.sessionTrackingSupported()) {
                    instance.mSessionTrackingDisabled = disabled;
                    //TODO persist this setting so the dev doesn't have to take care of this
                    //between launches?
                    if (!disabled) {
                        getApplication().registerActivityLifecycleCallbacks(instance);
                    }
                } else {
                    instance.mSessionTrackingDisabled = true;
                    getApplication().unregisterActivityLifecycleCallbacks(instance);
                }
            }
        }
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
        LAST_BACKGROUND.set(this.getTime());
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
                HockeyLog.log(TAG, "Starting & tracking session");
                renewSession();
            } else {
                HockeyLog.log(TAG, "Session management disabled by the developer");
            }
        } else {
            //we should already have a session now
            //check if the session should be renewed
            long now = this.getTime();
            long then = this.LAST_BACKGROUND.getAndSet(getTime());
            //TODO save session intervall in configuration file?
            boolean shouldRenew = ((now - then) >= SESSION_RENEWAL_INTERVAL);
            HockeyLog.log(TAG, "Checking if we have to renew a session, time difference is: " + (now - then));

            if (shouldRenew && sessionTrackingEnabled()) {
                HockeyLog.log(TAG, "Renewing session");
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
    }

    /**
     * Pack and forward the telemetry item to the queue.
     *
     * @param telemetryData The telemetry event to be persisted and sent
     * @return a base data object containing the telemetry data
     */
    protected Data<Domain> createData(TelemetryData telemetryData) {
        Data<Domain> data = new Data<>();
        data.setBaseData(telemetryData);
        data.setBaseType(telemetryData.getBaseType());
        data.QualifiedName = telemetryData.getEnvelopeName();

        return data;
    }
}

