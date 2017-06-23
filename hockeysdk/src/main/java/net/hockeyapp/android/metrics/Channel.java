package net.hockeyapp.android.metrics;

import net.hockeyapp.android.metrics.model.Base;
import net.hockeyapp.android.metrics.model.Data;
import net.hockeyapp.android.metrics.model.Domain;
import net.hockeyapp.android.metrics.model.Envelope;
import net.hockeyapp.android.metrics.model.TelemetryData;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.Util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <h3>Description</h3>
 *
 * Items get queued before they are persisted and sent out as a batch to save battery. This class
 * manages the queue, and forwards the batch to the persistence layer once the max batch count or
 * batch interval time limit has been reached.
 **/
class Channel {

    private static final String TAG = "HockeyApp-Metrics";

    /**
     * Synchronization lock.
     */
    private static final Object LOCK = new Object();
    /**
     * Number of queue items which will trigger synchronization with the persistence layer.
     */
    protected static final int MAX_BATCH_COUNT = 50;
    /**
     * Maximum time interval in milliseconds after which a synchronize will be triggered, regardless of queue size.
     */
    protected static final int MAX_BATCH_INTERVAL = 15 * 1000;
    /**
     * Number of queue items which will trigger synchronization in debug mode.
     */
    protected static final int MAX_BATCH_COUNT_DEBUG = 5;
    /**
     * Maximum time interval in milliseconds after which a synchronize will be triggered in debug mode.
     */
    protected static final int MAX_BATCH_INTERVAL_DEBUG = 3 * 1000;
    /**
     * The backing store queue for the channel.
     */
    protected final List<String> mQueue;
    /**
     * Telemetry context used by the channel to create the payload.
     */
    protected final TelemetryContext mTelemetryContext;
    /**
     * Persistence used for storing telemetry items before they get sent.
     */
    private final Persistence mPersistence;
    /**
     * Timer to run scheduled tasks on.
     */
    private final Timer mTimer;
    /**
     * Task to be scheduled for synchronizing at a certain max interval.
     */
    private SynchronizeChannelTask mSynchronizeTask;

    protected static int getMaxBatchCount() {
        return Util.isDebuggerConnected() ? MAX_BATCH_COUNT_DEBUG : MAX_BATCH_COUNT;
    }

    protected static int getMaxBatchInterval() {
        return Util.isDebuggerConnected() ? MAX_BATCH_INTERVAL_DEBUG : MAX_BATCH_INTERVAL;
    }

    /**
     * Creates and initializes a new instance.
     */
    public Channel(TelemetryContext telemetryContext, Persistence persistence) {
        mTelemetryContext = telemetryContext;
        mQueue = new LinkedList<>();
        mPersistence = persistence;
        mTimer = new Timer("HockeyApp User Metrics Sender Queue", true);
    }

    /**
     * Adds an item to the channel queue.
     *
     * @param serializedItem A serialized telemetry item to enqueue.
     */
    protected void enqueue(String serializedItem) {

        if (serializedItem == null) {
            return;
        }
        synchronized (LOCK) {
            if (mQueue.add(serializedItem)) {
                if ((mQueue.size() >= getMaxBatchCount())) {
                    synchronize();
                } else if (mQueue.size() == 1) {
                    scheduleSynchronizeTask();
                }
            } else {
                HockeyLog.verbose(TAG, "Unable to add item to queue");
            }
        }
    }

    /**
     * Synchronize all pending telemetry items with persistence.
     */
    protected void synchronize() {
        if (mSynchronizeTask != null) {
            mSynchronizeTask.cancel();
        }

        String[] data = null;
        synchronized (LOCK) {
            if (!mQueue.isEmpty()) {
                data = new String[mQueue.size()];
                mQueue.toArray(data);
                mQueue.clear();
            }
        }
        if (mPersistence != null && data != null) {
            mPersistence.persist(data);
        }
    }

    /**
     * Create a telemetry envelope with the given object as its base data.
     *
     * @param data The telemetry we want to wrap inside an Envelope and send to the server.
     * @return The envelope that includes the telemetry data.
     */
    protected Envelope createEnvelope(Data<Domain> data) {
        Envelope envelope = new Envelope();
        envelope.setData(data);
        Domain baseData = data.getBaseData();
        if (baseData instanceof TelemetryData) {
            String envelopeName = ((TelemetryData) baseData).getEnvelopeName();
            envelope.setName(envelopeName);
        }

        mTelemetryContext.updateScreenResolution();

        envelope.setTime(Util.dateToISO8601(new Date()));
        envelope.setIKey(mTelemetryContext.getInstrumentationKey());

        Map<String, String> tags = mTelemetryContext.getContextTags();
        if (tags != null) {
            envelope.setTags(tags);
        }
        return envelope;
    }

    protected void scheduleSynchronizeTask() {
        mSynchronizeTask = new SynchronizeChannelTask();
        mTimer.schedule(mSynchronizeTask, getMaxBatchInterval());
    }

    /**
     * Enqueue data in the channel queue.
     *
     * @param data The base data object to enqueue.
     */
    @SuppressWarnings("unchecked")
    public void enqueueData(Base data) {
        if (data instanceof Data) {
            Envelope envelope = null;
            try {
                envelope = createEnvelope((Data<Domain>) data);
            } catch (ClassCastException e) {
                HockeyLog.debug(TAG, "Telemetry not enqueued, could not create envelope, must be of type ITelemetry");
            }

            if (envelope != null) {
                // enqueueData to queue
                String serializedEnvelope = serializeEnvelope(envelope);
                enqueue(serializedEnvelope);
                HockeyLog.debug(TAG, "enqueued telemetry: " + envelope.getName());
            }
        } else {
            HockeyLog.debug(TAG, "Telemetry not enqueued, must be of type ITelemetry");
        }
    }

    /**
     * Serializes an envelope to a JSON string according to Common Schema.
     *
     * @param envelope The envelope object to serialize.
     */
    protected String serializeEnvelope(Envelope envelope) {
        try {
            if (envelope != null) {
                StringWriter stringWriter = new StringWriter();
                envelope.serialize(stringWriter);
                return stringWriter.toString();
            }
            HockeyLog.debug(TAG, "Envelope wasn't empty but failed to serialize anything, returning null");
            return null;
        } catch (IOException e) {
            HockeyLog.debug(TAG, "Failed to save data with exception: " + e.toString());
            return null;
        }
    }

    /**
     * Task to fire off after batch time interval has passed.
     */
    private class SynchronizeChannelTask extends TimerTask {

        public SynchronizeChannelTask() {
        }

        @Override
        public void run() {
            synchronize();
        }
    }
}
