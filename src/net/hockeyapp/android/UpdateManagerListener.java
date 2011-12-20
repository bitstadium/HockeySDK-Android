package net.hockeyapp.android;

public abstract class UpdateManagerListener {
  public Class<?> getUpdateActivityClass() {
    return UpdateActivity.class;
  }
  
  public void onNoUpdateAvailable() {
    // Do nothing
  }
  
  public void onUpdateAvailable() {
    // Do nothing
  }
}
