package no.cantara.service.loadtest.drivers;

import no.cantara.service.model.LoadTestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class MyRunnable implements Callable<LoadTestResult> {
    private final String url;
    private final LoadTestResult loadTestResult;
    private final LoadTestExecutionContext loadTestExecutionContext;
    private static final Logger log = LoggerFactory.getLogger(MyRunnable.class);

    public MyRunnable(String url, LoadTestResult loadTestResult, LoadTestExecutionContext loadTestExecutionContext) {
        this.url = url;
        this.loadTestResult = loadTestResult;
        this.loadTestExecutionContext = loadTestExecutionContext;
        this.loadTestResult.setTest_tags("URL: " + url);
    }

    @Override
    public LoadTestResult call() throws Exception {
        int workerConcurrencyDegreeAfterEntry = loadTestExecutionContext.workerConcurrencyDegree().incrementAndGet();
        loadTestResult.setWorker_concurrency_degree(workerConcurrencyDegreeAfterEntry);
        try {
            return doCall();
        } finally {
            loadTestExecutionContext.workerConcurrencyDegree().decrementAndGet();
        }
    }

    public LoadTestResult doCall() throws Exception {
        if (loadTestExecutionContext.stopped()) {
            return null;
        }

        long startNanoTime = System.nanoTime();

        logTimedCode(startNanoTime, loadTestResult.getTest_run_no() + " - starting processing!");

        String result = "";
        int code = 200;
        try {
            URL siteURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) siteURL
                    .openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            code = connection.getResponseCode();
            if (code == 200) {
                result = "Green\t";
                loadTestResult.setTest_success(true);
            }
        } catch (Exception e) {
            result = "->Red<-\t";
            loadTestResult.setTest_deviation_flag(true);
        }
        loadTestResult.setTest_duration((System.nanoTime() - startNanoTime) / 1000000.0);
        log.trace(url + "\t\tStatus:" + result);
        logTimedCode(startNanoTime, loadTestResult.getTest_run_no() + " - processing completed!");

        return loadTestResult;
    }

    private static void logTimedCode(long startNanoTime, String msg) {
        long elapsedMilliseconds = Math.round((System.nanoTime() - startNanoTime) / 1000000.0);
        log.trace("{}ms [{}] {}\n", elapsedMilliseconds, Thread.currentThread().getName(), msg);
    }

}
