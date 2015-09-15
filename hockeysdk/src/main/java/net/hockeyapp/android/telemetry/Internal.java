/*
 * Generated from ContextTagKeys.bond (https://github.com/Microsoft/bond)
*/
package net.hockeyapp.android.telemetry;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;

/**
 * Data contract class Internal.
 */
public class Internal implements IJsonSerializable, Serializable {
    /**
     * Backing field for property SdkVersion.
     */
    private String sdkVersion;

    /**
     * Backing field for property AgentVersion.
     */
    private String agentVersion;

    /**
     * Backing field for property DataCollectorReceivedTime.
     */
    private String dataCollectorReceivedTime;

    /**
     * Backing field for property ProfileId.
     */
    private String profileId;

    /**
     * Backing field for property ProfileClassId.
     */
    private String profileClassId;

    /**
     * Backing field for property AccountId.
     */
    private String accountId;

    /**
     * Backing field for property ApplicationName.
     */
    private String applicationName;

    /**
     * Backing field for property InstrumentationKey.
     */
    private String instrumentationKey;

    /**
     * Backing field for property TelemetryItemId.
     */
    private String telemetryItemId;

    /**
     * Backing field for property ApplicationType.
     */
    private String applicationType;

    /**
     * Backing field for property RequestSource.
     */
    private String requestSource;

    /**
     * Backing field for property FlowType.
     */
    private String flowType;

    /**
     * Backing field for property IsAudit.
     */
    private String isAudit;

    /**
     * Backing field for property TrackingSourceId.
     */
    private String trackingSourceId;

    /**
     * Backing field for property TrackingType.
     */
    private String trackingType;

    /**
     * Initializes a new instance of the Internal class.
     */
    public Internal() {
        this.InitializeFields();
    }

    /**
     * Gets the SdkVersion property.
     */
    public String getSdkVersion() {
        return this.sdkVersion;
    }

    /**
     * Sets the SdkVersion property.
     */
    public void setSdkVersion(String value) {
        this.sdkVersion = value;
    }

    /**
     * Gets the AgentVersion property.
     */
    public String getAgentVersion() {
        return this.agentVersion;
    }

    /**
     * Sets the AgentVersion property.
     */
    public void setAgentVersion(String value) {
        this.agentVersion = value;
    }

    /**
     * Gets the DataCollectorReceivedTime property.
     */
    public String getDataCollectorReceivedTime() {
        return this.dataCollectorReceivedTime;
    }

    /**
     * Sets the DataCollectorReceivedTime property.
     */
    public void setDataCollectorReceivedTime(String value) {
        this.dataCollectorReceivedTime = value;
    }

    /**
     * Gets the ProfileId property.
     */
    public String getProfileId() {
        return this.profileId;
    }

    /**
     * Sets the ProfileId property.
     */
    public void setProfileId(String value) {
        this.profileId = value;
    }

    /**
     * Gets the ProfileClassId property.
     */
    public String getProfileClassId() {
        return this.profileClassId;
    }

    /**
     * Sets the ProfileClassId property.
     */
    public void setProfileClassId(String value) {
        this.profileClassId = value;
    }

    /**
     * Gets the AccountId property.
     */
    public String getAccountId() {
        return this.accountId;
    }

    /**
     * Sets the AccountId property.
     */
    public void setAccountId(String value) {
        this.accountId = value;
    }

    /**
     * Gets the ApplicationName property.
     */
    public String getApplicationName() {
        return this.applicationName;
    }

    /**
     * Sets the ApplicationName property.
     */
    public void setApplicationName(String value) {
        this.applicationName = value;
    }

    /**
     * Gets the InstrumentationKey property.
     */
    public String getInstrumentationKey() {
        return this.instrumentationKey;
    }

    /**
     * Sets the InstrumentationKey property.
     */
    public void setInstrumentationKey(String value) {
        this.instrumentationKey = value;
    }

    /**
     * Gets the TelemetryItemId property.
     */
    public String getTelemetryItemId() {
        return this.telemetryItemId;
    }

    /**
     * Sets the TelemetryItemId property.
     */
    public void setTelemetryItemId(String value) {
        this.telemetryItemId = value;
    }

    /**
     * Gets the ApplicationType property.
     */
    public String getApplicationType() {
        return this.applicationType;
    }

    /**
     * Sets the ApplicationType property.
     */
    public void setApplicationType(String value) {
        this.applicationType = value;
    }

    /**
     * Gets the RequestSource property.
     */
    public String getRequestSource() {
        return this.requestSource;
    }

    /**
     * Sets the RequestSource property.
     */
    public void setRequestSource(String value) {
        this.requestSource = value;
    }

    /**
     * Gets the FlowType property.
     */
    public String getFlowType() {
        return this.flowType;
    }

    /**
     * Sets the FlowType property.
     */
    public void setFlowType(String value) {
        this.flowType = value;
    }

    /**
     * Gets the IsAudit property.
     */
    public String getIsAudit() {
        return this.isAudit;
    }

    /**
     * Sets the IsAudit property.
     */
    public void setIsAudit(String value) {
        this.isAudit = value;
    }

    /**
     * Gets the TrackingSourceId property.
     */
    public String getTrackingSourceId() {
        return this.trackingSourceId;
    }

    /**
     * Sets the TrackingSourceId property.
     */
    public void setTrackingSourceId(String value) {
        this.trackingSourceId = value;
    }

    /**
     * Gets the TrackingType property.
     */
    public String getTrackingType() {
        return this.trackingType;
    }

    /**
     * Sets the TrackingType property.
     */
    public void setTrackingType(String value) {
        this.trackingType = value;
    }


    /**
     * Adds all members of this class to a hashmap
     *
     * @param map to which the members of this class will be added.
     */
    public void addToHashMap(Map<String, String> map) {
        if (!(this.sdkVersion == null)) {
            map.put("ai.internal.sdkVersion", this.sdkVersion);
        }
        if (!(this.agentVersion == null)) {
            map.put("ai.internal.agentVersion", this.agentVersion);
        }
        if (!(this.dataCollectorReceivedTime == null)) {
            map.put("ai.internal.dataCollectorReceivedTime", this.dataCollectorReceivedTime);
        }
        if (!(this.profileId == null)) {
            map.put("ai.internal.profileId", this.profileId);
        }
        if (!(this.profileClassId == null)) {
            map.put("ai.internal.profileClassId", this.profileClassId);
        }
        if (!(this.accountId == null)) {
            map.put("ai.internal.accountId", this.accountId);
        }
        if (!(this.applicationName == null)) {
            map.put("ai.internal.applicationName", this.applicationName);
        }
        if (!(this.instrumentationKey == null)) {
            map.put("ai.internal.instrumentationKey", this.instrumentationKey);
        }
        if (!(this.telemetryItemId == null)) {
            map.put("ai.internal.telemetryItemId", this.telemetryItemId);
        }
        if (!(this.applicationType == null)) {
            map.put("ai.internal.applicationType", this.applicationType);
        }
        if (!(this.requestSource == null)) {
            map.put("ai.internal.requestSource", this.requestSource);
        }
        if (!(this.flowType == null)) {
            map.put("ai.internal.flowType", this.flowType);
        }
        if (!(this.isAudit == null)) {
            map.put("ai.internal.isAudit", this.isAudit);
        }
        if (!(this.trackingSourceId == null)) {
            map.put("ai.internal.trackingSourceId", this.trackingSourceId);
        }
        if (!(this.trackingType == null)) {
            map.put("ai.internal.trackingType", this.trackingType);
        }
    }


    /**
     * Serializes the beginning of this object to the passed in writer.
     *
     * @param writer The writer to serialize this object to.
     */
    @Override
    public void serialize(Writer writer) throws IOException {
        if (writer == null) {
            throw new IllegalArgumentException("writer");
        }

        writer.write('{');
        this.serializeContent(writer);
        writer.write('}');
    }

    /**
     * Serializes the beginning of this object to the passed in writer.
     *
     * @param writer The writer to serialize this object to.
     */
    protected String serializeContent(Writer writer) throws IOException {
        String prefix = "";
        if (!(this.sdkVersion == null)) {
            writer.write(prefix + "\"ai.internal.sdkVersion\":");
            writer.write(JsonHelper.convert(this.sdkVersion));
            prefix = ",";
        }

        if (!(this.agentVersion == null)) {
            writer.write(prefix + "\"ai.internal.agentVersion\":");
            writer.write(JsonHelper.convert(this.agentVersion));
            prefix = ",";
        }

        if (!(this.dataCollectorReceivedTime == null)) {
            writer.write(prefix + "\"ai.internal.dataCollectorReceivedTime\":");
            writer.write(JsonHelper.convert(this.dataCollectorReceivedTime));
            prefix = ",";
        }

        if (!(this.profileId == null)) {
            writer.write(prefix + "\"ai.internal.profileId\":");
            writer.write(JsonHelper.convert(this.profileId));
            prefix = ",";
        }

        if (!(this.profileClassId == null)) {
            writer.write(prefix + "\"ai.internal.profileClassId\":");
            writer.write(JsonHelper.convert(this.profileClassId));
            prefix = ",";
        }

        if (!(this.accountId == null)) {
            writer.write(prefix + "\"ai.internal.accountId\":");
            writer.write(JsonHelper.convert(this.accountId));
            prefix = ",";
        }

        if (!(this.applicationName == null)) {
            writer.write(prefix + "\"ai.internal.applicationName\":");
            writer.write(JsonHelper.convert(this.applicationName));
            prefix = ",";
        }

        if (!(this.instrumentationKey == null)) {
            writer.write(prefix + "\"ai.internal.instrumentationKey\":");
            writer.write(JsonHelper.convert(this.instrumentationKey));
            prefix = ",";
        }

        if (!(this.telemetryItemId == null)) {
            writer.write(prefix + "\"ai.internal.telemetryItemId\":");
            writer.write(JsonHelper.convert(this.telemetryItemId));
            prefix = ",";
        }

        if (!(this.applicationType == null)) {
            writer.write(prefix + "\"ai.internal.applicationType\":");
            writer.write(JsonHelper.convert(this.applicationType));
            prefix = ",";
        }

        if (!(this.requestSource == null)) {
            writer.write(prefix + "\"ai.internal.requestSource\":");
            writer.write(JsonHelper.convert(this.requestSource));
            prefix = ",";
        }

        if (!(this.flowType == null)) {
            writer.write(prefix + "\"ai.internal.flowType\":");
            writer.write(JsonHelper.convert(this.flowType));
            prefix = ",";
        }

        if (!(this.isAudit == null)) {
            writer.write(prefix + "\"ai.internal.isAudit\":");
            writer.write(JsonHelper.convert(this.isAudit));
            prefix = ",";
        }

        if (!(this.trackingSourceId == null)) {
            writer.write(prefix + "\"ai.internal.trackingSourceId\":");
            writer.write(JsonHelper.convert(this.trackingSourceId));
            prefix = ",";
        }

        if (!(this.trackingType == null)) {
            writer.write(prefix + "\"ai.internal.trackingType\":");
            writer.write(JsonHelper.convert(this.trackingType));
            prefix = ",";
        }

        return prefix;
    }

    /**
     * Optionally initializes fields for the current context.
     */
    protected void InitializeFields() {

    }
}
