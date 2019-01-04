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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class LoadTestResultUtil {

    public static final String RESULT_FILE_PATH = "./results";

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(LoadTestResultUtil.class);
    public static final String STATS_R_DEVIATIONS = "stats_r_deviations";
    public static final String STATS_R_SUCCESS = "stats_r_success";
    public static final String STATS_R_DURATION_MS = "stats_r_duration_ms";
    public static final String STATS_R_RESULTS = "stats_r_results";
    public static final String STATS_R_FAILURES = "stats_r_failures";
    public static final String STATS_W_DEVIATIONS = "stats_w_deviations";
    public static final String STATS_W_DURATION_MS = "stats_w_duration_ms";
    public static final String STATS_W_SUCCESS = "stats_w_success";
    public static final String STATS_W_RESULTS = "stats_w_results";
    public static final String STATS_W_FAILURES = "stats_w_failures";
    public static final String STATS_O_DEVIATIONS = "stats_o_deviations";
    public static final String STATS_O_SUCCESS = "stats_o_success";
    public static final String STATS_O_RESULTS = "stats_o_results";
    public static final String STATS_O_FAILURES = "stats_o_failures";
    public static final String STATS_T_DEVIATIONS = "stats_t_deviations";
    public static final String STATS_T_SUCCESS = "stats_t_success";
    public static final String STATS_T_RESULTS = "stats_t_results";
    public static final String STATS_T_FAILURES = "stats_t_failures";
    public static final String STATS_R_MEAN_SUCCESS_MS = "stats_r_mean_success_ms";
    public static final String STATS_R_MEDIAN_SUCCESS_MS = "stats_r_median_success_ms";
    public static final String STATS_R_NINETY_PERCENTINE_SUCCESS_MS = "stats_r_ninety_percentine_success_ms";
    public static final String STATS_R_NINETYFIVE_PERCENTINE_SUCCESS_MS = "stats_r_ninetyfive_percentine_success_ms";
    public static final String STATS_R_NINETYNINE_PERCENTINE_SUCCESS_MS = "stats_r_ninetynine_percentine_success_ms";
    public static final String STATS_W_MEAN_SUCCESS_MS = "stats_w_mean_success_ms";
    public static final String STATS_W_MEDIAN_SUCCESS_MS = "stats_w_median_success_ms";
    public static final String STATS_W_NINETY_PERCENTINE_SUCCESS_MS = "stats_w_ninety_percentine_success_ms";
    public static final String STATS_W_NINETYFIVE_PERCENTINE_SUCCESS_MS = "stats_w_ninetyfive_percentine_success_ms";
    public static final String STATS_W_NINETYNINE_PERCENTINE_SUCCESS_MS = "stats_w_ninetynine_percentine_success_ms";
    public static final String STATS_TOTAL_SUCCESSRATE = "stats_total_successrate";
    public static final String STATS_WORKER_CONCURRENCY_MEAN = "stats_worker_concurrency_mean";
    public static final String STATS_COMMAND_CONCURRENCY_MEAN = "stats_command_concurrency_mean";
    public static final String BENCHMARK_REQ_90_PERCENTILE_READ_DURATION_MS = "benchmark_req_90percentile_read_duration_ms";
    public static final String BENCHMARK_REQ_90_PERCENTILE_WRITE_DURATION_MS = "benchmark_req_90percentile_write_duration_ms";
    public static final String BENCHMARK_REQ_95_PERCENTILE_READ_DURATION_MS = "benchmark_req_95percentile_read_duration_ms";
    public static final String BENCHMARK_REQ_95_PERCENTILE_WRITE_DURATION_MS = "benchmark_req_95percentile_write_duration_ms";
    public static final String BENCHMARK_REQ_99_PERCENTILE_READ_DURATION_MS = "benchmark_req_99percentile_read_duration_ms";
    public static final String BENCHMARK_REQ_99_PERCENTILE_WRITE_DURATION_MS = "benchmark_req_99percentile_write_duration_ms";
    public static final String BENCHMARK_REQ_MEAN_READ_DURATION_MS = "benchmark_req_mean_read_duration_ms";
    public static final String BENCHMARK_REQ_MEAN_WRITE_DURATION_MS = "benchmark_req_mean_write_duration_ms";
    public static final String BENCHMARK_REQ_SUCESSRATE_PERCENT = "benchmark_req_sucessrate_percent";
    public static final String TIMESTAMP = "timestamp";
    public static final String IS_BENCHMARK_PASSED = "isBenchmarkPassed";

    private static LoadTestBenchmark loadTestBenchmark = new LoadTestBenchmark();

    public static ObjectMapper mapper() {
        return mapper;
    }

    public static final String DEFAULT_LOAD_TEST_BENCHMARK = "loadtest_setup/benchmarks/DefaultLoadTestBenchmark.json";

    static {
        try {

            InputStream is = Configuration.loadByName(DEFAULT_LOAD_TEST_BENCHMARK);

            loadTestBenchmark = mapper.readValue(is, LoadTestBenchmark.class);
            String loadTestJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(loadTestBenchmark);
            log.info("Loaded LoadtestBenchmark: {} ", loadTestJson);

        } catch (Exception e) {
            log.error("Unable to read default configuration for DefaultLoadTestBenchmark.", e);
        }
    }

    public static boolean hasPassedBenchmark(Map<String, String> statisticsMap) {
        if (statisticsMap == null || statisticsMap.size() < 10) {
            return true;
        }
        return Boolean.valueOf(statisticsMap.get(IS_BENCHMARK_PASSED));

    }

    public static SortedMap<String, String> hasPassedBenchmark(List<LoadTestResult> loadTestResults, boolean whileRunning) {
        SortedMap<String, String> statisticsMap = new TreeMap<String, String>();
        long nowTimestamp = System.currentTimeMillis();
        statisticsMap.put(TIMESTAMP, Long.toString(nowTimestamp));
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
        double r_duration_ms = 0;
        double w_duration_ms = 0;
        int r_results = 0;
        int w_deviations = 0;
        int w_success = 0;
        int w_results = 0;
        int deviations = 0;
        int success = 0;
        int results = 0;
        double r_mean_success_ms = 0;
        double r_median_success_ms = 0;
        double r_ninety_percentine_success_ms = 0;
        double r_ninetyfive_percentine_success_ms = 0;
        double r_ninetynine_percentine_success_ms = 0;
        double w_mean_success_ms = 0;
        double w_median_success_ms = 0;
        double w_ninety_percentine_success_ms = 0;
        double w_ninetyfive_percentine_success_ms = 0;
        double w_ninetynine_percentine_success_ms = 0;
        long workerConcurrencyDegreeSum = 0;
        long commandConcurrencyDegreeSum = 0;
        List<Double> r_times = new ArrayList<Double>();
        List<Double> w_times = new ArrayList<Double>();
        for (LoadTestResult loadTestResult : loadTestResults) {
            if (loadTestResult.getTest_id().startsWith("r-")) {
                r_results++;
                if (loadTestResult.isTest_deviation_flag()) {
                    r_deviations++;
                }
                if (loadTestResult.isTest_success()) {
                    r_success++;
                    r_duration_ms = r_duration_ms + loadTestResult.getTest_duration();
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
                        w_duration_ms = w_duration_ms + loadTestResult.getTest_duration();
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
            workerConcurrencyDegreeSum += loadTestResult.getWorker_concurrency_degree();
            commandConcurrencyDegreeSum += loadTestResult.getCommand_concurrency_degree();
        }

        // Evaluate
        if (r_success > 0) {
            r_mean_success_ms = r_duration_ms / r_success;
            Collections.sort(r_times);
            r_ninety_percentine_success_ms = r_times.get(r_times.size() * 9 / 10);
            r_ninetyfive_percentine_success_ms = r_times.get(r_times.size() * 95 / 100);
            r_ninetynine_percentine_success_ms = r_times.get(r_times.size() * 99 / 100);

            Double[] r_times_array = r_times.toArray(new Double[r_times.size()]);
            if (r_times_array.length % 2 == 0)
                r_median_success_ms = (r_times_array[r_times_array.length / 2] + r_times_array[r_times_array.length / 2 - 1]) / 2;
            else
                r_median_success_ms = r_times_array[r_times_array.length / 2];
        }
        if (w_success > 0) {
            w_mean_success_ms = w_duration_ms / w_success;
            Collections.sort(w_times);
            w_ninety_percentine_success_ms = w_times.get(w_times.size() * 9 / 10);
            w_ninetyfive_percentine_success_ms = w_times.get(w_times.size() * 95 / 100);
            w_ninetynine_percentine_success_ms = w_times.get(w_times.size() * 99 / 100);

            Double[] w_times_array = w_times.toArray(new Double[w_times.size()]);
            if (w_times_array.length % 2 == 0)
                w_median_success_ms = (w_times_array[w_times_array.length / 2] + w_times_array[w_times_array.length / 2 - 1]) / 2;
            else
                w_median_success_ms = w_times_array[w_times_array.length / 2];
        }

        int N = r_results + w_results + results;
        int total_successrate = 100 * ((r_success + w_success + success) / Math.max(1, N));
        double workerConcurrencyDegree = workerConcurrencyDegreeSum / (double) Math.max(1, N);
        double commandConcurrencyDegree = commandConcurrencyDegreeSum / (double) Math.max(1, N);

        statisticsMap.put(STATS_R_DEVIATIONS, Long.toString(r_deviations));
        statisticsMap.put(STATS_R_SUCCESS, Long.toString(r_success));
        statisticsMap.put(STATS_R_DURATION_MS, toRoundedString(r_duration_ms));
        statisticsMap.put(STATS_R_RESULTS, Long.toString(r_results));
        statisticsMap.put(STATS_R_FAILURES, Long.toString(r_results - r_success));
        statisticsMap.put(STATS_W_DEVIATIONS, Long.toString(w_deviations));
        statisticsMap.put(STATS_W_DURATION_MS, toRoundedString(w_duration_ms));
        statisticsMap.put(STATS_W_SUCCESS, Long.toString(w_success));
        statisticsMap.put(STATS_W_RESULTS, Long.toString(w_results));
        statisticsMap.put(STATS_W_FAILURES, Long.toString(w_results - w_success));
        statisticsMap.put(STATS_O_DEVIATIONS, Long.toString(deviations));
        statisticsMap.put(STATS_O_SUCCESS, Long.toString(success));
        statisticsMap.put(STATS_O_RESULTS, Long.toString(results));
        statisticsMap.put(STATS_O_FAILURES, Long.toString(results - success));
        statisticsMap.put(STATS_T_DEVIATIONS, Long.toString(r_deviations + w_deviations + deviations));
        statisticsMap.put(STATS_T_SUCCESS, Long.toString(r_success + w_success + success));
        statisticsMap.put(STATS_T_RESULTS, Long.toString(N));
        statisticsMap.put(STATS_T_FAILURES, Long.toString(N - (r_success + w_success + success)));
        statisticsMap.put(STATS_R_MEAN_SUCCESS_MS, toRoundedString(r_mean_success_ms));
        statisticsMap.put(STATS_R_MEDIAN_SUCCESS_MS, toRoundedString(r_median_success_ms));
        statisticsMap.put(STATS_R_NINETY_PERCENTINE_SUCCESS_MS, toRoundedString(r_ninety_percentine_success_ms));
        statisticsMap.put(STATS_R_NINETYFIVE_PERCENTINE_SUCCESS_MS, toRoundedString(r_ninetyfive_percentine_success_ms));
        statisticsMap.put(STATS_R_NINETYNINE_PERCENTINE_SUCCESS_MS, toRoundedString(r_ninetynine_percentine_success_ms));
        statisticsMap.put(STATS_W_MEAN_SUCCESS_MS, toRoundedString(w_mean_success_ms));
        statisticsMap.put(STATS_W_MEDIAN_SUCCESS_MS, toRoundedString(w_median_success_ms));
        statisticsMap.put(STATS_W_NINETY_PERCENTINE_SUCCESS_MS, toRoundedString(w_ninety_percentine_success_ms));
        statisticsMap.put(STATS_W_NINETYFIVE_PERCENTINE_SUCCESS_MS, toRoundedString(w_ninetyfive_percentine_success_ms));
        statisticsMap.put(STATS_W_NINETYNINE_PERCENTINE_SUCCESS_MS, toRoundedString(w_ninetynine_percentine_success_ms));
        statisticsMap.put(STATS_TOTAL_SUCCESSRATE, Long.toString(total_successrate));
        statisticsMap.put(STATS_WORKER_CONCURRENCY_MEAN, toRoundedString(workerConcurrencyDegree));
        statisticsMap.put(STATS_COMMAND_CONCURRENCY_MEAN, toRoundedString(commandConcurrencyDegree));

        boolean isBenchmarkPassed = true;
        if (loadTestBenchmark.getBenchmark_req_90percentile_read_duration_ms() > 0) {
            if (loadTestBenchmark.getBenchmark_req_90percentile_read_duration_ms() <= r_ninety_percentine_success_ms) {
                log.info("getBenchmark_req_90percentile_read_duration_ms failed");
                statisticsMap.put(BENCHMARK_REQ_90_PERCENTILE_READ_DURATION_MS, Boolean.toString(false));
                isBenchmarkPassed = false;
            } else {
                statisticsMap.put(BENCHMARK_REQ_90_PERCENTILE_READ_DURATION_MS, Boolean.toString(true));
            }
        }
        if (loadTestBenchmark.getBenchmark_req_90percentile_write_duration_ms() > 0) {
            if (loadTestBenchmark.getBenchmark_req_90percentile_write_duration_ms() <= w_ninety_percentine_success_ms) {
                log.info("getBenchmark_req_90percentile_write_duration_ms failed");
                statisticsMap.put(BENCHMARK_REQ_90_PERCENTILE_WRITE_DURATION_MS, Boolean.toString(false));
                isBenchmarkPassed = false;
            } else {
                statisticsMap.put(BENCHMARK_REQ_90_PERCENTILE_WRITE_DURATION_MS, Boolean.toString(true));
            }
        }
        if (loadTestBenchmark.getBenchmark_req_95percentile_read_duration_ms() > 0) {
            if (loadTestBenchmark.getBenchmark_req_95percentile_read_duration_ms() <= r_ninetyfive_percentine_success_ms) {
                log.info("getBenchmark_req_95percentile_read_duration_ms failed");
                statisticsMap.put(BENCHMARK_REQ_95_PERCENTILE_READ_DURATION_MS, Boolean.toString(false));
                isBenchmarkPassed = false;
            } else {
                statisticsMap.put(BENCHMARK_REQ_95_PERCENTILE_READ_DURATION_MS, Boolean.toString(true));
            }
        }
        if (loadTestBenchmark.getBenchmark_req_95percentile_write_duration_ms() > 0) {
            if (loadTestBenchmark.getBenchmark_req_95percentile_write_duration_ms() <= w_ninetyfive_percentine_success_ms) {
                log.info("getBenchmark_req_95percentile_write_duration_ms failed");
                statisticsMap.put(BENCHMARK_REQ_95_PERCENTILE_WRITE_DURATION_MS, Boolean.toString(false));
                isBenchmarkPassed = false;
            } else {
                statisticsMap.put(BENCHMARK_REQ_95_PERCENTILE_WRITE_DURATION_MS, Boolean.toString(true));
            }
        }
        if (loadTestBenchmark.getBenchmark_req_99percentile_read_duration_ms() > 0) {
            if (loadTestBenchmark.getBenchmark_req_99percentile_read_duration_ms() <= r_ninetynine_percentine_success_ms) {
                log.info("getBenchmark_req_99percentile_read_duration_ms failed");
                statisticsMap.put(BENCHMARK_REQ_99_PERCENTILE_READ_DURATION_MS, Boolean.toString(false));
                isBenchmarkPassed = false;
            } else {
                statisticsMap.put(BENCHMARK_REQ_99_PERCENTILE_READ_DURATION_MS, Boolean.toString(true));
            }
        }
        if (loadTestBenchmark.getBenchmark_req_99percentile_write_duration_ms() > 0) {
            if (loadTestBenchmark.getBenchmark_req_90percentile_write_duration_ms() <= w_ninetynine_percentine_success_ms) {
                log.info("getBenchmark_req_99percentile_write_duration_ms failed");
                statisticsMap.put(BENCHMARK_REQ_99_PERCENTILE_WRITE_DURATION_MS, Boolean.toString(false));
                isBenchmarkPassed = false;
            } else {
                statisticsMap.put(BENCHMARK_REQ_99_PERCENTILE_WRITE_DURATION_MS, Boolean.toString(true));
            }
        }
        if (loadTestBenchmark.getBenchmark_req_mean_read_duration_ms() > 0) {
            if (loadTestBenchmark.getBenchmark_req_mean_read_duration_ms() <= r_mean_success_ms) {
                log.info("getBenchmark_req_mean_read_duration_ms failed");
                statisticsMap.put(BENCHMARK_REQ_MEAN_READ_DURATION_MS, Boolean.toString(false));
                isBenchmarkPassed = false;
            } else {
                statisticsMap.put(BENCHMARK_REQ_MEAN_READ_DURATION_MS, Boolean.toString(true));
            }
        }
        if (loadTestBenchmark.getBenchmark_req_mean_write_duration_ms() > 0) {
            if (loadTestBenchmark.getBenchmark_req_mean_write_duration_ms() <= w_mean_success_ms) {
                log.info("getBenchmark_req_mean_write_duration_ms failed");
                statisticsMap.put(BENCHMARK_REQ_MEAN_WRITE_DURATION_MS, Boolean.toString(false));
                isBenchmarkPassed = false;
            } else {
                statisticsMap.put(BENCHMARK_REQ_MEAN_WRITE_DURATION_MS, Boolean.toString(true));
            }
        }
        if (loadTestBenchmark.getBenchmark_req_sucessrate_percent() > 0) {
            if (loadTestBenchmark.getBenchmark_req_sucessrate_percent() > total_successrate) {
                log.info("getBenchmark_req_sucessrate_percent failed, req:{}, measured: {}", loadTestBenchmark.getBenchmark_req_sucessrate_percent(), total_successrate);
                statisticsMap.put(BENCHMARK_REQ_SUCESSRATE_PERCENT, Boolean.toString(false));
                isBenchmarkPassed = false;
            } else {
                statisticsMap.put(BENCHMARK_REQ_SUCESSRATE_PERCENT, Boolean.toString(true));
            }
        }
        statisticsMap.put(IS_BENCHMARK_PASSED, Boolean.toString(isBenchmarkPassed));
        return statisticsMap;
    }

    public static String toRoundedString(double w_ninetynine_percentine_success_ms) {
        return new BigDecimal(w_ninetynine_percentine_success_ms).setScale(3, RoundingMode.HALF_UP).toPlainString();
    }

    public static String printStats(List<LoadTestResult> loadTestResults, boolean whileRunning) {
        Map<String, String> statsMap = hasPassedBenchmark(loadTestResults, whileRunning);
        DateFormat df = new SimpleDateFormat("dd/MM-yyyy  HH:mm:ss");
        String stats;
        if (LoadTestExecutorService.getLoadTestRunNo() > 0) {
            if (LoadTestExecutorService.getStopTime() == 0) {
                stats = "Started: " + df.format(new Date(LoadTestExecutorService.getStartTime())) + " Version:" + HealthResource.getVersion() + "  Now: " + df.format(new Date(Long.parseLong(statsMap.get(TIMESTAMP)))) + "  Running for " + (Long.parseLong(statsMap.get("timestamp")) - LoadTestExecutorService.getStartTime()) / 1000 + " seconds.\n";
            } else {
                stats = "Started: " + df.format(new Date(LoadTestExecutorService.getStartTime())) + " Version:" + HealthResource.getVersion() + "  Now: " + df.format(new Date(Long.parseLong(statsMap.get(TIMESTAMP)))) + "  Ran for " + (LoadTestExecutorService.getStopTime() - LoadTestExecutorService.getStartTime()) / 1000 + " seconds.\n";
            }
        } else {
            stats = "Started: " + df.format(new Date(Long.parseLong(statsMap.get(TIMESTAMP)))) + " Version:" + HealthResource.getVersion() + "  Now: " + df.format(new Date(Long.parseLong(statsMap.get(TIMESTAMP)))) + "\n";
        }

        if (statsMap.size() > 10) {
            stats = stats + "\n" + String.format(" %7d read tests resulted in %d successful runs where %d was marked failure and %d was marked as deviation(s).",
                    Integer.parseInt(statsMap.get(STATS_R_RESULTS)), Integer.parseInt(statsMap.get(STATS_R_SUCCESS)), Integer.parseInt(statsMap.get(STATS_R_FAILURES)), Integer.parseInt(statsMap.get(STATS_R_DEVIATIONS)));
            stats = stats + "\n" + String.format(" %7d write tests resulted in %d successful runs where %d was marked failure and %d was marked as deviation(s).",
                    Integer.parseInt(statsMap.get(STATS_W_RESULTS)), Integer.parseInt(statsMap.get(STATS_W_SUCCESS)), Integer.parseInt(statsMap.get(STATS_W_FAILURES)), Integer.parseInt(statsMap.get(STATS_W_DEVIATIONS)));
            stats = stats + "\n" + String.format(" %7d unmarked tests resulted in %d successful runs where %d was marked failure and  %d was marked as deviation(s).",
                    Integer.parseInt(statsMap.get(STATS_O_RESULTS)), Integer.parseInt(statsMap.get(STATS_O_SUCCESS)), Integer.parseInt(statsMap.get(STATS_O_FAILURES)), Integer.parseInt(statsMap.get(STATS_O_DEVIATIONS)));
            stats = stats + "\n" + String.format(" %7d total tests resulted in %d successful runs where %d was marked failure and %d was marked as deviation(s).",
                    Integer.parseInt(statsMap.get(STATS_T_RESULTS)), Integer.parseInt(statsMap.get(STATS_T_SUCCESS)), Integer.parseInt(statsMap.get(STATS_T_FAILURES)), Integer.parseInt(statsMap.get(STATS_T_DEVIATIONS)));
            stats = stats + "\n" + String.format(" %7d tasks scheduled, number of threads configured: %d, isRunning: %b ",
                    LoadTestExecutorService.getTasksScheduled(), LoadTestExecutorService.getThreadPoolSize(), LoadTestExecutorService.isRunning());
            stats = stats + "\n" + String.format(" %7.3g ms mean duration, %4.3g ms 90%% percentile, %4.3g ms 95%% percentile, %4.3g ms 99%% percentile successful read tests",
                    Double.parseDouble(statsMap.get(STATS_R_MEAN_SUCCESS_MS)), Double.parseDouble(statsMap.get(STATS_R_NINETY_PERCENTINE_SUCCESS_MS)), Double.parseDouble(statsMap.get(STATS_R_NINETYFIVE_PERCENTINE_SUCCESS_MS)), Double.parseDouble(statsMap.get(STATS_R_NINETYNINE_PERCENTINE_SUCCESS_MS)));
            stats = stats + "\n" + String.format(" %7.3g ms mean duration, %4.3g ms 90%% percentile, %4.3g ms 95%% percentile, %4.3g ms 99%% percentile successful write tests",
                    Double.parseDouble(statsMap.get(STATS_W_MEAN_SUCCESS_MS)), Double.parseDouble(statsMap.get(STATS_W_NINETY_PERCENTINE_SUCCESS_MS)), Double.parseDouble(statsMap.get(STATS_W_NINETYFIVE_PERCENTINE_SUCCESS_MS)), Double.parseDouble(statsMap.get(STATS_W_NINETYNINE_PERCENTINE_SUCCESS_MS)));
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

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

        try {


            File directory = new File(RESULT_FILE_PATH);
            if (!directory.exists()) {
                directory.mkdir();
                // If you require it to make the entire directory path including parents,
            }
            PrintWriter jsonwriter = new PrintWriter(RESULT_FILE_PATH + File.separator + LoadTestExecutorService.getActiveLoadTestConfig().getTest_id() + "_" + df.format(new Date(LoadTestExecutorService.getStartTime())) + ".json", "UTF-8");
            jsonwriter.println(LoadTestResource.getJsonResultString());
            jsonwriter.close();
            PrintWriter cvswriter = new PrintWriter(RESULT_FILE_PATH + File.separator + LoadTestExecutorService.getActiveLoadTestConfig().getTest_id() + "_" + df.format(new Date(LoadTestExecutorService.getStartTime())) + ".csv", "UTF-8");
            cvswriter.println(LoadTestResource.getCSVResultString());
            cvswriter.close();


            // Persist the specifications for the test-run
            PrintWriter readSpecWriter = new PrintWriter(RESULT_FILE_PATH + File.separator + LoadTestExecutorService.getActiveLoadTestConfig().getTest_id() + "_" + df.format(new Date(LoadTestExecutorService.getStartTime())) + "_read_specififation.json", "UTF-8");
            readSpecWriter.println(LoadTestExecutorService.getReadTestSpecificationListJson());
            readSpecWriter.close();

            PrintWriter writeSpecWriter = new PrintWriter(RESULT_FILE_PATH + File.separator + LoadTestExecutorService.getActiveLoadTestConfig().getTest_id() + "_" + df.format(new Date(LoadTestExecutorService.getStartTime())) + "_write_specififation.json", "UTF-8");
            writeSpecWriter.println(LoadTestExecutorService.getWriteTestSpecificationListJson());
            writeSpecWriter.close();

            PrintWriter loadSpecWriter = new PrintWriter(RESULT_FILE_PATH + File.separator + LoadTestExecutorService.getActiveLoadTestConfig().getTest_id() + "_" + df.format(new Date(LoadTestExecutorService.getStartTime())) + "_load_specififation.json", "UTF-8");
            loadSpecWriter.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(LoadTestExecutorService.getActiveLoadTestConfig()));
            loadSpecWriter.close();

            PrintWriter benchmarkSpecWriter = new PrintWriter(RESULT_FILE_PATH + File.separator + LoadTestExecutorService.getActiveLoadTestConfig().getTest_id() + "_" + df.format(new Date(LoadTestExecutorService.getStartTime())) + "_benchmark_specififation.json", "UTF-8");
            benchmarkSpecWriter.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(LoadTestResultUtil.getLoadTestBenchmark()));
            benchmarkSpecWriter.close();

            // Also persist benchmark result
            Map<String, String> resultMap = LoadTestResultUtil.hasPassedBenchmark(LoadTestExecutorService.getResultListSnapshot(), true);
            PrintWriter benchmarkwriter = new PrintWriter(RESULT_FILE_PATH + File.separator + LoadTestExecutorService.getActiveLoadTestConfig().getTest_id() + "_" + df.format(new Date(LoadTestExecutorService.getStartTime())) + "_benchmark_result.json", "UTF-8");
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