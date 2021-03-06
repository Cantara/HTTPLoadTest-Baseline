package no.cantara.service.loadtest.commands;

import com.github.kevinsawicki.http.HttpRequest;
import no.cantara.base.command.HttpSender;
import no.cantara.service.model.TestSpecification;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

class HystrixPostCommand extends AbstractHystrixBaseHttpPostCommand<String> implements Command {

    String contentType;
    static Random r = new Random();

    String httpAuthorizationString;
    String template = "";


    HystrixPostCommand(TestSpecification testSpecification, AtomicInteger commandConcurrencyDegree) {
        super(URI.create(testSpecification.getCommand_url()),
                "hystrixCommandPostFromTestSpecification_" + r.nextInt(100),
                commandConcurrencyDegree);
        this.template = testSpecification.getCommand_template();
        this.contentType = testSpecification.getCommand_contenttype();
        this.httpAuthorizationString = testSpecification.getCommand_http_authstring();
    }


    @Override
    protected HttpRequest dealWithRequestBeforeSend(HttpRequest request) {
        //super.dealWithRequestBeforeSend(request);
        if (this.httpAuthorizationString != null && this.httpAuthorizationString.length() > 10) {

            log.info("Added authorizarion header: {}", this.httpAuthorizationString);
            if (getFormParameters() == null || getFormParameters().isEmpty()) {
                return request.authorization(this.httpAuthorizationString).contentType(contentType).accept("*/*").send(this.template);

            } else {
                return request.authorization(this.httpAuthorizationString).contentType(contentType).accept("*/*").acceptJson();

            }
        }

        if (template.contains("soapenv:Envelope")) {
            //request.getConnection().addRequestProperty("SOAPAction", SOAP_ACTION);
        }

        return request.contentType(contentType).accept("*/*").send(this.template);
    }


    @Override
    protected Map<String, String> getFormParameters() {
        try {

            Map<String, String> data = new HashMap<String, String>();
            if (contentType.equalsIgnoreCase(HttpSender.APPLICATION_FORM_URLENCODED)){
                String[] formParams = template.split("&");
                for (String formParam : formParams) {
                    if (formParam.indexOf("=") > 1) {

                        String key = formParam.substring(0, formParam.indexOf("="));
                        String value = formParam.substring(formParam.indexOf("=") + 1, formParam.length());

                        data.put(key, value);
                    }
                }
            }
            return data;
        } catch (Exception e) {
            log.error("Unable to resove form: ", e);

        }
        return null;
    }

    @Override
    protected String dealWithFailedResponse(String responseBody, int statusCode) {

        if (statusCode < 300 && statusCode >= 200) {
            return responseBody;
        }
        return "StatusCode:" + statusCode + ":" + responseBody;

    }

    @Override
    protected String getTargetPath() {
        return "";
    }


    @Override
    protected String dealWithResponse(String responseBody) {
        //return "200" + ":" + super.dealWithResponse(response);
        return responseBody;
//        return super.dealWithResponse(response);
    }

}
