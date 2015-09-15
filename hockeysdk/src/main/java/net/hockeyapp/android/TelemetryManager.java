package net.hockeyapp.android;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import net.hockeyapp.android.utils.Util;

import java.lang.ref.WeakReference;
import java.util.Date;
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

public class TelemetryManager implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "TelemetryManager";

    /**
     * Synchronization LOCK for setting static context
     */
    private static final Object LOCK = new Object();

    private static boolean sessionTrackingDisabled;

    /**
     * The activity counter
     */
    protected static final AtomicInteger activityCount = new AtomicInteger(0);

    /**
     * The timestamp of the last activity
     */
    protected static final AtomicLong lastBackground = new AtomicLong(getTime());;

    /**
     * The application needed for auto collecting session data
     */
    private static WeakReference<Application> weakApplication;

    public static void register(Context context, Application application) {
        synchronized (LOCK) {
            TelemetryManager.weakApplication = new WeakReference<>(application);

            if (Util.sessionTrackingSupported()) {
                setSessionTrackingDisabled(true);

            } else {
                sessionTrackingDisabled = false;
            }
        }
    }

    /**
     * Determines if session tracking was enabled.
     *
     * @return YES if session tracking is enabled
     */
    public static boolean sessionTrackingEnabled() {
        return sessionTrackingDisabled;
    }


    /**
     * Enable and disable tracking of sessions
     *
     * @param disabled flag to indicate
     */
    public static void setSessionTrackingDisabled(Boolean disabled) {
        synchronized (LOCK) {
            if (Util.sessionTrackingSupported()) {
                sessionTrackingDisabled = disabled;
                if (!disabled) {
                    //TODO move back to singleton --> static class doesn't work as Callback
                    //or make LifeCyclecallback-Class
                    //getApplication().registerActivityLifecycleCallbacks(TelemetryManager);
                }
            } else {
                sessionTrackingDisabled = false;
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
            boolean shouldRenew = ((now - then) >= (20*1000));
            Log.d(TAG, "Checking if we have to renew a session, time difference is: " + (now - then));

            if (shouldRenew) {
                Log.d(TAG, "Renewing session");
                //TODO: renew ID for session
//                this.telemetryContext.renewSessionId();
            }
        }
    }

    /**
     * Get the current time
     *
     * @return the current time in milliseconds
     */
    private static long getTime() {
        return new Date().getTime();
    }
}

