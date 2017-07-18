package net.hockeyapp.android.util;

import java.util.concurrent.Executor;

public class DummyExecutor implements Executor {

    @SuppressWarnings("NullableProblems")
    @Override
    public void execute(Runnable command) {
        // do nothing
    }
}
