/*
 * Generated from AppInsightsTypes.bond (https://github.com/Microsoft/bond)
*/
package net.hockeyapp.android.metrics.model;

import net.hockeyapp.android.metrics.JsonHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data contract class EventData.
 */
public class EventData extends TelemetryData {
    /**
     * Backing field for property Ver.
     */
    private int ver = 2;

    /**
     * Backing field for property Name.
     */
    private String name;

    /**
     * Backing field for property Properties.
     */
    private Map<String, String> properties;

    /**
     * Backing field for property Measurements.
     */
    private Map<String, Double> measurements;

    /**
     * Initializes a new instance of the EventData class.
     */
    public EventData() {
        this.InitializeFields();
        this.SetupAttributes();
    }

    /**
     * Envelope Name for this telemetry.
     */
    public String getEnvelopeName() {
        return "Microsoft.ApplicationInsights.Event";
    }

    /**
     * Base Type for this telemetry.
     */
    public String getBaseType() {
        return "EventData";
    }

    /**
     * Gets the Ver property.
     */
    public int getVer() {
        return this.ver;
    }

    /**
     * Sets the Ver property.
     */
    public void setVer(int value) {
        this.ver = value;
    }

    /**
     * Gets the Name property.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the Name property.
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the Properties property.
     */
    public Map<String, String> getProperties() {
        if (this.properties == null) {
            this.properties = new LinkedHashMap<String, String>();
        }
        return this.properties;
    }

    /**
     * Sets the Properties property.
     */
    public void setProperties(Map<String, String> value) {
        this.properties = value != null ? new LinkedHashMap<String, String>(value) : null;
    }

    /**
     * Gets the Measurements property.
     */
    public Map<String, Double> getMeasurements() {
        if (this.measurements == null) {
            this.measurements = new LinkedHashMap<String, Double>();
        }
        return this.measurements;
    }

    /**
     * Sets the Measurements property.
     */
    public void setMeasurements(Map<String, Double> value) {
        this.measurements = value != null ? new LinkedHashMap<String, Double>(value) : null;
    }

    /**
     * Serializes the beginning of this object to the passed in writer.
     *
     * @param writer The writer to serialize this object to.
     */
    protected String serializeContent(Writer writer) throws IOException {
        String prefix = super.serializeContent(writer);
        writer.write(prefix + "\"ver\":");
        writer.write(JsonHelper.convert(this.ver));
        prefix = ",";

        writer.write(prefix + "\"name\":");
        writer.write(JsonHelper.convert(this.name));
        prefix = ",";

        if (!(this.properties == null)) {
            writer.write(prefix + "\"properties\":");
            JsonHelper.writeDictionary(writer, this.properties);
            prefix = ",";
        }

        if (!(this.measurements == null)) {
            writer.write(prefix + "\"measurements\":");
            JsonHelper.writeDictionary(writer, this.measurements);
            prefix = ",";
        }

        return prefix;
    }

    /**
     * Sets up the events attributes
     */
    public void SetupAttributes() {
    }

    /**
     * Optionally initializes fields for the current context.
     */
    protected void InitializeFields() {
        QualifiedName = "com.microsoft.applicationinsights.contracts.EventData";
    }
}
