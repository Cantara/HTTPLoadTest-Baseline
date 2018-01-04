package no.cantara.commands;

import no.cantara.service.commands.CommandGetOauth2ProtectedPing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.junit.Assert.assertTrue;

public class CommandGetOauth2ProtectedPingFallbackTest {
    private final static Logger log = LoggerFactory.getLogger(CommandGetOauth2ProtectdPingTest.class);


    private static String pingURL = "http://195.204.7.28";

    @Test(priority = 3, enabled = false)
    public void testCommandGetOauth2ProtectdPing() throws Exception {


        log.trace("Calling {}", pingURL);
        String returned_data = new CommandGetOauth2ProtectedPing(pingURL).execute();
        log.debug("Returned: " + returned_data);
        assertTrue(returned_data != null);
        //   assertTrue(returned_data.contains("PONG"));
    }
}