package no.cantara.service.loadtest.commands;

import com.github.kevinsawicki.http.HttpRequest;
import no.cantara.base.command.BaseHttpPostHystrixCommand;
import no.cantara.service.loadtest.util.TemplateUtil;
import no.cantara.service.model.TestSpecification;

import java.net.URI;
import java.util.Random;

public class CommandPostFromTestSpecification extends BaseHttpPostHystrixCommand<String> {

    String contentType = "text/xml;charset=UTF-8";
    static Random r = new Random();

    String httpAuthorizationString;
    String template = "";


    public CommandPostFromTestSpecification(TestSpecification testSpecification) {
        super(URI.create(testSpecification.getCommand_url()),
                "hystrixCommandPost FromTestSpecification");
        this.template = TemplateUtil.updateTemplateWithValuesFromMap(testSpecification.getCommand_template(), testSpecification.getCommand_replacement_map());
        this.contentType = testSpecification.getCommand_contenttype();
        this.httpAuthorizationString = testSpecification.getCommand_http_authstring();
    }




    @Override
    protected HttpRequest dealWithRequestBeforeSend(HttpRequest request) {
        super.dealWithRequestBeforeSend(request);
        if (httpAuthorizationString != null && httpAuthorizationString.length() > 10) {
            request.authorization(httpAuthorizationString);
        }

        if (template.contains("soapenv:Envelope")) {
            //request.getConnection().addRequestProperty("SOAPAction", SOAP_ACTION);
        }

        request.contentType(contentType).send(this.template);
        log.trace(request.header("Authorization"));
        return request;
    }


    @Override
    protected String getTargetPath() {
        return "";
    }

    @Override
    protected String dealWithFailedResponse(String responseBody, int statusCode) {
        if (statusCode < 300 && statusCode >= 200) {
            return responseBody;
        }
        return "StatusCode:" + statusCode + ":" + responseBody;
    }

    @Override
    protected String dealWithResponse(String response) {
        //return "200" + ":" + super.dealWithResponse(response);
        return super.dealWithResponse(response);
    }

}
