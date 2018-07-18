package net.hockeyapp.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * <h3>Description</h3>
 *
 * This class provides testing for the usage time tracking feature.
 */
@RunWith(AndroidJUnit4.class)
public class TrackingTest {

    @Rule
    public ActivityTestRule<FeedbackActivity> mActivityRule = new ActivityTestRule<>(FeedbackActivity.class);

    @Before
    public void setUp() throws Exception {
        if (Constants.APP_VERSION == null) {
            /**
             * Make sure Constants is loaded before performing the tests, otherwise the tests
             * will write to a different preferences key than the test tracking module
             */
            Constants.loadFromContext(mActivityRule.getActivity());
            Constants.DEVICE_IDENTIFIER.get();
        }
    }

    /**
     * Test to verify basic usage tracking works.
     *
     * @throws InterruptedException
     */
    @Test
    public void basicTrackingTest() throws InterruptedException {
        final Activity activity = mActivityRule.getActivity();

        Tracking.startUsage(activity);

        // Use preferences access to overwrite usage data, we want to fake 10 seconds of tracking data
        Tracking.getPreferences(activity)
                .edit()
                .putLong(Tracking.USAGE_TIME_KEY + Constants.APP_VERSION, 10000)
                .apply();

        Tracking.stopUsage(activity);

        assertEquals(10, Tracking.getUsageTime(activity));
    }

    /**
     * Test to verify that trying
     */
    @Test
    public void negativeTrackingAmountSegmentsIgnoredTest() {
        final Activity activity = mActivityRule.getActivity();

        SharedPreferences preferences = Tracking.getPreferences(activity);

        long reference = Tracking.getUsageTime(activity);

        Tracking.startUsage(activity);

        long now = System.currentTimeMillis();

        preferences.edit()
                .putLong(Tracking.START_TIME_KEY + activity.hashCode(), now + (60 * 60 * 1000))
                .apply();

        Tracking.stopUsage(activity);

        assertEquals(reference, Tracking.getUsageTime(activity));
    }

    /**
     * Test to verify that overflows in the preference store are not causing negative usage data
     */
    @SuppressWarnings("NumericOverflow")
    @Test
    public void trackingOverflowIgnoredTest() {

        // write negative total usage data to preference store
        Tracking.getPreferences(mActivityRule.getActivity())
                .edit()
                .putLong(Tracking.USAGE_TIME_KEY + Constants.APP_VERSION, -1)
                .apply();

        // usage tracking should return 0 instead of negative value
        assertEquals(0, Tracking.getUsageTime(mActivityRule.getActivity()));

        // write data close to overflow to preference store
        Tracking.getPreferences(mActivityRule.getActivity())
                .edit()
                .putLong(Tracking.USAGE_TIME_KEY + Constants.APP_VERSION, Long.MAX_VALUE)
                .apply();

        long reference = Tracking.getUsageTime(mActivityRule.getActivity());
        long now = System.currentTimeMillis();

        // add 10 seconds of usage data to make sure we have an overflow inside of preference store
        Tracking.startUsage(mActivityRule.getActivity());
        Tracking.getPreferences(mActivityRule.getActivity())
                .edit()
                .putLong(Tracking.START_TIME_KEY + mActivityRule.getActivity().hashCode(), now - (60 * 1000))
                .apply();
        Tracking.stopUsage(mActivityRule.getActivity());

        // usage tracking data should not have been changed by adding data which causes internal overflow
        assertEquals(reference, Tracking.getUsageTime(mActivityRule.getActivity()));
    }

}
