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
import java.util.concurrent.atomic.AtomicInteger;

class PlainHttpGetCommand implements Command {

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
    String httpAuthorizationString;
    String template;

    PlainHttpGetCommand(TestSpecification testSpecification, AtomicInteger commandConcurrencyDegree) {
        this.serviceUri = URI.create(testSpecification.getCommand_url());
        this.template = testSpecification.getCommand_template();
        this.contentType = testSpecification.getCommand_contenttype();
        this.httpAuthorizationString = testSpecification.getCommand_http_authstring();
        this.commandConcurrencyDegree = commandConcurrencyDegree;
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

    //@Override
    protected String run() {
        commandConcurrencyDegreeOnEntry = commandConcurrencyDegree.incrementAndGet();
        try {
            return doGetCommand();
        } finally {
            commandConcurrencyDegree.decrementAndGet();
        }
    }

    protected String doGetCommand() {
        try {
            String uriString = serviceUri.toString();
            if (getTargetPath() != null) {
                uriString += getTargetPath();
            }

            log.trace("TAG" + " - serviceUri={} ", uriString);

            long startTime = System.nanoTime();

            if (getQueryParameters() != null && getQueryParameters().length != 0) {
                request = HttpRequest.get(uriString, true, getQueryParameters());
            } else {
                request = HttpRequest.get(uriString);
            }

//            request.trustAllCerts();
//            request.trustAllHosts();
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

            String location = "";
            String responseAsText = StringConv.UTF8(responseBody);
            if (statusCode == 302) {
                location = request.getConnection().getHeaderField("Location");
                responseAsText = "{\"Location\": \"" + location + "\"}";
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
        log.debug(TAG + " - ok: " + responseBody);
    }

    protected Map<String, String> getFormParameters() {
        return new HashMap<String, String>();
    }

    protected Object[] getQueryParameters() {
        return new String[]{};
    }

    protected String dealWithResponse(String response) {
        return response;
    }

    private byte[] responseBody;

    public byte[] getResponseBodyAsByteArray() {
        return responseBody;
    }

    public long getRequestDurationMicroSeconds() {
        return requestDurationMicroSeconds;
    }

    public int getCommandConcurrencyDegreeOnEntry() {
        return commandConcurrencyDegreeOnEntry;
    }

    private String headerName(String header) {
        return header.substring(0, header.indexOf(":")).trim();
    }

    private String headerValue(String header) {
        return header.substring(header.indexOf(":") + 1, header.length()).trim();
    }

    protected HttpRequest dealWithRequestBeforeSend(HttpRequest request) {
        // request= super.dealWithRequestBeforeSend(request);
        if (this.httpAuthorizationString != null && this.httpAuthorizationString.length() > 10) {
            if (httpAuthorizationString.startsWith("X-AUTH")) {
                request = request.header(headerName(httpAuthorizationString), headerValue(httpAuthorizationString));
            } else {
                request = request.authorization(this.httpAuthorizationString);
                log.info("Added authorizarion header: {}", this.httpAuthorizationString);
                //log.trace(request.header("Authorization"));

            }

        }

        if (template.contains("soapenv:Envelope")) {
            //request.getConnection().addRequestProperty("SOAPAction", SOAP_ACTION);
        }

        request = request.contentType(contentType).accept("*/*");  //;
        if (template == null || template.length() > 1) {
            return request;
        } else {
            //request=request.body(this.template);
            return request;
        }
    }

    protected String dealWithFailedResponse(String responseBody, int statusCode) {
        if (statusCode < 300 && statusCode >= 200) {
            return responseBody;
        }
        if (statusCode == 302) {
            responseBody = "[{\"code\": \""
                    + responseBody.substring(responseBody.indexOf("code=") + 5, responseBody.indexOf("&state"))
                    + "\"}]";
            return responseBody;
        }
        return "StatusCode:" + statusCode + ":" + responseBody;
    }

    protected String getTargetPath() {
        return "";
    }
}
