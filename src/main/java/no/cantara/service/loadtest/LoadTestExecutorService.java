package no.cantara.service.loadtest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.netflix.hystrix.HystrixCommandProperties;
import no.cantara.service.Main;
import no.cantara.service.health.HealthResource;
import no.cantara.service.loadtest.drivers.MyReadRunnable;
import no.cantara.service.loadtest.drivers.MyRunnable;
import no.cantara.service.loadtest.drivers.MyWriteRunnable;
import no.cantara.service.model.LoadTestConfig;
import no.cantara.service.model.LoadTestResult;
import no.cantara.service.model.TestSpecification;
import no.cantara.service.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class LoadTestExecutorService {

    private static final Logger log = LoggerFactory.getLogger(LoadTestResource.class);
    public static final int THREAD_READINESS_FACTOR = 2;
    private static List<LoadTestResult> unsafeList = new ArrayList<>();
    private static List<LoadTestResult> resultList;// = Collections.synchronizedList(unsafeList);
    private static List<TestSpecification> readTestSpecificationList;
    private static List<TestSpecification> writeTestSpecificationList;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Random r = new Random();
    private static long startTime = System.currentTimeMillis();
    private static long stopTime = 0;
    private static int loadTestRunNo = 0;
    private static boolean isRunning = false;
    private static LoadTestConfig activeLoadTestConfig;
    private static ExecutorService threadExecutor;
    private static final Map<String, Object> configMap;

    private static int threadsScheduled = 0;
    private static int threadPoolSize = 0;

    static {

        if (Configuration.getBoolean("loadtest.cluster")) {
            InputStream xmlFileName = Configuration.loadByName("hazelcast.xml");
//        log.info("Loaded hazelcast configuration :" + xmlFileName);
            Config hazelcastConfig  = new XmlConfigBuilder(xmlFileName).build();
            //       log.info("Loading hazelcast configuration from :" + xmlFileName);

            hazelcastConfig.setProperty("hazelcast.logging.type", "slf4j");
            HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(hazelcastConfig);

            resultList = hazelcastInstance.getList("results");
            log.info("Connecting to list {} - map size: {}", "results", resultList.size());
            configMap = hazelcastInstance.getMap("configmap");
            log.info("Connecting to map {} - map size: {}", "config", configMap.size());
        } else {
            resultList = Collections.synchronizedList(unsafeList);
            configMap = new HashMap<>();
        }
        if (configMap.size() == 0) {
            try {

                InputStream is = Configuration.loadByName("DefaultReadTestSpecification.json");
                readTestSpecificationList = mapper.readValue(is, new TypeReference<List<TestSpecification>>() {
                });
                String jsonreadconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readTestSpecificationList);
                log.info("Loaded DefaultReadTestSpecification: {}", jsonreadconfig);
                InputStream wis = Configuration.loadByName("DefaultWriteTestSpecification.json");

                writeTestSpecificationList = mapper.readValue(wis, new TypeReference<List<TestSpecification>>() {
                });
                String jsonwriteconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(writeTestSpecificationList);
                log.info("Loaded DefaultWriteTestSpecification: {}", jsonwriteconfig);

                updateSpecMap();
            } catch (Exception e) {
                log.error("Unable to read default configuration for LoadTest.", e);
            }
            // Found other cluster nodes, loading speci from them
            getSpecFromMap();
        }

    }

    private static void getSpecFromMap() {
        if (Configuration.getBoolean("loadtest.cluster")) {
            readTestSpecificationList = (List<TestSpecification>) configMap.get("readTestSpecificationList");
            writeTestSpecificationList = (List<TestSpecification>) configMap.get("writeTestSpecificationList");
            activeLoadTestConfig = (LoadTestConfig) configMap.get("activeLoadTestConfig");
        }
    }

    private static void updateSpecMap() {
        if (Configuration.getBoolean("loadtest.cluster")) {
            configMap.put("readTestSpecificationList", readTestSpecificationList);
            configMap.put("writeTestSpecificationList", writeTestSpecificationList);
            configMap.put("activeLoadTestConfig", activeLoadTestConfig);
        }
    }

    public static List<TestSpecification> getReadTestSpecificationList() {
        return readTestSpecificationList;
    }

    public static String getReadTestSpecificationListJson() {
        String result = "[]";
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readTestSpecificationList);

        } catch (Exception e) {
            log.error("Unable to create json of readTestSpecification", e);
            return result;
        }
    }

    public static String getWriteTestSpecificationListJson() {
        String result = "[]";
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(writeTestSpecificationList);

        } catch (Exception e) {
            log.error("Unable to create json of writeTestSpecification", e);
            return result;
        }
    }

    public static void setReadTestSpecificationList(List<TestSpecification> readTestSpecificationList) {
        LoadTestExecutorService.readTestSpecificationList = readTestSpecificationList;
        updateSpecMap();

    }

    public static List<TestSpecification> getWriteTestSpecificationList() {
        return writeTestSpecificationList;
    }

    public static void setWriteTestSpecificationList(List<TestSpecification> writeTestSpecificationList) {
        LoadTestExecutorService.writeTestSpecificationList = writeTestSpecificationList;
        updateSpecMap();

    }

    public static synchronized void addResult(LoadTestResult loadTestResult) {
        resultList.add(loadTestResult);
        reduceThreadsScheduled();

//        LoadTestExecutorService.reduceThreadsScheduled();

        //log.info("ResultMapSize: {}", resultList.size());
    }


    public static List getResultList() {
        //List<LoadTestResult> copyList = new LinkedList<>();
        //for (LoadTestResult loadTestResult : resultList) {
        //    copyList.add(loadTestResult);
        //}

        return resultList;
    }

    public static List getLatestResultList() {
        return resultList.subList(Math.max(resultList.size() - 50, 0), resultList.size());
    }

    public static synchronized void executeLoadTest(LoadTestConfig loadTestConfig, boolean asNewThread) {
        /**
         * IExecutorService executor = hz.getExecutorService("executor");
         for (Integer key : map.keySet())
         executor.executeOnKeyOwner(new YourBigTask(), key);
         */
        unsafeList = new ArrayList<>();
        resultList = Collections.synchronizedList(unsafeList);
        HystrixCommandProperties.Setter().withFallbackIsolationSemaphoreMaxConcurrentRequests(loadTestConfig.getTest_no_of_threads());
        loadTestRunNo++;
        activeLoadTestConfig = loadTestConfig;
        updateSpecMap();

        long loadtestStartTimestamp = System.currentTimeMillis();
        isRunning = true;
        log.info("LoadTest {} started, max running time: {} secomnds", activeLoadTestConfig.getTest_id(), activeLoadTestConfig.getTest_duration_in_seconds());
        if (asNewThread) {
            ExecutorService loadTestExecutor = Executors.newFixedThreadPool(1);
            loadTestExecutor.submit(new Callable<Object>() {
                                        @Override
                                        public Object call() throws Exception {
                                            runLoadTest(loadTestConfig);  //runnable.run();
                                            return null;
                                        }
                                    }
            );
        } else {
            runLoadTest(loadTestConfig);
        }
        log.info("LoadTest {} completed, ran for {} seconds, max running time: {} seconds", activeLoadTestConfig.getTest_id(), (System.currentTimeMillis() - loadtestStartTimestamp) / 1000, activeLoadTestConfig.getTest_duration_in_seconds());
    }


    private static void runLoadTest(LoadTestConfig loadTestConfig) {

        startTime = System.currentTimeMillis();
        stopTime = 0;
        try {
            runWithTimeout(new Callable<String>() {
                @Override
                public String call() {
                    logTimedCode(startTime, loadTestConfig.getTest_id() + " - starting processing! max duration:" + loadTestConfig.getTest_duration_in_seconds());
                    runTask(loadTestConfig);
                    logTimedCode(startTime, loadTestConfig.getTest_id() + " - processing completed!");
                    return "";
                }
            }, loadTestConfig.getTest_duration_in_seconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            logTimedCode(startTime, loadTestConfig.getTest_id() + " - was interrupted!");
        }
        log.info("Async LoadTest {} completed, ran for {} seconds, max running time: {} seconds", activeLoadTestConfig.getTest_id(), (System.currentTimeMillis() - startTime) / 1000, activeLoadTestConfig.getTest_duration_in_seconds());
        isRunning = false;
        stopTime = System.currentTimeMillis();
        threadsScheduled = 0;
        threadExecutor.shutdown();
    }


    private static void runTask(LoadTestConfig loadTestConfig) {
        int runNo = 1;

        threadExecutor = Executors.newFixedThreadPool(loadTestConfig.getTest_no_of_threads());
        threadPoolSize = loadTestConfig.getTest_no_of_threads();
        int read_ratio = loadTestConfig.getTest_read_write_ratio();

        while (isRunning) {


            int chance = r.nextInt(100);
            long maxRunTimeMs = startTime + loadTestConfig.getTest_duration_in_seconds() * 1000 - System.currentTimeMillis();
            if (maxRunTimeMs > 50 && isRunning && threadsScheduled < (loadTestConfig.getTest_no_of_threads() * THREAD_READINESS_FACTOR)) {
                // We stop a little before known timeout, we quit on stop-signal... and we schedule max 10*the configured number of threads to avoid overusing memory
                // for long (endurance) loadtest runs
                log.info("MaxRunInMilliSeconds: {}, threadsScheduled: {}", maxRunTimeMs, threadsScheduled);
                if (read_ratio == 0) {
                    String url = "http://localhost:" + Main.PORT_NO + Main.CONTEXT_PATH;
                    LoadTestResult loadTestResult = new LoadTestResult();
                    loadTestResult.setTest_id(loadTestConfig.getTest_id());
                    loadTestResult.setTest_name(loadTestConfig.getTest_name());
                    loadTestResult.setTest_run_no(runNo++);
                    Runnable worker = new MyRunnable(url, loadTestResult);
                    threadsScheduled++;
                    try {
                        runWithTimeout(new Callable<String>() {
                            @Override
                            public String call() {
                                threadExecutor.execute(worker);
                                return "";

                            }
                        }, maxRunTimeMs, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        logTimedCode(startTime, loadTestConfig.getTest_id() + " - was interrupted!");
                        LoadTestExecutorService.reduceThreadsScheduled();
                    }

//                    threadExecutor.execute(worker);

                } else if (chance <= read_ratio) {
                    LoadTestResult loadTestResult = new LoadTestResult();
                    loadTestResult.setTest_id("r-" + loadTestConfig.getTest_id());
                    loadTestResult.setTest_name(loadTestConfig.getTest_name());
                    loadTestResult.setTest_run_no(runNo++);
                    Runnable worker = new MyReadRunnable(readTestSpecificationList, loadTestConfig, loadTestResult);
                    threadsScheduled++;

                    try {
                        runWithTimeout(new Callable<String>() {
                            @Override
                            public String call() {
                                threadExecutor.execute(worker);
                                return "";
                            }
                        }, maxRunTimeMs, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        logTimedCode(startTime, loadTestConfig.getTest_id() + " - was interrupted!");
                        LoadTestExecutorService.reduceThreadsScheduled();
                    }
//                    threadExecutor.execute(worker);

                } else {
                    LoadTestResult loadTestResult = new LoadTestResult();
                    loadTestResult.setTest_id("w-" + loadTestConfig.getTest_id());
                    loadTestResult.setTest_name(loadTestConfig.getTest_name());
                    loadTestResult.setTest_run_no(runNo++);
                    Runnable worker = new MyWriteRunnable(writeTestSpecificationList, loadTestConfig, loadTestResult);
                    threadsScheduled++;
                    try {
                        runWithTimeout(new Callable<String>() {
                            @Override
                            public String call() {
                                threadExecutor.execute(worker);
                                return "";
                            }
                        }, maxRunTimeMs, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        logTimedCode(startTime, loadTestConfig.getTest_id() + " - was interrupted!");
                        LoadTestExecutorService.reduceThreadsScheduled();
                    }
//                    threadExecutor.execute(worker);

                }
            }
        }


//        }
//        System.out.println("\nFinished all threads");
//        threadsScheduled = 0;
        threadExecutor.shutdown();
        stopTime = System.currentTimeMillis();
    }

    public static synchronized String printStats(List<LoadTestResult> loadTestResults) {
        if (loadTestResults == null) {
            return "";
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
        List<Long> r_times = new ArrayList<>();
        List<Long> w_times = new ArrayList<>();
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
        long nowTimestamp = System.currentTimeMillis();
        String stats;
        if (loadTestRunNo > 0) {
            if (stopTime == 0) {
                stats = "Started: " + df.format(new Date(startTime)) +" Version:"+ HealthResource.getVersion()+ "  Now: " + df.format(new Date(nowTimestamp)) + "  Running for " + (nowTimestamp - startTime) / 1000 + " seconds.\n";
            } else {
                stats = "Started: " + df.format(new Date(startTime)) +" Version:"+ HealthResource.getVersion()+ "  Now: " + df.format(new Date(nowTimestamp)) + "  Ran for " + (stopTime - startTime) / 1000 + " seconds.\n";
            }
        } else {
            stats = "Started: " + df.format(new Date(nowTimestamp)) + " Version:"+ HealthResource.getVersion()+"  Now: " + df.format(new Date(nowTimestamp)) + "\n";
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
        stats = stats + "\n" + String.format(" %4d read tests resulted in %d successful runs where %d was marked failure and %d was marked as deviation(s).", r_results, r_success, (r_results - r_success), r_deviations);
        stats = stats + "\n" + String.format(" %4d write tests resulted in %d successful runs where %d was marked failure and %d was marked as deviation(s).", w_results, w_success, (w_results - w_success), w_deviations);
        stats = stats + "\n" + String.format(" %4d unmarked tests resulted in %d successful runs where %d was marked failure and  %d was marked as deviation(s).", results, success, (results - success), deviations);
        stats = stats + "\n" + String.format(" %4d total tests resulted in %d successful runs where %d was marked failure and %d was marked as deviation(s).", r_results + w_results + results, r_success + w_success + success, (r_results + w_results + results) - (r_success + w_success + success), r_deviations + w_deviations + deviations);
        stats = stats + "\n" + String.format(" %4d active tests threads scheduled, number of threads configured: %d,  isRunning: %b ", threadsScheduled, threadPoolSize, isRunning);
        stats = stats + "\n" + String.format(" %4d ms mean duraction for successful read tests, %4d ms ninety percentile successful read tests ", r_mean_success, r_ninety_percentine_success);
        stats = stats + "\n" + String.format(" %4d ms mean duraction for successful write tests, %4d ms ninety percentile successful write tests ", w_mean_success, w_ninety_percentine_success);

        String loadTestJson = "";
        if (activeLoadTestConfig != null) {
            try {
                loadTestJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(activeLoadTestConfig);
                loadTestJson = loadTestJson + "\n\n";

            } catch (Exception e) {
                log.warn("Unable to serialize loadTestConfig to json", e);
            }
        }
        return stats + "\n\n" + loadTestJson;
    }

    private static void runWithTimeout(final Runnable runnable, long timeout, TimeUnit timeUnit) throws Exception {
        runWithTimeout(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                runnable.run();
                return null;
            }
        }, timeout, timeUnit);
    }

    private static <T> T runWithTimeout(Callable<T> callable, long timeout, TimeUnit timeUnit) throws Exception {
        if (isRunning) {
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            final Future<T> future = executor.submit(callable);
            executor.shutdown(); // This does not cancel the already-scheduled task.
            try {
                return future.get(timeout, timeUnit);
            } catch (TimeoutException e) {
                //remove this if you do not want to cancel the job in progress
                //or set the argument to 'false' if you do not want to interrupt the thread
                future.cancel(true);
                reduceThreadsScheduled();
                throw e;
            } catch (ExecutionException e) {
                //unwrap the root cause
                reduceThreadsScheduled();
                Throwable t = e.getCause();
                if (t instanceof Error) {
                    throw (Error) t;
                } else if (t instanceof Exception) {
                    throw (Exception) t;
                } else {
                    throw new IllegalStateException(t);
                }
            }

        }
        return null;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public static void stop() {
        isRunning = false;
        threadExecutor.shutdown();
        stopTime = System.currentTimeMillis();
        threadsScheduled = 0;
    }


    private static synchronized void reduceThreadsScheduled() {
        if (threadsScheduled > 0) {
            threadsScheduled = threadsScheduled - 1;
        } else {
            threadsScheduled = 0;
            isRunning = false;
            threadExecutor.shutdown();
            stopTime = System.currentTimeMillis();
        }
    }


    private static void logTimedCode(long startTime, String msg) {
        long elapsedSeconds = (System.currentTimeMillis() - startTime);
        log.info("{}ms [{}] {}\n", elapsedSeconds, Thread.currentThread().getName(), msg);
    }
}
