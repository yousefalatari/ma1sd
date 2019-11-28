package io.kamax.mxisd.test.hash;

import static org.junit.Assert.assertEquals;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

public class HashEngineTest {

    @Test
    public void sha256test() {
        assertEquals("a26de61ae3055f84b33ac1a179b9ad5301f9109024f4db1ae653ea525d2136f4",
            DigestUtils.sha256Hex("user2@mail.homeserver.tld email I9x4vpcWjqp9X8iiOY4a"));
    }
}
