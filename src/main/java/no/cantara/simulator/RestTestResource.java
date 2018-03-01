package no.cantara.simulator;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import nu.validator.messages.MessageEmitter;
import nu.validator.messages.MessageEmitterAdapter;
import nu.validator.messages.TextMessageEmitter;
import nu.validator.servlet.imagereview.ImageCollector;
import nu.validator.source.SourceCode;
import nu.validator.validation.SimpleDocumentValidator;
import nu.validator.xml.SystemErrErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.parsers.*;
import java.io.*;

@Path(RestTestResource.REST_PATH)
public class RestTestResource {
    public static final String REST_PATH = "/resttest";

    private static final Logger log = LoggerFactory.getLogger(RestTestResource.class);

    @Autowired
    public RestTestResource() {
    }

    @POST
    @Path("{path : .*}")
    public Response postRequest(@Context HttpHeaders headers, @RequestBody String input) {
        log.info("Invoked postRequest with input: {} and headers: {}", input, headers.getRequestHeaders());

        String parsedObject = parseInput(headers.getMediaType().getSubtype(), input);
        String response = String.format("{ \"entity\": %s }", parsedObject);

        return Response.ok(response).type(headers.getMediaType()).build();
    }

    @GET
    @Path("{path : .*}")
    public Response getRequest(@Context HttpHeaders headers, @PathParam("path") String path) {
        log.info("Invoked getRequest with path: {} and headers: {}", path, headers.getRequestHeaders());

        if (path.isEmpty()) {
            path = "emptyPath";
        }
        String response = String.format("{ \"path\": %s }", path);

        return Response.ok(response).build();
    }

    private String parseInput(String type, String input) {
        String parsedObject = "";
        switch (type) {
            case "json":
                parsedObject = parseJsonFromString(input);
                break;
            case "xml":
                parsedObject = parseXMLFromString(input);
                break;
            case "html":
                parsedObject = validateHtml(input);
                break;
            case "plain":
                parsedObject = input;
                break;
        }

        return parsedObject;
    }

    private String parseJsonFromString(String input) {
        JSONParser parser = new JSONParser();
        JSONObject json = new JSONObject();
        try {
            Object parsedObject = parser.parse(input);
            if (parsedObject instanceof JSONObject) {
                json = (JSONObject)parsedObject;
            }
        } catch (ParseException e) {
            log.error("Failed to parse json. " + e);
        }

        return json.toString();
    }

    private String parseXMLFromString(String xml) {
        DefaultHandler handler = new DefaultHandler();
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            saxParser.parse(stream, handler);
            return xml;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error("Failed to parse xml. " + e);
            return null;
        }
    }


    /**
     * Verifies that a HTML content is valid.
     * @param htmlContent the HTML content
     * @return true if it is valid, false otherwise
     */
    private String validateHtml( String htmlContent ) {
        InputStream in;
        MessageEmitterAdapter errorHandler;
        try {
            in = new ByteArrayInputStream( htmlContent.getBytes( "UTF-8" ));
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            SourceCode sourceCode = new SourceCode();
            ImageCollector imageCollector = new ImageCollector(sourceCode);
            boolean showSource = false;
            MessageEmitter emitter = new TextMessageEmitter( out, false );
            errorHandler = new MessageEmitterAdapter(sourceCode, showSource, imageCollector, 0, false, emitter );
            errorHandler.setErrorsOnly( true );

            SimpleDocumentValidator validator = new SimpleDocumentValidator();
            validator.setUpMainSchema( "http://s.validator.nu/html5-rdfalite.rnc", new SystemErrErrorHandler());
            validator.setUpValidatorAndParsers( errorHandler, true, false );
            validator.checkHtmlInputSource( new InputSource( in ));

            return in.toString();
        } catch (Exception e) {
            log.error("Failed to parse html with exception {}", e);
            return null;
        }
    }
}
