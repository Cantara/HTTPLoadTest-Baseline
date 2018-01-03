package no.cantara.service.loadtest.commands;

import no.cantara.base.command.BaseHttpGetHystrixCommand;

import java.net.URI;

public class CommandGetURL extends BaseHttpGetHystrixCommand<String> {

    String uri;

    public CommandGetURL(String uri) {

        super(URI.create(uri), "hystrixGroupKey");
        this.uri = uri;
    }

    public CommandGetURL(String uri, int timeout) {

        super(URI.create(uri), "hystrixGroupKey", timeout);
        this.uri = uri;
    }

    @Override
    protected String getTargetPath() {
        return "";
    }
}
