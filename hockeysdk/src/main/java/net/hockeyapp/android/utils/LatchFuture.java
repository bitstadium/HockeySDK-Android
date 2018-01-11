package net.hockeyapp.android.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LatchFuture<T> implements Future<T> {
    private T mResult;
    private CountDownLatch mLatch = new CountDownLatch(1);

    public synchronized void complete(T result) throws IllegalStateException {
        if (isDone()) {
            throw new IllegalStateException();
        }
        mResult = result;
        mLatch.countDown();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return mLatch.getCount() == 0;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        mLatch.await();
        return mResult;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        mLatch.await(timeout, unit);
        return mResult;
    }
}
