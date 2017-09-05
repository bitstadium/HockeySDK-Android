package net.hockeyapp.android.metrics.model;

import net.hockeyapp.android.metrics.JsonHelper;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;

/**
 * Data contract class Device.
 */
public class Device implements IJsonSerializable, Serializable {
    /**
     * Backing field for property Id.
     */
    private String id;

    /**
     * Backing field for property Ip.
     */
    private String ip;

    /**
     * Backing field for property Language.
     */
    private String language;

    /**
     * Backing field for property Locale.
     */
    private String locale;

    /**
     * Backing field for property Model.
     */
    private String model;

    /**
     * Backing field for property Network.
     */
    private String network;

    /**
     * Backing field for property NetworkName.
     */
    private String networkName;

    /**
     * Backing field for property OemName.
     */
    private String oemName;

    /**
     * Backing field for property Os.
     */
    private String os;

    /**
     * Backing field for property OsVersion.
     */
    private String osVersion;

    /**
     * Backing field for property RoleInstance.
     */
    private String roleInstance;

    /**
     * Backing field for property RoleName.
     */
    private String roleName;

    /**
     * Backing field for property ScreenResolution.
     */
    private String screenResolution;

    /**
     * Backing field for property Type.
     */
    private String type;

    /**
     * Backing field for property MachineName.
     */
    private String machineName;

    /**
     * Backing field for property VmName.
     */
    private String vmName;

    /**
     * Initializes a new instance of the Device class.
     */
    public Device() {
        this.InitializeFields();
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
     * Gets the Ip property.
     */
    public String getIp() {
        return this.ip;
    }

    /**
     * Sets the Ip property.
     */
    public void setIp(String value) {
        this.ip = value;
    }

    /**
     * Gets the Language property.
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Sets the Language property.
     */
    public void setLanguage(String value) {
        this.language = value;
    }

    /**
     * Gets the Locale property.
     */
    public String getLocale() {
        return this.locale;
    }

    /**
     * Sets the Locale property.
     */
    public void setLocale(String value) {
        this.locale = value;
    }

    /**
     * Gets the Model property.
     */
    public String getModel() {
        return this.model;
    }

    /**
     * Sets the Model property.
     */
    public void setModel(String value) {
        this.model = value;
    }

    /**
     * Gets the Network property.
     */
    public String getNetwork() {
        return this.network;
    }

    /**
     * Sets the Network property.
     */
    public void setNetwork(String value) {
        this.network = value;
    }

    /**
     * Gets the NetworkName property.
     */
    public String getNetworkName() {
        return this.networkName;
    }

    /**
     * Sets the NetworkName property.
     */
    public void setNetworkName(String value) {
        this.networkName = value;
    }

    /**
     * Gets the OemName property.
     */
    public String getOemName() {
        return this.oemName;
    }

    /**
     * Sets the OemName property.
     */
    public void setOemName(String value) {
        this.oemName = value;
    }

    /**
     * Gets the Os property.
     */
    public String getOs() {
        return this.os;
    }

    /**
     * Sets the Os property.
     */
    public void setOs(String value) {
        this.os = value;
    }

    /**
     * Gets the OsVersion property.
     */
    public String getOsVersion() {
        return this.osVersion;
    }

    /**
     * Sets the OsVersion property.
     */
    public void setOsVersion(String value) {
        this.osVersion = value;
    }

    /**
     * Gets the RoleInstance property.
     */
    public String getRoleInstance() {
        return this.roleInstance;
    }

    /**
     * Sets the RoleInstance property.
     */
    public void setRoleInstance(String value) {
        this.roleInstance = value;
    }

    /**
     * Gets the RoleName property.
     */
    public String getRoleName() {
        return this.roleName;
    }

    /**
     * Sets the RoleName property.
     */
    public void setRoleName(String value) {
        this.roleName = value;
    }

    /**
     * Gets the ScreenResolution property.
     */
    public String getScreenResolution() {
        return this.screenResolution;
    }

    /**
     * Sets the ScreenResolution property.
     */
    public void setScreenResolution(String value) {
        this.screenResolution = value;
    }

    /**
     * Gets the Type property.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets the Type property.
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the MachineName property.
     */
    public String getMachineName() {
        return this.machineName;
    }

    /**
     * Sets the MachineName property.
     */
    public void setMachineName(String value) {
        this.machineName = value;
    }

    /**
     * Gets the VmName property.
     */
    public String getVmName() {
        return this.vmName;
    }

    /**
     * Sets the VmName property.
     */
    public void setVmName(String value) {
        this.vmName = value;
    }


    /**
     * Adds all members of this class to a map
     *
     * @param map to which the members of this class will be added.
     */
    public void addToHashMap(Map<String, String> map) {
        if (!(this.id == null)) {
            map.put("ai.device.id", this.id);
        }
        if (!(this.ip == null)) {
            map.put("ai.device.ip", this.ip);
        }
        if (!(this.language == null)) {
            map.put("ai.device.language", this.language);
        }
        if (!(this.locale == null)) {
            map.put("ai.device.locale", this.locale);
        }
        if (!(this.model == null)) {
            map.put("ai.device.model", this.model);
        }
        if (!(this.network == null)) {
            map.put("ai.device.network", this.network);
        }
        if (!(this.networkName == null)) {
            map.put("ai.device.networkName", this.networkName);
        }
        if (!(this.oemName == null)) {
            map.put("ai.device.oemName", this.oemName);
        }
        if (!(this.os == null)) {
            map.put("ai.device.os", this.os);
        }
        if (!(this.osVersion == null)) {
            map.put("ai.device.osVersion", this.osVersion);
        }
        if (!(this.roleInstance == null)) {
            map.put("ai.device.roleInstance", this.roleInstance);
        }
        if (!(this.roleName == null)) {
            map.put("ai.device.roleName", this.roleName);
        }
        if (!(this.screenResolution == null)) {
            map.put("ai.device.screenResolution", this.screenResolution);
        }
        if (!(this.type == null)) {
            map.put("ai.device.type", this.type);
        }
        if (!(this.machineName == null)) {
            map.put("ai.device.machineName", this.machineName);
        }
        if (!(this.vmName == null)) {
            map.put("ai.device.vmName", this.vmName);
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
        if (!(this.id == null)) {
            writer.write(prefix + "\"ai.device.id\":");
            writer.write(JsonHelper.convert(this.id));
            prefix = ",";
        }

        if (!(this.ip == null)) {
            writer.write(prefix + "\"ai.device.ip\":");
            writer.write(JsonHelper.convert(this.ip));
            prefix = ",";
        }

        if (!(this.language == null)) {
            writer.write(prefix + "\"ai.device.language\":");
            writer.write(JsonHelper.convert(this.language));
            prefix = ",";
        }

        if (!(this.locale == null)) {
            writer.write(prefix + "\"ai.device.locale\":");
            writer.write(JsonHelper.convert(this.locale));
            prefix = ",";
        }

        if (!(this.model == null)) {
            writer.write(prefix + "\"ai.device.model\":");
            writer.write(JsonHelper.convert(this.model));
            prefix = ",";
        }

        if (!(this.network == null)) {
            writer.write(prefix + "\"ai.device.network\":");
            writer.write(JsonHelper.convert(this.network));
            prefix = ",";
        }

        if (!(this.networkName == null)) {
            writer.write(prefix + "\"ai.device.networkName\":");
            writer.write(JsonHelper.convert(this.networkName));
            prefix = ",";
        }

        if (!(this.oemName == null)) {
            writer.write(prefix + "\"ai.device.oemName\":");
            writer.write(JsonHelper.convert(this.oemName));
            prefix = ",";
        }

        if (!(this.os == null)) {
            writer.write(prefix + "\"ai.device.os\":");
            writer.write(JsonHelper.convert(this.os));
            prefix = ",";
        }

        if (!(this.osVersion == null)) {
            writer.write(prefix + "\"ai.device.osVersion\":");
            writer.write(JsonHelper.convert(this.osVersion));
            prefix = ",";
        }

        if (!(this.roleInstance == null)) {
            writer.write(prefix + "\"ai.device.roleInstance\":");
            writer.write(JsonHelper.convert(this.roleInstance));
            prefix = ",";
        }

        if (!(this.roleName == null)) {
            writer.write(prefix + "\"ai.device.roleName\":");
            writer.write(JsonHelper.convert(this.roleName));
            prefix = ",";
        }

        if (!(this.screenResolution == null)) {
            writer.write(prefix + "\"ai.device.screenResolution\":");
            writer.write(JsonHelper.convert(this.screenResolution));
            prefix = ",";
        }

        if (!(this.type == null)) {
            writer.write(prefix + "\"ai.device.type\":");
            writer.write(JsonHelper.convert(this.type));
            prefix = ",";
        }

        if (!(this.machineName == null)) {
            writer.write(prefix + "\"ai.device.machineName\":");
            writer.write(JsonHelper.convert(this.machineName));
            prefix = ",";
        }

        if (!(this.vmName == null)) {
            writer.write(prefix + "\"ai.device.vmName\":");
            writer.write(JsonHelper.convert(this.vmName));
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
