package net.hockeyapp.android.objects.metrics;

import junit.framework.TestCase;

import net.hockeyapp.android.metrics.model.Device;

import java.io.IOException;
import java.io.StringWriter;

/// <summary>
/// Data contract test class DeviceTests.
/// </summary>
public class DeviceTests extends TestCase {

    public void testIdProperty() {
        String expected = "Test string";
        Device item = new Device();
        item.setId(expected);
        String actual = item.getId();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setId(expected);
        actual = item.getId();
        assertEquals(expected, actual);
    }

    public void testIpProperty() {
        String expected = "Test string";
        Device item = new Device();
        item.setIp(expected);
        String actual = item.getIp();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setIp(expected);
        actual = item.getIp();
        assertEquals(expected, actual);
    }

    public void testLanguageProperty() {
        String expected = "Test string";
        Device item = new Device();
        item.setLanguage(expected);
        String actual = item.getLanguage();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setLanguage(expected);
        actual = item.getLanguage();
        assertEquals(expected, actual);
    }

    public void testLocaleProperty() {
        String expected = "Test string";
        Device item = new Device();
        item.setLocale(expected);
        String actual = item.getLocale();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setLocale(expected);
        actual = item.getLocale();
        assertEquals(expected, actual);
    }

    public void testModelProperty() {
        String expected = "Test string";
        Device item = new Device();
        item.setModel(expected);
        String actual = item.getModel();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setModel(expected);
        actual = item.getModel();
        assertEquals(expected, actual);
    }

    public void testNetworkProperty() {
        String expected = "Test string";
        Device item = new Device();
        item.setNetwork(expected);
        String actual = item.getNetwork();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setNetwork(expected);
        actual = item.getNetwork();
        assertEquals(expected, actual);
    }

    public void testOemNameProperty() {
        String expected = "Test string";
        Device item = new Device();
        item.setOemName(expected);
        String actual = item.getOemName();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setOemName(expected);
        actual = item.getOemName();
        assertEquals(expected, actual);
    }

    public void testOsProperty() {
        String expected = "Test string";
        Device item = new Device();
        item.setOs(expected);
        String actual = item.getOs();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setOs(expected);
        actual = item.getOs();
        assertEquals(expected, actual);
    }

    public void testOsVersionProperty() {
        String expected = "Test string";
        Device item = new Device();
        item.setOsVersion(expected);
        String actual = item.getOsVersion();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setOsVersion(expected);
        actual = item.getOsVersion();
        assertEquals(expected, actual);
    }

    public void testRoleInstanceProperty() {
        String expected = "Test string";
        Device item = new Device();
        item.setRoleInstance(expected);
        String actual = item.getRoleInstance();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setRoleInstance(expected);
        actual = item.getRoleInstance();
        assertEquals(expected, actual);
    }

    public void testRoleNameProperty() {
        String expected = "Test string";
        Device item = new Device();
        item.setRoleName(expected);
        String actual = item.getRoleName();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setRoleName(expected);
        actual = item.getRoleName();
        assertEquals(expected, actual);
    }

    public void testScreenResolutionProperty() {
        String expected = "Test string";
        Device item = new Device();
        item.setScreenResolution(expected);
        String actual = item.getScreenResolution();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setScreenResolution(expected);
        actual = item.getScreenResolution();
        assertEquals(expected, actual);
    }

    public void testTypeProperty() {
        String expected = "Test string";
        Device item = new Device();
        item.setType(expected);
        String actual = item.getType();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setType(expected);
        actual = item.getType();
        assertEquals(expected, actual);
    }

    public void testMachineNameProperty() {
        String expected = "Test string";
        Device item = new Device();
        item.setMachineName(expected);
        String actual = item.getMachineName();
        assertEquals(expected, actual);

        expected = "Other string";
        item.setMachineName(expected);
        actual = item.getMachineName();
        assertEquals(expected, actual);
    }

    public void testSerialize() throws IOException {
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
        assertEquals(expected, writer.toString());
    }

}
