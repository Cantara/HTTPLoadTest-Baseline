package no.cantara.service.loadtest;

import no.cantara.service.Main;
import no.cantara.service.health.HealthResource;
import no.cantara.service.loadtest.drivers.LoadTestExecutionContext;
import no.cantara.service.loadtest.drivers.MyReadRunnable;
import no.cantara.service.loadtest.drivers.MyRunnable;
import no.cantara.service.loadtest.drivers.MyWriteRunnable;
import no.cantara.service.loadtest.util.LoadTestResultUtil;
import no.cantara.service.model.LoadTestConfig;
import no.cantara.service.model.LoadTestResult;
import no.cantara.service.model.TestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SingleLoadTestExecution implements LoadTestExecutionContext {

    private static final Logger log = LoggerFactory.getLogger(SingleLoadTestExecution.class);

    private static final String RESULT_FILE_PATH = LoadTestExecutorService.RESULT_FILE_PATH;

    public static final int WORK_QUEUE_CAPACITY_FACTOR = 10;
    public static final int LOAD_TEST_RAMPDOWN_TIME_MS = 300;

    private final Random r = new Random();
    private final LoadTestConfig loadTestConfig;
    private final ExecutorService runTaskExecutor;
    private final BlockingQueue<Runnable> workQueue;
    private final int maxAllowedQueuedTasks;
    private final int loadTestRunNo;
    private final List<TestSpecification> readTestSpecificationList;
    private final List<TestSpecification> writeTestSpecificationList;
    private final int threadPoolSize;

    // mutable variables, all read/write access must be synchronized on this instance
    private int tasksScheduled = 0;
    private long startTime = System.currentTimeMillis();
    private long stopTime = 0;
    private Thread schedulingThread = null;
    private boolean isRunning = true;

    public SingleLoadTestExecution(List<TestSpecification> readTestSpecificationList,
                                   List<TestSpecification> writeTestSpecificationList,
                                   LoadTestConfig loadTestConfig,
                                   int loadTestRunNo) {
        this.readTestSpecificationList = readTestSpecificationList;
        this.writeTestSpecificationList = writeTestSpecificationList;
        this.loadTestConfig = loadTestConfig;

        threadPoolSize = loadTestConfig.getTest_no_of_threads();

        maxAllowedQueuedTasks = loadTestConfig.getTest_no_of_threads() * WORK_QUEUE_CAPACITY_FACTOR;
        this.loadTestRunNo = loadTestRunNo;

        workQueue = new LinkedBlockingQueue<>();

        runTaskExecutor = new ThreadPoolExecutor(
                threadPoolSize,
                threadPoolSize,
                0L, TimeUnit.MILLISECONDS,
                workQueue,
                new ThreadPoolExecutor.DiscardPolicy());
    }

    void runLoadTest() {

        long startTime;

        synchronized (this) {
            this.startTime = System.currentTimeMillis();
            startTime = this.startTime;
            schedulingThread = Thread.currentThread();
        }

        final int test_duration_in_seconds = loadTestConfig.getTest_duration_in_seconds();

        scheduleAsyncStopAfterDuration((1000 * test_duration_in_seconds) - LOAD_TEST_RAMPDOWN_TIME_MS);

        logTimedCode(startTime, loadTestConfig.getTest_id() + " - starting processing! max duration:" + loadTestConfig.getTest_duration_in_seconds());

        runTask(loadTestConfig);

        logTimedCode(startTime, loadTestConfig.getTest_id() + " - processing completed!");

        log.info("Async LoadTest {} completed, ran for {} seconds, max running time: {} seconds", loadTestConfig.getTest_id(), (System.currentTimeMillis() - startTime) / 1000, loadTestConfig.getTest_duration_in_seconds());

        synchronized (this) {
            stopTime = System.currentTimeMillis();
        }

        LoadTestResultUtil.storeResultToFiles();
    }

    private static void logTimedCode(long startTime, String msg) {
        long elapsedSeconds = (System.currentTimeMillis() - startTime);
        log.info("{}ms [{}] {}\n", elapsedSeconds, Thread.currentThread().getName(), msg);
    }

    private void scheduleAsyncStopAfterDuration(int test_duration_in_milliseconds) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(test_duration_in_milliseconds);
                    stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void runTask(LoadTestConfig loadTestConfig) {
        int runNo = 1;
        int read_ratio = loadTestConfig.getTest_read_write_ratio();

        try {

            while (isRunning) {

                int chance = r.nextInt(100);
                long maxRunTimeMs = startTime + loadTestConfig.getTest_duration_in_seconds() * 1000 - System.currentTimeMillis();

                // We stop a little before known timeout, we quit on stop-signal... and we schedule max 10*the configured number of threads to avoid overusing memory
                // for long (endurance) loadtest runs
                log.info("MaxRunInMilliSeconds: {}, LOAD_TEST_RAMPDOWN_TIME_MS:{} , maxThreadsAllowed: {}, tasks scheduled: {}", maxRunTimeMs, LOAD_TEST_RAMPDOWN_TIME_MS, maxAllowedQueuedTasks, tasksScheduled);

                if (read_ratio == 0) {
                    String url = "http://localhost:" + Main.PORT_NO + Main.CONTEXT_PATH;
                    LoadTestResult loadTestResult = new LoadTestResult();
                    loadTestResult.setTest_id(loadTestConfig.getTest_id());
                    loadTestResult.setTest_name(loadTestConfig.getTest_name());
                    loadTestResult.setTest_run_no(runNo++);
                    Runnable worker = new MyRunnable(url, loadTestResult, this);
                    tasksScheduled++;
                    runTaskExecutor.execute(worker);
                } else if (chance <= read_ratio) {
                    LoadTestResult loadTestResult = new LoadTestResult();
                    loadTestResult.setTest_id("r-" + loadTestConfig.getTest_id());
                    loadTestResult.setTest_name(loadTestConfig.getTest_name());
                    loadTestResult.setTest_run_no(runNo++);
                    Runnable worker = new MyReadRunnable(readTestSpecificationList, loadTestConfig, loadTestResult, this);
                    tasksScheduled++;
                    runTaskExecutor.execute(worker);
                } else {
                    LoadTestResult loadTestResult = new LoadTestResult();
                    loadTestResult.setTest_id("w-" + loadTestConfig.getTest_id());
                    loadTestResult.setTest_name(loadTestConfig.getTest_name());
                    loadTestResult.setTest_run_no(runNo++);
                    Runnable worker = new MyWriteRunnable(writeTestSpecificationList, loadTestConfig, loadTestResult, this);
                    tasksScheduled++;
                    runTaskExecutor.execute(worker);
                }

                // wait until we are allowed to queue more tasks
                while (workQueue.size() >= maxAllowedQueuedTasks) {
                    Thread.sleep(100);
                }
            }

        } catch (InterruptedException e) {
            // swallow interrupt
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return isRunning;
    }

    public void stop() {
        synchronized (this) {
            isRunning = false;
            if (schedulingThread != null) {
                schedulingThread.interrupt();
            }
        }
        shutdownAndAwaitTermination(runTaskExecutor);
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Pool did not terminate within the allotted shutdown time");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void addResult(LoadTestResult loadTestResult) {
        LoadTestExecutorService.addResult(loadTestResult); // TODO Circular dependency, this should be avoided!
    }

    String getPrintableStats(List<LoadTestResult> loadTestResults, boolean whileRunning) {
        long stopTime;
        long startTime;
        int tasksScheduled;

        // synchronize on this instance when reading mutable variables.
        synchronized (this) {
            stopTime = this.stopTime;
            startTime = this.startTime;
            tasksScheduled = this.tasksScheduled;
        }

        long nowTimestamp = System.currentTimeMillis();
        boolean shouldStop;

        shouldStop = stopTime == 0 && ((nowTimestamp - startTime) / 1000) > loadTestConfig.getTest_duration_in_seconds();
        if (shouldStop) {
            stop();  // We might get in trouble if no memory for native threads in high thread situations
        }
        if (loadTestResults == null || (whileRunning == false && isRunning())) {
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
        String stats = "";
        DateFormat df = new SimpleDateFormat("dd/MM-yyyy  HH:mm:ss");
        if (stopTime == 0) {
            stats = "Started: " + df.format(new Date(startTime)) + " Version:" + HealthResource.getVersion() + "  Now: " + df.format(new Date(nowTimestamp)) + "  Running for " + (nowTimestamp - startTime) / 1000 + " seconds.\n";
        } else {
            stats = "Started: " + df.format(new Date(startTime)) + " Version:" + HealthResource.getVersion() + "  Now: " + df.format(new Date(nowTimestamp)) + "  Ran for " + (stopTime - startTime) / 1000 + " seconds.\n";
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
        stats = stats + "\n" + String.format(" %4d tasks scheduled, number of threads configured: %d,  isRunning: %b ", tasksScheduled, threadPoolSize, isRunning());
        stats = stats + "\n" + String.format(" %4d ms mean duration for successful read tests, %4d ms ninety percentile successful read tests ", r_mean_success, r_ninety_percentine_success);
        stats = stats + "\n" + String.format(" %4d ms mean duration for successful write tests, %4d ms ninety percentile successful write tests ", w_mean_success, w_ninety_percentine_success);

        String loadTestJson = "";
        try {
            loadTestJson = LoadTestResultUtil.mapper().writerWithDefaultPrettyPrinter().writeValueAsString(loadTestConfig);
            loadTestJson = loadTestJson + "\n\n";

        } catch (Exception e) {
            log.warn("Unable to serialize loadTestConfig to json", e);
        }
        return stats + "\n\n" + loadTestJson;
    }

    public synchronized long getStartTime() {
        return startTime;
    }

    public synchronized long getStopTime() {
        return stopTime;
    }

    public synchronized int getTasksScheduled() {
        return tasksScheduled;
    }

    public int getLoadTestRunNo() {
        return loadTestRunNo;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }
}
