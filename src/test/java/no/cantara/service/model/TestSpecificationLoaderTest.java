package no.cantara.service.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertTrue;


public class TestSpecificationLoaderTest {

    private static final Logger log = LoggerFactory.getLogger(TestSpecificationLoaderTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void test_LoadspecificationsFromProperties() throws Exception {
        Map<String, String> configuredTests = TestSpecificationLoader.getPersistedTestSpacificationFilenameMap();

        for (String testSpecificationEntry : configuredTests.keySet()) {
            File file = new File(configuredTests.get(testSpecificationEntry));
            List<TestSpecification> readTestSpec = new ArrayList<>();
            readTestSpec = mapper.readValue(file, new TypeReference<List<TestSpecification>>() {
            });
            assertTrue(readTestSpec.size() > 0);
            for (TestSpecification testSpecification : readTestSpec) {
                log.trace("Loaded  testspecification: {}", configuredTests.get(testSpecificationEntry));
                testSpecification.loadTemplateReference();
                assertTrue(testSpecification.getCommand_url().length() > 0);
            }

        }
    }


}