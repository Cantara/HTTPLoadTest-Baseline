package no.cantara.service.loadtest.commands;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.junit.Assert.assertTrue;

public class CommandGetURLTest {
    private final static Logger log = LoggerFactory.getLogger(CommandGetURLTest.class);

    private static String testURL = "https://gmail.com";


    @Test(priority = 1, enabled = false)
    public void testCommandGetURLTest() throws Exception {


        log.trace("Calling {}", testURL);
        CommandGetURL command = new CommandGetURL(testURL, 7000);
        String returned_data = command.execute();
        log.debug("Returned: " + returned_data);
        assertTrue(!command.isFailedExecution());
        assertTrue(!command.isResponseFromFallback());
        assertTrue(returned_data != null);
        assertTrue(returned_data.contains("Gmail"));
    }

    @Test
    public void calculateBasicAuthString() {
        String name = "admin";
        String password = "admin";

        String authString = name + ":" + password;
        System.out.println("auth string: " + authString);
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = "Basic " + new String(authEncBytes);
        System.out.println("Base64 encoded auth header: " + authStringEnc);
        assertTrue("Basic YWRtaW46YWRtaW4=".equals(authStringEnc));

    }

    @Test
    public void calculateBasicAuthString2() {
        String webPage = "admin/admin";
        String[] upfields = webPage.split("/");
        String name = upfields[0];
        String password = upfields[1];

        String authString = name + ":" + password;
        System.out.println("auth string: " + authString);
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = "Basic " + new String(authEncBytes);
        System.out.println("Base64 encoded auth header: " + authStringEnc);
        assertTrue("Basic YWRtaW46YWRtaW4=".equals(authStringEnc));

    }

}