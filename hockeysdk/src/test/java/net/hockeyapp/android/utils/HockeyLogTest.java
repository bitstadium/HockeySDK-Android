package net.hockeyapp.android.utils;

import android.util.Log;

import junit.framework.Assert;

import org.junit.Test;

public class HockeyLogTest {

    @Test
    public void setLogLevelWorks() {
        HockeyLog.setLogLevel(Log.DEBUG);
        Assert.assertTrue(HockeyLog.getLogLevel() == Log.DEBUG);
        HockeyLog.setLogLevel(Log.INFO);
        Assert.assertTrue(HockeyLog.getLogLevel() == Log.INFO);
        HockeyLog.setLogLevel(Log.ERROR);
        Assert.assertTrue(HockeyLog.getLogLevel() == Log.ERROR);
        HockeyLog.setLogLevel(Log.WARN);
        Assert.assertTrue(HockeyLog.getLogLevel() == Log.WARN);
        HockeyLog.setLogLevel(Log.VERBOSE);
        Assert.assertTrue(HockeyLog.getLogLevel() == Log.VERBOSE);
    }

    @Test
    public void sanitizeTagWorks() {
        String actual = HockeyLog.sanitizeTag(null);
        String expected = "HockeyApp";
        Assert.assertEquals(expected, actual);

        actual = HockeyLog.sanitizeTag("");
        Assert.assertEquals(expected, actual);

        actual = HockeyLog.sanitizeTag("SomethingLongerThanTwentyThreeChars");
        Assert.assertEquals(expected, actual);

        expected = "Exactly2ThreeCharacters";
        actual = HockeyLog.sanitizeTag("Exactly2ThreeCharacters");
        Assert.assertEquals(expected, actual);

        actual = "Awesome Hockey SDK";
        expected = "Awesome Hockey SDK";
        Assert.assertEquals(expected, actual);
    }
}
