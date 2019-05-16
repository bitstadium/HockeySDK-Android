package net.hockeyapp.android.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import net.hockeyapp.android.Constants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.net.ssl.HttpsURLConnection;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h3>Description</h3>
 *
 * Builder class for HttpsURLConnection.
 *
 **/
public class HttpsURLConnectionBuilder {

    private static final int DEFAULT_TIMEOUT = 2 * 60 * 1000;
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final int FORM_FIELD_LIMIT = 4 * 1024 * 1024;
    public static final int FIELDS_LIMIT = 25;

    private final String mUrlString;

    private String mRequestMethod;
    private String mRequestBody;
    private SimpleMultipartEntity mMultipartEntity;
    private int mTimeout = DEFAULT_TIMEOUT;

    private final Map<String, String> mHeaders;

    public HttpsURLConnectionBuilder(String urlString) {
        mUrlString = urlString;
        mHeaders = new HashMap<>();
        mHeaders.put("User-Agent", Constants.SDK_USER_AGENT);
    }

    public HttpsURLConnectionBuilder setRequestMethod(String requestMethod) {
        mRequestMethod = requestMethod;
        return this;
    }

    public HttpsURLConnectionBuilder setRequestBody(String requestBody) {
        mRequestBody = requestBody;
        return this;
    }

    public HttpsURLConnectionBuilder writeFormFields(Map<String, String> fields) throws IllegalArgumentException {

        // We should add limit on fields because a large number of fields can throw the OOM exception
        if (fields.size() > FIELDS_LIMIT) {
            throw new IllegalArgumentException("Fields size too large: " + fields.size() + " - max allowed: " + FIELDS_LIMIT);
        }

        for (String key : fields.keySet()) {
            String value = fields.get(key);
            if (value != null && value.length() > FORM_FIELD_LIMIT) {
                throw new IllegalArgumentException("Form field \"" + key + "\" size too large: " + value.length() + " - max allowed: " + FORM_FIELD_LIMIT);
            }
        }

        try {
            String formString = getFormString(fields, DEFAULT_CHARSET);
            setHeader("Content-Type", "application/x-www-form-urlencoded");
            setRequestBody(formString);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public HttpsURLConnectionBuilder writeMultipartData(Map<String, String> fields, Context context, List<Uri> attachmentUris) {
        try {
            File tempFile = File.createTempFile("multipart", null, context.getCacheDir());
            mMultipartEntity = new SimpleMultipartEntity(tempFile);
            mMultipartEntity.writeFirstBoundaryIfNeeds();

            for (String key : fields.keySet()) {
                mMultipartEntity.addPart(key, fields.get(key));
            }

            for (int i = 0; i < attachmentUris.size(); i++) {
                Uri attachmentUri = attachmentUris.get(i);
                boolean lastFile = (i == attachmentUris.size() - 1);

                InputStream input = context.getContentResolver().openInputStream(attachmentUri);
                String filename = Util.getFileName(context, attachmentUri);
                mMultipartEntity.addPart("attachment" + i, filename, input, lastFile);
            }
            mMultipartEntity.writeLastBoundaryIfNeeds();

            setHeader("Content-Type", "multipart/form-data; boundary=" + mMultipartEntity.getBoundary());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public HttpsURLConnectionBuilder setTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("Timeout has to be positive.");
        }
        mTimeout = timeout;
        return this;
    }

    public HttpsURLConnectionBuilder setHeader(String name, String value) {
        mHeaders.put(name, value);
        return this;
    }

    public HttpsURLConnectionBuilder setBasicAuthorization(String username, String password) {
        String authString = "Basic " + net.hockeyapp.android.utils.Base64.encodeToString(
                (username + ":" + password).getBytes(), android.util.Base64.NO_WRAP);

        setHeader("Authorization", authString);
        return this;
    }

    public HttpsURLConnection build() throws IOException {
        HttpsURLConnection connection;
        URL url = new URL(mUrlString);
        connection = (HttpsURLConnection) url.openConnection();

        /*
         * Make sure we use TLS 1.2 when the device supports it but not enabled by default.
         * Don't hardcode TLS version when enabled by default to avoid unnecessary wrapping and
         * to support future versions of TLS such as say 1.3 without having to patch this code.
         *
         * TLS 1.2 was enabled by default only on Android 5.0:
         * https://developer.android.com/about/versions/android-5.0-changes#ssl
         * https://developer.android.com/reference/javax/net/ssl/SSLSocket#default-configuration-for-different-android-versions
         *
         * There is a problem that TLS 1.2 is still disabled by default on some Samsung devices
         * with API 21, so apply the rule to this API level as well.
         * See https://github.com/square/okhttp/issues/2372#issuecomment-244807676
         */
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            connection.setSSLSocketFactory(new TLS1_2SocketFactory());
        }

        connection.setConnectTimeout(mTimeout);
        connection.setReadTimeout(mTimeout);

        if (!TextUtils.isEmpty(mRequestMethod)) {
            connection.setRequestMethod(mRequestMethod);
            if (!TextUtils.isEmpty(mRequestBody) || mRequestMethod.equalsIgnoreCase("POST") || mRequestMethod.equalsIgnoreCase("PUT")) {
                connection.setDoOutput(true);
            }
        }

        for (String name : mHeaders.keySet()) {
            connection.setRequestProperty(name, mHeaders.get(name));
        }

        if (!TextUtils.isEmpty(mRequestBody)) {
            OutputStream outputStream = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, DEFAULT_CHARSET));
            writer.write(mRequestBody);
            writer.flush();
            writer.close();
        }

        if (mMultipartEntity != null) {
            connection.setRequestProperty("Content-Length", String.valueOf(mMultipartEntity.getContentLength()));
            mMultipartEntity.writeTo(connection.getOutputStream());
        }

        return connection;
    }

    private static String getFormString(Map<String, String> params, String charset) throws UnsupportedEncodingException {
        List<String> protoList = new ArrayList<>();
        for (String key : params.keySet()) {
            String value = params.get(key);
            key = URLEncoder.encode(key, charset);
            value = URLEncoder.encode(value, charset);
            protoList.add(key + "=" + value);
        }
        return TextUtils.join("&", protoList);
    }

}
