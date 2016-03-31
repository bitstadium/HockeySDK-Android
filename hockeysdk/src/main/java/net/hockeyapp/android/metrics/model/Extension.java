package net.hockeyapp.android.metrics.model;

import net.hockeyapp.android.metrics.JsonHelper;

import java.io.IOException;
import java.io.Writer;


/**
 * Data contract class Extension.
 */
public class Extension implements
        IJsonSerializable {
    /**
     * Backing field for property Ver.
     */
    private String ver = "1.0";

    /**
     * Initializes a new instance of the Extension class.
     */
    public Extension() {
        this.InitializeFields();
    }

    /**
     * Gets the Ver property.
     */
    public String getVer() {
        return this.ver;
    }

    /**
     * Sets the Ver property.
     */
    public void setVer(String value) {
        this.ver = value;
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
        if (!(this.ver == null)) {
            writer.write(prefix + "\"ver\":");
            writer.write(JsonHelper.convert(this.ver));
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
