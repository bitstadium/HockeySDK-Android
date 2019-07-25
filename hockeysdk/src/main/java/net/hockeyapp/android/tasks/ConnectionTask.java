package net.hockeyapp.android.tasks;

import android.os.AsyncTask;

import net.hockeyapp.android.utils.Util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.net.ssl.HttpsURLConnection;

/**
 * <h3>Description</h3>
 *
 * Base class for asynchronous HTTP connections.
 *
 **/
public abstract class ConnectionTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected static String getStringFromConnection(HttpsURLConnection connection) throws IOException {
        InputStream inputStream = new BufferedInputStream(connection.getInputStream());
        return Util.convertStreamToString(inputStream);
    }
}
