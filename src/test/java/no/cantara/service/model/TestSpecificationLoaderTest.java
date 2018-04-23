package no.cantara.service.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertTrue;


public class TestSpecificationLoaderTest {

    private static final Logger log = LoggerFactory.getLogger(TestSpecificationLoaderTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void test_LoadspecificationsFromProperties() throws Exception {
        Map<String, String> configuredTests = TestSpecificationLoader.getPersistedTestSpecificationFilenameMap();

        int n = 1;
        for (String testSpecificationEntry : configuredTests.keySet()) {
            InputStream file = no.cantara.util.Configuration.loadByName(configuredTests.get(testSpecificationEntry));
            List<TestSpecification> readTestSpec = new ArrayList<>();
            readTestSpec = mapper.readValue(file, new TypeReference<List<TestSpecification>>() {
            });
            assertTrue(readTestSpec.size() > 0);
            for (TestSpecification testSpecification : readTestSpec) {
                log.info("Loaded  testspecification: {}:{} - {}", n++, testSpecificationEntry, configuredTests.get(testSpecificationEntry));
                testSpecification.resolveVariables(null, null, null);//loadTemplateReference();
                assertTrue(testSpecification.getCommand_url().length() > 0);
            }

        }
    }

    @Test
    public void testCommandPostFromTestSpecification() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("loadtest_setup/specifications/read/TestReadConfigReadTestSpecification.json").getFile());
        List<TestSpecification> readTestSpec = mapper.readValue(file, new TypeReference<List<TestSpecification>>() {
        });
        Map<String, String> resolvedResultVariables = new HashMap<>();

        for (TestSpecification testSpecification : readTestSpec) {
            testSpecification.resolveVariables(null, null, resolvedResultVariables);//loadTemplateReference();
            assertTrue(testSpecification.getCommand_url().length() > 0);
        }

    }

    @Test
    public void test_Global_command_replacement_map() throws Exception {
        Map<String, String> globalVariableMap = TestSpecificationLoader.getGlobal_command_replacement_map();

        for (String globalVariable : globalVariableMap.keySet()) {
                log.trace("Loaded  global variable: {} - value:{}",globalVariable, globalVariableMap.get(globalVariable));
            }

        }


}