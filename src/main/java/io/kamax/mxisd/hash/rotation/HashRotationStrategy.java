package io.kamax.mxisd.hash.rotation;

import io.kamax.mxisd.hash.HashEngine;

public interface HashRotationStrategy {

    void register(HashEngine hashEngine);

    HashEngine getHashEngine();

    void newRequest();

    default void trigger() {
        getHashEngine().updateHashes();
    }
}
