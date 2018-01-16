package no.cantara.service.loadtest.drivers;

import no.cantara.service.model.LoadTestResult;

public interface LoadTestExecutionContext {

    /**
     * Indicate that result from a single test is ready.
     *
     * @param loadTestResult
     */
    void addResult(LoadTestResult loadTestResult);

    /**
     * Whether this execution-context is still running.
     *
     * @return
     */
    boolean isRunning();
}
