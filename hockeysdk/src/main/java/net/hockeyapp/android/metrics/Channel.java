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

/**
 * <h3>Description</h3>
 *
 * Items get queued before they are persisted and sent out as a batch to save battery. This class
 * managed the queue, and forwards the batch to the persistence layer once the max batch count has
 * been reached.
 **/
class Channel {

    private static final String TAG = "HockeyApp-Metrics";

    /**
     * Synchronization LOCK
     */
    private static final Object LOCK = new Object();
    /**
     * Number of queue items which will trigger a flush (testing).
     */
    protected static int mMaxBatchCount = 1;
    /**
     * The linked queue for this queue.
     */
    protected final List<String> mQueue;
    /**
     * Telemetry context used by the channel to create the payload.
     */
    protected final TelemetryContext mTelemetryContext;
    /**
     * Persistence used for storing telemetry items before they get sent out.
     */
    private final Persistence mPersistence;

    /**
     * Instantiates a new INSTANCE of Channel
     */
    public Channel(TelemetryContext telemetryContext, Persistence persistence) {
        mTelemetryContext = telemetryContext;
        mQueue = new LinkedList<>();
        mPersistence = persistence;
    }

    /**
     * Adds an item to the sender queue
     *
     * @param serializedItem a serialized telemetry item to enqueue
     */
    protected void enqueue(String serializedItem) {

        if (serializedItem == null) {
            return;
        }
        synchronized (LOCK) {
            if (mQueue.add(serializedItem)) {
                if ((mQueue.size() >= mMaxBatchCount)) {
                    synchronize();
                }
            } else {
                HockeyLog.verbose(TAG, "Unable to add item to queue");
            }
        }
    }

    /**
     * Persist all pending items.
     */
    protected void synchronize() {
        String[] data;
        if (!mQueue.isEmpty()) {
            data = new String[mQueue.size()];
            mQueue.toArray(data);
            mQueue.clear();

            if (mPersistence != null) {
                mPersistence.persist(data);
            }
        }
    }

    /**
     * Create an envelope with the given object as its base data
     *
     * @param data The telemetry we want to wrap inside an Envelope and send to the server
     * @return the envelope that includes the telemetry data
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

    /**
     * Records the passed in data.
     *
     * @param data the base object to enqueue
     */
    @SuppressWarnings("unchecked")
    public void enqueueData(Base data) {
        if (data instanceof Data) {
            Envelope envelope = null;
            try {
                envelope = createEnvelope((Data<Domain>) data);
            } catch (ClassCastException e) {
                HockeyLog.debug(TAG, "Telemetry not enqueued, could not create Envelope, must be of type ITelemetry");
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
     * Converts an envelope to a JSON string.
     *
     * @param envelope the envelope object to record
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
}
