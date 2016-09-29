package net.hockeyapp.android.util;

import java.util.concurrent.Executor;

public class DummyExecutor implements Executor {

    @Override
    public void execute(Runnable command) {
        // do nothing
    }
}
