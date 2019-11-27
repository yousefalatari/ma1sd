package io.kamax.mxisd.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class HashingConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(HashingConfig.class);

    private boolean enabled = false;
    private int pepperLength = 20;
    private RotationPolicyEnum rotationPolicy;
    private HashStorageEnum hashStorageType;
    private long delay = 10;
    private List<Algorithm> algorithms = new ArrayList<>();

    public void build() {
        if (isEnabled()) {
            LOGGER.info("--- Hash configuration ---");
            LOGGER.info("   Pepper length: {}", getPepperLength());
            LOGGER.info("   Rotation policy: {}", getRotationPolicy());
            LOGGER.info("   Hash storage type: {}", getHashStorageType());
            if (RotationPolicyEnum.per_seconds == rotationPolicy) {
                LOGGER.info("   Rotation delay: {}", delay);
            }
            LOGGER.info("   Algorithms: {}", algorithms);
        } else {
            LOGGER.info("Hash configuration disabled, used only `none` pepper.");
        }
    }

    public enum Algorithm {
        none,
        sha256
    }

    public enum RotationPolicyEnum {
        per_requests,
        per_seconds
    }

    public enum HashStorageEnum {
        in_memory,
        sql
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPepperLength() {
        return pepperLength;
    }

    public void setPepperLength(int pepperLength) {
        this.pepperLength = pepperLength;
    }

    public RotationPolicyEnum getRotationPolicy() {
        return rotationPolicy;
    }

    public void setRotationPolicy(RotationPolicyEnum rotationPolicy) {
        this.rotationPolicy = rotationPolicy;
    }

    public HashStorageEnum getHashStorageType() {
        return hashStorageType;
    }

    public void setHashStorageType(HashStorageEnum hashStorageType) {
        this.hashStorageType = hashStorageType;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public List<Algorithm> getAlgorithms() {
        return algorithms;
    }

    public void setAlgorithms(List<Algorithm> algorithms) {
        this.algorithms = algorithms;
    }
}
