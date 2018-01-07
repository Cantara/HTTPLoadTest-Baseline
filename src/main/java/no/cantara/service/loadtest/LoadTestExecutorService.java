package no.cantara.service.loadtest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.service.loadtest.drivers.MyReadRunnable;
import no.cantara.service.loadtest.drivers.MyRunnable;
import no.cantara.service.loadtest.drivers.MyWriteRunnable;
import no.cantara.service.model.LoadTestConfig;
import no.cantara.service.model.LoadTestResult;
import no.cantara.service.model.TestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class LoadTestExecutorService {

    private static final Logger log = LoggerFactory.getLogger(LoadTestResource.class);
    private static List<LoadTestResult> unsafeList = new ArrayList<>();
    private static List<LoadTestResult> resultList = Collections.synchronizedList(unsafeList);
    private static List<TestSpecification> readTestSpecificationList;
    private static List<TestSpecification> writeTestSpecificationList;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static Random r = new Random();
    private static long startTime = System.currentTimeMillis();
    ;
    private static int loadTestRunNo = 0;
    private static boolean isRunning = false;
    private static LoadTestConfig activeLoadTestConfig;
    private static ExecutorService threadExecutor;

    private static int threadsScheduled = 0;

    static {
        try {

            InputStream is = LoadTestExecutorService.class.getClassLoader().getResourceAsStream("DefaultReadTestSpecification.json");
            readTestSpecificationList = mapper.readValue(is, new TypeReference<List<TestSpecification>>() {
            });
            String jsonreadconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readTestSpecificationList);
            log.info("Loaded DefaultReadTestSpecification: {}", jsonreadconfig);
            InputStream wis = LoadTestExecutorService.class.getClassLoader().getResourceAsStream("DefaultReadTestSpecification.json");

            writeTestSpecificationList = mapper.readValue(wis, new TypeReference<List<TestSpecification>>() {
            });
            String jsonwriteconfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(writeTestSpecificationList);
            log.info("Loaded DefaultWriteTestSpecification: {}", jsonwriteconfig);

        } catch (Exception e) {
            log.error("Unable to read default configuration for LoadTest.", e);
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
    }

    public static List<TestSpecification> getWriteTestSpecificationList() {
        return writeTestSpecificationList;
    }

    public static void setWriteTestSpecificationList(List<TestSpecification> writeTestSpecificationList) {
        LoadTestExecutorService.writeTestSpecificationList = writeTestSpecificationList;
    }

    public static void addResult(LoadTestResult loadTestResult) {
        resultList.add(loadTestResult);
        threadsScheduled--;
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

    public static void executeLoadTest(LoadTestConfig loadTestConfig, boolean asNewThread) {
        loadTestRunNo++;
        activeLoadTestConfig = loadTestConfig;
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
        threadExecutor.shutdown();
    }


    private static void runTask(LoadTestConfig loadTestConfig) {
        int runNo = 1;
        threadExecutor = Executors.newFixedThreadPool(loadTestConfig.getTest_no_of_threads());

        String[] hostList = {"http://google.com"}; /**, "http://yahoo.com",
         "http://www.ebay.com", "http://google.com",
         "http://www.example.co", "https://paypal.com",
         "http://bing.com/", "http://techcrunch.com/",
         "http://mashable.com/", "http://thenextweb.com/",
         "http://wordpress.com/", "http://wordpress.org/",
         "http://example.com/", "http://sjsu.edu/",
         "http://ebay.co.uk/", "http://google.co.uk/",
         "http://www.wikipedia.org/",
         "http://en.wikipedia.org/wiki/Main_Page"};
         **/
        int read_ratio = loadTestConfig.getTest_read_write_ratio();

        while (isRunning) {


            for (int i = 0; i < hostList.length; i++) {
                int chance = r.nextInt(100);
                long maxRunTimeMs = startTime + loadTestConfig.getTest_duration_in_seconds() * 1000 - System.currentTimeMillis();
                if (maxRunTimeMs > 50 && isRunning && threadsScheduled < (loadTestConfig.getTest_no_of_threads() * 10)) {
                    // We stop a little before known timeout, we quit on stop-signal... and we schedule max 10*the configured number of threads to avoid overusing memory
                    // for long (endurance) loadtest runs
                    log.info("MaxRunInMilliSeconds: {}, threadsScheduled: {}", maxRunTimeMs, threadsScheduled);
                    if (read_ratio == 0) {
                        String url = hostList[i];
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


        }
//        System.out.println("\nFinished all threads");
        threadsScheduled = 0;
        threadExecutor.shutdown();
    }

    public static String printStats(List<LoadTestResult> loadTestResults) {
        if (loadTestResults == null) {
            return "";
        }
        int r_deviations = 0;
        int r_success = 0;
        int r_results = 0;
        int w_deviations = 0;
        int w_success = 0;
        int w_results = 0;
        int deviations = 0;
        int success = 0;
        int results = 0;
        for (LoadTestResult loadTestResult : loadTestResults) {
            if (loadTestResult.getTest_id().startsWith("r-")) {
                r_results++;
                if (loadTestResult.isTest_deviation_flag()) {
                    r_deviations++;
                }
                if (loadTestResult.isTest_success()) {
                    r_success++;
                }

            } else {
                if (loadTestResult.getTest_id().startsWith("w-")) {
                    w_results++;
                    if (loadTestResult.isTest_deviation_flag()) {
                        w_deviations++;
                    }
                    if (loadTestResult.isTest_success()) {
                        w_success++;
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
            stats = "Started: " + df.format(new Date(startTime)) + "  Now: " + df.format(new Date(nowTimestamp)) + "  Running for " + (nowTimestamp - startTime) / 1000 + " seconds.\n";

        } else {
            stats = "Started: " + df.format(new Date(nowTimestamp)) + "  Now: " + df.format(new Date(nowTimestamp)) + "\n";
        }
        log.info(" {} read tests resulted in {} successful runs where {} was marked as deviation(s).", r_results, r_success, r_deviations);
        stats = stats + "\n" + String.format(" %4d read tests resulted in %d successful runs where %d was marked failure and %d was marked as deviation(s).", r_results, r_success, (r_results - r_success), r_deviations);

        log.info(" {} write tests resulted in {} successful runs where {} was marked as deviation(s).", w_results, w_success, w_deviations);
        stats = stats + "\n" + String.format(" %4d write tests resulted in %d successful runs where %d was marked failure and %d was marked as deviation(s).", w_results, w_success, (w_results - w_success), w_deviations);

        log.info(" {} unmarked tests resulted in {} successful runs where {} was marked as deviation(s).", results, success, deviations);
        stats = stats + "\n" + String.format(" %4d unmarked tests resulted in %d successful runs where %d was marked failure and  %d was marked as deviation(s).", results, success, (results - success), deviations);

        log.info(" {} total tests resulted in {} successful runs where {} was marked as deviations(s)", r_results + w_results + results, r_success + w_success + success, r_deviations + w_deviations + deviations);
        stats = stats + "\n" + String.format(" %4d total tests resulted in %d successful runs where %d was marked failure and %d was marked as deviation(s).", r_results + w_results + results, (r_results + w_results + results) - (r_success + w_success + success), r_success + w_success + success, r_deviations + w_deviations + deviations);
        stats = stats + "\n" + String.format(" %4d active tests threads sceduled, isRunning: %b ", threadsScheduled, isRunning);

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

    public static void runWithTimeout(final Runnable runnable, long timeout, TimeUnit timeUnit) throws Exception {
        runWithTimeout(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                runnable.run();
                return null;
            }
        }, timeout, timeUnit);
    }

    public static <T> T runWithTimeout(Callable<T> callable, long timeout, TimeUnit timeUnit) throws Exception {
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
                throw e;
            } catch (ExecutionException e) {
                //unwrap the root cause
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
    }

    public static int getThreadsScheduled() {
        return threadsScheduled;
    }

    private static void reduceThreadsScheduled() {
        threadsScheduled = threadsScheduled - 1;
    }


    private static void logTimedCode(long startTime, String msg) {
        long elapsedSeconds = (System.currentTimeMillis() - startTime);
        log.info("{}ms [{}] {}\n", elapsedSeconds, Thread.currentThread().getName(), msg);
    }
}
