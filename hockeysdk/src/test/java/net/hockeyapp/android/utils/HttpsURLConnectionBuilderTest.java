package net.hockeyapp.android.utils;

import android.os.Build;
import android.text.TextUtils;

import net.hockeyapp.android.TestUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@SuppressWarnings("unused")
@PrepareForTest({
        HttpsURLConnectionBuilder.class,
        TextUtils.class
})
public class HttpsURLConnectionBuilderTest {

    private static final String TEST_URL = "https://sdk.hockeyapp.net";

    @Rule
    public PowerMockRule mRule = new PowerMockRule();

    @Before
    public void setup() {
        mockStatic(TextUtils.class);
        Mockito.when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {

            @Override
            public Boolean answer(InvocationOnMock invocation) {
                CharSequence str = (CharSequence) invocation.getArguments()[0];
                return str == null || str.length() == 0;
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        TestUtils.setInternalState(Build.VERSION.class, "SDK_INT", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormFieldSizeLimit() {

        String mockString = new String(new char[(4 * 1024 * 1024) + 1]).replace('\0', ' ');

        HttpsURLConnectionBuilder builder = new HttpsURLConnectionBuilder(TEST_URL);
        builder.setRequestMethod("POST");

        Map<String, String> fields = new HashMap<>();
        fields.put("test", mockString);

        builder.writeFormFields(fields);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormFieldsSizeLimit() {

        Map<String, String> fields = new HashMap<>();
        for (int i = 0; i < HttpsURLConnectionBuilder.FIELDS_LIMIT + 1; ++i) {
            String keyValue = String.valueOf(i);
            fields.put(keyValue, keyValue);
        }

        HttpsURLConnectionBuilder builder = new HttpsURLConnectionBuilder(TEST_URL);
        builder.setRequestMethod("POST");
        builder.writeFormFields(fields);
    }

    @Test
    public void tls1_2Enforcement() throws Exception {
        for (int apiLevel = Build.VERSION_CODES.JELLY_BEAN; apiLevel <= Build.VERSION_CODES.LOLLIPOP; apiLevel++) {
            testTls1_2Setting(apiLevel, 1);
        }
        for (int apiLevel = Build.VERSION_CODES.LOLLIPOP_MR1; apiLevel <= Build.VERSION_CODES.O_MR1; apiLevel++) {
            testTls1_2Setting(apiLevel, 0);
        }
    }

    private void testTls1_2Setting(int apiLevel, int tlsSetExpectedCalls) throws Exception {
        TestUtils.setInternalState(Build.VERSION.class, "SDK_INT", apiLevel);
        URL url = mock(URL.class);
        whenNew(URL.class).withArguments(TEST_URL).thenReturn(url);
        HttpsURLConnection urlConnection = mock(HttpsURLConnection.class);
        Mockito.when(url.openConnection()).thenReturn(urlConnection);
        HttpsURLConnectionBuilder builder = new HttpsURLConnectionBuilder(TEST_URL);
        builder.build();
        verify(urlConnection, times(tlsSetExpectedCalls)).setSSLSocketFactory(argThat(new ArgumentMatcher<SSLSocketFactory>() {

            @Override
            public boolean matches(Object argument) {
                return argument instanceof TLS1_2SocketFactory;
            }
        }));
    }
}
