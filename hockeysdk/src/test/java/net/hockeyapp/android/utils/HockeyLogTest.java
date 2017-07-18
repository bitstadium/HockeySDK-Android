package net.hockeyapp.android.utils;

import android.util.Log;

import org.junit.Test;

import static junit.framework.Assert.*;

public class HockeyLogTest {

    @Test
    public void setLogLevelWorks() {
        HockeyLog.setLogLevel(Log.DEBUG);
        assertTrue(HockeyLog.getLogLevel() == Log.DEBUG);
        HockeyLog.setLogLevel(Log.INFO);
        assertTrue(HockeyLog.getLogLevel() == Log.INFO);
        HockeyLog.setLogLevel(Log.ERROR);
        assertTrue(HockeyLog.getLogLevel() == Log.ERROR);
        HockeyLog.setLogLevel(Log.WARN);
        assertTrue(HockeyLog.getLogLevel() == Log.WARN);
        HockeyLog.setLogLevel(Log.VERBOSE);
        assertTrue(HockeyLog.getLogLevel() == Log.VERBOSE);
    }

    @Test
    public void sanitizeTagWorks() {
        String expected = HockeyLog.HOCKEY_TAG;
        String actual = HockeyLog.sanitizeTag(null);
        assertEquals(expected, actual);

        actual = HockeyLog.sanitizeTag("");
        assertEquals(expected, actual);

        actual = HockeyLog.sanitizeTag("SomethingLongerThanTwentyThreeChars");
        assertEquals(expected, actual);

        expected = "Exactly2ThreeCharacters";
        actual = HockeyLog.sanitizeTag("Exactly2ThreeCharacters");
        assertEquals(expected, actual);

        expected = "Awesome Hockey SDK";
        actual = HockeyLog.sanitizeTag("Awesome Hockey SDK");
        assertEquals(expected, actual);
    }
}
