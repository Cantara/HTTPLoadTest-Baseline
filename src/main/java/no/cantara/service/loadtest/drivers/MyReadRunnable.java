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

import static no.cantara.service.loadtest.util.HTTPResultUtil.*;


public class MyReadRunnable implements Callable<LoadTestResult> {
    private static Random r = new Random();
    private final LoadTestResult loadTestResult;
    private final LoadTestConfig loadTestConfig;
    private final LoadTestExecutionContext loadTestExecutionContext;
    private final List<TestSpecification> testSpecificationList;
    private static final boolean BREAK_ON_FAILURE = Configuration.getBoolean("loadtest.breakflowonfailure");
    private static final Logger log = LoggerFactory.getLogger(MyReadRunnable.class);

    public MyReadRunnable(List<TestSpecification> testSpecificationList, LoadTestConfig loadTestConfig, LoadTestResult loadTestResult, LoadTestExecutionContext loadTestExecutionContext) {
        this.testSpecificationList = testSpecificationList;
        this.loadTestResult = loadTestResult;
        this.loadTestConfig = loadTestConfig;
        this.loadTestExecutionContext = loadTestExecutionContext;
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
            logTimedCode(startTime, loadTestConfig.getTest_id() + " - MyReadRunnable was interrupted!");
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
        Map<String, String> inheritedVariables = loadTestConfig.getTest_global_variables_map();

        long commandDurationMicroSeconds = 0;
        int readCommandNo = 1;
        for (TestSpecification testSpecificationo : testSpecificationList) {
            try {
                TestSpecification testSpecification = testSpecificationo.clone();
                testSpecification.resolveVariables(loadTestConfig.getTest_global_variables_map(), inheritedVariables, resolvedResultVariables);
                inheritedVariables.putAll(testSpecification.getCommand_replacement_map());

                if (testSpecification.getCommand_url().length() > 0) {


                    log.info("Calling {} \n- template:{}", testSpecification.getCommand_url(), testSpecification.getCommand_template());
                    loadTestResult.setTest_success(false);
                    loadTestResult.setTest_tags(loadTestResult.getTest_tags() +
                            " - (Read-URL:" + readCommandNo++ + "/" + Thread.currentThread().getName() + " " + testSpecification.getCommand_url() + ")");

                    String result = null;
                    try {
                        if (testSpecification.isCommand_http_post()) {
                            CommandPostFromTestSpecification postcommand = new CommandPostFromTestSpecification(testSpecification);
                            result = postcommand.execute();
                            commandDurationMicroSeconds = commandDurationMicroSeconds + postcommand.getRequestDurationMicroSeconds();
                            log.info("{} returned response: {}", testSpecification.getCommand_url(), result);
                            if (!postcommand.isSuccessfulExecution()) {
                                loadTestResult.setTest_success(false);
                                loadTestResult.setTest_tags(loadTestResult.getTest_tags() + ":F(" + firstX(result, 250) + ") + Req:( -" + testSpecification.toLongString() + ") - ");
                            }
                            if (postcommand.isResponseRejected()) {
                                loadTestResult.setTest_deviation_flag(true);
                                loadTestResult.setTest_tags(loadTestResult.getTest_tags() + ":R(" + first50(result) + ") -");
                            }
                        } else {
                            CommandGetFromTestSpecification getcommand = new CommandGetFromTestSpecification(testSpecification);
                            result = getcommand.execute();
                            commandDurationMicroSeconds = commandDurationMicroSeconds + getcommand.getRequestDurationMicroSeconds();
                            log.info("{} returned response: {}", testSpecification.getCommand_url(), result);
                            if (!getcommand.isSuccessfulExecution()) {
                                loadTestResult.setTest_success(false);
                                loadTestResult.setTest_tags(loadTestResult.getTest_tags() + ":F(" + firstX(result, 250) + ") + Req:( -" + testSpecification.toLongString() + ") - ");
                            }
                            if (getcommand.isResponseRejected()) {
                                loadTestResult.setTest_deviation_flag(true);
                                loadTestResult.setTest_tags(loadTestResult.getTest_tags() + ":R(" + first50(result) + ") -");
                            }
                        }
                    } catch (Exception e) {
                        log.error("Unable to instansiate TestSpecification", e);
                        loadTestResult.setTest_tags(loadTestResult.getTest_tags() + ":Unable to instansiate TestSpecification(" + first50(e.getMessage()) + ") -");
                    }
                    log.info("Returned result: R-{}.{} - {} ", loadTestResult.getTest_run_no(), readCommandNo, result);
                    if (result == null || result.startsWith("StatusCode:")) {
                        loadTestResult.setTest_success(false);
                        loadTestResult.setTest_tags(loadTestResult.getTest_tags() + ":F(" + firstX(result, 250) + ") + Req:( -" + testSpecification.toLongString() + ") - ");
                    } else {
                        loadTestResult.setTest_success(true);
                        resolvedResultVariables = HTTPResultUtil.parse(result, testSpecification.getCommand_response_map());
                        log.info("{} Resolved variables: {}", testSpecification.getCommand_url(), resolvedResultVariables);
                        loadTestResult.setTest_tags(loadTestResult.getTest_tags() + ":S(" + first150(result) + ") -:vars(" + resolvedResultVariables + ") + Req:( -" + testSpecification.toLongString() + ") - ");
                    }

                }
            } catch (Exception e) {
                log.error("Unable to clone TestSpecification");
            }

            // We break the flow if one step fail
            if (!loadTestResult.isTest_success() && BREAK_ON_FAILURE) {
                break;
            }
        }

        if (commandDurationMicroSeconds <= 0) {
            log.warn("commandDuration: {}, using fallback", commandDurationMicroSeconds);
            // fallback to include test-bench processing overhead as part of measured duration
            commandDurationMicroSeconds = 1000 * Long.valueOf(System.currentTimeMillis() - startTime);
        }

        loadTestResult.setTest_duration(Math.round(commandDurationMicroSeconds / 1000.0));
        logTimedCode(startTime, loadTestResult.getTest_run_no() + " - processing completed!");

        return loadTestResult;

    }

    private static void logTimedCode(long startTime, String msg) {
        // long elapsedSeconds = (System.currentTimeMillis() - startTime);
        //log.trace("{}ms [{}] {}\n", elapsedSeconds, Thread.currentThread().getName(), msg);
    }

}
