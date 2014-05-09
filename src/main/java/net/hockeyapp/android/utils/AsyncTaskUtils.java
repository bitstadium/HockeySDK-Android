package net.hockeyapp.android.utils;

import android.os.AsyncTask;
import android.os.Build;

/**
 * Either calls execute or executeOnExecutor on an AsyncTask depending on the
 * API level.
 *
 * @author Patrick Eschenbach
 */
public class AsyncTaskUtils {

  public static void execute(AsyncTask<Void, ?, ?> asyncTask) {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) {
      asyncTask.execute();
    } else {
      asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
  }
}
