package no.cantara.service.loadtest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.netflix.hystrix.HystrixCommandProperties;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class LoadTestExecutorService {

    private static final Logger log = LoggerFactory.getLogger(LoadTestExecutorService.class);

    public static final String RESULT_FILE_PATH = "./results";

    private static final AtomicReference<List<LoadTestResult>> unsafeList = new AtomicReference<>(new ArrayList<>());
    private static final AtomicReference<List<LoadTestResult>> resultList = new AtomicReference<>();
    private static final AtomicReference<List<TestSpecification>> readTestSpecificationList = new AtomicReference<>();
    private static final AtomicReference<List<TestSpecification>> writeTestSpecificationList = new AtomicReference<>();

    private static final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
    private static final AtomicInteger loadTestRunNo = new AtomicInteger(0);
    private static final AtomicReference<LoadTestConfig> activeLoadTestConfig = new AtomicReference<>();

    private static final AtomicReference<SingleLoadTestExecution> activeSingleLoadTestExecution = new AtomicReference<>();

    private static final Map<String, Object> configMap;

    private static final ObjectMapper mapper = new ObjectMapper();

    static {

        if (Configuration.getBoolean("loadtest.cluster")) {
            InputStream xmlFileName = Configuration.loadByName("hazelcast.xml");
            //        log.info("Loaded hazelcast configuration :" + xmlFileName);
            Config hazelcastConfig = new XmlConfigBuilder(xmlFileName).build();
            //       log.info("Loading hazelcast configuration from :" + xmlFileName);

            hazelcastConfig.setProperty("hazelcast.logging.type", "slf4j");
            HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(hazelcastConfig);

            resultList.set(hazelcastInstance.getList("results"));
            log.info("Connecting to list {} - map size: {}", "results", resultList.get().size());
            configMap = hazelcastInstance.getMap("configmap");
            log.info("Connecting to map {} - map size: {}", "config", configMap.size());
        } else {
            resultList.set(Collections.synchronizedList(unsafeList.get()));
            configMap = Collections.synchronizedMap(new HashMap<>());
        }
        if (configMap.size() == 0) {
            try {

                InputStream is = Configuration.loadByName("DefaultReadTestSpecification.json");
                readTestSpecificationList.set(mapper.readValue(is, new TypeReference<List<TestSpecification>>() {
                }));
                String jsonreadconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readTestSpecificationList.get());
                log.info("Loaded DefaultReadTestSpecification: {}", jsonreadconfig);
                InputStream wis = Configuration.loadByName("DefaultWriteTestSpecification.json");

                writeTestSpecificationList.set(mapper.readValue(wis, new TypeReference<List<TestSpecification>>() {
                }));
                String jsonwriteconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(writeTestSpecificationList.get());
                log.info("Loaded DefaultWriteTestSpecification: {}", jsonwriteconfig);

                updateSpecMap();
            } catch (Exception e) {
                log.error("Unable to read default configuration for LoadTest.", e);
            }
            // Found other cluster nodes, loading speci from them
            getSpecFromMap();
        }

        LoadTestConfig zeroTestConfig = new LoadTestConfig();
        zeroTestConfig.setTest_id("zero");
        zeroTestConfig.setTest_duration_in_seconds(0);
        zeroTestConfig.setTest_global_variables_map(new HashMap<>());
        zeroTestConfig.setTest_name("Zero");
        zeroTestConfig.setTest_no_of_threads(1);
        zeroTestConfig.setTest_randomize_sleeptime(false);
        zeroTestConfig.setTest_read_write_ratio(80);
        zeroTestConfig.setTest_sleep_in_ms(1);
        activeLoadTestConfig.set(zeroTestConfig);
        activeSingleLoadTestExecution.set(new SingleLoadTestExecution(readTestSpecificationList.get(), writeTestSpecificationList.get(), zeroTestConfig, loadTestRunNo.get()));
    }

    private static void getSpecFromMap() {
        if (Configuration.getBoolean("loadtest.cluster")) {
            readTestSpecificationList.set((List<TestSpecification>) configMap.get("readTestSpecificationList"));
            writeTestSpecificationList.set((List<TestSpecification>) configMap.get("writeTestSpecificationList"));
            activeLoadTestConfig.set((LoadTestConfig) configMap.get("activeLoadTestConfig"));
        }
    }

    private static void updateSpecMap() {
        if (Configuration.getBoolean("loadtest.cluster")) {
            configMap.put("readTestSpecificationList", readTestSpecificationList.get());
            configMap.put("writeTestSpecificationList", writeTestSpecificationList.get());
            configMap.put("activeLoadTestConfig", activeLoadTestConfig.get());
        }
    }


    public static String getReadTestSpecificationListJson() {
        String result = "[]";
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readTestSpecificationList.get());

        } catch (Exception e) {
            log.error("Unable to create json of readTestSpecification", e);
            return result;
        }
    }

    public static String getWriteTestSpecificationListJson() {
        String result = "[]";
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(writeTestSpecificationList.get());

        } catch (Exception e) {
            log.error("Unable to create json of writeTestSpecification", e);
            return result;
        }
    }

    public static void setReadTestSpecificationList(List<TestSpecification> readTestSpecificationList) {
        LoadTestExecutorService.readTestSpecificationList.set(readTestSpecificationList);
        updateSpecMap();

    }

    public static void setWriteTestSpecificationList(List<TestSpecification> writeTestSpecificationList) {
        LoadTestExecutorService.writeTestSpecificationList.set(writeTestSpecificationList);
        updateSpecMap();

    }

    static void addResult(LoadTestResult loadTestResult) {
        resultList.get().add(loadTestResult);
    }

    public static List<LoadTestResult> getResultListSnapshot() {
        List<LoadTestResult> resultList = LoadTestExecutorService.resultList.get();
        synchronized (resultList) {
            return new ArrayList<>(resultList);
        }
    }

    public static List getLatestResultListSnapshot() {
        List<LoadTestResult> resultList = LoadTestExecutorService.resultList.get();
        // TODO this just picks the last 50 entires and returns as "lastest result list", is this correct?
        synchronized (resultList) {
            return new ArrayList<>(resultList.subList(Math.max(resultList.size() - 50, 0), resultList.size()));
        }
    }

    public static void executeLoadTest(LoadTestConfig loadTestConfig, boolean asNewThread) {
        /**
         * IExecutorService executor = hz.getExecutorService("executor");
         for (Integer key : map.keySet())
         executor.executeOnKeyOwner(new YourBigTask(), key);
         */
        unsafeList.set(new ArrayList<>());
        resultList.set(Collections.synchronizedList(unsafeList.get())); // TODO Should this not be a hazelcast list when running in cluster?
        if (Configuration.getBoolean("loadtest.HystrixFallbackIsolationSemaphoreMaxConcurrentRequests")) {
            HystrixCommandProperties.Setter().withFallbackIsolationSemaphoreMaxConcurrentRequests(loadTestConfig.getTest_no_of_threads() * 10);
        }
        loadTestRunNo.incrementAndGet();
        activeLoadTestConfig.set(loadTestConfig);
        updateSpecMap();

        log.info("LoadTest {} started, max running time: {} seconds", loadTestConfig.getTest_id(), loadTestConfig.getTest_duration_in_seconds());

        activeSingleLoadTestExecution.set(new SingleLoadTestExecution(readTestSpecificationList.get(), writeTestSpecificationList.get(), loadTestConfig, loadTestRunNo.get()));

        if (asNewThread) {
            ExecutorService loadTestExecutor = Executors.newFixedThreadPool(1);
            loadTestExecutor.submit(new Callable<Object>() {
                                        @Override
                                        public Object call() throws Exception {
                                            activeSingleLoadTestExecution.get().runLoadTest();
                                            return null;
                                        }
                                    }
            );
        } else {
            activeSingleLoadTestExecution.get().runLoadTest();
        }
    }

    public static boolean isRunning() {
        return activeSingleLoadTestExecution.get().isRunning();
    }

    public static void stop() {
        activeSingleLoadTestExecution.get().stop();
    }

    public static long getStartTime() {
        return activeSingleLoadTestExecution.get().getStartTime();
    }

    public static long getStopTime() {
        return activeSingleLoadTestExecution.get().getStopTime();
    }

    public static LoadTestConfig getActiveLoadTestConfig() {
        return activeLoadTestConfig.get();
    }

    public static int getTasksScheduled() {
        return activeSingleLoadTestExecution.get().getTasksScheduled();
    }

    public static int getLoadTestRunNo() {
        return loadTestRunNo.get();
    }

    public static int getThreadPoolSize() {
        return activeSingleLoadTestExecution.get().getThreadPoolSize();
    }
}
