package net.hockeyapp.android;

import java.util.LinkedList;
import java.util.List;

public final class PrivateEventManager {

    public static final int EVENT_TYPE_UNCAUGHT_EXCEPTION = 1;

    private static List<HockeyEventListener> sEventListeners = new LinkedList<>();

    public static void addEventListener(HockeyEventListener listener) {
        sEventListeners.add(listener);
    }

    static void postEvent(Event event) {
        for (HockeyEventListener listener : sEventListeners) {
            listener.onHockeyEvent(event);
        }
    }

    public interface HockeyEventListener {

        void onHockeyEvent(Event event);

    }

    public static final class Event {

        private final int mType;

        protected Event(int type) {
            mType = type;
        }

        public int getType() {
            return mType;
        }

    }

}
