package net.hockeyapp.android;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import net.hockeyapp.android.PrivateEventManager;
import net.hockeyapp.android.suite.InstrumentationTestSuite;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(AndroidJUnit4.class)
public class PrivateEventManagerTest extends InstrumentationTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void addEventListenerWorks() {
        EventListener listener = mock(EventListener.class);
        PrivateEventManager.addEventListener(listener);
        verifyZeroInteractions(listener);
    }

    @Test
    public void postEventWorks() {
        EventListener listener = mock(EventListener.class);
        PrivateEventManager.addEventListener(listener);
        verifyZeroInteractions(listener);

        PrivateEventManager.Event postedEvent = new PrivateEventManager.Event(PrivateEventManager.EVENT_TYPE_UNCAUGHT_EXCEPTION);

        PrivateEventManager.postEvent(postedEvent);
        verify(listener).onHockeyEvent(postedEvent);
    }

    public static class EventListener implements PrivateEventManager.HockeyEventListener {
        @Override
        public void onHockeyEvent(PrivateEventManager.Event event) {

        }
    }

}
