package no.cantara.service.loadtest.commands;

import no.cantara.service.model.TestSpecification;

import java.util.concurrent.atomic.AtomicInteger;

public class CommandFactory {

    public static Command createPostCommand(TestSpecification testSpecification, AtomicInteger commandConcurrencyDegree) {
        if (testSpecification.isCommand_use_hystrix()) {
            return createHystrixPostCommand(testSpecification, commandConcurrencyDegree);
        } else {
            return createPlainPostCommand(testSpecification, commandConcurrencyDegree);
        }
    }

    public static Command createGetCommand(TestSpecification testSpecification, AtomicInteger commandConcurrencyDegree) {
        if (testSpecification.isCommand_use_hystrix()) {
            return createHystrixGetCommand(testSpecification, commandConcurrencyDegree);
        } else {
            return createPlainGetCommand(testSpecification, commandConcurrencyDegree);
        }
    }

    public static Command createHystrixPostCommand(TestSpecification testSpecification, AtomicInteger commandConcurrencyDegree) {
        return new HystrixPostCommand(testSpecification, commandConcurrencyDegree);
    }

    public static Command createHystrixGetCommand(TestSpecification testSpecification, AtomicInteger commandConcurrencyDegree) {
        return new HystrixGetCommand(testSpecification, commandConcurrencyDegree);
    }

    public static Command createPlainPostCommand(TestSpecification testSpecification, AtomicInteger commandConcurrencyDegree) {
        return new PlainHttpPostCommand(testSpecification, commandConcurrencyDegree);
    }

    public static Command createPlainGetCommand(TestSpecification testSpecification, AtomicInteger commandConcurrencyDegree) {
        return new PlainHttpGetCommand(testSpecification, commandConcurrencyDegree);
    }
}
