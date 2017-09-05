package net.hockeyapp.android.metrics.model;

import net.hockeyapp.android.metrics.JsonHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;

/**
 * Data contract class Base.
 */
public class Base implements IJsonSerializable {
    /**
     * A map for holding event attributes.
     */
    public LinkedHashMap<String, String> Attributes;

    /**
     * The name for this type
     */
    public String QualifiedName;

    /**
     * Backing field for property BaseType.
     */
    private String baseType;

    /**
     * Initializes a new instance of the Base class.
     */
    public Base() {
        this.InitializeFields();
        Attributes = new LinkedHashMap<>();
    }

    /**
     * Gets the BaseType property.
     */
    public String getBaseType() {
        return this.baseType;
    }

    /**
     * Sets the BaseType property.
     */
    public void setBaseType(String value) {
        this.baseType = value;
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
        if (!(this.baseType == null)) {
            writer.write(prefix + "\"baseType\":");
            writer.write(JsonHelper.convert(this.baseType));
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
