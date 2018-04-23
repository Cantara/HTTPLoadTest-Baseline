package no.cantara.service.loadtest.drivers;

import no.cantara.service.loadtest.commands.CommandGetFromTestSpecification;
import no.cantara.service.loadtest.commands.CommandPostFromTestSpecification;
import no.cantara.service.loadtest.util.HTTPResultUtil;
import no.cantara.service.loadtest.util.TimedProcessingUtil;
import no.cantara.service.model.LoadTestConfig;
import no.cantara.service.model.LoadTestResult;
import no.cantara.service.model.TestSpecification;
import no.cantara.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static no.cantara.service.loadtest.util.HTTPResultUtil.first150;
import static no.cantara.service.loadtest.util.HTTPResultUtil.first50;

public class MyWriteRunnable implements Callable<LoadTestResult> {
    private final List<TestSpecification> testSpecificationList;
    private static Random r = new Random();
    private final LoadTestResult loadTestResult;
    private final LoadTestConfig loadTestConfig;
    private final LoadTestExecutionContext loadTestExecutionContext;
    private static final boolean BREAK_ON_FAILURE = Configuration.getBoolean("loadtest.breakflowonfailure");
    private static final Logger log = LoggerFactory.getLogger(MyWriteRunnable.class);

    public MyWriteRunnable(List<TestSpecification> testSpecificationList, LoadTestConfig loadTestConfig, LoadTestResult loadTestResult, LoadTestExecutionContext loadTestExecutionContext) {
        this.testSpecificationList = testSpecificationList;
        this.loadTestResult = loadTestResult;
        this.loadTestConfig = loadTestConfig;
        this.loadTestExecutionContext = loadTestExecutionContext;
        //this.loadTestResult.setTest_tags("testSpecificationList: " + testSpecificationList);
        this.loadTestResult.setTest_tags("LoadTestId: " + loadTestConfig.getTest_id());
    }

    @Override
    public LoadTestResult call() {
        if (loadTestExecutionContext.stopped()) {
            return null;
        }

        long startTime = System.currentTimeMillis();
        try {
            return TimedProcessingUtil.runWithTimeout(new Callable<LoadTestResult>() {
                @Override
                public LoadTestResult call() {
                    return execute();
                }
            }, loadTestConfig.getTest_duration_in_seconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            logTimedCode(startTime, loadTestConfig.getTest_id() + " - MyWriteRunnable was interrupted!");
            return null;
        }
    }

    private LoadTestResult execute() {
        if (loadTestExecutionContext.stopped()) {
            return null;
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
        Map<String, String> inheritedVariables = new HashMap<>();

        int writeCommandNo = 1;
        for (TestSpecification testSpecificationo : testSpecificationList) {
            try {
                TestSpecification testSpecification = testSpecificationo.clone();

                testSpecification.resolveVariables(loadTestConfig.getTest_global_variables_map(), inheritedVariables, resolvedResultVariables);
                inheritedVariables = testSpecification.getCommand_replacement_map();

                if (testSpecification.getCommand_url().length() > 0) {
                    log.info("Calling {} \n- template:{}", testSpecification.getCommand_url(), testSpecification.getCommand_template());
                    loadTestResult.setTest_success(false);
                    loadTestResult.setTest_tags(loadTestResult.getTest_tags() + " - (Write-URL:" + writeCommandNo++ + "/" +
                            Thread.currentThread().getName() + " " + testSpecification.getCommand_url() + ")");
                    String result;
                    if (testSpecification.isCommand_http_post()) {
                        CommandPostFromTestSpecification command = new CommandPostFromTestSpecification(testSpecification);
                        result = command.execute();
                        log.info("Response: {}", result);
                        if (!command.isSuccessfulExecution()) {
                            loadTestResult.setTest_success(false);
                            loadTestResult.setTest_tags(loadTestResult.getTest_tags() + ":F(" + first150(result) + ") + Req:( -" + testSpecification.toLongString() + ") - ");
                        }
                        if (command.isResponseRejected()) {
                            loadTestResult.setTest_deviation_flag(true);
                            loadTestResult.setTest_tags(loadTestResult.getTest_tags() + ":D(" + first50(result) + ") -");
                        }
                    } else {
                        CommandGetFromTestSpecification command = new CommandGetFromTestSpecification(testSpecification);
                        result = command.execute();
                        log.info("Response: {}", result);
                        if (!command.isSuccessfulExecution()) {
                            loadTestResult.setTest_success(false);
                            loadTestResult.setTest_tags(loadTestResult.getTest_tags() + ":F(" + first150(result) + ") + Req:( -" + testSpecification.toLongString() + ") - ");
                        }
                        if (command.isResponseRejected()) {
                            loadTestResult.setTest_deviation_flag(true);
                            loadTestResult.setTest_tags(loadTestResult.getTest_tags() + ":D(" + first50(result) + ") -");
                        }
                    }
//            log.trace("Returned result: " + result);
                    if (result == null || result.startsWith("StatusCode:")) {
                        loadTestResult.setTest_success(false);
                        loadTestResult.setTest_tags(loadTestResult.getTest_tags() + ":F(" + first150(result) + ") + Req:( -" + testSpecification.toLongString() + ") - ");
                    } else {
                        loadTestResult.setTest_success(true);
                        resolvedResultVariables = HTTPResultUtil.parse(result, testSpecification.getCommand_response_map());
                        loadTestResult.setTest_tags(loadTestResult.getTest_tags() + ":S(" + first50(result) + ") -:Vars(" + resolvedResultVariables + ") - ");
                        log.info("Resolved variables: {} result: {} from command_response_map: {}", resolvedResultVariables, result, testSpecification.getCommand_response_map());
                    }
                }
                // We break the flow if one step fail
                if (!loadTestResult.isTest_success() && BREAK_ON_FAILURE) {
                    break;
                }
            } catch (Exception e) {
                log.error("Unable to clone TestSpecification");
            }


        }
        loadTestResult.setTest_duration(Long.valueOf(System.currentTimeMillis() - startTime));
        logTimedCode(startTime, loadTestResult.getTest_run_no() + " - processing completed!");

        return loadTestResult;

    }

    private static void logTimedCode(long startTime, String msg) {
        // long elapsedSeconds = (System.currentTimeMillis() - startTime);
        // log.trace("{}ms [{}] {}\n", elapsedSeconds, Thread.currentThread().getName(), msg);
    }

}
