package no.cantara.service.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.junit.Assert.assertTrue;

public class LoadTestBenchmarkTest {
    private static final Logger log = LoggerFactory.getLogger(LoadTestBenchmarkTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();


    @Test
    public void testInstansiateLoadTestBenchmarkTest() throws Exception {
        LoadTestBenchmark loadTestBenchmark = new LoadTestBenchmark();
        loadTestBenchmark.setBenchmark_id("BenchmarkID");

        String loadTestJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(loadTestBenchmark);
        LoadTestBenchmark testBenchmark = mapper.readValue(loadTestJson, LoadTestBenchmark.class);

        assertTrue(loadTestBenchmark.getBenchmark_id().equalsIgnoreCase(testBenchmark.getBenchmark_id()));


    }

    @Test
    public void readTestConfigFromFile() throws Exception {

        InputStream file = no.cantara.util.Configuration.loadByName("LoadTestBenchmark.json");
        LoadTestBenchmark fileLoadtest = mapper.readValue(file, LoadTestBenchmark.class);
        assertTrue(fileLoadtest.getBenchmark_id().equalsIgnoreCase("BenchmarkID"));
    }
}