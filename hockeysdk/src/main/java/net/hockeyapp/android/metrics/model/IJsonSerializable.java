package net.hockeyapp.android.metrics.model;

import java.io.IOException;
import java.io.Writer;

/**
 * this is the interface for all the data contract objects.
 */
public interface IJsonSerializable {

    /**
     * Serialize the contract objects with writer
     *
     * @param writer the writer to serialize data
     * @throws IOException will be thrown if something wrong with the output
     *                     stream
     */
    void serialize(Writer writer) throws IOException;
}
