package net.hockeyapp.android;

import org.json.JSONArray;

import java.util.Date;

/**
 * <h4>Description</h4>
 * 
 * Abstract class for callbacks to be invoked from the UpdateManager. 
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

  /**
   * Called when the update manager found an update.
   * @param data Information about the update.
   * @param url Link to apk file update.
   */
  public void onUpdateAvailable(JSONArray data, String url){
    onUpdateAvailable();
  }
  
  /**
   * Return an expiry date for this build or null. After this date the
   * build will be blocked by a dialog.
   */
  public Date getExpiryDate() {
    return null;
  }
  
  /**
   * Called when the build is expired. Return false to if you handle
   * the expiry in your code.
   */
  public boolean onBuildExpired() {
    return true;
  }
}
  
