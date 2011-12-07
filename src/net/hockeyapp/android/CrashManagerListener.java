package net.hockeyapp.android;

public abstract class CrashManagerListener {
  public Boolean ignoreDefaultHandler() {
    return false;
  }
  
  public String getContact() {
    return null;
  }
  
  public String getDescription() {
    return null;
  }
  
  public String getUserID() {
    return null;
  }
  
  public Boolean onCrashesFound() {
    return false;
  }
  
  public void onCrashesSent() {
  }
  
  public void onCrashesNotSent() {
  }
}
