package no.cantara.service.loadtest.drivers;

import java.util.concurrent.atomic.AtomicInteger;

public interface LoadTestExecutionContext {

    /**
     * Whether this execution-context has not yet received a stop signal.
     *
     * @return
     */
    boolean stopped();

    AtomicInteger workerConcurrencyDegree();

    AtomicInteger commandConcurrencyDegree();
}
