package no.cantara.service.loadtest.commands;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.service.loadtest.util.HTTPResultUtil;
import no.cantara.service.model.TestSpecification;
import no.cantara.service.testsupport.TestServer;
import no.cantara.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class CommandReadTestSpecificationFromFileSpecTest {
    private final static Logger log = LoggerFactory.getLogger(CommandReadTestSpecificationFromFileSpecTest.class);
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
        String filenameIntestResourcesToCreateAndRunTestFrom = "specifications/read/TestReadConfigReadTestSpecification.json";

        InputStream inputStream = Configuration.loadByName(filenameIntestResourcesToCreateAndRunTestFrom);
        List<TestSpecification> readTestSpec = mapper.readValue(inputStream, new TypeReference<List<TestSpecification>>() {
        });
        Map<String, String> resolvedResultVariables = new HashMap<>();

        CommandGetFromTestSpecification myGetCommand = null;
        CommandPostFromTestSpecification myPostCommand = null;
        int n = 1;
        for (TestSpecification testSpecificationo : readTestSpec) {
            TestSpecification testSpecification = testSpecificationo.clone();
            testSpecification.resolveVariables(null, null, resolvedResultVariables);
            assertTrue(testSpecification.getCommand_url().length() > 0);
            log.debug("{}  - Calling {}", n, testSpecification.getCommand_url());
            String result;
            if (testSpecification.isCommand_http_post()) {
                myPostCommand = new CommandPostFromTestSpecification(testSpecification);
                result = myPostCommand.execute();
            } else {
                myGetCommand = new CommandGetFromTestSpecification(testSpecification);
                result = myGetCommand.execute();
            }
            log.info("{} - Returned result: " + result + "\n" + myGetCommand + "\n" + myPostCommand);
            resolvedResultVariables = HTTPResultUtil.parse(result, testSpecification.getCommand_response_map());

            n++;
        }

    }


    @Test
    public void testCommandPostFromTestSpecification() throws Exception {

        String filenameIntestResourcesToCreateAndRunTestFrom = "specifications/read/OAuth2ConfigReadTestSpecification.json";

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

