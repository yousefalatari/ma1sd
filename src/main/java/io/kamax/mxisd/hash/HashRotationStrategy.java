package io.kamax.mxisd.hash;

public interface HashRotationStrategy {

    void register(HashEngine hashEngine);

    HashEngine getHashEngine();

    void newRequest();

    default void trigger() {
        getHashEngine().updateHashes();
    }
}
