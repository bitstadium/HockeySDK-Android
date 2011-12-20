package net.hockeyapp.android;

public abstract class UpdateManagerListener {
  public Class<? extends UpdateActivity> getUpdateActivityClass() {
    return UpdateActivity.class;
  }

  public Class<? extends UpdateFragment> getUpdateFragmentClass() {
    return UpdateFragment.class;
  }
  
  public void onNoUpdateAvailable() {
    // Do nothing
  }
  
  public void onUpdateAvailable() {
    // Do nothing
  }
}
