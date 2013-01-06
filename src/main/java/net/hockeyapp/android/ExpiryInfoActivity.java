package net.hockeyapp.android;

import net.hockeyapp.android.views.ExpiryInfoView;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * <h4>Description</h4>
 * 
 * The expiry activity is shown when the build is expired.
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
  
  /**
   * Returns the string for a given resource ID.
   * 
   * @param resourceID The string's resource ID.
   * @return Instance of String
   */
  protected String getStringResource(int resourceID) {
    UpdateManagerListener listener = UpdateManager.getLastListener();
    return Strings.get(listener, resourceID);
  }
}
