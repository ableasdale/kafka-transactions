package kafka.transactions;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static org.junit.jupiter.api.Assertions.*;

class LibraryTest {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test void someLibraryMethodReturnsTrue() {
        LOG.info("Does this test run?");
        assertTrue(true, "someLibraryMethod should return 'true'");
    }
}
