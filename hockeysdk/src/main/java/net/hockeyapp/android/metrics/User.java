package net.hockeyapp.android.metrics;


import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;

/**
 * Data contract class User.
 */
public class User
      implements IJsonSerializable, Serializable {
    /**
     * Backing field for property AccountAcquisitionDate.
     */
    private String accountAcquisitionDate;

    /**
     * Backing field for property AccountId.
     */
    private String accountId;

    /**
     * Backing field for property UserAgent.
     */
    private String userAgent;

    /**
     * Backing field for property Id.
     */
    private String id;

    /**
     * Backing field for property StoreRegion.
     */
    private String storeRegion;

    /**
     * Backing field for property AuthUserId.
     */
    private String authUserId;

    /**
     * Backing field for property AnonUserAcquisitionDate.
     */
    private String anonUserAcquisitionDate;

    /**
     * Backing field for property AuthUserAcquisitionDate.
     */
    private String authUserAcquisitionDate;

    /**
     * Initializes a new instance of the User class.
     */
    public User() {
        this.InitializeFields();
    }

    /**
     * Gets the AccountAcquisitionDate property.
     */
    public String getAccountAcquisitionDate() {
        return this.accountAcquisitionDate;
    }

    /**
     * Sets the AccountAcquisitionDate property.
     */
    public void setAccountAcquisitionDate(String value) {
        this.accountAcquisitionDate = value;
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
     * Gets the UserAgent property.
     */
    public String getUserAgent() {
        return this.userAgent;
    }

    /**
     * Sets the UserAgent property.
     */
    public void setUserAgent(String value) {
        this.userAgent = value;
    }

    /**
     * Gets the Id property.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the Id property.
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the StoreRegion property.
     */
    public String getStoreRegion() {
        return this.storeRegion;
    }

    /**
     * Sets the StoreRegion property.
     */
    public void setStoreRegion(String value) {
        this.storeRegion = value;
    }

    /**
     * Gets the AuthUserId property.
     */
    public String getAuthUserId() {
        return this.authUserId;
    }

    /**
     * Sets the AuthUserId property.
     */
    public void setAuthUserId(String value) {
        this.authUserId = value;
    }

    /**
     * Gets the AnonUserAcquisitionDate property.
     */
    public String getAnonUserAcquisitionDate() {
        return this.anonUserAcquisitionDate;
    }

    /**
     * Sets the AnonUserAcquisitionDate property.
     */
    public void setAnonUserAcquisitionDate(String value) {
        this.anonUserAcquisitionDate = value;
    }

    /**
     * Gets the AuthUserAcquisitionDate property.
     */
    public String getAuthUserAcquisitionDate() {
        return this.authUserAcquisitionDate;
    }

    /**
     * Sets the AuthUserAcquisitionDate property.
     */
    public void setAuthUserAcquisitionDate(String value) {
        this.authUserAcquisitionDate = value;
    }


    /**
     * Adds all members of this class to a hashmap
     *
     * @param map to which the members of this class will be added.
     */
    public void addToHashMap(Map<String, String> map) {
        if (!(this.accountAcquisitionDate == null)) {
            map.put("ai.user.accountAcquisitionDate", this.accountAcquisitionDate);
        }
        if (!(this.accountId == null)) {
            map.put("ai.user.accountId", this.accountId);
        }
        if (!(this.userAgent == null)) {
            map.put("ai.user.userAgent", this.userAgent);
        }
        if (!(this.id == null)) {
            map.put("ai.user.id", this.id);
        }
        if (!(this.storeRegion == null)) {
            map.put("ai.user.storeRegion", this.storeRegion);
        }
        if (!(this.authUserId == null)) {
            map.put("ai.user.authUserId", this.authUserId);
        }
        if (!(this.anonUserAcquisitionDate == null)) {
            map.put("ai.user.anonUserAcquisitionDate", this.anonUserAcquisitionDate);
        }
        if (!(this.authUserAcquisitionDate == null)) {
            map.put("ai.user.authUserAcquisitionDate", this.authUserAcquisitionDate);
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
        if (!(this.accountAcquisitionDate == null)) {
            writer.write(prefix + "\"ai.user.accountAcquisitionDate\":");
            writer.write(JsonHelper.convert(this.accountAcquisitionDate));
            prefix = ",";
        }

        if (!(this.accountId == null)) {
            writer.write(prefix + "\"ai.user.accountId\":");
            writer.write(JsonHelper.convert(this.accountId));
            prefix = ",";
        }

        if (!(this.userAgent == null)) {
            writer.write(prefix + "\"ai.user.userAgent\":");
            writer.write(JsonHelper.convert(this.userAgent));
            prefix = ",";
        }

        if (!(this.id == null)) {
            writer.write(prefix + "\"ai.user.id\":");
            writer.write(JsonHelper.convert(this.id));
            prefix = ",";
        }

        if (!(this.storeRegion == null)) {
            writer.write(prefix + "\"ai.user.storeRegion\":");
            writer.write(JsonHelper.convert(this.storeRegion));
            prefix = ",";
        }

        if (!(this.authUserId == null)) {
            writer.write(prefix + "\"ai.user.authUserId\":");
            writer.write(JsonHelper.convert(this.authUserId));
            prefix = ",";
        }

        if (!(this.anonUserAcquisitionDate == null)) {
            writer.write(prefix + "\"ai.user.anonUserAcquisitionDate\":");
            writer.write(JsonHelper.convert(this.anonUserAcquisitionDate));
            prefix = ",";
        }

        if (!(this.authUserAcquisitionDate == null)) {
            writer.write(prefix + "\"ai.user.authUserAcquisitionDate\":");
            writer.write(JsonHelper.convert(this.authUserAcquisitionDate));
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
