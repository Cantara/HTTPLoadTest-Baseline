package no.cantara.service.loadtest.drivers;

import no.cantara.service.LoadTestConfig;
import no.cantara.service.LoadTestResult;
import no.cantara.service.loadtest.LoadTestExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MyReadRunnable implements Runnable {
    private final String url;
    private static Random r = new Random();
    private final LoadTestResult loadTestResult;
    private final LoadTestConfig loadTestConfig;
    private static final Logger log = LoggerFactory.getLogger(MyReadRunnable.class);

    public MyReadRunnable(String url, LoadTestConfig loadTestConfig, LoadTestResult loadTestResult) {
        this.url = url;
        this.loadTestResult = loadTestResult;
        this.loadTestConfig = loadTestConfig;
        this.loadTestResult.setTest_tags("URL: " + url);
    }

    @Override
    public void run() {
        long sleeptime = 0L + loadTestConfig.getTest_sleep_in_ms();
        // Check if we should randomize sleeptime
        if (loadTestConfig.isTest_randomize_sleeptime()) {
            int chance = r.nextInt(100);
            sleeptime = 0L + loadTestConfig.getTest_sleep_in_ms() * chance / 100;
        }
        try {
            log.trace("Sleeping {} ms before test as configured in the loadTestConfig", sleeptime);
            Thread.sleep(sleeptime);
        } catch (Exception e) {
            log.warn("Thread interrupted in wait sleep", e);
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
