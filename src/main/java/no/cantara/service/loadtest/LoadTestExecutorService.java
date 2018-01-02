package no.cantara.service.loadtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.service.LoadTestConfig;
import no.cantara.service.LoadTestResult;
import no.cantara.service.loadtest.drivers.MyReadRunnable;
import no.cantara.service.loadtest.drivers.MyRunnable;
import no.cantara.service.loadtest.drivers.MyWriteRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class LoadTestExecutorService {

    private static final Logger log = LoggerFactory.getLogger(LoadTestResource.class);
    private static List<LoadTestResult> unsafeList = new ArrayList<>();
    //private static  List<LoadTestResult> resultList = new LinkedList<>();
    private static List<LoadTestResult> resultList = Collections.synchronizedList(unsafeList);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static Random r = new Random();
    private static long startTime;
    private static int loadTestRunNo = 1;


    public static void addResult(LoadTestResult loadTestResult) {
        resultList.add(loadTestResult);
        //log.info("ResultMapSize: {}", resultList.size());
    }


    public static List getResultList() {
        //List<LoadTestResult> copyList = new LinkedList<>();
        //for (LoadTestResult loadTestResult : resultList) {
        //    copyList.add(loadTestResult);
        //}

        return resultList;
    }

    public static void executeLoadTest(LoadTestConfig loadTestConfig, boolean asNewThread) {
        loadTestRunNo++;
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
    }





    private static void runTask(LoadTestConfig loadTestConfig) {
        int runNo = 1;
        ExecutorService threadExecutor = Executors.newFixedThreadPool(loadTestConfig.getTest_no_of_threads());

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

        while (true) {


            for (int i = 0; i < hostList.length; i++) {
                int chance = r.nextInt(100);
                long maxRunTimeMs = startTime + loadTestConfig.getTest_duration_in_seconds() * 1000 - System.currentTimeMillis();
                if (maxRunTimeMs > 500) {
                    // We stop a little before known timeout
                    log.trace("MaxRunInMilliSeconds: {}", maxRunTimeMs);
                    if (read_ratio == 0) {
                        String url = hostList[i];
                        LoadTestResult loadTestResult = new LoadTestResult();
                        loadTestResult.setTest_id(loadTestConfig.getTest_id());
                        loadTestResult.setTest_name(loadTestConfig.getTest_name());
                        loadTestResult.setTest_run_no(runNo++);
                        Runnable worker = new MyRunnable(url, loadTestResult);
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
                        }

//                    threadExecutor.execute(worker);

                    } else if (chance <= read_ratio) {
                        String url = hostList[i];
                        LoadTestResult loadTestResult = new LoadTestResult();
                        loadTestResult.setTest_id("r-" + loadTestConfig.getTest_id());
                        loadTestResult.setTest_name(loadTestConfig.getTest_name());
                        loadTestResult.setTest_run_no(runNo++);
                        Runnable worker = new MyReadRunnable(url, loadTestConfig, loadTestResult);
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
                        }
//                    threadExecutor.execute(worker);

                    } else {
                        String url = hostList[i];
                        LoadTestResult loadTestResult = new LoadTestResult();
                        loadTestResult.setTest_id("w-" + loadTestConfig.getTest_id());
                        loadTestResult.setTest_name(loadTestConfig.getTest_name());
                        loadTestResult.setTest_run_no(runNo++);
                        Runnable worker = new MyWriteRunnable(url, loadTestConfig, loadTestResult);
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
                        }
//                    threadExecutor.execute(worker);

                    }
                }
            }


        }
//        System.out.println("\nFinished all threads");
    }

    public static String printStats(List<LoadTestResult> loadTestResults) {
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
        String stats = "";
        log.info(" {} read tests resulted in {} successfull runs where {} was marked as deviations.", r_results, r_success, r_deviations);
        stats = stats + "\n" + String.format(" %4d read tests resulted in %4d successfull runs where %4d was marked as deviations.", r_results, r_success, r_deviations);

        log.info(" {} write tests resulted in {} successfull runs where {} was marked as deviations.", w_results, w_success, w_deviations);
        stats = stats + "\n" + String.format(" %4d write tests resulted in %4d successfull runs where %4d was marked as deviations.", w_results, w_success, w_deviations);

        log.info(" {} unmarked tests resulted in {} successfull runs where {} was marked as deviations.", results, success, deviations);
        stats = stats + "\n" + String.format(" %4d unmarked tests resulted in %4d successfull runs where %4d was marked as deviations.", results, success, deviations);

        log.info(" {} total tests resulted in {} successfull runs where {} was marked as deviations.", r_results + w_results + results, r_success + w_success + success, r_deviations + w_deviations + deviations);
        stats = stats + "\n" + String.format(" %4d total tests resulted in %4d successfull runs where %4d was marked as deviations.", r_results + w_results + results, r_success + w_success + success, r_deviations + w_deviations + deviations);

        return stats + "\n\n";
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


    private static void logTimedCode(long startTime, String msg) {
        long elapsedSeconds = (System.currentTimeMillis() - startTime);
        log.info("{}ms [{}] {}\n", elapsedSeconds, Thread.currentThread().getName(), msg);
    }
}
