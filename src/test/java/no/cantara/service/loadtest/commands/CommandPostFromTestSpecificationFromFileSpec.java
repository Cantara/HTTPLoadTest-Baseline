package no.cantara.service.loadtest.commands;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.service.model.TestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class CommandPostFromTestSpecificationFromFileSpec {
    private final static Logger log = LoggerFactory.getLogger(CommandPostFromTestSpecificationFromFileSpec.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    String filenameIntestResourcesToCreateAndRunTestFrom = "readconfig.json";


    @Test
    public void testCommandPostFromTestSpecification() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(filenameIntestResourcesToCreateAndRunTestFrom).getFile());
        List<TestSpecification> readTestSpec = new ArrayList<>();
        readTestSpec = mapper.readValue(file, new TypeReference<List<TestSpecification>>() {
        });
        CommandGetFromTestSpecification myGetCommand = null;
        CommandPostFromTestSpecification myPostCommand = null;
        for (TestSpecification testSpecification : readTestSpec) {
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
}

