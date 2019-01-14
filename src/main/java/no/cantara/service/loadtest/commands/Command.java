package no.cantara.service.loadtest.commands;

public interface Command {
    String execute();

    int getCommandConcurrencyDegreeOnEntry();

    long getRequestDurationMicroSeconds();

    boolean isSuccessfulExecution();

    boolean isResponseRejected();
}
