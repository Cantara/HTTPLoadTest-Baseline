package no.cantara.service.loadtest.drivers;

import no.cantara.service.loadtest.LoadTestExecutorService;
import no.cantara.service.loadtest.commands.CommandGetFromTestSpecification;
import no.cantara.service.loadtest.commands.CommandPostFromTestSpecification;
import no.cantara.service.loadtest.util.HTTPResultUtil;
import no.cantara.service.model.LoadTestConfig;
import no.cantara.service.model.LoadTestResult;
import no.cantara.service.model.TestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static no.cantara.service.loadtest.LoadTestExecutorService.isRunning;


public class MyReadRunnable implements Runnable {
    private static Random r = new Random();
    private final LoadTestResult loadTestResult;
    private final LoadTestConfig loadTestConfig;
    private final List<TestSpecification> testSpecificationList;
    private static final Logger log = LoggerFactory.getLogger(MyReadRunnable.class);

    public MyReadRunnable(List<TestSpecification> testSpecificationList, LoadTestConfig loadTestConfig, LoadTestResult loadTestResult) {
        this.testSpecificationList = testSpecificationList;
        this.loadTestResult = loadTestResult;
        this.loadTestConfig = loadTestConfig;
        this.loadTestResult.setTest_tags("LoadTestId: " + loadTestConfig.getTest_id());
    }

    @Override
    public void run() {
        if (!isRunning()) {
            return;
        }
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
        long startTime = System.currentTimeMillis();

        logTimedCode(startTime, loadTestResult.getTest_run_no() + " - starting processing!");
        Map<String, String> resolvedResultVariables = new HashMap<>();

        int readCommandNo = 1;
        for (TestSpecification testSpecification : testSpecificationList) {
            testSpecification.addMapToCommand_replacement_map(resolvedResultVariables);

            if (testSpecification.getCommand_url().length() > 0) {


                log.trace("Calling {}", testSpecification.getCommand_url());
                loadTestResult.setTest_success(true);
                loadTestResult.setTest_tags(loadTestResult.getTest_tags() +
                        " - (Read-URL:" + readCommandNo++ + "/" + Thread.currentThread().getName() + " " + testSpecification.getCommand_url() + ")");

                String result;
                if (testSpecification.isCommand_http_post()) {
                    CommandPostFromTestSpecification command = new CommandPostFromTestSpecification(testSpecification);
                    result = command.execute();
                    if (!command.isSuccessfulExecution()) {
                        loadTestResult.setTest_success(false);
                    }
                    if (command.isResponseRejected()) {
                        loadTestResult.setTest_deviation_flag(true);
                    }
                } else {
                    CommandGetFromTestSpecification command = new CommandGetFromTestSpecification(testSpecification);
                    result = command.execute();
                    if (!command.isSuccessfulExecution()) {
                        loadTestResult.setTest_success(false);
                    }
                    if (command.isResponseRejected()) {
                        loadTestResult.setTest_deviation_flag(true);
                    }
                }
//            log.trace("Returned result: " + result);
                resolvedResultVariables = HTTPResultUtil.parseWithJsonPath(result, testSpecification.getCommand_response_map());
            }
        }

        loadTestResult.setTest_duration(Long.valueOf(System.currentTimeMillis() - startTime));
        logTimedCode(startTime, loadTestResult.getTest_run_no() + " - processing completed!");

        LoadTestExecutorService.addResult(loadTestResult);

    }

    private static void logTimedCode(long startTime, String msg) {
        long elapsedSeconds = (System.currentTimeMillis() - startTime);
        //log.trace("{}ms [{}] {}\n", elapsedSeconds, Thread.currentThread().getName(), msg);
    }

}
