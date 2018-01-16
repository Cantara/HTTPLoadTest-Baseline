package no.cantara.service.loadtest.commands;

import com.github.kevinsawicki.http.HttpRequest;
import no.cantara.base.command.BaseHttpGetHystrixCommand;
import no.cantara.service.loadtest.util.TemplateUtil;
import no.cantara.service.model.TestSpecification;

import java.net.URI;
import java.util.Random;

public class CommandGetFromTestSpecification extends BaseHttpGetHystrixCommand<String> {

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
                "hystrixCommandGetFromTestSpecification");
        this.template = TemplateUtil.updateTemplateWithValuesFromMap(testSpecification.getCommand_template(), testSpecification.getCommand_replacement_map());
        this.contentType = testSpecification.getCommand_contenttype();
        this.httpAuthorizationString = testSpecification.getCommand_http_authstring();
    }



    @Override
    protected HttpRequest dealWithRequestBeforeSend(HttpRequest request) {
        // request= super.dealWithRequestBeforeSend(request);
        if (httpAuthorizationString != null && httpAuthorizationString.length() > 10) {
            request = request.authorization(httpAuthorizationString);
            //log.trace(request.header("Authorization"));
        }

        if (template.contains("soapenv:Envelope")) {
            //request.getConnection().addRequestProperty("SOAPAction", SOAP_ACTION);
        }

        request = request.contentType(contentType);  //;
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
