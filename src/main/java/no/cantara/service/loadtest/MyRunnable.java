package no.cantara.service.loadtest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;

public class MyRunnable implements Runnable {
    private final String url;
    private final String t_id;
    private static final Logger log = LoggerFactory.getLogger(LoadTestResource.class);

    MyRunnable(String url, String t_id) {
        this.url = url;
        this.t_id = url;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();

        logTimedCode(startTime, t_id + " - starting processing!");

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
            }
        } catch (Exception e) {
            result = "->Red<-\t";
        }
        System.out.println(url + "\t\tStatus:" + result);
        logTimedCode(startTime, t_id + " - processing completed!");

    }

    private static void logTimedCode(long startTime, String msg) {
        long elapsedSeconds = (System.currentTimeMillis() - startTime);
        log.trace("{}ms [{}] {}\n", elapsedSeconds, Thread.currentThread().getName(), msg);
    }

}
