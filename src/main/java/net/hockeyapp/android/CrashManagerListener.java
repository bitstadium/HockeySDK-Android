package net.hockeyapp.android;

public abstract class CrashManagerListener {
  /**
   * Return true to ignore the default exception handler, i.e. the user will not
   * get the alert dialog with the "Force Close" button.
   */
  public Boolean ignoreDefaultHandler() {
    return false;
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
   */
  public Boolean onCrashesFound() {
    return false;
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
}
