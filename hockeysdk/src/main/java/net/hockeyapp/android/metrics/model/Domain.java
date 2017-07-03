package net.hockeyapp.android.metrics.model;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;

/**
 * Data contract class Domain.
 */
public class Domain implements IJsonSerializable {
    /**
     * A map for holding event attributes.
     */
    public LinkedHashMap<String, String> Attributes = new LinkedHashMap<>();

    /**
     * The name for this type
     */
    public String QualifiedName;

    /**
     * Initializes a new instance of the Domain class.
     */
    public Domain() {
        this.InitializeFields();
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
        return "";
    }

    /**
     * Optionally initializes fields for the current context.
     */
    protected void InitializeFields() {

    }
}
