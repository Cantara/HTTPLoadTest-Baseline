package no.cantara.service.commands.oauth2;

import no.cantara.base.command.BaseHttpPostHystrixCommand;

import java.net.HttpURLConnection;
import java.net.URI;


public class CommandAuthorizeOAuth2Application extends BaseHttpPostHystrixCommand<String> {
    /**
     * POST https://api.oauth2server.com/token
     * grant_type=client_credentials&
     * client_id=CLIENT_ID&
     * client_secret=CLIENT_SECRET
     */


    private String CLIENT_ID = "CLIENT_ID";
    private String CLIENT_SECRET = "CLIENT_SECRET";
    int retryCnt = 0;


    public CommandAuthorizeOAuth2Application(String uri) {
        super(URI.create(uri), "hystrixGroupKey", 3000);
    }

    @Override
    protected String dealWithFailedResponse(String responseBody, int statusCode) {
        if (statusCode != HttpURLConnection.HTTP_CONFLICT && retryCnt < 1) {
            retryCnt++;
            return doPostCommand();
        } else {
            return null;
        }
    }

    @Override
    protected String getTargetPath() {

        return "/token"+"?grant_type=client_credentials"+"&client_id"+CLIENT_ID+"&client_secret"+CLIENT_SECRET;
    }


}
