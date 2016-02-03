package net.hockeyapp.android.utils;

import junit.framework.Assert;

import org.junit.Test;

public class HockeyLogTest {

    @Test
    public void setLogLevelWorks() {
        HockeyLog.setHockeyLogLevel(HockeyLog.LogLevel.DEBUG);
        Assert.assertTrue(HockeyLog.getHockeyLogLevel() == HockeyLog.LogLevel.DEBUG);
        HockeyLog.setHockeyLogLevel(HockeyLog.LogLevel.INFO);
        Assert.assertTrue(HockeyLog.getHockeyLogLevel() == HockeyLog.LogLevel.INFO);
        HockeyLog.setHockeyLogLevel(HockeyLog.LogLevel.ERROR);
        Assert.assertTrue(HockeyLog.getHockeyLogLevel() == HockeyLog.LogLevel.ERROR);
        HockeyLog.setHockeyLogLevel(HockeyLog.LogLevel.WARN);
        Assert.assertTrue(HockeyLog.getHockeyLogLevel() == HockeyLog.LogLevel.WARN);
        HockeyLog.setHockeyLogLevel(HockeyLog.LogLevel.VERBOSE);
        Assert.assertTrue(HockeyLog.getHockeyLogLevel() == HockeyLog.LogLevel.VERBOSE);
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
