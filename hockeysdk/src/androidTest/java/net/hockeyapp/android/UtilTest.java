package net.hockeyapp.android;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import junit.framework.Assert;

import net.hockeyapp.android.utils.Util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UtilTest extends ActivityInstrumentationTestCase2<UpdateActivity> {
    public UtilTest() {
        super(UpdateActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void encodeParamTest() throws Exception {
        String testParam = "Something something 2015 2.0 ê";
        String expected = "Something+something+2015+2.0+%C3%AA";
        Assert.assertTrue(Util.encodeParam(testParam).equals(expected));
    }

    @Test
    public void validMailTest() throws Exception {
        String validMail = "test@example.com";
        Assert.assertTrue(Util.isValidEmail(validMail));
    }

    @Test
    public void invalidMailTest() throws Exception {
        String invalidMail = "1235 %4 something";
        Assert.assertFalse(Util.isValidEmail(invalidMail));

        invalidMail = "me@example";
        Assert.assertFalse(Util.isValidEmail(invalidMail));

        invalidMail = "mail@example .com";
        Assert.assertFalse(Util.isValidEmail(invalidMail));
    }

}