package net.hockeyapp.android.util;

import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

public class DummyExecutor implements Executor {

    @SuppressWarnings("NullableProblems")
    @Override
    public void execute(@NonNull Runnable command) {
        // do nothing
    }
}
