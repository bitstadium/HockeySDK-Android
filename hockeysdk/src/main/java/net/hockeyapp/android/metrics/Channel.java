package net.hockeyapp.android.metrics;

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
 * <p/>
 * Items get queued before they are persisted and sent out as a batch to save battery. This class
 * managed the queue, and forwards the batch to the persistence layer once the max batch count has
 * been reached.
 **/
class Channel {

    private static final String TAG = "HockeyApp Metrics Channel";

    /**
     *  Number of queue items which will trigger a flush (testing).
     */
    protected static int MAX_BATCH_COUNT = 1;

    /**
     *  Telemetry context used by the channel to create the payload.
     */
    protected TelemetryContext telemetryContext;

    /**
     * Synchronization LOCK
     */
    private static final Object LOCK = new Object();

    /**
     * Persistence used for storing telemetry items before they get sent out.
     */
    private Persistence persistence;

    /**
     * The linked queue for this queue.
     */
    protected final List<String> queue;

    /**
     * Instantiates a new INSTANCE of Channel
     */
    public Channel(TelemetryContext telemetryContext, Persistence persistence) {
        this.telemetryContext = telemetryContext;
        this.queue = new LinkedList<String>();
        this.persistence = persistence;
    }

    /**
     * Adds an item to the sender queue
     *
     * @param serializedItem a serialized telemetry item to enqueue
     * @return true if the item was successfully added to the queue
     */
    protected void enqueue(String serializedItem) {

        if (serializedItem == null) {
            return;
        }
        synchronized (this.LOCK) {
            if (this.queue.add(serializedItem)) {
                if ((this.queue.size() >= MAX_BATCH_COUNT)) {
                    synchronize();
                }
            } else {
                HockeyLog.log(TAG, "Unable to add item to queue");
            }
        }
    }

    /**
     * Persist all pending items.
     */
    protected void synchronize() {
        String[] data;
        if (!queue.isEmpty()) {
            data = new String[queue.size()];
            queue.toArray(data);
            queue.clear();

            if (data != null) {
                if (this.persistence != null) {
                    this.persistence.persist(data);
                }
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

        this.telemetryContext.updateScreenResolution();
        envelope.setAppId(this.telemetryContext.getPackageName());
        envelope.setAppVer(this.telemetryContext.getAppVersion());
        envelope.setTime(Util.dateToISO8601(new Date()));
        envelope.setIKey(this.telemetryContext.getInstrumentationKey());
        envelope.setOsVer(this.telemetryContext.getOsVersion());
        envelope.setOs(this.telemetryContext.getOsName());

        Map<String, String> tags = this.telemetryContext.getContextTags();
        if (tags != null) {
            envelope.setTags(tags);
        }
        return envelope;
    }

    /**
     * Records the passed in data.
     *
     * @param data the base object to record
     */
    public void log(Base data) {
        if(data instanceof Data) {
            Envelope envelope = createEnvelope((Data<Domain>) data);

            // log to queue
            String serializedEnvelope = serializeEnvelope(envelope);
            enqueue(serializedEnvelope);
            HockeyLog.log(TAG, "enqueued telemetry: " + envelope.getName());
        } else {
            HockeyLog.log(TAG, "telemetry not enqueued, must be of type ITelemetry");
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
            HockeyLog.log(TAG, "Envelope wasn't empty but failed to serialize anything, returning null");
            return null;
        } catch (IOException e) {
            HockeyLog.log(TAG, "Failed to save data with exception: " + e.toString());
            return null;
        }
    }
}
