package io.kamax.mxisd.hash.rotation;

import io.kamax.mxisd.hash.HashEngine;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeBasedRotation implements HashRotationStrategy {

    private final long delay;
    private HashEngine hashEngine;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public TimeBasedRotation(long delay) {
        this.delay = delay;
    }

    @Override
    public void register(HashEngine hashEngine) {
        this.hashEngine = hashEngine;
        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));
        executorService.scheduleWithFixedDelay(this::trigger, 0, delay, TimeUnit.SECONDS);
    }

    @Override
    public HashEngine getHashEngine() {
        return hashEngine;
    }

    @Override
    public void newRequest() {
    }
}
