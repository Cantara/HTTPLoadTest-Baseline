package no.cantara.service.loadtest;

import no.cantara.service.Main;
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

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

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

            LoadTestResultUtil.storeResultToFiles();

        } finally {
            synchronized (this) {
                isRunning = false;
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
                    stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void runTask(LoadTestConfig loadTestConfig) {
        try {
            int runNo = 1;
            int read_ratio = loadTestConfig.getTest_read_write_ratio();

            while (notStopped()) {

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

    private synchronized boolean notStopped() {
        return !stopInitiated;
    }

    @Override
    public synchronized boolean isRunning() {
        return isRunning;
    }

    public void stop() {
        synchronized (this) {
            stopInitiated = true;
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
