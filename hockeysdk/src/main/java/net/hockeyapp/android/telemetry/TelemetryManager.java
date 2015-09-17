package net.hockeyapp.android.telemetry;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import net.hockeyapp.android.utils.Util;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <h3>Description</h3>
 * <p/>
 * The TelemetryManager provides functionality to gather telemetry information about your users,
 * sessions, and – eventually – events and pageviews.
 * <p/>
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
public class TelemetryManager implements Application.ActivityLifecycleCallbacks {
    /**
     * The activity counter
     */
    protected static final AtomicInteger activityCount = new AtomicInteger(0);
    /**
     * The timestamp of the last activity
     */
    protected static final AtomicLong lastBackground = new AtomicLong(getTime());
    private static final String TAG = "TelemetryManager";
    /**
     * Synchronization LOCK for setting static context
     */
    private static final Object LOCK = new Object();

    private static volatile TelemetryManager instance;

    /**
     * The application needed for auto collecting session data
     */
    private static WeakReference<Application> weakApplication;
    /**
     * Flag that indicates disabled session tracking.
     * Default is false.
     */
    private volatile boolean sessionTrackingDisabled;
    /**
     * A channel for collecting new events before storing and sending them.
     */
    private Channel channel;

    /**
     * A telemetry context which is used to add meta info to events, before they're sent out.
     */
    private TelemetryContext telemetryContext;



    /**
     * Restrict access to the default constructor
     * Create a new INSTANCE of the TelemetryManager class
     */
    protected TelemetryManager() {
    }

    protected static void register(Application application, TelemetryContext context) {

    }

    public static void register(Context context, Application application, String appIdentifier) {
        TelemetryManager result = instance;
        if (result == null) {
            synchronized (LOCK) {
                result = instance;        // thread may have instantiated the object.
                if (result == null) {
                    result = new TelemetryManager();
                    result.telemetryContext = new TelemetryContext(context,
                          appIdentifier);
                    result.weakApplication = new WeakReference<>(application);
                }
                if (Util.sessionTrackingSupported()) {
                    result.sessionTrackingDisabled = false;

                } else {
                    result.sessionTrackingDisabled = true;
                }
                instance = result;
            }
        }
    }

    /**
     * Determines if session tracking was enabled.
     *
     * @return YES if session tracking is enabled
     */
    public static boolean sessionTrackingEnabled() {
        return !instance.sessionTrackingDisabled;
    }

    /**
     * Enable and disable tracking of sessions
     *
     * @param disabled flag to indicate
     */
    public static void setSessionTrackingDisabled(Boolean disabled) {
        if (instance == null) {
            Log.d(TAG, "TelemetryManager hasn't been registered");
        } else {
            synchronized (LOCK) {
                if (Util.sessionTrackingSupported()) {
                    instance.sessionTrackingDisabled = disabled;
                    //TODO persist this setting so the dev doesn't have to take care of this
                    //between launches
                    if (!disabled) {
                        getApplication().registerActivityLifecycleCallbacks(instance);
                    }
                } else {
                    instance.sessionTrackingDisabled = true;
                    getApplication().unregisterActivityLifecycleCallbacks(instance);
                }
            }
        }
    }

    /**
     * Get the reference to the Application (used for life-cycle tracking)
     *
     * @return the reference to the application that was used during initialization of the SDK
     */
    private static Application getApplication() {
        Application application = null;
        if (weakApplication != null) {
            application = weakApplication.get();
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
        sessionManagement();
    }

    @Override
    public void onActivityPaused(Activity activity) {// unused but required to implement ActivityLifecycleCallbacks
        // unused but required to implement ActivityLifecycleCallbacks
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

    // TODO: Change method name to something like updateSession
    private void sessionManagement() {
        int count = this.activityCount.getAndIncrement();
        if (count == 0) {
            if (sessionTrackingEnabled()) {
                Log.d(TAG, "Starting & tracking session");
            } else {
                Log.d(TAG, "Session management disabled by the developer");
            }
        } else {
            //we should already have a session now
            //check if the session should be renewed
            long now = this.getTime();
            long then = this.lastBackground.getAndSet(getTime());
            //TODO save session intervall in configuration
            boolean shouldRenew = ((now - then) >= (20 * 1000));
            Log.d(TAG, "Checking if we have to renew a session, time difference is: " + (now - then));

            if (shouldRenew) {
                Log.d(TAG, "Renewing session");
                //TODO: renew ID for session
                String sessionId = UUID.randomUUID().toString();
                telemetryContext.updateSessionContext(sessionId);
                trackSessionState(SessionState.START);
            }
        }
    }

    /**
     * Creates and enqueues a session event for the given state.
     *
     * @param sessionState value that determines whether the session started or ended
     */
    private void trackSessionState(SessionState sessionState) {
        // TODO: Do not create & log events on main thread
        SessionStateData sessionItem = new SessionStateData();
        sessionItem.setState(sessionState);
        Data<Domain> data = createData(sessionItem);
        channel.log(data);
    }

    /**
     * Pack and forward the telemetry item to the queue.
     *
     * @param telemetryData The telemetry event to be persisted and sent
     * @return a base data object containing the telemetry data
     */
    protected Data<Domain> createData(TelemetryData telemetryData) {

        Data<Domain> data = new Data<Domain>();
        data.setBaseData(telemetryData);
        data.setBaseType(telemetryData.getBaseType());
        data.QualifiedName = telemetryData.getEnvelopeName();

        return data;
    }
}

