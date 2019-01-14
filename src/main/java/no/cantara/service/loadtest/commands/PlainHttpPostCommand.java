package no.cantara.service.loadtest.commands;

import com.github.kevinsawicki.http.HttpRequest;
import no.cantara.base.command.HttpSender;
import no.cantara.base.util.StringConv;
import no.cantara.service.model.TestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static no.cantara.service.loadtest.util.HTTPResultUtil.first50;

class PlainHttpPostCommand implements Command {

    protected Logger log;
    protected URI serviceUri;
    protected String TAG = "";
    protected HttpRequest request;
    protected long requestDurationMicroSeconds = 0;
    protected final AtomicInteger commandConcurrencyDegree;
    protected int commandConcurrencyDegreeOnEntry;
    protected boolean successfullExecution = false;
    protected boolean responseRejected = false;

    String contentType;
    static Random r = new Random();

    String httpAuthorizationString;
    String template = "";

    PlainHttpPostCommand(TestSpecification testSpecification, AtomicInteger commandConcurrencyDegree) {
        this.serviceUri = URI.create(testSpecification.getCommand_url());
        this.commandConcurrencyDegree = commandConcurrencyDegree;
        this.template = testSpecification.getCommand_template();
        this.contentType = testSpecification.getCommand_contenttype();
        this.httpAuthorizationString = testSpecification.getCommand_http_authstring();
        this.TAG = this.getClass().getName() + ", pool : shared";
        this.log = LoggerFactory.getLogger(TAG);
    }

    public String execute() {
        String result = run();
        successfullExecution = true;
        return result;
    }

    public boolean isSuccessfulExecution() {
        return successfullExecution;
    }

    public boolean isResponseRejected() {
        return responseRejected;
    }

    public boolean isCircuitBreakerOpen() {
        return false;
    }

    //@Override
    protected String run() {
        commandConcurrencyDegreeOnEntry = commandConcurrencyDegree.incrementAndGet();
        try {
            return doPostCommand();
        } finally {
            commandConcurrencyDegree.decrementAndGet();
        }
    }

    protected String doPostCommand() {
        try {
            String uriString = serviceUri.toString();
            if (getTargetPath() != null) {
                uriString += getTargetPath();
            }

            log.trace("TAG" + " - serviceUri={}", uriString);

            long startTime = System.nanoTime();

            if (getQueryParameters() != null && getQueryParameters().length != 0) {
                request = HttpRequest.post(uriString, true, getQueryParameters());
            } else {
                request = HttpRequest.post(uriString);
            }
            request.trustAllCerts();
            request.trustAllHosts();
            request.followRedirects(false);


            request = dealWithRequestBeforeSend(request);

            int statusCode;
            try {
                if (getFormParameters() != null && !getFormParameters().isEmpty()) {
                    request.contentType(HttpSender.APPLICATION_FORM_URLENCODED);
                    request.form(getFormParameters());
                }

                responseBody = request.bytes();
                statusCode = request.code();
            } finally {
                requestDurationMicroSeconds = Math.round((System.nanoTime() - startTime) / 1000.0);
            }

            byte[] responseBodyCopy = responseBody.clone();
            String responseAsText = StringConv.UTF8(responseBodyCopy);
            if (responseBodyCopy.length > 0) {
                log.trace("StringConv: {}", responseAsText);
            }
            switch (statusCode) {
                case java.net.HttpURLConnection.HTTP_OK:
                    onCompleted(responseAsText);
                    return dealWithResponse(responseAsText);
                default:
                    onFailed(responseAsText, statusCode);
                    return dealWithFailedResponse(responseAsText, statusCode);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("TAG" + " - Application authentication failed to execute");
        }
    }

    protected void onFailed(String responseBody, int statusCode) {
        log.debug(TAG + " - Unexpected response from {}. Status code is {} content is {} ", serviceUri, String.valueOf(statusCode) + responseBody);
    }

    protected void onCompleted(String responseBody) {
        log.debug(TAG + " - ok: " + first50(responseBody));
    }

    protected Object[] getQueryParameters() {
        return new String[]{};
    }

    private byte[] responseBody;

    public byte[] getResponseBodyAsByteArray() {
        return responseBody.clone();
    }

    public long getRequestDurationMicroSeconds() {
        return requestDurationMicroSeconds;
    }

    public int getCommandConcurrencyDegreeOnEntry() {
        return commandConcurrencyDegreeOnEntry;
    }

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


    protected Map<String, String> getFormParameters() {
        try {

            Map<String, String> data = new HashMap<String, String>();
            if (contentType.equalsIgnoreCase(HttpSender.APPLICATION_FORM_URLENCODED)) {
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

    protected String dealWithFailedResponse(String responseBody, int statusCode) {

        if (statusCode < 300 && statusCode >= 200) {
            return responseBody;
        }
        return "StatusCode:" + statusCode + ":" + responseBody;

    }

    protected String getTargetPath() {
        return "";
    }

    protected String dealWithResponse(String responseBody) {
        return responseBody;
    }

}

