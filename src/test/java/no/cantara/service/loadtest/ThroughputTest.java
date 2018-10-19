package no.cantara.service.loadtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.service.loadtest.util.LoadTestResultUtil;
import no.cantara.service.model.LoadTestConfig;
import no.cantara.service.model.LoadTestResult;
import no.cantara.service.testsupport.TestServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ThroughputTest {

    private static final Logger log = LoggerFactory.getLogger(ThroughputTest.class);

    private SimpleHttpServer httpServer;
    private TestServer testServer;

    @BeforeClass
    public void startServer() throws Exception {
        testServer = new TestServer(getClass());
        testServer.start();
        httpServer = new SimpleHttpServer();
        httpServer.start();
    }

    @AfterClass
    public void stop() {
        testServer.stop();
        httpServer.stop();
    }

    @Test
    public void thatTestBenchThroughputIsAcceptableWithSingleTestSpecificationPerReadWrite() throws IOException {
        throughputTest(1, 100);
    }

    @Test
    public void thatTestBenchThroughputIsAcceptableWithManyTestSpecificationsPerReadWrite() throws IOException {
        throughputTest(10, 100);
    }

    void throughputTest(int domainCount, int throughputLowerLimitMsgsPerSecond) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        /*
         * Prepare read-specification, write-specification, and test-configuration.
         */
        TestSetupHelper helper = new TestSetupHelper(mapper);
        helper.generateSpecifications(domainCount, httpServer.getPort());
        LoadTestExecutorService.setWriteTestSpecificationList(helper.getWriteSpecification());
        LoadTestExecutorService.setReadTestSpecificationList(helper.getReadSpecification());
        LoadTestConfig loadTestConfig = mapper.readValue(helper.getLoadTestConfigJson(), LoadTestConfig.class);


        /*
         * Execute load-test and wait for completion
         */
        long startTime = System.currentTimeMillis();
        LoadTestExecutorService.executeLoadTest(loadTestConfig, false);
        long endTime = System.currentTimeMillis();


        /*
         * Analyze test-results
         */
        List<LoadTestResult> resultList = LoadTestExecutorService.getResultListSnapshot();
        log.info("Results from tests:" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultList));

        log.info(LoadTestResultUtil.printStats(resultList, true));
        long actualTestDuration = endTime - startTime;
        log.info("Run-time: {} ms, configured run-time: {}", actualTestDuration, loadTestConfig.getTest_duration_in_seconds() * 1000);

        Map<String, String> resultMap = LoadTestResultUtil.hasPassedBenchmark(resultList, true);
        long nRead = Long.parseLong(resultMap.get(LoadTestResultUtil.STATS_R_SUCCESS));
        long nWrite = Long.parseLong(resultMap.get(LoadTestResultUtil.STATS_W_SUCCESS));

        double singleThreadThroughputMsgsPerSecond = 1000.0 * (nRead + nWrite) / actualTestDuration;
        double averageTimePerMessageMilliseconds = (double) actualTestDuration / (nRead + nWrite);

        long meanRead = Long.parseLong(resultMap.get(LoadTestResultUtil.STATS_R_MEAN_SUCCESS_MS));
        long meanWrite = Long.parseLong(resultMap.get(LoadTestResultUtil.STATS_W_MEAN_SUCCESS_MS));

        double weightedMean = (double) (Math.max(1, meanRead) * nRead + Math.max(1, meanWrite) * nWrite) / nRead + nWrite;

        double timeSpentInTestBenchPerMessage = averageTimePerMessageMilliseconds - weightedMean;
        log.info("Single-thread throughput: {} msgs/sec", Math.round(singleThreadThroughputMsgsPerSecond));

        Assert.assertTrue(singleThreadThroughputMsgsPerSecond > throughputLowerLimitMsgsPerSecond, "Horrible throughput: " + Math.round(singleThreadThroughputMsgsPerSecond) + " msgs/sec, Throughput expected way above 100 msgs/sec. Per-message time spent in test-bench was " + Math.round(timeSpentInTestBenchPerMessage) + " ms.");
    }
}
