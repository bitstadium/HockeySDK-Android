package net.hockeyapp.android;

import junit.framework.Assert;
import junit.framework.TestCase;

import net.hockeyapp.android.utils.Util;

public class UtilTests extends TestCase {

    public void testValidAppIdentifierGetsConvertedToGuid() {
        String appIdentifier = "    ca2aba1482cb9458a67b917930b202c8      ";
        String expected = "ca2aba14-82cb-9458-a67b-917930b202c8";

        String actual = Util.convertAppIdentifierToGuid(appIdentifier);
        Assert.assertEquals(expected, actual);
    }

}
