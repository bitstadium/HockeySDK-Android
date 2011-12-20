package net.hockeyapp.android;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.AsyncTask.Status;

public class UpdateManager {
  private static CheckUpdateTask updateTask = null;
  
  public static void register(Activity activity, String appIdentifier, UpdateManagerListener listener) {
    if ((fragmentsSupported()) && (dialogShown(activity))) {
      return;
    }
    
    if ((updateTask == null) || (updateTask.getStatus() == Status.FINISHED)) {
      updateTask = new CheckUpdateTask(activity, "https://rink.hockeyapp.net/", appIdentifier, listener);
      updateTask.execute();
    }
    else {
      updateTask.attach(activity);
    }
  }

  public static void register(Activity activity, String appIdentifier) {
    register(activity, appIdentifier, null);
  }

  private static boolean dialogShown(Activity activity) {
    Fragment existingFragment = activity.getFragmentManager().findFragmentByTag("hockey_update_dialog");
    return (existingFragment != null);
  }

  public static Boolean fragmentsSupported() {
    try {
      return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) && (android.app.Fragment.class != null);
    }
    catch (NoClassDefFoundError e) {
      return false;
    }
  }

  public static Boolean runsOnTablet(Activity activity) {
    Configuration configuration = activity.getResources().getConfiguration();
    return (((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) || ((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE));
  }
}
