package no.cantara.simulator.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.RequestWrapper;
import java.util.Enumeration;
import java.util.stream.Collectors;

@Controller
@Path("/test")
public class TestResource {

    private static final Logger log = LoggerFactory.getLogger(TestResource.class);


    @Autowired
    private HttpServletRequest context;

    @Autowired
    public TestResource() {
    }


    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response testResourceController()  {

        HttpServletRequest request=context;

        // print the request
        log.debug(request.getMethod() + " " + request.getRequestURL() + " HTTP/1.1");

        final Enumeration<String> attributeNames = request.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            log.debug("{attribute} " + request.getParameter(attributeNames.nextElement()));
        }

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            log.debug("{parameter} " + request.getParameter(parameterNames.nextElement()));
        }
        if ("POST".equalsIgnoreCase(request.getMethod()))
        {
            try {
            log.debug("{body} "+request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
            } catch (Exception e){
                log.debug("Unable to log request body");
            }
        }
        return Response.ok().build();
    }

    @GET
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response testGETResourceController()  {

        HttpServletRequest request=context;
        // print the request
        log.debug(request.getMethod() + " " + request.getRequestURL() + " HTTP/1.1");

        final Enumeration<String> attributeNames = request.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            log.debug("{attribute} " + request.getParameter(attributeNames.nextElement()));
        }

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            log.debug("{parameter} " + request.getParameter(parameterNames.nextElement()));
        }
        if ("POST".equalsIgnoreCase(request.getMethod()))
        {
            try {
                log.debug("{body} "+request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
            } catch (Exception e){
                log.debug("Unable to log request body");
            }
        }
        return Response.ok().build();
    }
}
