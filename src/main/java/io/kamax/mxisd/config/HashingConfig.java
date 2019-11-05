package io.kamax.mxisd.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashingConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(HashingConfig.class);

    private boolean enabled = false;
    private int pepperLength = 10;
    private RotationPolicyEnum rotationPolicy;
    private HashStorageEnum hashStorageType;
    private long delay = 10;

    public void build() {
        if (isEnabled()) {
            LOGGER.info("--- Hash configuration ---");
            LOGGER.info("   Pepper length: {}", getPepperLength());
            LOGGER.info("   Rotation policy: {}", getRotationPolicy());
            LOGGER.info("   Hash storage type: {}", getHashStorageType());
            if (RotationPolicyEnum.PER_SECONDS == rotationPolicy) {
                LOGGER.info("   Rotation delay: {}", delay);
            }
        } else {
            LOGGER.info("Hash configuration disabled, used only `none` pepper.");
        }
    }

    public enum RotationPolicyEnum {
        PER_REQUESTS,
        PER_SECONDS
    }

    public enum HashStorageEnum {
        IN_MEMORY,
        SQL
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
}
