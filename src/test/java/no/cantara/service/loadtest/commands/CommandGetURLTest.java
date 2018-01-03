package no.cantara.service.loadtest.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.junit.Assert.assertTrue;

public class CommandGetURLTest {
    private final static Logger log = LoggerFactory.getLogger(CommandGetURLTest.class);

    private static String testURL = "https://gmail.com";


    @Test
    public void testCommandGetURLTest() throws Exception {


        log.trace("Calling {}", testURL);
        CommandGetURL command = new CommandGetURL(testURL, 3000);
        String returned_data = command.execute();
        log.debug("Returned: " + returned_data);
        assertTrue(!command.isFailedExecution());
        assertTrue(!command.isResponseFromFallback());
        assertTrue(returned_data != null);
        assertTrue(returned_data.contains("Gmail"));
    }

}