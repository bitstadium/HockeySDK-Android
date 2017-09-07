package net.hockeyapp.android.objects.metrics;

import junit.framework.TestCase;

import net.hockeyapp.android.metrics.model.Base;
import net.hockeyapp.android.metrics.model.Envelope;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/// <summary>
/// Data contract test class EnvelopeTests.
/// </summary>
public class EnvelopeTests extends TestCase {

    public void testVerProperty() {
        int expected = 123;
        Envelope item = new Envelope();
        item.setVer(expected);
        int actual = item.getVer();
        assertEquals(expected, actual);

        expected = 456;
        item.setVer(expected);
        actual = item.getVer();
        assertEquals(expected, actual);
    }

    public void testNameProperty() {
        String expected = "Test string";
        Envelope item = new Envelope();
        item.setName(expected);
        String actual = item.getName();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setName(expected);
        actual = item.getName();
        assertEquals(expected, actual);
    }

    public void testTimeProperty() {
        String expected = "Test string";
        Envelope item = new Envelope();
        item.setTime(expected);
        String actual = item.getTime();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setTime(expected);
        actual = item.getTime();
        assertEquals(expected, actual);
    }

    public void testI_keyProperty() {
        String expected = "Test string";
        Envelope item = new Envelope();
        item.setIKey(expected);
        String actual = item.getIKey();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setIKey(expected);
        actual = item.getIKey();
        assertEquals(expected, actual);
    }

    public void testFlagsProperty() {
        long expected = 42;
        Envelope item = new Envelope();
        item.setFlags(expected);
        long actual = item.getFlags();
        assertEquals(expected, actual);

        expected = 13;
        item.setFlags(expected);
        actual = item.getFlags();
        assertEquals(expected, actual);
    }

    public void testOsProperty() {
        String expected = "Test string";
        Envelope item = new Envelope();
        item.setOs(expected);
        String actual = item.getOs();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setOs(expected);
        actual = item.getOs();
        assertEquals(expected, actual);
    }

    public void testOs_verProperty() {
        String expected = "Test string";
        Envelope item = new Envelope();
        item.setOsVer(expected);
        String actual = item.getOsVer();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setOsVer(expected);
        actual = item.getOsVer();
        assertEquals(expected, actual);
    }

    public void testApp_idProperty() {
        String expected = "Test string";
        Envelope item = new Envelope();
        item.setAppId(expected);
        String actual = item.getAppId();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setAppId(expected);
        actual = item.getAppId();
        assertEquals(expected, actual);
    }

    public void testApp_verProperty() {
        String expected = "Test string";
        Envelope item = new Envelope();
        item.setAppVer(expected);
        String actual = item.getAppVer();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setAppVer(expected);
        actual = item.getAppVer();
        assertEquals(expected, actual);
    }

    public void testTagsProperty() {
        Envelope item = new Envelope();
        LinkedHashMap<String, String> actual = (LinkedHashMap<String, String>) item.getTags();
        assertNotNull(actual);
    }

    public void testDataProperty() {
        Base expected = new Base();
        Envelope item = new Envelope();
        item.setData(expected);
        Base actual = item.getData();
        assertEquals(expected, actual);

        expected = new Base();
        item.setData(expected);
        actual = item.getData();
        assertEquals(expected, actual);
    }

    public void testSerialize() throws IOException {
        Envelope item = new Envelope();
        item.setVer(1234);
        item.setName("Test string");
        item.setTime("Test string");
        item.setIKey("Test string");
        item.setFlags(42);
        item.setOs("Test string");
        item.setOsVer("Test string");
        item.setAppId("Test string");
        item.setAppVer("Test string");
        for (Map.Entry<String, String> entry : new LinkedHashMap<String, String>() {{
            put("key1", "test value 1");
            put("key2", "test value 2");
        }}.entrySet()) {
            item.getTags().put(entry.getKey(), entry.getValue());
        }
        item.setData(new Base());
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ver\":1234,\"name\":\"Test string\",\"time\":\"Test string\",\"sampleRate\":100,\"iKey\":\"Test string\",\"flags\":42,\"os\":\"Test string\",\"osVer\":\"Test string\",\"appId\":\"Test string\",\"appVer\":\"Test string\",\"tags\":{\"key1\":\"test value 1\",\"key2\":\"test value 2\"},\"data\":{}}";
        assertEquals(expected, writer.toString());
    }

}
