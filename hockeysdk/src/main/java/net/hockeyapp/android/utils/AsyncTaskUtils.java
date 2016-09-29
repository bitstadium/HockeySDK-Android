package net.hockeyapp.android.utils;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

import java.util.concurrent.Executor;

/**
 * <h3>Description</h3>
 *
 * Either calls execute or executeOnExecutor on an AsyncTask depending on the
 * API level.
 */
public class AsyncTaskUtils {

    private static Executor sCustomExecutor;

    @SuppressLint("InlinedApi")
    public static void execute(AsyncTask<Void, ?, ?> asyncTask) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) {
            asyncTask.execute();
        } else {
            asyncTask.executeOnExecutor(sCustomExecutor != null ? sCustomExecutor : AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public static Executor getCustomExecutor() {
        return sCustomExecutor;
    }

    public static void setCustomExecutor(Executor customExecutor) {
        sCustomExecutor = customExecutor;
    }

}
