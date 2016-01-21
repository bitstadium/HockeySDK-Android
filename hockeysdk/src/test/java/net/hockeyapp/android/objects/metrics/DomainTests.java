package net.hockeyapp.android.objects.metrics;

import junit.framework.Assert;
import junit.framework.TestCase;

import net.hockeyapp.android.metrics.model.Domain;

import java.io.IOException;
import java.io.StringWriter;

/// <summary>
/// Data contract test class DomainTests.
/// </summary>
public class DomainTests extends TestCase {

    public void testSerialize() throws IOException {
        Domain item = new Domain();
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{}";
        Assert.assertEquals(expected, writer.toString());
    }

}
