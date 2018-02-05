package no.cantara.service.loadtest.commands;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.service.model.TestSpecification;
import no.cantara.service.testsupport.TestServer;
import no.cantara.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class CommandPostFromTestSpecificationFromFileSpecTest {
    private final static Logger log = LoggerFactory.getLogger(CommandPostFromTestSpecificationFromFileSpecTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();

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
    public void testCommandGetFromTestSpecification() throws Exception {
        String filenameIntestResourcesToCreateAndRunTestFrom = "readconfig.json";

        InputStream inputStream = Configuration.loadByName(filenameIntestResourcesToCreateAndRunTestFrom);
        List<TestSpecification> readTestSpec = readTestSpec = mapper.readValue(inputStream, new TypeReference<List<TestSpecification>>() {
        });
        CommandGetFromTestSpecification myGetCommand = null;
        CommandPostFromTestSpecification myPostCommand = null;
        for (TestSpecification testSpecification : readTestSpec) {
            testSpecification.resolveVariables(null, null, null);
            assertTrue(testSpecification.getCommand_url().length() > 0);
            log.trace("Calling {}", testSpecification.getCommand_url());
            String result;
            if (testSpecification.isCommand_http_post()) {
                myPostCommand = new CommandPostFromTestSpecification(testSpecification);
                result = myPostCommand.execute();
            } else {
                myGetCommand = new CommandGetFromTestSpecification(testSpecification);
                result = myGetCommand.execute();
            }
            log.debug("Returned result: " + result + "\n" + myGetCommand + "\n" + myPostCommand);
        }

    }


    @Test
    public void testCommandPostFromTestSpecification() throws Exception {

        String filenameIntestResourcesToCreateAndRunTestFrom = "oauthconfig.json";

        InputStream inputStream = Configuration.loadByName(filenameIntestResourcesToCreateAndRunTestFrom);
        List<TestSpecification> readTestSpec = readTestSpec = mapper.readValue(inputStream, new TypeReference<List<TestSpecification>>() {
        });
        CommandGetFromTestSpecification myGetCommand = null;
        CommandPostFromTestSpecification myPostCommand = null;
        for (TestSpecification testSpecification : readTestSpec) {
            assertTrue(testSpecification.getCommand_url().length() > 0);
            testSpecification.resolveVariables(null, null, null);
            // replace URL with randomized port
            if (testSpecification.getCommand_url().contains("verify")) {
                testSpecification.setCommand_url(testServer.getUrl() + "/verify");
            }
            if (testSpecification.getCommand_url().contains("ping")) {
                testSpecification.setCommand_url(testServer.getUrl() + "/ping");
            }
            log.trace("Calling {}", testSpecification.getCommand_url());
            String result;
            if (testSpecification.isCommand_http_post()) {
                myPostCommand = new CommandPostFromTestSpecification(testSpecification);
                result = myPostCommand.execute();
            } else {
                myGetCommand = new CommandGetFromTestSpecification(testSpecification);
                result = myGetCommand.execute();
            }
            log.info("Returned result: " + result + "\n" + myGetCommand + "\n" + myPostCommand);
        }

    }
}

