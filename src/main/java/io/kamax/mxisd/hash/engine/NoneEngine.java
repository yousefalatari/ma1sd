package io.kamax.mxisd.hash.engine;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoneEngine implements Engine {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoneEngine.class);
    private static final String PEPPER = RandomStringUtils.random(8, true, true);

    @Override
    public void updateHashes() {
        LOGGER.info("Nothing to update.");
    }

    @Override
    public String getPepper() {
        return PEPPER;
    }
}
