package net.hockeyapp.android;

import android.app.Activity;
import android.os.AsyncTask.Status;

public class UpdateManager {
  private static CheckUpdateTask updateTask = null;
  
  public static void register(Activity activity, String urlString, String appIdentifier, int iconDrawableId) {
    UpdateActivity.iconDrawableId = iconDrawableId;
    
    if ((updateTask == null) || (updateTask.getStatus() == Status.FINISHED)) {
      updateTask = new CheckUpdateTask(activity, urlString, appIdentifier);
      updateTask.execute();
    }
    else {
      updateTask.attach(activity);
    }
  }
}
