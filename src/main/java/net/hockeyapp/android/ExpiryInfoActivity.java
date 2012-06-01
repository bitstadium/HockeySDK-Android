package net.hockeyapp.android;

import net.hockeyapp.android.internal.ExpiryInfoView;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class ExpiryInfoActivity extends Activity {
  /**
   * Called when the activity is starting. Sets the title and content view.
   * Configures the list view adapter. Attaches itself to a previously 
   * started download task.
   * 
   * @param savedInstanceState Data it most recently supplied in 
   *                           onSaveInstanceState(Bundle)
   */
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setTitle(getStringResource(Strings.EXPIRY_INFO_TITLE_ID));
    setContentView(getLayoutView());
  }

  /**
   * Creates and returns a new instance of UpdateView.
   * 
   * @return Instance of UpdateView
   */
  protected View getLayoutView() {
    return new ExpiryInfoView(this, getStringResource(Strings.EXPIRY_INFO_TEXT_ID));
  }
  
  protected String getStringResource(int resourceID) {
    UpdateManagerListener listener = UpdateManager.getLastListener();
    return Strings.get(listener, resourceID);
  }
}
