package no.cantara.service.loadtest.commands;

import com.github.kevinsawicki.http.HttpRequest;
import no.cantara.base.command.BaseHttpGetHystrixCommand;

import java.net.URI;

public class CommandGetAuthUrl extends BaseHttpGetHystrixCommand<String> {

    String uri;
    String httpAuthorizationString;

    public CommandGetAuthUrl(String uri, String httpAuthorizationString) {

        super(URI.create(uri), "hystrixGroupKey");
        this.httpAuthorizationString = httpAuthorizationString;
        this.uri = uri;
    }

    public CommandGetAuthUrl(String uri, String httpAuthorizationString, int timeout) {

        super(URI.create(uri), "hystrixGroupKey", timeout);
        this.httpAuthorizationString = httpAuthorizationString;
        this.uri = uri;
    }

    @Override
    protected HttpRequest dealWithRequestBeforeSend(HttpRequest request) {
        if (httpAuthorizationString == null || httpAuthorizationString.length() < 10) {
            return request.authorization(httpAuthorizationString);
        }
        return request;
    }


    @Override
    protected String getTargetPath() {
        return "";
    }
}


