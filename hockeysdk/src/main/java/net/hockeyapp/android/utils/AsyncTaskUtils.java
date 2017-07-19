package net.hockeyapp.android.utils;

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

    public static void execute(AsyncTask<Void, ?, ?> asyncTask) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            asyncTask.executeOnExecutor(sCustomExecutor != null ? sCustomExecutor : AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            asyncTask.execute();
        }
    }

    public static Executor getCustomExecutor() {
        return sCustomExecutor;
    }

    public static void setCustomExecutor(Executor customExecutor) {
        sCustomExecutor = customExecutor;
    }

}
