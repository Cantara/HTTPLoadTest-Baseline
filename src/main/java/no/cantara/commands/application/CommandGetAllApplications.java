package no.cantara.commands.application;

import no.cantara.base.command.BaseHttpGetHystrixCommand;

import java.net.URI;

public class CommandGetAllApplications extends BaseHttpGetHystrixCommand<String> {

    String uri;

    public CommandGetAllApplications(String uri){

        super(URI.create(uri), "hystrixGroupKey");
        this.uri = uri;
    }


    @Override
	protected String getTargetPath() {
        return "basicauthapplication/";
    }
}
