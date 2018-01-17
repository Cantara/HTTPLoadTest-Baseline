package no.cantara.service.loadtest.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.service.health.HealthResource;
import no.cantara.service.loadtest.LoadTestExecutorService;
import no.cantara.service.loadtest.LoadTestResource;
import no.cantara.service.model.LoadTestBenchmark;
import no.cantara.service.model.LoadTestResult;
import no.cantara.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class LoadTestResultUtil {

    public static final String RESULT_FILE_PATH = "./results";

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(LoadTestResultUtil.class);

    private static LoadTestBenchmark loadTestBenchmark;

    public static ObjectMapper mapper() {
        return mapper;
    }

    static {
        try {

            InputStream is = Configuration.loadByName("DefaultLoadTestBenchmark.json");

            loadTestBenchmark = mapper.readValue(is, LoadTestBenchmark.class);
            String loadTestJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(loadTestBenchmark);
            log.info("Loaded LoadtestBenchmark: {} ", loadTestJson);

        } catch (Exception e) {
            log.error("Unable to read default configuration for DefaultLoadTestBenchmark.", e);
        }
    }

    public static boolean hasPassedBenchmark(List<LoadTestResult> loadTestResults, boolean whileRunning) {
        long nowTimestamp = System.currentTimeMillis();
        if (LoadTestExecutorService.getActiveLoadTestConfig() != null && LoadTestExecutorService.getStopTime() == 0 && ((nowTimestamp - LoadTestExecutorService.getStartTime()) / 1000) > LoadTestExecutorService.getActiveLoadTestConfig().getTest_duration_in_seconds()) {
            LoadTestExecutorService.stop();  // We might get in trouble if no memory for native threads in high thread situations
        }
        if (loadTestResults == null || LoadTestExecutorService.isRunning()) {
            log.info("hasPassedBenchmark skipped - failed");
            return false;  // We ship on empty results and if tests are running and whileRunning flag is not set
        }
        int r_deviations = 0;
        int r_success = 0;
        long r_duration = 0;
        long w_duration = 0;
        int r_results = 0;
        int w_deviations = 0;
        int w_success = 0;
        int w_results = 0;
        int deviations = 0;
        int success = 0;
        int results = 0;
        long r_mean_success = 0;
        long r_ninety_percentine_success = 0;
        long w_mean_success = 0;
        long w_ninety_percentine_success = 0;
        List<Long> r_times = new ArrayList<Long>();
        List<Long> w_times = new ArrayList<Long>();
        for (LoadTestResult loadTestResult : loadTestResults) {
            if (loadTestResult.getTest_id().startsWith("r-")) {
                r_results++;
                if (loadTestResult.isTest_deviation_flag()) {
                    r_deviations++;
                }
                if (loadTestResult.isTest_success()) {
                    r_success++;
                    r_duration = r_duration + loadTestResult.getTest_duration();
                    r_times.add(loadTestResult.getTest_duration());
                }

            } else {
                if (loadTestResult.getTest_id().startsWith("w-")) {
                    w_results++;
                    if (loadTestResult.isTest_deviation_flag()) {
                        w_deviations++;
                    }
                    if (loadTestResult.isTest_success()) {
                        w_success++;
                        w_duration = w_duration + loadTestResult.getTest_duration();
                        w_times.add(loadTestResult.getTest_duration());
                    }

                } else {
                    results++;
                    if (loadTestResult.isTest_deviation_flag()) {
                        deviations++;
                    }
                    if (loadTestResult.isTest_success()) {
                        success++;
                    }

                }
            }
        }

        // Evaluate
        if (r_success > 0) {
            r_mean_success = r_duration / r_success;
            Collections.sort(r_times);
            r_ninety_percentine_success = r_times.get(r_times.size() * 9 / 10);
        }
        if (w_success > 0) {
            w_mean_success = w_duration / w_success;
            Collections.sort(w_times);
            w_ninety_percentine_success = w_times.get(w_times.size() * 9 / 10);
        }

        int total_successrate = ((r_success + w_success + success) / Math.max(1, (r_results + w_results + results)));

        if (loadTestBenchmark.getBenchmark_req_90percentile_read_duration_ms() <= r_ninety_percentine_success) {
            log.info("getBenchmark_req_90percentile_read_duration_ms failed");
            return false;
        }
        if (loadTestBenchmark.getBenchmark_req_90percentile_write_duration_ms() <= w_ninety_percentine_success) {
            log.info("getBenchmark_req_90percentile_write_duration_ms failed");
            return false;
        }
        if (loadTestBenchmark.getBenchmark_req_mean_read_duration_ms() <= r_mean_success) {
            log.info("getBenchmark_req_mean_read_duration_ms failed");
            return false;
        }
        if (loadTestBenchmark.getBenchmark_req_mean_write_duration_ms() <= w_mean_success) {
            log.info("getBenchmark_req_mean_write_duration_ms failed");
            return false;
        }
        if (loadTestBenchmark.getBenchmark_req_sucessrate_percent() <= total_successrate) {
            log.info("getBenchmark_req_sucessrate_percent failed");
            return false;
        }
        return true;
    }

    public static String printStats(List<LoadTestResult> loadTestResults, boolean whileRunning) {
        long nowTimestamp = System.currentTimeMillis();
        if (LoadTestExecutorService.getActiveLoadTestConfig() != null && LoadTestExecutorService.getStopTime() == 0 && ((nowTimestamp - LoadTestExecutorService.getStartTime()) / 1000) > LoadTestExecutorService.getActiveLoadTestConfig().getTest_duration_in_seconds()) {
            LoadTestExecutorService.stop();  // We might get in trouble if no memory for native threads in high thread situations
        }
        if (loadTestResults == null || (whileRunning == false && LoadTestExecutorService.isRunning() == true)) {
            return "";  // We ship on empty results and if tests are running and whileRunning flag is not set
        }
        int r_deviations = 0;
        int r_success = 0;
        long r_duration = 0;
        long w_duration = 0;
        int r_results = 0;
        int w_deviations = 0;
        int w_success = 0;
        int w_results = 0;
        int deviations = 0;
        int success = 0;
        int results = 0;
        long r_mean_success = 0;
        long r_ninety_percentine_success = 0;
        long w_mean_success = 0;
        long w_ninety_percentine_success = 0;
        List<Long> r_times = new ArrayList<Long>();
        List<Long> w_times = new ArrayList<Long>();
        for (LoadTestResult loadTestResult : loadTestResults) {
            if (loadTestResult.getTest_id().startsWith("r-")) {
                r_results++;
                if (loadTestResult.isTest_deviation_flag()) {
                    r_deviations++;
                }
                if (loadTestResult.isTest_success()) {
                    r_success++;
                    r_duration = r_duration + loadTestResult.getTest_duration();
                    r_times.add(loadTestResult.getTest_duration());
                }

            } else {
                if (loadTestResult.getTest_id().startsWith("w-")) {
                    w_results++;
                    if (loadTestResult.isTest_deviation_flag()) {
                        w_deviations++;
                    }
                    if (loadTestResult.isTest_success()) {
                        w_success++;
                        w_duration = w_duration + loadTestResult.getTest_duration();
                        w_times.add(loadTestResult.getTest_duration());
                    }

                } else {
                    results++;
                    if (loadTestResult.isTest_deviation_flag()) {
                        deviations++;
                    }
                    if (loadTestResult.isTest_success()) {
                        success++;
                    }

                }
            }
        }
        DateFormat df = new SimpleDateFormat("dd/MM-yyyy  HH:mm:ss");
        String stats;
        if (LoadTestExecutorService.getLoadTestRunNo() > 0) {
            if (LoadTestExecutorService.getStopTime() == 0) {
                stats = "Started: " + df.format(new Date(LoadTestExecutorService.getStartTime())) + " Version:" + HealthResource.getVersion() + "  Now: " + df.format(new Date(nowTimestamp)) + "  Running for " + (nowTimestamp - LoadTestExecutorService.getStartTime()) / 1000 + " seconds.\n";
            } else {
                stats = "Started: " + df.format(new Date(LoadTestExecutorService.getStartTime())) + " Version:" + HealthResource.getVersion() + "  Now: " + df.format(new Date(nowTimestamp)) + "  Ran for " + (LoadTestExecutorService.getStopTime() - LoadTestExecutorService.getStartTime()) / 1000 + " seconds.\n";
            }
        } else {
            stats = "Started: " + df.format(new Date(nowTimestamp)) + " Version:" + HealthResource.getVersion() + "  Now: " + df.format(new Date(nowTimestamp)) + "\n";
        }
        if (r_success > 0) {
            r_mean_success = r_duration / r_success;
            Collections.sort(r_times);
            r_ninety_percentine_success = r_times.get(r_times.size() * 9 / 10);
        }
        if (w_success > 0) {
            w_mean_success = w_duration / w_success;
            Collections.sort(w_times);
            w_ninety_percentine_success = w_times.get(w_times.size() * 9 / 10);
        }
        stats = stats + "\n" + String.format(" %5d read tests resulted in %d successful runs where %d was marked failure and %d was marked as deviation(s).", r_results, r_success, (r_results - r_success), r_deviations);
        stats = stats + "\n" + String.format(" %5d write tests resulted in %d successful runs where %d was marked failure and %d was marked as deviation(s).", w_results, w_success, (w_results - w_success), w_deviations);
        stats = stats + "\n" + String.format(" %5d unmarked tests resulted in %d successful runs where %d was marked failure and  %d was marked as deviation(s).", results, success, (results - success), deviations);
        stats = stats + "\n" + String.format(" %5d total tests resulted in %d successful runs where %d was marked failure and %d was marked as deviation(s).", r_results + w_results + results, r_success + w_success + success, (r_results + w_results + results) - (r_success + w_success + success), r_deviations + w_deviations + deviations);
        stats = stats + "\n" + String.format(" %5d tasks scheduled, number of threads configured: %d,  isRunning: %b ", LoadTestExecutorService.getTasksScheduled(), LoadTestExecutorService.getThreadPoolSize(), LoadTestExecutorService.isRunning());
        stats = stats + "\n" + String.format(" %5d ms mean duraction for successful read tests, %4d ms ninety percentile successful read tests ", r_mean_success, r_ninety_percentine_success);
        stats = stats + "\n" + String.format(" %5d ms mean duraction for successful write tests, %4d ms ninety percentile successful write tests ", w_mean_success, w_ninety_percentine_success);

        String loadTestJson = "";
        if (LoadTestExecutorService.getActiveLoadTestConfig() != null) {
            try {
                loadTestJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(LoadTestExecutorService.getActiveLoadTestConfig());
                loadTestJson = loadTestJson + "\n\n";

            } catch (Exception e) {
                log.warn("Unable to serialize loadTestConfig to json", e);
            }
        }
        return stats + "\n\n" + loadTestJson;
    }

    public static void storeResultToFiles() {

        try {


            File directory = new File(RESULT_FILE_PATH);
            if (!directory.exists()) {
                directory.mkdir();
                // If you require it to make the entire directory path including parents,
            }
            PrintWriter jsonwriter = new PrintWriter(RESULT_FILE_PATH + File.separator + LoadTestExecutorService.getActiveLoadTestConfig().getTest_id() + "_" + LoadTestExecutorService.getStartTime() + ".json", "UTF-8");
            jsonwriter.println(LoadTestResource.getJsonResultString());
            jsonwriter.close();
            PrintWriter cvswriter = new PrintWriter(RESULT_FILE_PATH + File.separator + LoadTestExecutorService.getActiveLoadTestConfig().getTest_id() + "_" + LoadTestExecutorService.getStartTime() + ".csv", "UTF-8");
            cvswriter.println(LoadTestResource.getCSVResultString());
            cvswriter.close();
        } catch (Exception e) {
            log.error("Unable to persist resultfiles. ", e);
        }
    }

    public static String listStoredResults() {
        String resultlistOfResults = "";
        try {

            File folder = new File(RESULT_FILE_PATH);
            File[] listOfFiles = folder.listFiles();

            if (listOfFiles == null) {
                return resultlistOfResults;
            }
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    resultlistOfResults = resultlistOfResults + listOfFiles[i].getName() + ", ";
                } else if (listOfFiles[i].isDirectory()) {
                    //   System.out.println("Directory " + listOfFiles[i].getName());
                }
            }
            return resultlistOfResults.substring(0, resultlistOfResults.length() - 2);
        } catch (Exception e) {
            log.error("Unable to look for resultfiles. ", e);
        }
        return resultlistOfResults;
    }
}