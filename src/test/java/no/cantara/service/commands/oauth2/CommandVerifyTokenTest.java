package no.cantara.service.commands.oauth2;

import no.cantara.service.commands.config.ConstantValue;
import no.cantara.service.testsupport.TestServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class CommandVerifyTokenTest {
    private final static Logger log = LoggerFactory.getLogger(CommandVerifyTokenTest.class);


    private TestServer testServer;

    @BeforeClass
    public void startServer() throws Exception {
        testServer = new TestServer(getClass());
        testServer.start();
    }

    @AfterClass
    public void stop() {
        testServer.stop();
    }

    @Test
    public void testCommandGetOauth2ProtectdPing() throws Exception {


        log.trace("Calling {}", testServer.getUrl());
        String returned_data = new CommandVerifyToken(testServer.getUrl(), ConstantValue.ATOKEN).execute();
        log.debug("Returned: " + returned_data);
        assertTrue(returned_data != null);
        assertTrue(returned_data.contains("auth_user_id"));
    }
}