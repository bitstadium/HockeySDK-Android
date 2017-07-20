package net.hockeyapp.android;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;

@RunWith(AndroidJUnit4.class)
public class PrivateEventManagerTest {

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
