package no.cantara.service.loadtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.service.LoadTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class LoadTestExecutorServiceTest {

    private static final Logger log = LoggerFactory.getLogger(LoadTestExecutorServiceTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void executeTestConfigFromFile() throws Exception {


        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("loadtestconfig.json").getFile());
        LoadTestConfig fileLoadtest = mapper.readValue(file, LoadTestConfig.class);
        assertTrue(fileLoadtest.getTest_id().equalsIgnoreCase("TestID"));
        long startTime = System.currentTimeMillis();

        LoadTestExecutorService.executeLoadTest(fileLoadtest);
        long endTime = System.currentTimeMillis();

        Thread.sleep(2000);

//        log.info("Results from test:"+LoadTestExecutorService.getResultSizeAsJson());
        log.info("Run-time: {} ms", endTime - startTime);

        List resultList = LoadTestExecutorService.getResultList();
        log.info("Results from tests:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultList));

    }

}