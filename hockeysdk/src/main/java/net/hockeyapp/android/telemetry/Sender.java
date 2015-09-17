package net.hockeyapp.android.telemetry;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import net.hockeyapp.android.utils.AsyncTaskUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

/**
 * <h3>Description</h3>
 * <p/>
 * Either calls execute or executeOnExecutor on an AsyncTask depending on the
 * API level.
 * <p/>
 * <h3>License</h3>
 * <p/>
 * <pre>
 * Copyright (c) 2011-2015 Bit Stadium GmbH
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * </pre>
 *
 * @author Benjamin Reimold
 */
public class Sender {

    static final String DEFAULT_ENDPOINT_URL = "https://dc.services.visualstudio.com/v2/track";
    static final int DEFAULT_SENDER_READ_TIMEOUT = 10 * 1000;
    static final int DEFAULT_SENDER_CONNECT_TIMEOUT = 15 * 1000;
    private static final String TAG = "Sender";
    private final int MAX_REQUEST_COUNT = 10;
    /**
     * Persistence object used to reserve, free, or delete files.
     */
    protected Persistence persistence;
    /**
     * Thread safe counter to keep track of num of operations
     */
    private AtomicInteger requestCount;

    /**
     * Create a Sender instance
     *
     * @param persistence Persistence object used to reserve, free or delete files
     */
    protected Sender(Persistence persistence) {
        this.requestCount = new AtomicInteger(0);
        this.persistence = persistence;
    }

    protected void triggerSending() {
        //as sendNextFile() is NOT guarranteed to be executed from a background thread, we need to
        //create an async task if necessary
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.d(TAG, "Kick of new async task");
            AsyncTaskUtils.execute(createAsyncTask());//TODO will this happen for HA SDK?
        } else {
            if (requestCount() < MAX_REQUEST_COUNT) {
                this.requestCount.getAndIncrement();
                // Send the persisted data
                send();
            } else {
                Log.d(TAG, "We have already 10 pending reguests");
            }
        }
    }

    private AsyncTask<Void, Void, Void> createAsyncTask() {
        return new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                triggerSending();
                return null;
            }
        };
    }

    protected void send() {
        if (this.persistence != null) {
            File fileToSend = this.persistence.nextTelemetryFile();
            if (fileToSend != null) {
                String persistedData = this.persistence.load(fileToSend);
                if (!persistedData.isEmpty()) {
                    HttpURLConnection connection = createConnection();
                    if (connection != null) {
                        try {
                            logRequest(connection, persistedData);
                            // Starts the query
                            connection.connect();
                            // read the response code while we're ready to catch the IO exception
                            int responseCode = connection.getResponseCode();
                            // process the response
                            onResponse(connection, responseCode, persistedData, fileToSend);
                        } catch (IOException e) {
                            Log.d(TAG, "Couldn't send data with IOException: " + e.toString());
                            if (this.persistence != null) {
                                Log.d(TAG, "Persisting because of IOException: We're probably offline =)");
                                this.persistence.makeAvailable(fileToSend); //send again later
                            }
                        }
                    }
                } else {
                    this.persistence.deleteFile(fileToSend);
                }
            }
        }
    }

    private HttpURLConnection createConnection() {
        URL url;
        HttpURLConnection connection = null;
        try {
            url = new URL(DEFAULT_ENDPOINT_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(DEFAULT_SENDER_READ_TIMEOUT);
            connection.setConnectTimeout(DEFAULT_SENDER_CONNECT_TIMEOUT);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-json-stream");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
        } catch (IOException e) {
            Log.e(TAG, "Could not open connection for provided URL with exception: ", e);
        }
        return connection;
    }

    private void logRequest(HttpURLConnection connection, String payload) {
        Writer writer = null;
        try {
            Log.d(TAG, "Logging payload:\n" + payload);
            writer = getWriter(connection);
            writer.write(payload);
            writer.flush();
        } catch (IOException e) {
            Log.d(TAG, "Couldn't log data with: " + e.toString());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.d(TAG, "Couldn't close writer with: " + e.toString());
                }
            }
        }
    }

    /**
     * Callback for the http response from the sender
     *
     * @param connection   a connection containing a response
     * @param responseCode the response code from the connection
     * @param payload      the payload which generated this response
     * @param fileToSend   reference to the file we want to send
     */
    protected void onResponse(HttpURLConnection connection, int responseCode, String payload, File fileToSend) {
        this.requestCount.getAndDecrement();//TODO do sanity check – was done in sendNextFile()
        // in AI SDK!

        Log.d(TAG, "response code " + Integer.toString(responseCode));

        boolean isRecoverableError = isRecoverableError(responseCode);
        if (isRecoverableError) {
            Log.d(TAG, "Recoverable error (probably a server error), persisting data:\n" + payload);
            if (this.persistence != null) {
                this.persistence.makeAvailable(fileToSend);
            }
        } else {
            //delete in case of success or unrecoverable errors
            if (this.persistence != null) {
                this.persistence.deleteFile(fileToSend);
            }

            //trigger send next file or log unexpected responses
            StringBuilder builder = new StringBuilder();
            if (isExpected(responseCode)) {
                this.onExpected(connection, builder);
                triggerSending();
            } else {
                this.onUnexpected(connection, responseCode, builder);
            }
        }
    }

    protected boolean isRecoverableError(int responseCode) {
        List<Integer> recoverableCodes = Arrays.asList(408, 429, 500, 503, 511);
        return recoverableCodes.contains(responseCode);
    }

    protected boolean isExpected(int responseCode) {
        return (199 < responseCode && responseCode <= 203);
    }

    /**
     * Process the expected response. If {code:TelemetryChannelConfig.isDeveloperMode}, read the
     * response and log it.
     *
     * @param connection a connection containing a response
     * @param builder    a string builder for storing the response
     */
    protected void onExpected(HttpURLConnection connection, StringBuilder builder) {
        this.readResponse(connection, builder);
    }

    /**
     * @param connection   a connection containing a response
     * @param responseCode the response code from the connection
     * @param builder      a string builder for storing the response
     */
    protected void onUnexpected(HttpURLConnection connection, int responseCode, StringBuilder builder) {
        String message = String.format(Locale.ROOT, "Unexpected response code: %d", responseCode);
        builder.append(message);
        builder.append("\n");

        // log the unexpected response
        Log.d(TAG, message);

        // attempt to read the response stream
        this.readResponse(connection, builder);
    }

    /**
     * Reads the response from a connection.
     *
     * @param connection the connection which will read the response
     * @param builder    a string builder for storing the response
     */
    protected void readResponse(HttpURLConnection connection, StringBuilder builder) {
        BufferedReader reader = null;
        try {
            InputStream inputStream = connection.getErrorStream();
            if (inputStream == null) {
                inputStream = connection.getInputStream();
            }

            if (inputStream != null) {
                InputStreamReader streamReader = new InputStreamReader(inputStream, "UTF-8");
                reader = new BufferedReader(streamReader);
                String responseLine = reader.readLine();
                while (responseLine != null) {
                    builder.append(responseLine);
                    responseLine = reader.readLine();
                }
            } else {
                builder.append(connection.getResponseMessage());
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
            }
        }
    }

    /**
     * Gets a writer from the connection stream (allows for test hooks into the write stream)
     *
     * @param connection the connection to which the stream will be flushed
     * @return a writer for the given connection stream
     * @throws java.io.IOException Exception thrown by GZIP (used in SDK 19+)
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected Writer getWriter(HttpURLConnection connection) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // GZIP if we are running SDK 19 or higher
            connection.addRequestProperty("Content-Encoding", "gzip");
            connection.setRequestProperty("Content-Type", "application/x-json-stream");
            GZIPOutputStream gzip = new GZIPOutputStream(connection.getOutputStream(), true);
            return new OutputStreamWriter(gzip);
        } else {
            // no GZIP for older devices
            return new OutputStreamWriter(connection.getOutputStream());
        }
    }

    /**
     * Set persistence used to reserve, free, or delete files (enables dependency injection).
     *
     * @param persistence a persistence used to reserve, free, or delete files
     */
    protected void setPersistence(Persistence persistence) {
        this.persistence = persistence;
    }

    protected int requestCount() {
        return this.requestCount.get();
    }
}
