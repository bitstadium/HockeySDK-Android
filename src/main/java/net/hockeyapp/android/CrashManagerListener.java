package net.hockeyapp.android;

/**
 * <h4>Description</h4>
 * 
 * Abstract class for callbacks to be invoked from the CrashManager. 
 * 
 * <h4>License</h4>
 * 
 * <pre>
 * Copyright (c) 2011-2013 Bit Stadium GmbH
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
 * @author Thomas Dohmke
 **/
public abstract class CrashManagerListener extends StringListener {
  /**
   * Return true to ignore the default exception handler, i.e. the user will not
   * get the alert dialog with the "Force Close" button.
   */
  public boolean ignoreDefaultHandler() {
    return false;
  }

  /**
   * Return false to remove the device data (OS version, manufacturer, model)
   * from the crash log, e.g. if some of your testers are using unreleased
   * devices.
   */
  public boolean includeDeviceData() {
    return true;
  }

  /**
   * Return contact data or similar; note that this has privacy implications,
   * so you might want to return nil for release builds! The string will be
   * limited to 255 characters.
   */
  public String getContact() {
    return null;
  }
  
  /**
   * Return additional data, i.e. parts of the system log, the last server
   * response or similar. This string is not limited to a certain size.
   */
  public String getDescription() {
    return null;
  }
  
  /**
   * Return a user ID or similar; note that this has privacy implications,
   * so you might want to return nil for release builds! The string will be
   * limited to 255 characters.
   */
  public String getUserID() {
    return null;
  }
  
  /**
   * Called when the crash manager found one or more crashes. Return true 
   * if you want to auto-send crashes (i.e. not ask the user)
   * 
   * @deprecated Replace this method with onNewCrashesFound, 
   *             onConfirmedCrashesFound, and shouldAutoUploadCrashReport.
   */
  public boolean onCrashesFound() {
    return false;
  }
  
  /**
   * Return true if you want to auto-send crashes. Note that this method
   * is only called if new crashes were found. 
   */
  public boolean shouldAutoUploadCrashes() {
    return false;
  }

  /**
   * Called when the crash manager has found new crash logs. 
   */
  public void onNewCrashesFound() {
  }

  /**
   * Called when the crash manager has found crash logs that were already
   * confirmed by the user or should have been auto uploaded, but the upload
   * failed, e.g. in case of a network failure. 
   */
  public void onConfirmedCrashesFound() {
  }

  /**
   * Called when the crash manager has sent crashes to HockeyApp. 
   */
  public void onCrashesSent() {
  }
  
  /**
   * Called when the crash manager failed to send crashes to HockeyApp, e.g.
   * because the device has no network connections.
   */
  public void onCrashesNotSent() {
  }
  
  /**
   * Called when the user denied to send crashes to HockeyApp.
   */
  public void onUserDeniedCrashes() {
  }
}
