package io.kamax.mxisd.hash;

import io.kamax.matrix.ThreePid;
import io.kamax.mxisd.lookup.ThreePidMapping;

public interface HashStorage {

    Iterable<ThreePidMapping> find(Iterable<String> hashes);

    void add(ThreePidMapping pidMapping, String hash);

    void clear();
}
