package com.talhanation.recruits.util;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class DelayedExecutor {
    private static final ThreadFactory threadFactory = runnable -> {
        Thread thread = new Thread(runnable, "Recruits-DelayedExecutor");
        thread.setDaemon(true);
        return thread;
    };
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(threadFactory);

    public static void runLater(Runnable task, long delayMillis) {
        try {
            scheduler.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException ignored) {
            // server is stopping
        }
    }

    public static void shutdown() {
        scheduler.shutdownNow();
    }
}
