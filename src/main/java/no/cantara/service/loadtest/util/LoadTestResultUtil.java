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
import java.util.*;

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

    public static boolean hasPassedBenchmark(Map<String, String> statisticsMap) {
        if (statisticsMap == null) {
            return true;
        }
        return Boolean.valueOf(statisticsMap.get(statisticsMap.get("isBenchmarkPassed failed")));

    }

    public static SortedMap hasPassedBenchmark(List<LoadTestResult> loadTestResults, boolean whileRunning) {
//        Map<String, String> statisticsMap = new HashMap<>();
        SortedMap<String, String> statisticsMap = new TreeMap<String, String>();
        long nowTimestamp = System.currentTimeMillis();
        statisticsMap.put("timestamp", Long.toString(nowTimestamp));
        if (LoadTestExecutorService.getActiveLoadTestConfig() != null && LoadTestExecutorService.getStopTime() == 0 && ((nowTimestamp - LoadTestExecutorService.getStartTime()) / 1000) > LoadTestExecutorService.getActiveLoadTestConfig().getTest_duration_in_seconds()) {
            LoadTestExecutorService.stop();  // We might get in trouble if no memory for native threads in high thread situations
        }
        if (loadTestResults == null) {
            log.info("hasPassedBenchmark - no results - skipping");
            return statisticsMap;  // We ship on empty results and if tests are running and whileRunning flag is not set
        }
        if (!whileRunning && LoadTestExecutorService.isRunning()) {
            log.info("hasPassedBenchmark - is running - whileRunning=false - skipping");
            return statisticsMap;  // We ship on empty results and if tests are running and whileRunning flag is not set

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

        statisticsMap.put("stats_r_deviations", Long.toString(r_deviations));
        statisticsMap.put("stats_r_success", Long.toString(r_success));
        statisticsMap.put("stats_r_duration", Long.toString(r_duration));
        statisticsMap.put("stats_r_results", Long.toString(r_results));
        statisticsMap.put("stats_r_failures", Long.toString(r_results - r_success));
        statisticsMap.put("stats_w_deviations", Long.toString(w_deviations));
        statisticsMap.put("stats_w_duration", Long.toString(w_duration));
        statisticsMap.put("stats_w_success", Long.toString(w_success));
        statisticsMap.put("stats_w_results", Long.toString(w_results));
        statisticsMap.put("stats_w_failures", Long.toString(w_results - w_success));
        statisticsMap.put("stats_o_deviations", Long.toString(deviations));
        statisticsMap.put("stats_o_success", Long.toString(success));
        statisticsMap.put("stats_o_results", Long.toString(results));
        statisticsMap.put("stats_o_failures", Long.toString(results - success));
        statisticsMap.put("stats_t_deviations", Long.toString(r_deviations + w_deviations + deviations));
        statisticsMap.put("stats_t_success", Long.toString(r_success + w_success + success));
        statisticsMap.put("stats_t_results", Long.toString(r_results + w_results + results));
        statisticsMap.put("stats_t_failures", Long.toString(r_results + w_results + results - (r_success + w_success + success)));
        statisticsMap.put("stats_r_mean_success", Long.toString(r_mean_success));
        statisticsMap.put("stats_r_ninety_percentine_success", Long.toString(r_ninety_percentine_success));
        statisticsMap.put("stats_w_mean_success", Long.toString(w_mean_success));
        statisticsMap.put("stats_w_ninety_percentine_success", Long.toString(w_ninety_percentine_success));
        statisticsMap.put("stats_total_successrate", Long.toString(total_successrate));

        boolean isBenchmarkPassed = true;
        if (loadTestBenchmark.getBenchmark_req_90percentile_read_duration_ms() <= r_ninety_percentine_success) {
            log.info("getBenchmark_req_90percentile_read_duration_ms failed");
            statisticsMap.put("benchmark_req_90percentile_read_duration_ms", Boolean.toString(false));
            isBenchmarkPassed = false;
        } else {
            statisticsMap.put("benchmark_req_90percentile_read_duration_ms", Boolean.toString(true));

        }
        if (loadTestBenchmark.getBenchmark_req_90percentile_write_duration_ms() <= w_ninety_percentine_success) {
            log.info("getBenchmark_req_90percentile_write_duration_ms failed");
            statisticsMap.put("benchmark_req_90percentile_write_duration_ms", Boolean.toString(false));
            isBenchmarkPassed = false;
        } else {
            statisticsMap.put("benchmark_req_90percentile_write_duration_ms", Boolean.toString(true));
        }
        if (loadTestBenchmark.getBenchmark_req_mean_read_duration_ms() <= r_mean_success) {
            log.info("getBenchmark_req_mean_read_duration_ms failed");
            statisticsMap.put("benchmark_req_mean_read_duration_ms", Boolean.toString(false));
            isBenchmarkPassed = false;
        } else {
            statisticsMap.put("benchmark_req_mean_read_duration_ms", Boolean.toString(true));
        }
        if (loadTestBenchmark.getBenchmark_req_mean_write_duration_ms() <= w_mean_success) {
            log.info("getBenchmark_req_mean_write_duration_ms failed");
            statisticsMap.put("benchmark_req_mean_write_duration_ms", Boolean.toString(false));
            isBenchmarkPassed = false;
        } else {
            statisticsMap.put("benchmark_req_mean_write_duration_ms", Boolean.toString(true));
        }
        if (loadTestBenchmark.getBenchmark_req_sucessrate_percent() <= total_successrate) {
            log.info("getBenchmark_req_sucessrate_percent failed");
            statisticsMap.put("benchmark_req_sucessrate_percent failed", Boolean.toString(false));
            isBenchmarkPassed = false;
        } else {
            statisticsMap.put("benchmark_req_sucessrate_percent failed", Boolean.toString(true));
        }
        statisticsMap.put("isBenchmarkPassed failed", Boolean.toString(isBenchmarkPassed));
        return statisticsMap;
    }

    public static String printStats(List<LoadTestResult> loadTestResults, boolean whileRunning) {
        Map<String, String> statsMap = hasPassedBenchmark(loadTestResults, whileRunning);
        DateFormat df = new SimpleDateFormat("dd/MM-yyyy  HH:mm:ss");
        String stats;
        if (LoadTestExecutorService.getLoadTestRunNo() > 0) {
            if (LoadTestExecutorService.getStopTime() == 0) {
                stats = "Started: " + df.format(new Date(LoadTestExecutorService.getStartTime())) + " Version:" + HealthResource.getVersion() + "  Now: " + df.format(new Date(Long.parseLong(statsMap.get("timestamp")))) + "  Running for " + (Long.parseLong(statsMap.get("timestamp")) - LoadTestExecutorService.getStartTime()) / 1000 + " seconds.\n";
            } else {
                stats = "Started: " + df.format(new Date(LoadTestExecutorService.getStartTime())) + " Version:" + HealthResource.getVersion() + "  Now: " + df.format(new Date(Long.parseLong(statsMap.get("timestamp")))) + "  Ran for " + (LoadTestExecutorService.getStopTime() - LoadTestExecutorService.getStartTime()) / 1000 + " seconds.\n";
            }
        } else {
            stats = "Started: " + df.format(new Date(Long.parseLong(statsMap.get("timestamp")))) + " Version:" + HealthResource.getVersion() + "  Now: " + df.format(new Date(Long.parseLong(statsMap.get("timestamp")))) + "\n";
        }

        if (statsMap.size() > 10) {
            stats = stats + "\n" + String.format(" %5d read tests resulted in %d successful runs where %d was marked failure and %d was marked as deviation(s).",
                    Integer.parseInt(statsMap.get("stats_r_results")), Integer.parseInt(statsMap.get("stats_r_success")), Integer.parseInt(statsMap.get("stats_r_failures")), Integer.parseInt(statsMap.get("stats_r_deviations")));
            stats = stats + "\n" + String.format(" %5d write tests resulted in %d successful runs where %d was marked failure and %d was marked as deviation(s).",
                    Integer.parseInt(statsMap.get("stats_w_results")), Integer.parseInt(statsMap.get("stats_w_success")), Integer.parseInt(statsMap.get("stats_w_failures")), Integer.parseInt(statsMap.get("stats_w_deviations")));
            stats = stats + "\n" + String.format(" %5d unmarked tests resulted in %d successful runs where %d was marked failure and  %d was marked as deviation(s).",
                    Integer.parseInt(statsMap.get("stats_o_results")), Integer.parseInt(statsMap.get("stats_o_success")), Integer.parseInt(statsMap.get("stats_o_failures")), Integer.parseInt(statsMap.get("stats_o_deviations")));
            stats = stats + "\n" + String.format(" %5d total tests resulted in %d successful runs where %d was marked failure and %d was marked as deviation(s).",
                    Integer.parseInt(statsMap.get("stats_t_results")), Integer.parseInt(statsMap.get("stats_t_success")), Integer.parseInt(statsMap.get("stats_t_failures")), Integer.parseInt(statsMap.get("stats_t_deviations")));
            stats = stats + "\n" + String.format(" %5d tasks scheduled, number of threads configured: %d,  isRunning: %b ",
                    LoadTestExecutorService.getTasksScheduled(), LoadTestExecutorService.getThreadPoolSize(), LoadTestExecutorService.isRunning());
            stats = stats + "\n" + String.format(" %5d ms mean duration for successful read tests, %4d ms ninety percentile successful read tests ",
                    Integer.parseInt(statsMap.get("stats_r_mean_success")), Integer.parseInt(statsMap.get("stats_r_ninety_percentine_success")));
            stats = stats + "\n" + String.format(" %5d ms mean duration for successful write tests, %4d ms ninety percentile successful write tests ",
                    Integer.parseInt(statsMap.get("stats_w_mean_success")), Integer.parseInt(statsMap.get("stats_w_ninety_percentine_success")));
        }
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


            // Persist the specifications for the test-run
            PrintWriter readSpecWriter = new PrintWriter(RESULT_FILE_PATH + File.separator + LoadTestExecutorService.getActiveLoadTestConfig().getTest_id() + "_" + LoadTestExecutorService.getStartTime() + "_read_specififation.json", "UTF-8");
            readSpecWriter.println(LoadTestExecutorService.getReadTestSpecificationListJson());
            readSpecWriter.close();

            PrintWriter writeSpecWriter = new PrintWriter(RESULT_FILE_PATH + File.separator + LoadTestExecutorService.getActiveLoadTestConfig().getTest_id() + "_" + LoadTestExecutorService.getStartTime() + "_write_specififation.json", "UTF-8");
            writeSpecWriter.println(LoadTestExecutorService.getWriteTestSpecificationListJson());
            writeSpecWriter.close();

            PrintWriter loadSpecWriter = new PrintWriter(RESULT_FILE_PATH + File.separator + LoadTestExecutorService.getActiveLoadTestConfig().getTest_id() + "_" + LoadTestExecutorService.getStartTime() + "_load_specififation.json", "UTF-8");
            loadSpecWriter.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(LoadTestExecutorService.getActiveLoadTestConfig()));
            loadSpecWriter.close();

            PrintWriter benchmarkSpecWriter = new PrintWriter(RESULT_FILE_PATH + File.separator + LoadTestExecutorService.getActiveLoadTestConfig().getTest_id() + "_" + LoadTestExecutorService.getStartTime() + "_benchmark_specififation.json", "UTF-8");
            benchmarkSpecWriter.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(LoadTestResultUtil.getLoadTestBenchmark()));
            benchmarkSpecWriter.close();

            // Also persist benchmark result
            Map<String, String> resultMap = LoadTestResultUtil.hasPassedBenchmark(LoadTestExecutorService.getResultListSnapshot(), true);
            PrintWriter benchmarkwriter = new PrintWriter(RESULT_FILE_PATH + File.separator + LoadTestExecutorService.getActiveLoadTestConfig().getTest_id() + "_" + LoadTestExecutorService.getStartTime() + "_benchmark_result.json", "UTF-8");
            benchmarkwriter.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultMap));
            benchmarkwriter.close();
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

    public static LoadTestBenchmark getLoadTestBenchmark() {
        return loadTestBenchmark;
    }

    public static void setLoadTestBenchmark(LoadTestBenchmark loadTestBenchmark) {
        LoadTestResultUtil.loadTestBenchmark = loadTestBenchmark;
    }


}