package no.cantara.service.loadtest.commands;

import com.github.kevinsawicki.http.HttpRequest;
import no.cantara.service.loadtest.util.TemplateUtil;
import no.cantara.service.model.TestSpecification;

import java.net.URI;
import java.util.Random;

public class CommandGetFromTestSpecification extends MyBaseHttpGetHystrixCommand<String> {

    String contentType = "text/xml;charset=UTF-8";
    String httpAuthorizationString;
    static Random r = new Random();
    String template = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:aut=\"http://dbonline.no/webservices/xsd/Autorisasjon\" xmlns:per=\"http://dbonline.no/webservices/xsd/PersonInfo\">\n" +
            "   <soapenv:Header>\n" +
            "      <aut:UserAuthorization>\n" +
            "         <UserID>#BrukerID</UserID>\n" +
            "         <Passord>#Passord</Passord>\n" +
            "         <EndUser>MyEndUserName</EndUser>\n" +
            "         <Versjon>v1-1-0</Versjon>\n" +
            "     </aut:UserAuthorization>\n" +
            "   </soapenv:Header>\n" +
            "   <soapenv:Body>\n" +
            "      <per:GetPerson>\n" +
            "         <Internref>XYZXYZXYZXYZ</Internref>\n" +
            "         <NameAddress>1</NameAddress>\n" +
            "         <InterestCode>1</InterestCode>\n" +
            "         <Beta>Detaljer</Beta>\n" +
            "      </per:GetPerson>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>\n";


    public CommandGetFromTestSpecification(TestSpecification testSpecification) {
        super(URI.create(testSpecification.getCommand_url()),
                "hystrixCommandGetFromTestSpecification_" + r.nextInt(100));
        this.template = TemplateUtil.updateTemplateWithValuesFromMap(testSpecification.getCommand_template(), testSpecification.getCommand_replacement_map());
        this.contentType = testSpecification.getCommand_contenttype();
        this.httpAuthorizationString = testSpecification.getCommand_http_authstring();
    }


    private String headerName(String header) {
        return header.substring(0, header.indexOf(":")).trim();
    }

    private String headerValue(String header) {
        return header.substring(header.indexOf(":") + 1, header.length()).trim();
    }
    @Override
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

        request = request.contentType(contentType).accept(contentType);  //;
        if (template == null || template.length() > 1) {
            return request;
        } else {
            //request=request.body(this.template);
            return request;
        }
    }


    @Override
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

    @Override
    protected String dealWithResponse(String response) {
        //return "200" + ":" + super.dealWithResponse(response);
        return super.dealWithResponse(response);
    }


    @Override
    protected String getTargetPath() {
        return "";
    }
}
