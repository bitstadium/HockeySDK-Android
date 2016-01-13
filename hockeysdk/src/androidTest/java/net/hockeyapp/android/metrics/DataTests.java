package net.hockeyapp.android.metrics;

import junit.framework.Assert;
import junit.framework.TestCase;

import net.hockeyapp.android.metrics.model.Data;
import net.hockeyapp.android.metrics.model.Domain;
import net.hockeyapp.android.metrics.model.SessionStateData;

import java.io.IOException;
import java.io.StringWriter;

/// <summary>
/// Data contract test class DataTests.
/// </summary>
public class DataTests extends TestCase {

    public void testBaseDataProperty() {
        ITelemetry expected = new SessionStateData();
        Data item = new Data();
        item.setBaseData(expected);
        Domain actual = item.getBaseData();
        Assert.assertEquals(expected, actual);

        expected = new SessionStateData();
        item.setBaseData(expected);
        actual = item.getBaseData();
        Assert.assertEquals(expected, actual);
    }

    public void testSerialize() throws IOException {
        Data item = new Data();
        item.setBaseData(new SessionStateData());
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"baseData\":{\"ver\":2,\"state\":0}}";
        Assert.assertEquals(expected, writer.toString());
    }

}
