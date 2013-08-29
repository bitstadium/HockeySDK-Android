package net.hockeyapp.android.utils;

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

/**
 * {@link HttpClient} manager class
 * @author Bogdan Nistor
 *
 */
public class ConnectionManager {
  private HttpClient httpClient;
  
  /** Private constructor prevents instantiation from other classes */
  private ConnectionManager() {
    /** Sets up parameters */
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
    registry.register(new Scheme("https", sslSocketFactory, 443));
  
    ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);
    httpClient = new DefaultHttpClient(manager, params);
  }

  /**
  * ConnectionManagerHolder is loaded on the first execution of ConnectionManager.getInstance() 
  * or the first access to ConnectionManagerHolder.INSTANCE, not before.
  */
  private static class ConnectionManagerHolder { 
    public static final ConnectionManager INSTANCE = new ConnectionManager();
  }

  public static ConnectionManager getInstance() {
    return ConnectionManagerHolder.INSTANCE;
  }

  public HttpClient getHttpClient() {
    return httpClient;
  }
}
