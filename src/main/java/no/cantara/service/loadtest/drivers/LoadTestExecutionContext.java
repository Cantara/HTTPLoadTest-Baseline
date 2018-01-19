package no.cantara.service.loadtest.drivers;

public interface LoadTestExecutionContext {

    /**
     * Whether this execution-context has not yet received a stop signal.
     *
     * @return
     */
    boolean stopped();
}
