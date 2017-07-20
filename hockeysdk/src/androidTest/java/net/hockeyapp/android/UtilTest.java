package net.hockeyapp.android;

import android.support.test.runner.AndroidJUnit4;

import net.hockeyapp.android.utils.Util;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class UtilTest {

    @Test
    public void encodeParamTest() throws Exception {
        String testParam = "Something something 2015 2.0 ê";
        String expected = "Something+something+2015+2.0+%C3%AA";
        assertTrue(Util.encodeParam(testParam).equals(expected));
    }

    @Test
    public void validMailTest() throws Exception {
        String validMail = "test@example.com";
        assertTrue(Util.isValidEmail(validMail));
    }

    @Test
    public void invalidMailTest() throws Exception {
        String invalidMail = "1235 %4 something";
        assertFalse(Util.isValidEmail(invalidMail));

        invalidMail = "me@example";
        assertFalse(Util.isValidEmail(invalidMail));

        invalidMail = "mail@example .com";
        assertFalse(Util.isValidEmail(invalidMail));
    }

    @Test
    public void testValidAppIdentifierGetsConvertedToGuid() {
        String appIdentifier = "ca2aba1482cb9458a67b917930b202c8";
        String expected = "ca2aba14-82cb-9458-a67b-917930b202c8";

        String actual = Util.convertAppIdentifierToGuid(appIdentifier);
        assertEquals(expected, actual);
    }

}
