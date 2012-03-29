package net.hockeyapp.android;

public class Strings {
  public final static int CRASH_DIALOG_TITLE_ID                     = 0x00;
  public final static int CRASH_DIALOG_MESSAGE_ID                   = 0x01;
  public final static int CRASH_DIALOG_NEGATIVE_BUTTON_ID           = 0x02;
  public final static int CRASH_DIALOG_POSITIVE_BUTTON_ID           = 0x03;

  public final static int DOWNLOAD_FAILED_DIALOG_TITLE_ID           = 0x04;
  public final static int DOWNLOAD_FAILED_DIALOG_MESSAGE_ID         = 0x05;
  public final static int DOWNLOAD_FAILED_DIALOG_NEGATIVE_BUTTON_ID = 0x06;
  public final static int DOWNLOAD_FAILED_DIALOG_POSITIVE_BUTTON_ID = 0x07;

  public final static int UPDATE_MANDATORY_TOAST_ID                 = 0x08;
      
  public final static int UPDATE_DIALOG_TITLE_ID                    = 0x09;
  public final static int UPDATE_DIALOG_MESSAGE_ID                  = 0x0a;
  public final static int UPDATE_DIALOG_NEGATIVE_BUTTON_ID          = 0x0b;
  public final static int UPDATE_DIALOG_POSITIVE_BUTTON_ID          = 0x0c;

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
      "Show"
  };
  
  public static String get(int resourceID) {
    return get(null, resourceID);
  }
  
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
