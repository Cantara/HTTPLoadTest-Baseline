package no.cantara.service.loadtest.drivers;

import no.cantara.service.loadtest.LoadTestExecutorService;
import no.cantara.service.model.LoadTestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;

import static no.cantara.service.loadtest.LoadTestExecutorService.isRunning;

public class MyRunnable implements Runnable {
    private final String url;
    private final LoadTestResult loadTestResult;
    private static final Logger log = LoggerFactory.getLogger(MyRunnable.class);

    public MyRunnable(String url, LoadTestResult loadTestResult) {
        this.url = url;
        this.loadTestResult = loadTestResult;
        this.loadTestResult.setTest_tags("URL: " + url);
    }

    @Override
    public void run() {
        if (!isRunning) {
            return;
        }

        long startTime = System.currentTimeMillis();

        logTimedCode(startTime, loadTestResult.getTest_run_no() + " - starting processing!");

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
        loadTestResult.setTest_duration(Long.valueOf(System.currentTimeMillis() - startTime));
        log.trace(url + "\t\tStatus:" + result);
        logTimedCode(startTime, loadTestResult.getTest_run_no() + " - processing completed!");

        LoadTestExecutorService.addResult(loadTestResult);

    }

    private static void logTimedCode(long startTime, String msg) {
        long elapsedSeconds = (System.currentTimeMillis() - startTime);
        log.trace("{}ms [{}] {}\n", elapsedSeconds, Thread.currentThread().getName(), msg);
    }

}
