package no.cantara.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path(RestTestResource.REST_PATH)
public class RestTestResource {
    public static final String REST_PATH = "/resttest";

    private static final Logger log = LoggerFactory.getLogger(RestTestResource.class);

    @Autowired
    public RestTestResource() {
    }

    @POST
    @Path("{path : .*}")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response postRequest(@Context HttpHeaders headers, @RequestBody String json) {
        log.info("Invoked postRequest with input: {} and headers: {}", json, headers.getRequestHeaders());
        String response = String.format("{ \"fruit\": %s }", json);

        return Response.ok(response).build();
    }

    @GET
    @Path("{path : .*}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getRequest(@Context HttpHeaders headers, @PathParam("path") String path) {
        log.info("Invoked getRequest with path: {} and headers: {}", path, headers.getRequestHeaders());
        if (path.isEmpty()) {
            path = "emptyPath";
        }
        String response = String.format("{ \"path\": %s }", path);

        return Response.ok(response).build();
    }
}
