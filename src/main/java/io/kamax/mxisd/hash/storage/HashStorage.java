package io.kamax.mxisd.hash.storage;

import io.kamax.mxisd.lookup.ThreePidMapping;

public interface HashStorage {

    Iterable<ThreePidMapping> find(Iterable<String> hashes);

    void add(ThreePidMapping pidMapping, String hash);

    void clear();
}
