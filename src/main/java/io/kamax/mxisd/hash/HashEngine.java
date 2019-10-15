package io.kamax.mxisd.hash;

import io.kamax.matrix.codec.MxSha256;
import io.kamax.mxisd.config.HashingConfig;
import io.kamax.mxisd.lookup.ThreePidMapping;
import io.kamax.mxisd.lookup.provider.IThreePidProvider;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;

public class HashEngine {

    private final List<? extends IThreePidProvider> providers;
    private final HashStorage hashStorage;
    private final MxSha256 sha256 = new MxSha256();
    private final HashingConfig config;
    private String pepper;

    public HashEngine(List<? extends IThreePidProvider> providers, HashStorage hashStorage, HashingConfig config) {
        this.providers = providers;
        this.hashStorage = hashStorage;
        this.config = config;
    }

    public void updateHashes() {
        synchronized (hashStorage) {
            this.pepper = newPepper();
            hashStorage.clear();
            for (IThreePidProvider provider : providers) {
                for (ThreePidMapping pidMapping : provider.populateHashes()) {
                    hashStorage.add(pidMapping, hash(pidMapping));
                }
            }
        }
    }

    public String getPepper() {
        synchronized (hashStorage) {
            return pepper;
        }
    }

    protected String hash(ThreePidMapping pidMapping) {
        return sha256.hash(pidMapping.getMedium() + " " + pidMapping.getValue() + " " + getPepper());
    }

    protected String newPepper() {
        return RandomStringUtils.random(config.getPepperLength());
    }
}
