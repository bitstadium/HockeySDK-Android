package net.hockeyapp.android.objects.metrics;

import junit.framework.TestCase;

import net.hockeyapp.android.metrics.model.SessionState;
import net.hockeyapp.android.metrics.model.SessionStateData;

import java.io.IOException;
import java.io.StringWriter;

/// <summary>
/// Data contract test class SessionStateDataTests.
/// </summary>
public class SessionStateDataTests extends TestCase {

    public void testVerProperty() {
        int expected = 42;
        SessionStateData item = new SessionStateData();
        item.setVer(expected);
        int actual = item.getVer();
        assertEquals(expected, actual);

        expected = 13;
        item.setVer(expected);
        actual = item.getVer();
        assertEquals(expected, actual);
    }

    public void testStateProperty() {
        SessionState expected = SessionState.START;
        SessionStateData item = new SessionStateData();
        item.setState(expected);
        SessionState actual = item.getState();
        assertEquals(expected.getValue(), actual.getValue());

        expected = SessionState.END;
        item.setState(expected);
        actual = item.getState();
        assertEquals(expected.getValue(), actual.getValue());
    }

    public void testSerialize() throws IOException {
        SessionStateData item = new SessionStateData();
        item.setVer(42);
        item.setState(SessionState.START);
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ver\":42,\"state\":0}";
        assertEquals(expected, writer.toString());
    }

}
