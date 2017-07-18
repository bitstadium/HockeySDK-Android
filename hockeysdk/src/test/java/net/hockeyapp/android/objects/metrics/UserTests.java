package net.hockeyapp.android.objects.metrics;

import junit.framework.TestCase;

import net.hockeyapp.android.metrics.model.User;

import java.io.IOException;
import java.io.StringWriter;

/// <summary>
/// Data contract test class UserTests.
/// </summary>
public class UserTests extends TestCase {

    public void testAccountAcquisitionDateProperty() {
        String expected = "Test string";
        User item = new User();
        item.setAccountAcquisitionDate(expected);
        String actual = item.getAccountAcquisitionDate();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setAccountAcquisitionDate(expected);
        actual = item.getAccountAcquisitionDate();
        assertEquals(expected, actual);
    }

    public void testAccountIdProperty() {
        String expected = "Test string";
        User item = new User();
        item.setAccountId(expected);
        String actual = item.getAccountId();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setAccountId(expected);
        actual = item.getAccountId();
        assertEquals(expected, actual);
    }

    public void testUserAgentProperty() {
        String expected = "Test string";
        User item = new User();
        item.setUserAgent(expected);
        String actual = item.getUserAgent();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setUserAgent(expected);
        actual = item.getUserAgent();
        assertEquals(expected, actual);
    }

    public void testIdProperty() {
        String expected = "Test string";
        User item = new User();
        item.setId(expected);
        String actual = item.getId();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setId(expected);
        actual = item.getId();
        assertEquals(expected, actual);
    }

    public void testSerialize() throws IOException {
        User item = new User();
        item.setAccountAcquisitionDate("Test string");
        item.setAccountId("Test string");
        item.setUserAgent("Test string");
        item.setId("Test string");
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ai.user.accountAcquisitionDate\":\"Test string\",\"ai.user.accountId\":\"Test string\",\"ai.user.userAgent\":\"Test string\",\"ai.user.id\":\"Test string\"}";
        assertEquals(expected, writer.toString());
    }

}
