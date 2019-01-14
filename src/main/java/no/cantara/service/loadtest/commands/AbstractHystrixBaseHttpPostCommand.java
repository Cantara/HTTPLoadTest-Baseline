package no.cantara.service.loadtest.commands;

import com.github.kevinsawicki.http.HttpRequest;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import no.cantara.base.command.HttpSender;
import no.cantara.base.util.StringConv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.cantara.service.loadtest.util.HTTPResultUtil.first50;

abstract class AbstractHystrixBaseHttpPostCommand<R> extends HystrixCommand<R> {

    protected Logger log;
    protected URI serviceUri;
    protected String TAG = "";
    protected HttpRequest request;
    protected long requestDurationMicroSeconds = 0;
    protected final AtomicInteger commandConcurrencyDegree;
    protected int commandConcurrencyDegreeOnEntry;

    AbstractHystrixBaseHttpPostCommand(URI serviceUri, String hystrixGroupKey, AtomicInteger commandConcurrencyDegree) {
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(hystrixGroupKey)));
        this.commandConcurrencyDegree = commandConcurrencyDegree;
        init(serviceUri, hystrixGroupKey);
    }


    private void init(URI serviceUri, String hystrixGroupKey) {
        this.serviceUri = serviceUri;
        this.TAG = this.getClass().getName() + ", pool :" + hystrixGroupKey;
        this.log = LoggerFactory.getLogger(TAG);
    }


    @Override
    protected R run() {
        commandConcurrencyDegreeOnEntry = commandConcurrencyDegree.incrementAndGet();
        try {
            return doPostCommand();
        } finally {
            commandConcurrencyDegree.decrementAndGet();
        }
    }

    protected R doPostCommand() {
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

    protected R dealWithFailedResponse(String responseBody, int statusCode) {
        return null;
    }

    protected HttpRequest dealWithRequestBeforeSend(HttpRequest request) {
        //CAN USE MULTIPART

        //JUST EXAMPLE

        //		HttpRequest request = HttpRequest.post("http://google.com");
        //		request.part("status[body]", "Making a multipart request");
        //		request.part("status[image]", new File("/home/kevin/Pictures/ide.png"));

        //OR SEND SOME DATA

        //request.send("name=huydo")
        //or something like
        //request.contentType("application/json").send(applicationJson);

        return request;
    }

    protected void onFailed(String responseBody, int statusCode) {
        log.debug(TAG + " - Unexpected response from {}. Status code is {} content is {} ", serviceUri, String.valueOf(statusCode) + responseBody);
    }

    protected void onCompleted(String responseBody) {
        log.debug(TAG + " - ok: " + first50(responseBody));
    }


    protected abstract String getTargetPath();

    protected Map<String, String> getFormParameters() {
        return new HashMap<String, String>();
    }

    protected Object[] getQueryParameters() {
        return new String[]{};
    }

    @SuppressWarnings("unchecked")
    protected R dealWithResponse(String response) {
        return (R) response;
    }

    @Override
    protected R getFallback() {
        log.warn(TAG + " - fallback - serviceUri={}", serviceUri.toString() + getTargetPath());
        return null;
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
}

