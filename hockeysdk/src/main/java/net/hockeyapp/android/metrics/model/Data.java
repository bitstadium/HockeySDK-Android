package net.hockeyapp.android.metrics.model;

import net.hockeyapp.android.metrics.JsonHelper;

import java.io.IOException;
import java.io.Writer;

/**
 * Data contract class Data.
 */
public class Data<TDomain extends Domain> extends Base implements ITelemetryData {
    /**
     * Backing field for property BaseData.
     */
    private TDomain baseData;

    /**
     * Initializes a new instance of the Data{TDomain} class.
     */
    public Data() {
        this.InitializeFields();
        this.SetupAttributes();
    }

    /**
     * Gets the BaseData property.
     */
    public TDomain getBaseData() {
        return this.baseData;
    }

    /**
     * Sets the BaseData property.
     */
    public void setBaseData(TDomain value) {
        this.baseData = value;
    }


    /**
     * Serializes the beginning of this object to the passed in writer.
     *
     * @param writer The writer to serialize this object to.
     */
    protected String serializeContent(Writer writer) throws IOException {
        String prefix = super.serializeContent(writer);
        writer.write(prefix + "\"baseData\":");
        JsonHelper.writeJsonSerializable(writer, this.baseData);
        prefix = ",";

        return prefix;
    }

    /**
     * Sets up the events attributes
     */
    public void SetupAttributes() {
        this.Attributes.put("Description", "Data struct to contain both B and C sections.");
    }

    /**
     * Optionally initializes fields for the current context.
     */
    protected void InitializeFields() {
        QualifiedName = "com.microsoft.telemetry.Data";
    }
}
