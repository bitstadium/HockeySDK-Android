package net.hockeyapp.android.utils;

import android.content.Context;

import net.hockeyapp.android.SSLPins;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.thoughtcrime.ssl.pinning.PinningSSLSocketFactory;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

/**
 * <h3>Description</h3>
 *
 * {@link HttpClient} manager class
 *
 * <h3>License</h3>
 *
 * <pre>
 * Copyright (c) 2011-2014 Bit Stadium GmbH
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
 * @author Bogdan Nistor
 */
public class ConnectionManager {
  private HttpClient httpClient;
  private static ConnectionManager INSTANCE;

  /** Private constructor prevents instantiation from other classes */
  private ConnectionManager(Context context) {
      // Attempt to create a HttpClient with SSL Pinning
      // but fallback to a standard trust model if that fails
      // (which should never happen)
      if (!createHttpClientWithPinningPreference(context, true)) {
          createHttpClientWithPinningPreference(context, false);
      }
  }


    /**
     * Creates a {@link org.apache.http.client.HttpClient} optionally
     * with SSL pinning.
     *
     * Returns whether the operation was successful
     */
  private boolean createHttpClientWithPinningPreference(Context context, boolean usePinning) {
      /** Sets up parameters */
      try {
          HttpParams params = new BasicHttpParams();
          HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
          HttpProtocolParams.setContentCharset(params, "utf-8");
          params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
          params.setParameter(CoreProtocolPNames.USER_AGENT, "HockeySDK/Android");

          //registers schemes for both http and https
          SchemeRegistry registry = new SchemeRegistry();
          registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
          final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
          sslSocketFactory.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
          if (usePinning) {
              registry.register(new Scheme("https", new PinningSSLSocketFactory(context, SSLPins.PINS, 0), 443));
          } else {
              registry.register(new Scheme("https", sslSocketFactory, 443));
          }

          ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);
          httpClient = new DefaultHttpClient(manager, params);
          return true;
      } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
          e.printStackTrace();
      }
      return false;
  }

  public static ConnectionManager getInstance(Context context) {
    if (INSTANCE == null) {
        INSTANCE = new ConnectionManager(context);
    }
    return INSTANCE;
  }

  public HttpClient getHttpClient() {
    return httpClient;
  }
}
