package net.hockeyapp.android.utils;

import net.hockeyapp.android.Constants;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * Device helper class
 * @author Bogdan Nistor
 *
 */
public class DeviceUtils {
  /** Private constructor prevents instantiation from other classes */
  private DeviceUtils() { 
  }

  /**
   * DeviceUtilsHolder is loaded on the first execution of DeviceUtils.getInstance() 
   * or the first access to DeviceUtilsHolder.INSTANCE, not before.
   */
  private static class DeviceUtilsHolder { 
    public static final DeviceUtils INSTANCE = new DeviceUtils();
  }

  public static DeviceUtils getInstance() {
    return DeviceUtilsHolder.INSTANCE;
  }
  
  /**
   * Returns the current version of the app.
   * 
   * @return The version code as integer.
   */
  public int getCurrentVersionCode(Context context) {
    return Integer.parseInt(Constants.APP_VERSION);
  }

  /**
   * Returns the app's name.
   * 
   * @return The app's name as a String.
   */
  public String getAppName(Context context) {
    if (context == null) {
      return "";
    }
    
    try {
      PackageManager pm = context.getPackageManager();
      if (pm == null) {
        return "";  
      }
      
      ApplicationInfo applicationInfo = pm.getApplicationInfo(context.getPackageName(), 0);
      
      return pm.getApplicationLabel(applicationInfo).toString();
    } catch (NameNotFoundException e) {
      e.printStackTrace();
    }
    
    return "";
  }
}