package net.hockeyapp.android.utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompletedFuture<T> implements Future<T> {
    private final T mResult;

    public CompletedFuture(final T result) {
        mResult = result;
    }

    @Override
    public boolean cancel(final boolean b) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return mResult;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public T get(final long l, final TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        return mResult;
    }
}
