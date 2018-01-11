package no.cantara.service.commands.oauth2;

import com.github.kevinsawicki.http.HttpRequest;
import no.cantara.base.command.BaseHttpGetHystrixCommand;

import java.net.URI;

public class CommandVerifyToken extends BaseHttpGetHystrixCommand<String> {

    String token;

    public CommandVerifyToken(String uri, String token) {

        super(URI.create(uri), "systemevents");
        this.token = token;
    }


    @Override
    protected HttpRequest dealWithRequestBeforeSend(HttpRequest request) {
        return request.authorization("Bearer " + this.token);
    }

    @Override
    protected String getTargetPath() {
        return "/verify";
    }
}

