package io.kamax.mxisd.hash.storage;

import io.kamax.mxisd.lookup.ThreePidMapping;

import java.util.Collections;

public class EmptyStorage implements HashStorage {

    @Override
    public Iterable<ThreePidMapping> find(Iterable<String> hashes) {
        return Collections.emptyList();
    }

    @Override
    public void add(ThreePidMapping pidMapping, String hash) {

    }

    @Override
    public void clear() {

    }
}
