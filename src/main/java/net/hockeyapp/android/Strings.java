package net.hockeyapp.android;

import java.util.HashMap;
import java.util.Map;

/**
 * <h4>Description</h4>
 * 
 * Helper class to hold strings constants and defaults values.
 * 
 * <h4>License</h4>
 * 
 * <pre>
 * Copyright (c) 2011-2014 Bit Stadium GmbH
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
 * @author Patrick Eschenbach
 **/
public class Strings {
  ///////////////////////////////////////////////////
  // Crash Dialog
  ///////////////////////////////////////////////////

  /** Resource ID for the title of the dialog when a new crash was found. */
  public final static int CRASH_DIALOG_TITLE_ID           = 0x000;

  /** Resource ID for the message of the dialog when a new crash was found. */
  public final static int CRASH_DIALOG_MESSAGE_ID         = 0x001;

  /** Resource ID for the negative button label of the dialog when a new crash was found. */
  public final static int CRASH_DIALOG_NEGATIVE_BUTTON_ID = 0x002;

  /** Resource ID for the neutral button label of the dialog when a new crash was found. */
  public final static int CRASH_DIALOG_NEUTRAL_BUTTON_ID  = 0x003;

  /** Resource ID for the positive button label of the dialog when a new crash was found. */
  public final static int CRASH_DIALOG_POSITIVE_BUTTON_ID = 0x004;

  ///////////////////////////////////////////////////
  // Download Failed Dialog
  ///////////////////////////////////////////////////

  /** Resource ID for the title of the dialog when the apk download failed. */
  public final static int DOWNLOAD_FAILED_DIALOG_TITLE_ID           = 0x100;
  
  /** Resource ID for the message of the dialog when the apk download failed. */
  public final static int DOWNLOAD_FAILED_DIALOG_MESSAGE_ID         = 0x101;

  /** Resource ID for the label on the negative button of the dialog when the apk download failed. */
  public final static int DOWNLOAD_FAILED_DIALOG_NEGATIVE_BUTTON_ID = 0x102;

  /** Resource ID for the label on the positive button of the dialog when the apk download failed. */
  public final static int DOWNLOAD_FAILED_DIALOG_POSITIVE_BUTTON_ID = 0x103;

  ///////////////////////////////////////////////////
  // Update
  ///////////////////////////////////////////////////

  /** Resource ID for the text of the toast when an update is mandatory. */
  public final static int UPDATE_MANDATORY_TOAST_ID        = 0x200;
      
  /** Resource ID for the title of the dialog when a new update was found. */
  public final static int UPDATE_DIALOG_TITLE_ID           = 0x201;
  
  /** Resource ID for the message of the dialog when a new update was found. */
  public final static int UPDATE_DIALOG_MESSAGE_ID         = 0x202;
  
  /** Resource ID for the label on the negative button of the dialog when a new update was found. */
  public final static int UPDATE_DIALOG_NEGATIVE_BUTTON_ID = 0x203;
  
  /** Resource ID for the label on the positive button of the dialog when a new update was found. */
  public final static int UPDATE_DIALOG_POSITIVE_BUTTON_ID = 0x204;

  ///////////////////////////////////////////////////
  // Expiry Info
  ///////////////////////////////////////////////////
  
  /** Resource ID for the title of the expiry info view. */
  public final static int EXPIRY_INFO_TITLE_ID = 0x300;
  
  /** Resource ID for the text of the expiry info view. */
  public final static int EXPIRY_INFO_TEXT_ID  = 0x301;

  ///////////////////////////////////////////////////
  // Feedback Activity
  ///////////////////////////////////////////////////

  /** Resource ID for the title of the Feedback Failed info view. */
  public final static int FEEDBACK_FAILED_TITLE_ID           = 0x400;

  /** Resource ID for the text of the Feedback Failed info view. */
  public final static int FEEDBACK_FAILED_TEXT_ID            = 0x401;

  /** Resource ID for the FeedbackView name input hint text. */
  public final static int FEEDBACK_NAME_INPUT_HINT_ID        = 0x402;

  /** Resource ID for the FeedbackView email input hint text. */
  public final static int FEEDBACK_EMAIL_INPUT_HINT_ID       = 0x403;

  /** Resource ID for the FeedbackView subject input hint text. */
  public final static int FEEDBACK_SUBJECT_INPUT_HINT_ID     = 0x404;

  /** Resource ID for the FeedbackView message input hint text. */
  public final static int FEEDBACK_MESSAGE_INPUT_HINT_ID     = 0x405;

  /** Resource ID for the FeedbackView last updated text. */
  public final static int FEEDBACK_LAST_UPDATED_TEXT_ID      = 0x406;

  /** Resource ID for the FeedbackView attachment button text. */
  public final static int FEEDBACK_ATTACHMENT_BUTTON_TEXT_ID = 0x407;

  /** Resource ID for the FeedbackView send button text. */
  public final static int FEEDBACK_SEND_BUTTON_TEXT_ID       = 0x408;

  /** Resource ID for the FeedbackView response button text. */
  public final static int FEEDBACK_RESPONSE_BUTTON_TEXT_ID   = 0x409;

  /** Resource ID for the FeedbackView refresh button text. */
  public final static int FEEDBACK_REFRESH_BUTTON_TEXT_ID    = 0x40a;

  /** Resource ID for the title of the FeedbackActivity. */
  public final static int FEEDBACK_TITLE_ID                  = 0x40b;

  /** Resource ID for the message when feedback could not be sent (generic). */
  public final static int FEEDBACK_SEND_GENERIC_ERROR_ID     = 0x40c;

  /** Resource ID for the message when feedback could not be sent (network error). */
  public final static int FEEDBACK_SEND_NETWORK_ERROR_ID     = 0x40d;

  /** Resource ID for the message when not all input fields all filled out. */
  public final static int FEEDBACK_VALIDATE_INPUT_ERROR_ID   = 0x40e;

  /** Resource ID for the message when the entered email is invalid. */
  public final static int FEEDBACK_VALIDATE_EMAIL_ERROR_ID   = 0x40f;
  
  /** Resource ID for the message when a generic error has occurred. */
  public final static int FEEDBACK_GENERIC_ERROR_ID          = 0x410;
  

  ///////////////////////////////////////////////////
  // Login Activity
  ///////////////////////////////////////////////////

  /** Resource ID for the LoginView headline. */
  public final static int LOGIN_HEADLINE_TEXT_ID             = 0x500;

  /** Resource ID for the LoginView missing credentials toast. */
  public final static int LOGIN_MISSING_CREDENTIALS_TOAST_ID = 0x501;

  /** Resource ID for the LoginView email input hint text. */
  public final static int LOGIN_EMAIL_INPUT_HINT_ID          = 0x502;

  /** Resource ID for the LoginView password input hint text. */
  public final static int LOGIN_PASSWORD_INPUT_HINT_ID       = 0x503;

  /** Resource ID for the LoginView login button text. */
  public final static int LOGIN_LOGIN_BUTTON_TEXT_ID         = 0x504;

  ///////////////////////////////////////////////////
  // Paint Activity
  ///////////////////////////////////////////////////

  /** Resource ID for the PaintActivity indicator toast. */
  public final static int PAINT_INDICATOR_TOAST_ID        = 0x600;

  /** Resource ID for the PaintActivity menu save button. */
  public final static int PAINT_MENU_SAVE_ID              = 0x601;

  /** Resource ID for the PaintActivity menu undo button. */
  public final static int PAINT_MENU_UNDO_ID              = 0x602;

  /** Resource ID for the PaintActivity menu clear button. */
  public final static int PAINT_MENU_CLEAR_ID             = 0x603;

  /** Resource ID for the dialog message when activity is exited with changes. */
  public final static int PAINT_DIALOG_MESSAGE_ID         = 0x604;

  /** Resource ID for the negative button label of the dialog when activity is exited with changes. */
  public final static int PAINT_DIALOG_NEGATIVE_BUTTON_ID = 0x605;

  /** Resource ID for the positive button label of the dialog when activity is exited with changes. */
  public final static int PAINT_DIALOG_POSITIVE_BUTTON_ID = 0x606;

  /**
   * Default strings.
   */
  private final static Map<Integer, String> DEFAULT = new HashMap<Integer, String>();
  static {
    // Crash Dialog
    DEFAULT.put(CRASH_DIALOG_TITLE_ID,           "Crash Data");
    DEFAULT.put(CRASH_DIALOG_MESSAGE_ID,         "The app found information about previous crashes. Would you like to send this data to the developer?");
    DEFAULT.put(CRASH_DIALOG_NEGATIVE_BUTTON_ID, "Dismiss");
    DEFAULT.put(CRASH_DIALOG_NEUTRAL_BUTTON_ID,  "Always send");
    DEFAULT.put(CRASH_DIALOG_POSITIVE_BUTTON_ID, "Send");

    // Download Failed
    DEFAULT.put(DOWNLOAD_FAILED_DIALOG_TITLE_ID,           "Download Failed");
    DEFAULT.put(DOWNLOAD_FAILED_DIALOG_MESSAGE_ID,         "The update could not be downloaded. Would you like to try again?");
    DEFAULT.put(DOWNLOAD_FAILED_DIALOG_NEGATIVE_BUTTON_ID, "Cancel");
    DEFAULT.put(DOWNLOAD_FAILED_DIALOG_POSITIVE_BUTTON_ID, "Retry");

    // Update
    DEFAULT.put(UPDATE_MANDATORY_TOAST_ID,        "Please install the latest version to continue to use this app.");
    DEFAULT.put(UPDATE_DIALOG_TITLE_ID,           "Update Available");
    DEFAULT.put(UPDATE_DIALOG_MESSAGE_ID,         "Show information about the new update?");
    DEFAULT.put(UPDATE_DIALOG_NEGATIVE_BUTTON_ID, "Dismiss");
    DEFAULT.put(UPDATE_DIALOG_POSITIVE_BUTTON_ID, "Show");

    // Expiry Info
    DEFAULT.put(EXPIRY_INFO_TITLE_ID, "Build Expired");
    DEFAULT.put(EXPIRY_INFO_TEXT_ID,  "This has build has expired. Please check HockeyApp for any updates.");

    // Feedback Activity
    DEFAULT.put(FEEDBACK_FAILED_TITLE_ID,           "Feedback Failed");
    DEFAULT.put(FEEDBACK_FAILED_TEXT_ID,            "Would you like to send your feedback again?");
    DEFAULT.put(FEEDBACK_NAME_INPUT_HINT_ID,        "Name");
    DEFAULT.put(FEEDBACK_EMAIL_INPUT_HINT_ID,       "Email");
    DEFAULT.put(FEEDBACK_SUBJECT_INPUT_HINT_ID,     "Subject");
    DEFAULT.put(FEEDBACK_MESSAGE_INPUT_HINT_ID,     "Message");
    DEFAULT.put(FEEDBACK_LAST_UPDATED_TEXT_ID,      "Last Updated: ");
    DEFAULT.put(FEEDBACK_ATTACHMENT_BUTTON_TEXT_ID, "Add Attachment");
    DEFAULT.put(FEEDBACK_SEND_BUTTON_TEXT_ID,       "Send Feedback");
    DEFAULT.put(FEEDBACK_RESPONSE_BUTTON_TEXT_ID,   "Add a Response");
    DEFAULT.put(FEEDBACK_REFRESH_BUTTON_TEXT_ID,    "Refresh");
    DEFAULT.put(FEEDBACK_TITLE_ID,                  "Feedback");
    DEFAULT.put(FEEDBACK_SEND_GENERIC_ERROR_ID,     "Message couldn't be posted. Please check your input values and your connection, then try again.");
    DEFAULT.put(FEEDBACK_SEND_NETWORK_ERROR_ID,     "No response from server. Please check your connection, then try again.");
    DEFAULT.put(FEEDBACK_VALIDATE_INPUT_ERROR_ID,   "Message couldn't be posted. Please fill-out all input fields.");
    DEFAULT.put(FEEDBACK_VALIDATE_EMAIL_ERROR_ID,   "Message couldn't be posted. Please check the format of your email address.");
    DEFAULT.put(FEEDBACK_GENERIC_ERROR_ID,          "An error has occured");

    // Login Activity
    DEFAULT.put(LOGIN_HEADLINE_TEXT_ID,             "Please enter your account credentials.");
    DEFAULT.put(LOGIN_MISSING_CREDENTIALS_TOAST_ID, "Please fill in the missing account credentials.");
    DEFAULT.put(LOGIN_EMAIL_INPUT_HINT_ID,          "Email");
    DEFAULT.put(LOGIN_PASSWORD_INPUT_HINT_ID,       "Password");
    DEFAULT.put(LOGIN_LOGIN_BUTTON_TEXT_ID,         "Login");

    // Paint Activity
    DEFAULT.put(PAINT_INDICATOR_TOAST_ID,        "Draw something!");
    DEFAULT.put(PAINT_MENU_SAVE_ID,              "Save");
    DEFAULT.put(PAINT_MENU_UNDO_ID,              "Undo");
    DEFAULT.put(PAINT_MENU_CLEAR_ID,             "Clear");
    DEFAULT.put(PAINT_DIALOG_MESSAGE_ID,         "Discard your drawings?");
    DEFAULT.put(PAINT_DIALOG_NEGATIVE_BUTTON_ID, "No");
    DEFAULT.put(PAINT_DIALOG_POSITIVE_BUTTON_ID, "Yes");
  }
  
  /**
   * Returns the default string for the given resource ID.
   * 
   * @param resourceID The ID of the string resource.
   * @return The default string or null if the resourceID doesn't exist.
   */
  public static String get(int resourceID) {
    return get(null, resourceID);
  }

  /**
   * Sets the default string for the given resource ID.
   *
   * @param resourceID The ID of the string resource.
   * @param string The new default string.
   */
  public static void set(int resourceID, String string) {
    if (string != null) {
      DEFAULT.put(resourceID, string);
    }
  }

  /**
   * Returns a string for the given resource ID. The method first runs the 
   * callback method from the listener (if specified). If this returns null, 
   * it then uses the default string.
   * 
   * @param listener An instance of StringListener.
   * @param resourceID The ID of the string resource.
   * @return The string or null if the resourceID doesn't exist.
   */
  public static String get(StringListener listener, int resourceID) {
    String result = null;
    
    if (listener != null) {
      result = listener.getStringForResource(resourceID);
    }
    
    if (result == null) {
      result = DEFAULT.get(resourceID);
    }
    
    return result;
  }
}
