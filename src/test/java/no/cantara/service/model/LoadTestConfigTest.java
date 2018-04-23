package no.cantara.service.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.junit.Assert.assertTrue;

public class LoadTestConfigTest {

    private static final Logger log = LoggerFactory.getLogger(LoadTestConfigTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();


    @Test
    public void testInstanciateLoadTestConfig() throws Exception {
        LoadTestConfig loadTestConfig = new LoadTestConfig();
        loadTestConfig.setTest_id("TestID");

        String loadTestJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(loadTestConfig);
        LoadTestConfig mappedLoadtest = mapper.readValue(loadTestJson, LoadTestConfig.class);

        assertTrue(loadTestConfig.getTest_id().equalsIgnoreCase(mappedLoadtest.getTest_id()));


    }

    @Test
    public void readTestConfigFromFile() throws Exception {

        InputStream file = no.cantara.util.Configuration.loadByName("configurations/LoadTestConfig.json");
        LoadTestConfig fileLoadtest = mapper.readValue(file, LoadTestConfig.class);
        assertTrue(fileLoadtest.getTest_id().equalsIgnoreCase("TestID"));
    }

}
