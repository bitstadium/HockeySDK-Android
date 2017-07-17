package net.hockeyapp.android.utils;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

/**
 * <h3>Description</h3>
 *
 * Either calls execute or executeOnExecutor on an AsyncTask depending on the
 * API level.
 */
public class AsyncTaskUtils {

    private static Executor sCustomExecutor;

    public static void execute(@NonNull AsyncTask<Void, ?, ?> asyncTask) {
        Executor executor = sCustomExecutor != null ? sCustomExecutor : AsyncTask.THREAD_POOL_EXECUTOR;
        asyncTask.executeOnExecutor(executor);
    }

    public static <T> FutureTask<T> execute(@NonNull Callable<T> callable) {
        Executor executor = sCustomExecutor != null ? sCustomExecutor : AsyncTask.THREAD_POOL_EXECUTOR;
        FutureTask<T> futureTask = new FutureTask<>(callable);
        executor.execute(futureTask);
        return futureTask;
    }

    public static Executor getCustomExecutor() {
        return sCustomExecutor;
    }

    public static void setCustomExecutor(Executor customExecutor) {
        sCustomExecutor = customExecutor;
    }

}
