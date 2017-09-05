package net.hockeyapp.android.objects.metrics;

import junit.framework.TestCase;

import net.hockeyapp.android.metrics.model.Internal;

import java.io.IOException;
import java.io.StringWriter;

/// <summary>
/// Data contract test class InternalTests.
/// </summary>
public class InternalTests extends TestCase {
    public void testSdk_versionProperty() {
        String expected = "Test string";
        Internal item = new Internal();
        item.setSdkVersion(expected);
        String actual = item.getSdkVersion();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setSdkVersion(expected);
        actual = item.getSdkVersion();
        assertEquals(expected, actual);
    }

    public void testAgent_versionProperty() {
        String expected = "Test string";
        Internal item = new Internal();
        item.setAgentVersion(expected);
        String actual = item.getAgentVersion();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setAgentVersion(expected);
        actual = item.getAgentVersion();
        assertEquals(expected, actual);
    }

    public void testSerialize() throws IOException {
        Internal item = new Internal();
        item.setSdkVersion("Test string");
        item.setAgentVersion("Test string");
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ai.internal.sdkVersion\":\"Test string\",\"ai.internal.agentVersion\":\"Test string\"}";
        assertEquals(expected, writer.toString());
    }

}
