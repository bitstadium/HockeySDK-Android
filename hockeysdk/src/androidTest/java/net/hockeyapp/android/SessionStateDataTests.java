package net.hockeyapp.android;

import junit.framework.Assert;
import junit.framework.TestCase;

import net.hockeyapp.android.telemetry.SessionState;
import net.hockeyapp.android.telemetry.SessionStateData;

import java.io.IOException;
import java.io.StringWriter;

/// <summary>
/// Data contract test class SessionStateDataTests.
/// </summary>
public class SessionStateDataTests extends TestCase {
    public void testVerPropertyWorksAsExpected() {
        int expected = 42;
        SessionStateData item = new SessionStateData();
        item.setVer(expected);
        int actual = item.getVer();
        Assert.assertEquals(expected, actual);

        expected = 13;
        item.setVer(expected);
        actual = item.getVer();
        Assert.assertEquals(expected, actual);
    }

    public void testStatePropertyWorksAsExpected() {
        SessionState expected = SessionState.START;
        SessionStateData item = new SessionStateData();
        item.setState(expected);
        SessionState actual = item.getState();
        Assert.assertEquals(expected.getValue(), actual.getValue());

        expected = SessionState.END;
        item.setState(expected);
        actual = item.getState();
        Assert.assertEquals(expected.getValue(), actual.getValue());
    }

    public void testSerialize() throws IOException {
        SessionStateData item = new SessionStateData();
        item.setVer(42);
        item.setState(SessionState.START);
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ver\":42,\"state\":0}";
        Assert.assertEquals(expected, writer.toString());
    }

}
