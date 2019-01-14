package no.cantara.service.loadtest.drivers;

import no.cantara.service.loadtest.commands.CommandGetFromTestSpecification;
import no.cantara.service.loadtest.commands.CommandPostFromTestSpecification;
import no.cantara.service.loadtest.util.HTTPResultUtil;
import no.cantara.service.model.LoadTestConfig;
import no.cantara.service.model.LoadTestResult;
import no.cantara.service.model.TestSpecification;
import no.cantara.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static no.cantara.service.loadtest.util.HTTPResultUtil.first150;
import static no.cantara.service.loadtest.util.HTTPResultUtil.first50;
import static no.cantara.service.loadtest.util.HTTPResultUtil.firstX;


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
        long startNanoTime = System.nanoTime();

        logTimedCode(startNanoTime, loadTestResult.getTest_run_no() + " - starting processing!");
        Map<String, String> resolvedResultVariables = new LinkedHashMap<>();
        Map<String, String> inheritedVariables = loadTestConfig.getTest_global_variables_map();

        long commandDurationMicroSeconds = 0;
        int readCommandNo = 1;
        for (TestSpecification testSpecificationo : testSpecificationList) {
            try {
                TestSpecification testSpecification = testSpecificationo.clone();
                testSpecification.resolveVariables(loadTestConfig.getTest_global_variables_map(), inheritedVariables, resolvedResultVariables);
                inheritedVariables.putAll(testSpecification.getCommand_replacement_map());

                if (testSpecification.getCommand_url().length() > 0) {


                    log.trace("Calling {} \n- template:{}", testSpecification.getCommand_url(), testSpecification.getCommand_template());
                    loadTestResult.setTest_success(false);
                    loadTestResult.setTest_tags(loadTestResult.getTest_tags() +
                            " - (Read-URL:" + readCommandNo++ + "/" + Thread.currentThread().getName() + " " + testSpecification.getCommand_url() + ")");

                    String result = null;
                    try {
                        if (testSpecification.isCommand_http_post()) {
                            CommandPostFromTestSpecification postcommand = new CommandPostFromTestSpecification(testSpecification, loadTestExecutionContext.commandConcurrencyDegree());
                            result = postcommand.execute();
                            loadTestResult.setCommand_concurrency_degree(postcommand.getCommandConcurrencyDegreeOnEntry());
                            commandDurationMicroSeconds = commandDurationMicroSeconds + postcommand.getRequestDurationMicroSeconds();
                            log.trace("{} returned response: {}", testSpecification.getCommand_url(), result);
                            if (!postcommand.isSuccessfulExecution()) {
                                loadTestResult.setTest_success(false);
                                loadTestResult.setTest_tags(loadTestResult.getTest_tags() + ":F(" + firstX(result, 250) + ") + Req:( -" + testSpecification.toLongString() + ") - ");
                            }
                            if (postcommand.isResponseRejected()) {
                                loadTestResult.setTest_deviation_flag(true);
                                loadTestResult.setTest_tags(loadTestResult.getTest_tags() + ":R(" + first50(result) + ") -");
                            }
                        } else {
                            CommandGetFromTestSpecification getcommand = new CommandGetFromTestSpecification(testSpecification, loadTestExecutionContext.commandConcurrencyDegree());
                            result = getcommand.execute();
                            loadTestResult.setCommand_concurrency_degree(getcommand.getCommandConcurrencyDegreeOnEntry());
                            commandDurationMicroSeconds = commandDurationMicroSeconds + getcommand.getRequestDurationMicroSeconds();
                            log.trace("{} returned response: {}", testSpecification.getCommand_url(), result);
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
                    log.trace("Returned result: R-{}.{} - {} ", loadTestResult.getTest_run_no(), readCommandNo, result);
                    if (result == null || result.startsWith("StatusCode:")) {
                        loadTestResult.setTest_success(false);
                        loadTestResult.setTest_tags(loadTestResult.getTest_tags() + ":F(" + firstX(result, 250) + ") + Req:( -" + testSpecification.toLongString() + ") - ");
                    } else {
                        loadTestResult.setTest_success(true);
                        resolvedResultVariables = HTTPResultUtil.parse(result, testSpecification.getCommand_response_map());
                        log.trace("{} Resolved variables: {}", testSpecification.getCommand_url(), resolvedResultVariables);
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
            log.warn("commandDuration: {} microseconds, using fallback", commandDurationMicroSeconds);
            // fallback to include test-bench processing overhead as part of measured duration
            commandDurationMicroSeconds = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startNanoTime);
        }

        loadTestResult.setTest_duration(commandDurationMicroSeconds / 1000.0);
        logTimedCode(startNanoTime, loadTestResult.getTest_run_no() + " - processing completed!");

        return loadTestResult;

    }

    private static void logTimedCode(long startNanoTime, String msg) {
        // long elapsedMilliseconds = Math.round((System.nanoTime() - startNanoTime) / 1000000.0);
        // log.trace("{}ms [{}] {}\n", elapsedMilliseconds, Thread.currentThread().getName(), msg);
    }

}
