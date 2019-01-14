package no.cantara.service.loadtest;

import no.cantara.service.Main;
import no.cantara.service.loadtest.drivers.LoadTestExecutionContext;
import no.cantara.service.loadtest.drivers.MyReadRunnable;
import no.cantara.service.loadtest.drivers.MyRunnable;
import no.cantara.service.loadtest.drivers.MyWriteRunnable;
import no.cantara.service.loadtest.util.BlockingExecutor;
import no.cantara.service.loadtest.util.LoadTestResultUtil;
import no.cantara.service.model.LoadTestConfig;
import no.cantara.service.model.LoadTestResult;
import no.cantara.service.model.TestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SingleLoadTestExecution implements LoadTestExecutionContext {

    private static final Logger log = LoggerFactory.getLogger(SingleLoadTestExecution.class);

    public static final int WORK_QUEUE_CAPACITY_FACTOR = 2;
    public static final int LOAD_TEST_RAMPDOWN_TIME_MS = 300;
    public static final int THREAD_POOL_SHUTDOWN_TIMEOUT_SECONDS = 5;

    private final Random r = new Random();
    private final LoadTestConfig loadTestConfig;
    private final ExecutorService runTaskExecutor;
    private final int loadTestRunNo;
    private final List<TestSpecification> readTestSpecificationList;
    private final List<TestSpecification> writeTestSpecificationList;
    private final int threadPoolSize;

    // mutable variables
    private final AtomicInteger tasksScheduled = new AtomicInteger(0);
    private final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
    private final AtomicLong stopTime = new AtomicLong(0);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean stopInitiated = new AtomicBoolean(false);
    private final AtomicInteger workerConcurrencyDegree = new AtomicInteger(0);
    private final AtomicInteger commandConcurrencyDegree = new AtomicInteger(0);

    public SingleLoadTestExecution(List<TestSpecification> readTestSpecificationList,
                                   List<TestSpecification> writeTestSpecificationList,
                                   LoadTestConfig loadTestConfig,
                                   int loadTestRunNo) {
        this.readTestSpecificationList = readTestSpecificationList;
        this.writeTestSpecificationList = writeTestSpecificationList;
        this.loadTestConfig = loadTestConfig;
        this.threadPoolSize = loadTestConfig.getTest_no_of_threads();
        this.loadTestRunNo = loadTestRunNo;
        runTaskExecutor = new BlockingExecutor(threadPoolSize, threadPoolSize * WORK_QUEUE_CAPACITY_FACTOR);
    }

    void runLoadTest() {

        long startTime = System.currentTimeMillis();
        this.startTime.set(startTime);

        isRunning.set(true);

        try {

            final int test_duration_in_seconds = loadTestConfig.getTest_duration_in_seconds();

            scheduleAsyncStopAfterDuration((1000 * test_duration_in_seconds) - LOAD_TEST_RAMPDOWN_TIME_MS);

            logTimedCode(startTime, loadTestConfig.getTest_id() + " - starting processing! max duration:" + loadTestConfig.getTest_duration_in_seconds());

            runTask(loadTestConfig);

            logTimedCode(startTime, loadTestConfig.getTest_id() + " - processing completed!");

            log.info("Async LoadTest {} completed, ran for {} seconds, max running time: {} seconds", loadTestConfig.getTest_id(), (System.currentTimeMillis() - startTime) / 1000, loadTestConfig.getTest_duration_in_seconds());

            stopTime.set(System.currentTimeMillis());

        } finally {
            try {
                LoadTestResultUtil.storeResultToFiles();
            } finally {
                isRunning.set(false);
            }
        }
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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    stop();
                }
            }
        }).start();
    }

    private void runTask(LoadTestConfig loadTestConfig) {
        try {
            int runNo = 1;
            int read_ratio = loadTestConfig.getTest_read_write_ratio();

            while (!stopped()) {

                int chance = r.nextInt(100);

                Callable<LoadTestResult> worker;

                if (read_ratio == 0) {
                    String url = "http://localhost:" + Main.PORT_NO + Main.CONTEXT_PATH;
                    LoadTestResult loadTestResult = createLoadTestResult(loadTestConfig.getTest_id(), runNo++);
                    worker = new MyRunnable(url, loadTestResult, this);
                } else if (chance <= read_ratio) {
                    LoadTestResult loadTestResult = createLoadTestResult("r-" + loadTestConfig.getTest_id(), runNo++);
                    worker = new MyReadRunnable(readTestSpecificationList, loadTestConfig, loadTestResult, this);
                } else {
                    LoadTestResult loadTestResult = createLoadTestResult("w-" + loadTestConfig.getTest_id(), runNo++);
                    worker = new MyWriteRunnable(writeTestSpecificationList, loadTestConfig, loadTestResult, this);
                }

                submitTestTask(worker);
                tasksScheduled.incrementAndGet();
            }
        } finally {
            shutdownAndAwaitTermination(runTaskExecutor);
        }
    }

    private void submitTestTask(final Callable<LoadTestResult> worker) {
        // The first call to runAsync on runTaskExecutor will block until work-queue capacity is available
        CompletableFuture.runAsync(() -> {
            long sleeptime = 0L + loadTestConfig.getTest_sleep_in_ms();
            // Check if we should randomize sleeptime
            if (loadTestConfig.isTest_randomize_sleeptime()) {
                int chance = r.nextInt(100);
                sleeptime = 0L + loadTestConfig.getTest_sleep_in_ms() * chance / 100;
            }
            try {
                //log.trace("Sleeping {} ms before test as configured in the loadTestConfig", sleeptime);
                Thread.sleep(sleeptime);
            } catch (Exception e) {
                log.warn("Thread interrupted in wait sleep", e);
            }
        }, runTaskExecutor).thenAccept(v -> {
            try {
                LoadTestResult ltr;
                int workerConcurrencyDegreeAfterEntry = workerConcurrencyDegree().incrementAndGet();
                try {
                    ltr = worker.call();
                } finally {
                    workerConcurrencyDegree().decrementAndGet();
                }
                ltr.setWorker_concurrency_degree(workerConcurrencyDegreeAfterEntry);
                LoadTestExecutorService.addResult(ltr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).exceptionally(t -> {
            t.printStackTrace();
            return null;
        });
    }

    private LoadTestResult createLoadTestResult(String testId, int runNo) {
        LoadTestResult loadTestResult = new LoadTestResult();
        loadTestResult.setTest_run_no(runNo);
        loadTestResult.setTest_name(loadTestConfig.getTest_name());
        loadTestResult.setTest_id(testId);
        return loadTestResult;
    }

    @Override
    public AtomicInteger workerConcurrencyDegree() {
        return workerConcurrencyDegree;
    }

    @Override
    public AtomicInteger commandConcurrencyDegree() {
        return commandConcurrencyDegree;
    }

    @Override
    public boolean stopped() {
        return stopInitiated.get();
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public void stop() {
        stopInitiated.set(true);
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(THREAD_POOL_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(THREAD_POOL_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
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

    public long getStartTime() {
        return startTime.get();
    }

    public long getStopTime() {
        return stopTime.get();
    }

    public int getTasksScheduled() {
        return tasksScheduled.get();
    }

    public int getLoadTestRunNo() {
        return loadTestRunNo;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }
}
