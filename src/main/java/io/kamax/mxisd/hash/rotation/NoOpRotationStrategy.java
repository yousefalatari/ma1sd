package io.kamax.mxisd.hash.rotation;

import io.kamax.mxisd.hash.HashEngine;

public class NoOpRotationStrategy implements HashRotationStrategy {

    private HashEngine hashEngine;

    @Override
    public void register(HashEngine hashEngine) {
        this.hashEngine = hashEngine;
    }

    @Override
    public HashEngine getHashEngine() {
        return hashEngine;
    }

    @Override
    public void newRequest() {
        // nothing to do
    }
}
