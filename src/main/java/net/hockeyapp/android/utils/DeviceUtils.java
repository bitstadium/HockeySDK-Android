package net.hockeyapp.android.utils;

import net.hockeyapp.android.Constants;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * <h3>Description</h3>
 * 
 * Device helper class.
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
   * @param context the context to use
   * @return The version code as integer.
   */
  public int getCurrentVersionCode(Context context) {
    return Integer.parseInt(Constants.APP_VERSION);
  }

  /**
   * Returns the app's name.
   * 
   * @param context the context to use
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