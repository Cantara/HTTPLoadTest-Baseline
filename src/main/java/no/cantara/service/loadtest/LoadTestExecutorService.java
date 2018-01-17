package no.cantara.service.loadtest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.netflix.hystrix.HystrixCommandProperties;
import no.cantara.service.Main;
import no.cantara.service.loadtest.drivers.MyReadRunnable;
import no.cantara.service.loadtest.drivers.MyRunnable;
import no.cantara.service.loadtest.drivers.MyWriteRunnable;
import no.cantara.service.loadtest.util.LoadTestResultUtil;
import no.cantara.service.loadtest.util.LoadTestThreadPool;
import no.cantara.service.loadtest.util.TimedProcessingUtil;
import no.cantara.service.model.LoadTestConfig;
import no.cantara.service.model.LoadTestResult;
import no.cantara.service.model.TestSpecification;
import no.cantara.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LoadTestExecutorService {

    private static final Logger log = LoggerFactory.getLogger(LoadTestExecutorService.class);
    public static final int THREAD_READINESS_FACTOR = 10;
    public static final int LOAD_TEST_RAMPDOWN_TIME_MS = 50;
    public static final String RESULT_FILE_PATH = "./results";
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
    private static LoadTestThreadPool runTaskThreadPool = new LoadTestThreadPool(7);


    private static final Map<String, Object> configMap;

    private static int threadsScheduled = 0;
    private static int tasksStarted = 0;
    private static int threadPoolSize = 0;

    static {

        if (Configuration.getBoolean("loadtest.cluster")) {
            InputStream xmlFileName = Configuration.loadByName("hazelcast.xml");
            //        log.info("Loaded hazelcast configuration :" + xmlFileName);
            Config hazelcastConfig = new XmlConfigBuilder(xmlFileName).build();
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

    public static List<TestSpecification> getReadTestSpecificationList() {
        return readTestSpecificationList;
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
    }


    public static List getResultList() {
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
            TimedProcessingUtil.runWithTimeout(new Callable<String>() {
                @Override
                public String call() {
                    logTimedCode(startTime, loadTestConfig.getTest_id() + " - starting processing! max duration:" + loadTestConfig.getTest_duration_in_seconds());
                    runTask(loadTestConfig);
                    logTimedCode(startTime, loadTestConfig.getTest_id() + " - processing completed!");
                    return "";
                }
            }, loadTestConfig.getTest_duration_in_seconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Exception {}", e);
            logTimedCode(startTime, loadTestConfig.getTest_id() + " - LoadTestConfig was interrupted!");
        }
        log.info("Async LoadTest {} completed, ran for {} seconds, max running time: {} seconds", activeLoadTestConfig.getTest_id(), (System.currentTimeMillis() - startTime) / 1000, activeLoadTestConfig.getTest_duration_in_seconds());
        stop();
    }


    private static void runTask(LoadTestConfig loadTestConfig) {
        int runNo = 1;

        threadPoolSize = loadTestConfig.getTest_no_of_threads();
        runTaskThreadPool = new LoadTestThreadPool(threadPoolSize);
        int read_ratio = loadTestConfig.getTest_read_write_ratio();

        while (isRunning) {
            int chance = r.nextInt(100);
            long maxRunTimeMs = startTime + loadTestConfig.getTest_duration_in_seconds() * 1000 - System.currentTimeMillis();
            if ((maxRunTimeMs > LOAD_TEST_RAMPDOWN_TIME_MS) && isRunning && (threadsScheduled < (loadTestConfig.getTest_no_of_threads() * THREAD_READINESS_FACTOR))) {
                // We stop a little before known timeout, we quit on stop-signal... and we schedule max 10*the configured number of threads to avoid overusing memory
                // for long (endurance) loadtest runs
                log.info("MaxRunInMilliSeconds: {}, LOAD_TEST_RAMPDOWN_TIME_MS:{} , threadsScheduled: {}, maxThreadsAllowed: {}, taskthreads Created: {}", maxRunTimeMs, LOAD_TEST_RAMPDOWN_TIME_MS, threadsScheduled, (loadTestConfig.getTest_no_of_threads() * THREAD_READINESS_FACTOR), tasksStarted);
                if (read_ratio == 0) {
                    String url = "http://localhost:" + Main.PORT_NO + Main.CONTEXT_PATH;
                    LoadTestResult loadTestResult = new LoadTestResult();
                    loadTestResult.setTest_id(loadTestConfig.getTest_id());
                    loadTestResult.setTest_name(loadTestConfig.getTest_name());
                    loadTestResult.setTest_run_no(runNo++);
                    Runnable worker = new MyRunnable(url, loadTestResult);
                    threadsScheduled++;
                    tasksStarted++;
                    runTaskThreadPool.execute(worker);
                } else if (chance <= read_ratio) {
                    LoadTestResult loadTestResult = new LoadTestResult();
                    loadTestResult.setTest_id("r-" + loadTestConfig.getTest_id());
                    loadTestResult.setTest_name(loadTestConfig.getTest_name());
                    loadTestResult.setTest_run_no(runNo++);
                    Runnable worker = new MyReadRunnable(readTestSpecificationList, loadTestConfig, loadTestResult);
                    threadsScheduled++;
                    tasksStarted++;
                    runTaskThreadPool.execute(worker);
                } else {
                    LoadTestResult loadTestResult = new LoadTestResult();
                    loadTestResult.setTest_id("w-" + loadTestConfig.getTest_id());
                    loadTestResult.setTest_name(loadTestConfig.getTest_name());
                    loadTestResult.setTest_run_no(runNo++);
                    Runnable worker = new MyWriteRunnable(writeTestSpecificationList, loadTestConfig, loadTestResult);
                    threadsScheduled++;
                    tasksStarted++;
                    runTaskThreadPool.execute(worker);

                }
            }
        }
        stop();
    }


    public static boolean isRunning() {
        return isRunning;
    }

    public static void stop() {
        isRunning = false;
        stopTime = System.currentTimeMillis();
        threadsScheduled = 0;
        tasksStarted = 0;
        LoadTestResultUtil.storeResultToFiles();
    }


    public static synchronized void reduceThreadsScheduled() {
        if (threadsScheduled > 0) {
            threadsScheduled = threadsScheduled - 1;
            log.info("scheduledThreads= {}, reduced", threadsScheduled);
        } else {
            threadsScheduled = 0;
            log.info("scheduledThreads=0");
            if (((System.currentTimeMillis() - startTime) / 1000) < activeLoadTestConfig.getTest_duration_in_seconds()) {
                log.info("scheduledThreads=0, LoadTestRun completed");
                //stop();
            }
        }
    }

    public static long getStartTime() {
        return startTime;
    }

    public static long getStopTime() {
        return stopTime;
    }

    public static LoadTestConfig getActiveLoadTestConfig() {
        return activeLoadTestConfig;
    }

    public static int getThreadsScheduled() {
        return threadsScheduled;
    }

    public static int getLoadTestRunNo() {
        return loadTestRunNo;
    }

    public static int getThreadPoolSize() {
        return threadPoolSize;
    }

    private static void logTimedCode(long startTime, String msg) {
        long elapsedSeconds = (System.currentTimeMillis() - startTime);
        log.info("{}ms [{}] {}\n", elapsedSeconds, Thread.currentThread().getName(), msg);
    }

}
