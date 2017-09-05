package net.hockeyapp.android.objects.metrics;

import junit.framework.TestCase;

import net.hockeyapp.android.metrics.model.Session;

import java.io.IOException;
import java.io.StringWriter;

/// <summary>
/// Data contract test class SessionTests.
/// </summary>
public class SessionTests extends TestCase {

    public void testIdProperty() {
        String expected = "Test string";
        Session item = new Session();
        item.setId(expected);
        String actual = item.getId();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setId(expected);
        actual = item.getId();
        assertEquals(expected, actual);
    }

    public void testIsFirstProperty() {
        String expected = "Test string";
        Session item = new Session();
        item.setIsFirst(expected);
        String actual = item.getIsFirst();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setIsFirst(expected);
        actual = item.getIsFirst();
        assertEquals(expected, actual);
    }

    public void testIsNewProperty() {
        String expected = "Test string";
        Session item = new Session();
        item.setIsNew(expected);
        String actual = item.getIsNew();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setIsNew(expected);
        actual = item.getIsNew();
        assertEquals(expected, actual);
    }

    public void testSerialize() throws IOException {
        Session item = new Session();
        item.setId("Test string");
        item.setIsFirst("Test string");
        item.setIsNew("Test string");
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ai.session.id\":\"Test string\",\"ai.session.isFirst\":\"Test string\",\"ai.session.isNew\":\"Test string\"}";
        assertEquals(expected, writer.toString());
    }

}
