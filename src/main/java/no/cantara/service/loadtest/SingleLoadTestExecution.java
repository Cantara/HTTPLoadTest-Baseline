package no.cantara.service.loadtest;

import no.cantara.service.Main;
import no.cantara.service.loadtest.drivers.LoadTestExecutionContext;
import no.cantara.service.loadtest.drivers.MyReadRunnable;
import no.cantara.service.loadtest.drivers.MyRunnable;
import no.cantara.service.loadtest.drivers.MyWriteRunnable;
import no.cantara.service.loadtest.util.BlockingExecutor;
import no.cantara.service.loadtest.util.FutureSelector;
import no.cantara.service.loadtest.util.LoadTestResultUtil;
import no.cantara.service.model.LoadTestConfig;
import no.cantara.service.model.LoadTestResult;
import no.cantara.service.model.TestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

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

    // mutable variables, all read/write access must be synchronized on this instance
    private int tasksScheduled = 0;
    private long startTime = System.currentTimeMillis();
    private long stopTime = 0;
    private boolean isRunning = false;
    private boolean stopInitiated = false;

    public SingleLoadTestExecution(List<TestSpecification> readTestSpecificationList,
                                   List<TestSpecification> writeTestSpecificationList,
                                   LoadTestConfig loadTestConfig,
                                   int loadTestRunNo) {
        this.readTestSpecificationList = readTestSpecificationList;
        this.writeTestSpecificationList = writeTestSpecificationList;
        this.loadTestConfig = loadTestConfig;

        threadPoolSize = loadTestConfig.getTest_no_of_threads();

        this.loadTestRunNo = loadTestRunNo;

        runTaskExecutor = new BlockingExecutor(threadPoolSize, threadPoolSize * WORK_QUEUE_CAPACITY_FACTOR);
    }

    void runLoadTest() {

        long startTime;

        synchronized (this) {
            this.startTime = System.currentTimeMillis();
            startTime = this.startTime;
            isRunning = true;
        }

        try {

            final int test_duration_in_seconds = loadTestConfig.getTest_duration_in_seconds();

            scheduleAsyncStopAfterDuration((1000 * test_duration_in_seconds) - LOAD_TEST_RAMPDOWN_TIME_MS);

            logTimedCode(startTime, loadTestConfig.getTest_id() + " - starting processing! max duration:" + loadTestConfig.getTest_duration_in_seconds());

            runTask(loadTestConfig);

            logTimedCode(startTime, loadTestConfig.getTest_id() + " - processing completed!");

            log.info("Async LoadTest {} completed, ran for {} seconds, max running time: {} seconds", loadTestConfig.getTest_id(), (System.currentTimeMillis() - startTime) / 1000, loadTestConfig.getTest_duration_in_seconds());

            synchronized (this) {
                stopTime = System.currentTimeMillis();
            }


        } finally {
            try {
                LoadTestResultUtil.storeResultToFiles();
            } finally {
                synchronized (this) {
                    isRunning = false;
                }
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
        FutureSelector<LoadTestResult> futureSelector = new FutureSelector<>();
        try {
            int runNo = 1;
            int read_ratio = loadTestConfig.getTest_read_write_ratio();

            while (!stopped()) {

                int chance = r.nextInt(100);
                long maxRunTimeMs = startTime + loadTestConfig.getTest_duration_in_seconds() * 1000 - System.currentTimeMillis();

                // We stop a little before known timeout, we quit on stop-signal... and we schedule max 10*the configured number of threads to avoid overusing memory
                // for long (endurance) loadtest runs
                log.info("MaxRunInMilliSeconds: {}, LOAD_TEST_RAMPDOWN_TIME_MS:{} , tasks scheduled: {}", maxRunTimeMs, LOAD_TEST_RAMPDOWN_TIME_MS, tasksScheduled);

                if (read_ratio == 0) {
                    String url = "http://localhost:" + Main.PORT_NO + Main.CONTEXT_PATH;
                    LoadTestResult loadTestResult = new LoadTestResult();
                    loadTestResult.setTest_id(loadTestConfig.getTest_id());
                    loadTestResult.setTest_name(loadTestConfig.getTest_name());
                    loadTestResult.setTest_run_no(runNo++);
                    Callable<LoadTestResult> worker = new MyRunnable(url, loadTestResult, this);
                    try {
                        futureSelector.add(runTaskExecutor.submit(worker));
                        tasksScheduled++;
                    } catch (RejectedExecutionException e) {
                        // This will typically happen if the thread-pool is stopped while this thread is waiting
                        // for available work-queue capacity
                    }
                } else if (chance <= read_ratio) {
                    LoadTestResult loadTestResult = new LoadTestResult();
                    loadTestResult.setTest_id("r-" + loadTestConfig.getTest_id());
                    loadTestResult.setTest_name(loadTestConfig.getTest_name());
                    loadTestResult.setTest_run_no(runNo++);
                    Callable<LoadTestResult> worker = new MyReadRunnable(readTestSpecificationList, loadTestConfig, loadTestResult, this);
                    try {
                        futureSelector.add(runTaskExecutor.submit(worker));
                        tasksScheduled++;
                    } catch (RejectedExecutionException e) {
                        // This will typically happen if the thread-pool is stopped while this thread is waiting
                        // for available work-queue capacity
                    }
                } else {
                    LoadTestResult loadTestResult = new LoadTestResult();
                    loadTestResult.setTest_id("w-" + loadTestConfig.getTest_id());
                    loadTestResult.setTest_name(loadTestConfig.getTest_name());
                    loadTestResult.setTest_run_no(runNo++);
                    Callable<LoadTestResult> worker = new MyWriteRunnable(writeTestSpecificationList, loadTestConfig, loadTestResult, this);
                    try {
                        futureSelector.add(runTaskExecutor.submit(worker));
                        tasksScheduled++;
                    } catch (RejectedExecutionException e) {
                        // This will typically happen if the thread-pool is stopped while this thread is waiting
                        // for available work-queue capacity
                    }
                }

                handleCompletedResults(futureSelector);
            }
        } finally {
            try {
                runTaskExecutor.awaitTermination(2 * THREAD_POOL_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.warn("", e); // log unexpected interrupt
            }
            try {
                handleIncompleteResults(futureSelector);
                handleCompletedResults(futureSelector);
            } catch (RuntimeException e) {
                log.warn("", e);
            }
        }
    }

    private void handleCompletedResults(FutureSelector<LoadTestResult> futureSelector) {
        Collection<Future<LoadTestResult>> doneFutures = futureSelector.selectAllDone();
        for (Future<LoadTestResult> future : doneFutures) {
            try {
                if (future.isCancelled()) {
                    log.warn("Task cancelled!");
                } else {
                    LoadTestResult loadTestResult = future.get(1, TimeUnit.MILLISECONDS);
                    if (loadTestResult != null) {
                        LoadTestExecutorService.addResult(loadTestResult); // TODO Circular dependency, this should be avoided!
                    }
                }
            } catch (InterruptedException e) {
                // ignore
            } catch (ExecutionException e) {
                log.error("", e);
            } catch (TimeoutException e) {
                // ignore
            }
        }
    }

    private void handleIncompleteResults(FutureSelector<LoadTestResult> futureSelector) {
        Collection<Future<LoadTestResult>> notDoneFutures = futureSelector.selectAllNotDone();
        int size = notDoneFutures.size();
        if (size > 0) {
            log.warn("{} task(s) are still in progress, their results will be discarded!", size);
        }
    }

    @Override
    public synchronized boolean stopped() {
        return stopInitiated;
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    public void stop() {
        synchronized (this) {
            stopInitiated = true;
        }
        shutdownAndAwaitTermination(runTaskExecutor);
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
