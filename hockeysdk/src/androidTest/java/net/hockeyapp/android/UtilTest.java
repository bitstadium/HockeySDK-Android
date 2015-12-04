package net.hockeyapp.android;

import android.app.Application;
import android.test.ApplicationTestCase;

import junit.framework.Assert;

import net.hockeyapp.android.utils.Util;

public class UtilTest extends ApplicationTestCase<Application> {
    public UtilTest() {
        super(Application.class);
    }

    public void testValidMail() throws Exception {
        String validMail = "test@example.com";
        Assert.assertTrue(Util.isValidEmail(validMail));
    }
}