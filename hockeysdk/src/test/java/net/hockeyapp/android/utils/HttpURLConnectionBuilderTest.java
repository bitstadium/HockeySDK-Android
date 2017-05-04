package net.hockeyapp.android.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class HttpURLConnectionBuilderTest {

    private static final String TEST_URL = "https://sdk.hockeyapp.net";

    @Test(expected = IllegalArgumentException.class)
    public void testFormFieldSizeLimit() {

        String mockString = new String(new char[(4 * 1024 * 1024) + 1]).replace('\0', ' ');

        HttpURLConnectionBuilder builder = new HttpURLConnectionBuilder(TEST_URL);
        builder.setRequestMethod("POST");

        Map<String, String> fields = new HashMap<>();
        fields.put("test", mockString);

        builder.writeFormFields(fields);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormFieldsSizeLimit() {

        Map<String, String> fields = new HashMap<>();
        for(int i = 0; i < HttpURLConnectionBuilder.FIELDS_LIMIT + 1; ++i) {
            String keyValue = String.valueOf(i);
            fields.put(keyValue,keyValue);
        }

        HttpURLConnectionBuilder builder = new HttpURLConnectionBuilder(TEST_URL);
        builder.setRequestMethod("POST");
        builder.writeFormFields(fields);
    }

}
