package net.hockeyapp.android.metrics;

import java.util.Map;

public abstract class ITelemetry extends Domain {
    /**
     * Gets the properties.
     */
    public abstract Map<String, String> getProperties();

    /**
     * Sets the properties.
     */
    public abstract void setProperties(Map<String, String> value);

    /**
     * Sets the version
     */
    public abstract void setVer(int ver);

    /**
     * Gets the envelope name for this telemetry object.
     */
    public abstract String getEnvelopeName();

    /**
     * Gets the base type for this telemetry object.
     */
    public abstract String getBaseType();
}
