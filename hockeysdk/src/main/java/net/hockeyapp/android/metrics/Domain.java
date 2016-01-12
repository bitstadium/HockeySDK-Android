/*
 * Generated from Microsoft.Telemetry.bond (https://github.com/Microsoft/bond)
*/
package net.hockeyapp.android.metrics;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;

/**
 * Data contract class Domain.
 */
public class Domain implements
        IJsonSerializable
{
    /**
     * A map for holding event attributes.
     */
    public LinkedHashMap<String, String> Attributes = new LinkedHashMap<String, String>();
    
    /**
     * The name for thie type
     */
    public String QualifiedName;
    
    /**
     * Initializes a new instance of the Domain class.
     */
    public Domain()
    {
        this.InitializeFields();
    }
    

    /**
     * Serializes the beginning of this object to the passed in writer.
     * @param writer The writer to serialize this object to.
     */
    @Override
    public void serialize(Writer writer) throws IOException
    {
        if (writer == null)
        {
            throw new IllegalArgumentException("writer");
        }
        
        writer.write('{');
        this.serializeContent(writer);
        writer.write('}');
    }

    /**
     * Serializes the beginning of this object to the passed in writer.
     * @param writer The writer to serialize this object to.
     */
    protected String serializeContent(Writer writer) throws IOException
    {
        String prefix = "";
        return prefix;
    }
    
    /**
     * Optionally initializes fields for the current context.
     */
    protected void InitializeFields() {
        
    }
}
