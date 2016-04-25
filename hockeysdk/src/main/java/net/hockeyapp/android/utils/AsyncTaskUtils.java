package net.hockeyapp.android.utils;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

/**
 * <h3>Description</h3>
 *
 * Either calls execute or executeOnExecutor on an AsyncTask depending on the
 * API level.
 */
public class AsyncTaskUtils {

    @SuppressLint("InlinedApi")
    public static void execute(AsyncTask<Void, ?, ?> asyncTask) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) {
            asyncTask.execute();
        } else {
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
}
