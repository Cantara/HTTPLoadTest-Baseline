package no.cantara.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(RestTestResource.REST_PATH)
public class RestTestResource {
    public static final String REST_PATH = "/restTest";
    public static final String REST_PATH_READ = "/restTest/read";
    public static final String REST_PATH_WRITE = "/restTest/write";


    private static final Logger log = LoggerFactory.getLogger(RestTestResource.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public RestTestResource() {
    }

    @POST
    @Path("/write")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response postRequest(@RequestBody String json) {
        log.info("Invoked postRequest with input {}", json);
        String response = String.format("{ \"fruit\": %s }", json);

        return Response.ok(response).build();
    }

    @GET
    @Path("/read/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getRequest(@PathParam("id") String id) {
        log.info("getRequest: {}", id);
        String response = String.format("{ \"ID\": %s }", id);

        return Response.ok(response).build();
    }
}
