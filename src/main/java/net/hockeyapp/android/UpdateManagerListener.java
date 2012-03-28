package net.hockeyapp.android;

public abstract class UpdateManagerListener extends StringListener {
  /**
   * Return your own subclass of UpdateActivity for customization. 
   */
  public Class<? extends UpdateActivity> getUpdateActivityClass() {
    return UpdateActivity.class;
  }

  /**
   * Return your own subclass of UpdateFragment for customization. 
   */
  public Class<? extends UpdateFragment> getUpdateFragmentClass() {
    return UpdateFragment.class;
  }
  
  /**
   * Called when the update manager found no update. 
   */
  public void onNoUpdateAvailable() {
    // Do nothing
  }
  
  /**
   * Called when the update manager found an update. 
   */
  public void onUpdateAvailable() {
    // Do nothing
  }
}
  
