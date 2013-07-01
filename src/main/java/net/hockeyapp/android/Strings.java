package net.hockeyapp.android;

/**
 * <h4>Description</h4>
 * 
 * Helper class to hold strings constants and defaults values.
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
public class Strings {
  /**
   * Resource ID for the title of the dialog when a new crash was found.
   */
  public final static int CRASH_DIALOG_TITLE_ID                     = 0x00;

  /**
   * Resource ID for the message of the dialog when a new crash was found.
   */
  public final static int CRASH_DIALOG_MESSAGE_ID                   = 0x01;

  /**
   * Resource ID for the label on the negative button of the dialog when a new 
   * crash was found.
   */
  public final static int CRASH_DIALOG_NEGATIVE_BUTTON_ID           = 0x02;

  /**
   * Resource ID for the label on the positive button of the dialog when a new 
   * crash was found.
   */
  public final static int CRASH_DIALOG_POSITIVE_BUTTON_ID           = 0x03;

  /**
   * Resource ID for the title of the dialog when the apk download failed.
   */
  public final static int DOWNLOAD_FAILED_DIALOG_TITLE_ID           = 0x04;
  
  /**
   * Resource ID for the message of the dialog when the apk download failed.
   */
  public final static int DOWNLOAD_FAILED_DIALOG_MESSAGE_ID         = 0x05;

  /**
   * Resource ID for the label on the negative button of the dialog when the 
   * apk download failed.
   */
  public final static int DOWNLOAD_FAILED_DIALOG_NEGATIVE_BUTTON_ID = 0x06;

  /**
   * Resource ID for the label on the positive button of the dialog when the 
   * apk download failed.
   */
  public final static int DOWNLOAD_FAILED_DIALOG_POSITIVE_BUTTON_ID = 0x07;

  /**
   * Resource ID for the text of the toast when an update is mandatory.
   */
  public final static int UPDATE_MANDATORY_TOAST_ID                 = 0x08;
      
  /**
   * Resource ID for the title of the dialog when a new update was found.
   */
  public final static int UPDATE_DIALOG_TITLE_ID                    = 0x09;
  
  /**
   * Resource ID for the message of the dialog when a new update was found.
   */
  public final static int UPDATE_DIALOG_MESSAGE_ID                  = 0x0a;
  
  /**
   * Resource ID for the label on the negative button of the dialog when a new 
   * update was found.
   */
  public final static int UPDATE_DIALOG_NEGATIVE_BUTTON_ID          = 0x0b;
  
  /**
   * Resource ID for the label on the positive button of the dialog when a new 
   * update was found.
   */
  public final static int UPDATE_DIALOG_POSITIVE_BUTTON_ID          = 0x0c;
  
  /**
   * Resource ID for the title of the expiry info view.
   */
  public final static int EXPIRY_INFO_TITLE_ID                      = 0x0d;
  
  /**
   * Resource ID for the text of the expiry info view.
   */
  public final static int EXPIRY_INFO_TEXT_ID                       = 0x0e;

  /**
   * Resource ID for the title of the Feedback Failed info view.
   */
  public final static int FEEDBACK_FAILED_TITLE_ID                  = 0x0f;

  /**
   * Resource ID for the text of the Feedback Failed info view.
   */
  public final static int FEEDBACK_FAILED_TEXT_ID                   = 0x10;


  /**
   * Default strings.
   */
  public final static String[] ENGLISH = new String[] {
      "Crash Data",
      "The app found information about previous crashes. Would you like to send this data to the developer?",
      "Dismiss",
      "Send",
      
      "Download Failed",
      "The update could not be downloaded. Would you like to try again?",
      "Cancel",
      "Retry",
      
      "Please install the latest version to continue to use this app.",
      
      "Update Available",
      "Show information about the new update?",
      "Dismiss",
      "Show",
      
      "Build Expired",
      "This has build has expired. Please check HockeyApp for any updates.",
      
      "Feedback Failed",
      "Would you like to send your feedback again?"
  };
  
  /**
   * Returns the default string for the given resource ID.
   * 
   * @param resourceID The ID of the string resource.
   * @return The default string.
   */
  public static String get(int resourceID) {
    return get(null, resourceID);
  }

  /**
   * Returns a string for the given resource ID. The method first runs the 
   * callback method from the listener (if specified). If this returns null, 
   * it then uses the default string.
   * 
   * @param listener An instance of StringListener.
   * @param resourceID The ID of the string resource.
   * @return The string.
   */
  public static String get(StringListener listener, int resourceID) {
    String result = null;
    
    if (listener != null) {
      result = listener.getStringForResource(resourceID);
    }
    
    if ((result == null) && (resourceID >= 0) && (resourceID <= ENGLISH.length)) {
      result = ENGLISH[resourceID];
    }
    
    return result;
  }
}
