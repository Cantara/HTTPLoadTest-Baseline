package no.cantara.service.loadtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.service.LoadTestConfig;
import no.cantara.service.LoadTestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class LoadTestExecutorService {

    private static final Logger log = LoggerFactory.getLogger(LoadTestResource.class);
    public static Random random = new Random();
    private static List<LoadTestResult> resultList = new LinkedList<>();
    private static final ObjectMapper mapper = new ObjectMapper();


    public static void addResult(LoadTestResult loadTestResult) {
        resultList.add(loadTestResult);
        log.info("ResultMapSize: {}", resultList.size());
    }




    public static List getResultList() {
        return resultList;
    }
    public static void executeLoadTest(LoadTestConfig loadTestConfig) {

        long startTime = System.currentTimeMillis();
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

        String[] hostList = {"http://crunchify.com"}; /**, "http://yahoo.com",
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
        while (true) {

            for (int i = 0; i < hostList.length; i++) {

                String url = hostList[i];
                LoadTestResult loadTestResult = new LoadTestResult();
                loadTestResult.setTest_id(loadTestConfig.getTest_id());
                loadTestResult.setTest_name(loadTestConfig.getTest_name());
                loadTestResult.setTest_run_no(runNo++);
                Runnable worker = new MyRunnable(url, loadTestResult);
                threadExecutor.execute(worker);
            }
        }
//        System.out.println("\nFinished all threads");
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
