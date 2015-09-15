package net.hockeyapp.android.telemetry;

import junit.framework.Assert;
import junit.framework.TestCase;

import net.hockeyapp.android.telemetry.Session;

import java.io.IOException;
import java.io.StringWriter;

/// <summary>
/// Data contract test class SessionTests.
/// </summary>
public class SessionTests extends TestCase
{
    public void testIdPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Session item = new Session();
        item.setId(expected);
        String actual = item.getId();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setId(expected);
        actual = item.getId();
        Assert.assertEquals(expected, actual);
    }
    
    public void testIs_firstPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Session item = new Session();
        item.setIsFirst(expected);
        String actual = item.getIsFirst();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setIsFirst(expected);
        actual = item.getIsFirst();
        Assert.assertEquals(expected, actual);
    }
    
    public void testIs_newPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Session item = new Session();
        item.setIsNew(expected);
        String actual = item.getIsNew();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setIsNew(expected);
        actual = item.getIsNew();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSerialize() throws IOException
    {
        Session item = new Session();
        item.setId("Test string");
        item.setIsFirst("Test string");
        item.setIsNew("Test string");
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ai.session.id\":\"Test string\",\"ai.session.isFirst\":\"Test string\",\"ai.session.isNew\":\"Test string\"}";
        Assert.assertEquals(expected, writer.toString());
    }

}
