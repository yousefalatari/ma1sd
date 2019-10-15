package io.kamax.mxisd.hash;

import io.kamax.mxisd.config.HashingConfig;
import io.kamax.mxisd.lookup.provider.IThreePidProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HashManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(HashManager.class);

    private HashEngine hashEngine;
    private HashRotationStrategy rotationStrategy;
    private HashStorage hashStorage;
    private HashingConfig config;

    public void init(HashingConfig config, List<? extends IThreePidProvider> providers) {
        this.config = config;
        initStorage();
        hashEngine = new HashEngine(providers, getHashStorage(), config);
        initRotationStrategy();
    }

    private void initStorage() {
        switch (config.getHashStorageType()) {
            case IN_MEMORY:
                this.hashStorage = new InMemoryHashStorage();
                break;
            default:
                throw new IllegalArgumentException("Unknown storage type: " + config.getHashStorageType());
        }
    }

    private void initRotationStrategy() {
        switch (config.getRotationPolicy()) {
            case PER_REQUESTS:
                this.rotationStrategy = new RotationPerRequests();
                break;
            default:
                throw new IllegalArgumentException("Unknown rotation type: " + config.getHashStorageType());
        }
        this.rotationStrategy.register(getHashEngine());
    }

    public HashEngine getHashEngine() {
        return hashEngine;
    }

    public HashRotationStrategy getRotationStrategy() {
        return rotationStrategy;
    }

    public HashStorage getHashStorage() {
        return hashStorage;
    }

    public HashingConfig getConfig() {
        return config;
    }
}
