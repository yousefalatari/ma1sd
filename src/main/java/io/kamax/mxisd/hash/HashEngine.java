package io.kamax.mxisd.hash;

import io.kamax.matrix.codec.MxSha256;
import io.kamax.mxisd.config.HashingConfig;
import io.kamax.mxisd.hash.storage.HashStorage;
import io.kamax.mxisd.lookup.ThreePidMapping;
import io.kamax.mxisd.lookup.provider.IThreePidProvider;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HashEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(HashEngine.class);

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
        LOGGER.info("Start update hashes.");
        synchronized (hashStorage) {
            this.pepper = newPepper();
            hashStorage.clear();
            for (IThreePidProvider provider : providers) {
                try {
                    for (ThreePidMapping pidMapping : provider.populateHashes()) {
                        hashStorage.add(pidMapping, hash(pidMapping));
                    }
                } catch (Exception e) {
                    LOGGER.error("Unable to update hashes of the provider: " + provider.toString(), e);
                }
            }
        }
        LOGGER.info("Finish update hashes.");
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
        return RandomStringUtils.random(config.getPepperLength(), true, true);
    }
}
