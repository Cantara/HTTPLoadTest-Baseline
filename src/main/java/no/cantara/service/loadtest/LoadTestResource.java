package no.cantara.service.loadtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import no.cantara.service.LoadTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-09-12.
 */
@Path(LoadTestResource.APPLICATION_PATH)
public class LoadTestResource {
    public static final String APPLICATION_PATH = "/loadTest";
    private static final Logger log = LoggerFactory.getLogger(LoadTestResource.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String loadTest = "{}";
    private final String loadTests = "{}";
    private final String loadTestStatus = "{}";

    @Autowired
    public LoadTestResource() {
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startLoadTest(@RequestParam("test_id") String json) {
        log.trace("Invoked startLoadTest with {}", json);
        Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);
        String testId = JsonPath.read(document, "$.test_id");
        if (testId == null) {
            Response.Status status = Response.Status.BAD_REQUEST;
            log.warn("Invalid json. Returning {} {}, json={}", status.getStatusCode(), status.getReasonPhrase(), json);
            return Response.status(status).build();
        }

        try {

            LoadTestConfig loadTestConfig = mapper.readValue(json, LoadTestConfig.class);
            LoadTestExecutorService.executeLoadTest(loadTestConfig);
            return Response.ok(mapper.writeValueAsString(loadTestConfig)).build();
        } catch (Exception e) {
            log.warn("Could not convert to Json {}", json.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
//        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllLoadTests() {
        log.trace("getAllLoadTests");
        String jsonResponse;
        try {
            jsonResponse = mapper.writeValueAsString(loadTests);
        } catch (JsonProcessingException e) {
            log.warn("Could not convert to Json {}", loadTests);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok(jsonResponse).build();
    }

    @GET
    @Path("/{test_id}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatusForLoadTestInstance(@PathParam("test_id") String artifactId) {
        log.trace("getStatusForLoadTestInstances loadTestId={}", artifactId);


        String jsonResult;
        try {
            jsonResult = mapper.writeValueAsString(loadTestStatus);
        } catch (IOException e) {
            log.warn("Could not convert to Json {}", loadTestStatus);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.ok(jsonResult).build();
    }
}
