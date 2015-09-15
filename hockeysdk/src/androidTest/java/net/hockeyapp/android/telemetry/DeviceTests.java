package net.hockeyapp.android.telemetry;

import junit.framework.Assert;
import junit.framework.TestCase;

import net.hockeyapp.android.telemetry.Device;

import java.io.IOException;
import java.io.StringWriter;

/// <summary>
/// Data contract test class DeviceTests.
/// </summary>
public class DeviceTests extends TestCase
{
    public void testIdPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Device item = new Device();
        item.setId(expected);
        String actual = item.getId();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setId(expected);
        actual = item.getId();
        Assert.assertEquals(expected, actual);
    }
    
    public void testIpPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Device item = new Device();
        item.setIp(expected);
        String actual = item.getIp();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setIp(expected);
        actual = item.getIp();
        Assert.assertEquals(expected, actual);
    }
    
    public void testLanguagePropertyWorksAsExpected()
    {
        String expected = "Test string";
        Device item = new Device();
        item.setLanguage(expected);
        String actual = item.getLanguage();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setLanguage(expected);
        actual = item.getLanguage();
        Assert.assertEquals(expected, actual);
    }
    
    public void testLocalePropertyWorksAsExpected()
    {
        String expected = "Test string";
        Device item = new Device();
        item.setLocale(expected);
        String actual = item.getLocale();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setLocale(expected);
        actual = item.getLocale();
        Assert.assertEquals(expected, actual);
    }
    
    public void testModelPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Device item = new Device();
        item.setModel(expected);
        String actual = item.getModel();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setModel(expected);
        actual = item.getModel();
        Assert.assertEquals(expected, actual);
    }
    
    public void testNetworkPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Device item = new Device();
        item.setNetwork(expected);
        String actual = item.getNetwork();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setNetwork(expected);
        actual = item.getNetwork();
        Assert.assertEquals(expected, actual);
    }
    
    public void testOem_namePropertyWorksAsExpected()
    {
        String expected = "Test string";
        Device item = new Device();
        item.setOemName(expected);
        String actual = item.getOemName();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setOemName(expected);
        actual = item.getOemName();
        Assert.assertEquals(expected, actual);
    }
    
    public void testOsPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Device item = new Device();
        item.setOs(expected);
        String actual = item.getOs();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setOs(expected);
        actual = item.getOs();
        Assert.assertEquals(expected, actual);
    }
    
    public void testOs_versionPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Device item = new Device();
        item.setOsVersion(expected);
        String actual = item.getOsVersion();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setOsVersion(expected);
        actual = item.getOsVersion();
        Assert.assertEquals(expected, actual);
    }
    
    public void testRole_instancePropertyWorksAsExpected()
    {
        String expected = "Test string";
        Device item = new Device();
        item.setRoleInstance(expected);
        String actual = item.getRoleInstance();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setRoleInstance(expected);
        actual = item.getRoleInstance();
        Assert.assertEquals(expected, actual);
    }
    
    public void testRole_namePropertyWorksAsExpected()
    {
        String expected = "Test string";
        Device item = new Device();
        item.setRoleName(expected);
        String actual = item.getRoleName();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setRoleName(expected);
        actual = item.getRoleName();
        Assert.assertEquals(expected, actual);
    }
    
    public void testScreen_resolutionPropertyWorksAsExpected()
    {
        String expected = "Test string";
        Device item = new Device();
        item.setScreenResolution(expected);
        String actual = item.getScreenResolution();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setScreenResolution(expected);
        actual = item.getScreenResolution();
        Assert.assertEquals(expected, actual);
    }
    
    public void testTypePropertyWorksAsExpected()
    {
        String expected = "Test string";
        Device item = new Device();
        item.setType(expected);
        String actual = item.getType();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setType(expected);
        actual = item.getType();
        Assert.assertEquals(expected, actual);
    }
    
    public void testMachine_namePropertyWorksAsExpected()
    {
        String expected = "Test string";
        Device item = new Device();
        item.setMachineName(expected);
        String actual = item.getMachineName();
        Assert.assertEquals(expected, actual);
        
        expected = "Other string";
        item.setMachineName(expected);
        actual = item.getMachineName();
        Assert.assertEquals(expected, actual);
    }
    
    public void testSerialize() throws IOException
    {
        Device item = new Device();
        item.setId("Test string");
        item.setIp("Test string");
        item.setLanguage("Test string");
        item.setLocale("Test string");
        item.setModel("Test string");
        item.setNetwork("Test string");
        item.setOemName("Test string");
        item.setOs("Test string");
        item.setOsVersion("Test string");
        item.setRoleInstance("Test string");
        item.setRoleName("Test string");
        item.setScreenResolution("Test string");
        item.setType("Test string");
        item.setMachineName("Test string");
        StringWriter writer = new StringWriter();
        item.serialize(writer);
        String expected = "{\"ai.device.id\":\"Test string\",\"ai.device.ip\":\"Test string\",\"ai.device.language\":\"Test string\",\"ai.device.locale\":\"Test string\",\"ai.device.model\":\"Test string\",\"ai.device.network\":\"Test string\",\"ai.device.oemName\":\"Test string\",\"ai.device.os\":\"Test string\",\"ai.device.osVersion\":\"Test string\",\"ai.device.roleInstance\":\"Test string\",\"ai.device.roleName\":\"Test string\",\"ai.device.screenResolution\":\"Test string\",\"ai.device.type\":\"Test string\",\"ai.device.machineName\":\"Test string\"}";
        Assert.assertEquals(expected, writer.toString());
    }

}
