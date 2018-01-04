package no.cantara.service.loadtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import no.cantara.service.model.LoadTestConfig;
import no.cantara.service.model.TestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static no.cantara.service.Main.CONTEXT_PATH;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-09-12.
 */
@Path(LoadTestResource.APPLICATION_PATH)
public class LoadTestResource {
    public static final String APPLICATION_PATH = "/loadTest";
    public static final String APPLICATION_PATH_FORM = "/loadTest/form";
    public static final String APPLICATION_PATH_FORM_READ = "/loadTest/form/read";
    public static final String APPLICATION_PATH_FORM_WRITE = "/loadTest/form/write";
    public static final String APPLICATION_PATH_STATUS = "/loadTest/status";
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
    public Response startLoadTest(@RequestBody String json) {
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
            LoadTestExecutorService.executeLoadTest(loadTestConfig, true);
            return Response.ok(json).build();
        } catch (Exception e) {
            log.warn("Could not convert to Json {}", json.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/form")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startLoadTestForm(@FormParam("jsonConfig") String json) {
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
            LoadTestExecutorService.executeLoadTest(loadTestConfig, true);
            return Response.status(Response.Status.FOUND).header("Location", (CONTEXT_PATH + APPLICATION_PATH_STATUS)).build();
//            return Response.temporaryRedirect(new java.net.URI(CONTEXT_PATH + APPLICATION_PATH_STATUS)).build();
        } catch (Exception e) {
            log.warn("Could not convert to Json {}", json.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/form/read")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateReadTestSpecificationForm(@FormParam("jsonConfig") String json) {
        log.trace("Invoked updateReadTestSpecificationForm with {}", json);
        try {

            List<TestSpecification> readTestSpec = new ArrayList<>();
            readTestSpec = mapper.readValue(json, new TypeReference<List<TestSpecification>>() {
            });
            LoadTestExecutorService.setReadTestSpecificationList(readTestSpec);
            return Response.ok(json).build();
        } catch (Exception e) {
            log.warn("Could not convert to Json {}", json.toString());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/form/write")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateWriteTestSpecificationForm(@FormParam("jsonConfig") String json) {
        log.trace("Invoked updateWriteTestSpecificationForm with {}", json);

        try {

            List<TestSpecification> writeTestSpec = new ArrayList<>();
            writeTestSpec = mapper.readValue(json, new TypeReference<List<TestSpecification>>() {
            });
            LoadTestExecutorService.setWriteTestSpecificationList(writeTestSpec);
            return Response.ok(json).build();
        } catch (Exception e) {
            log.warn("Could not convert to Json {}", json.toString());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllLoadTestsStatusJson() {
        return getAllLoadTests();
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllLoadTests() {
        log.trace("getAllLoadTests");
        String jsonResponse = ""; //LoadTestExecutorService.printStats(LoadTestExecutorService.getResultList());
        try {
            jsonResponse = jsonResponse + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(LoadTestExecutorService.getLatestResultList());
        } catch (JsonProcessingException e) {
            log.warn("Could not convert to Json {}", loadTests);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        String response = String.format("{ \"HTTPLoadTest-status\": \n\"%s\", \n\n\"test-run-results\": %s}",
                LoadTestExecutorService.printStats(LoadTestExecutorService.getResultList()), jsonResponse);

        return Response.ok(response).build();
    }

    @GET
    @Path("/{test_id}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatusForLoadTestInstance(@PathParam("test_id") String artifactId) {
        log.trace("getStatusForLoadTestInstances loadTestId={}", artifactId);


        String jsonResult;
        try {
            jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(LoadTestExecutorService.getResultList());
        } catch (IOException e) {
            log.warn("Could not convert to Json {}", loadTestStatus);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.ok(jsonResult).build();
    }
}
