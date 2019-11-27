package io.kamax.mxisd.hash.rotation;

import io.kamax.mxisd.hash.HashEngine;

import java.util.concurrent.atomic.AtomicInteger;

public class RotationPerRequests implements HashRotationStrategy {

    private HashEngine hashEngine;
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void register(HashEngine hashEngine) {
        this.hashEngine = hashEngine;
        trigger();
    }

    @Override
    public HashEngine getHashEngine() {
        return hashEngine;
    }

    @Override
    public synchronized void newRequest() {
        int newValue = counter.incrementAndGet();
        if (newValue >= 10) {
            counter.set(0);
            trigger();
        }
    }
}
