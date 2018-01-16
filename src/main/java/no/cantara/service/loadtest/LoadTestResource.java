package no.cantara.service.loadtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import no.cantara.service.loadtest.util.LoadTestResultUtil;
import no.cantara.service.model.LoadTestConfig;
import no.cantara.service.model.LoadTestResult;
import no.cantara.service.model.TestSpecification;
import no.cantara.service.model.TestSpecificationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static no.cantara.service.Main.CONTEXT_PATH;
import static no.cantara.service.loadtest.LoadTestExecutorService.RESULT_FILE_PATH;
import static no.cantara.service.util.Configuration.loadByName;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-09-12.
 */
@Path(LoadTestResource.APPLICATION_PATH)
public class LoadTestResource {
    public static final String APPLICATION_PATH = "/loadTest";
    public static final String APPLICATION_PATH_FORM = "/loadTest/form";
    public static final String APPLICATION_PATH_FORM_READ = "/loadTest/form/read";
    public static final String APPLICATION_PATH_FORM_WRITE = "/loadTest/form/write";
    public static final String APPLICATION_PATH_FORM_SELECT = "/loadTest/form/select";
    public static final String APPLICATION_PATH_STATUS = "/loadTest/status";
    public static final String APPLICATION_PATH_FULLSTATUS = "/loadTest/fullstatus";
    public static final String APPLICATION_PATH_FULLSTATUS_CSV = "/loadTest/fullstatus_csv";
    public static final String APPLICATION_PATH_STOP = "/loadTest/stop";
    private static final Logger log = LoggerFactory.getLogger(LoadTestResource.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final CsvMapper csvMapper = new CsvMapper();
    private final String loadTest = "{}";
    private final String loadTests = "{}";

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
        log.trace("Invoked startLoadTestForm with {}", json);
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
            log.warn("/form Could not convert to Json {}, e", json.toString(), e);
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

            List<TestSpecification> readTestSpec = mapper.readValue(json, new TypeReference<List<TestSpecification>>() {
            });
            LoadTestExecutorService.setReadTestSpecificationList(readTestSpec);
            return Response.ok(json).build();
        } catch (Exception e) {
            log.warn("/form/read Could not convert to Json {},\n {e}", json.toString(), e);
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

            List<TestSpecification> writeTestSpec =  mapper.readValue(json, new TypeReference<List<TestSpecification>>() {
            });
            LoadTestExecutorService.setWriteTestSpecificationList(writeTestSpec);
            return Response.ok(json).build();
        } catch (Exception e) {
            log.warn("/form/write Could not convert to Json {} \n{}", json.toString(), e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/form/select")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSelectedTestSpecificationForm(@FormParam("jsonConfig") String testSpecificationNumber) {
        log.trace("Invoked updateSelectedTestSpecificationForm with {}", testSpecificationNumber);

        String jsonResponse = "";
        Map<String, String> configuredTests = TestSpecificationLoader.getPersistedTestSpecificationFilenameMap();
        try {

            for (String testSpecificationEntry : configuredTests.keySet()) {
                if (testSpecificationEntry.contains(testSpecificationNumber + ".readfilename")) {
                    log.info("Trying Configured TestSpecification: {}, filename:{}", configuredTests.get(testSpecificationEntry), configuredTests.get(testSpecificationEntry));
                    InputStream file = loadByName(configuredTests.get(testSpecificationEntry));
                    List<TestSpecification> readTestSpec = new ArrayList<>();
                    readTestSpec = mapper.readValue(file, new TypeReference<List<TestSpecification>>() {
                    });
                    String loadTestJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readTestSpec);

                    jsonResponse = "[{\"Read_TestSpecification\": \n" + loadTestJson + "}";
                    LoadTestExecutorService.setReadTestSpecificationList(readTestSpec);
                    log.info("Loaded Configured TestSpecification: {}", configuredTests.get(testSpecificationEntry));
                }
                if (testSpecificationEntry.contains(testSpecificationNumber + ".writefilename")) {
//                    File file = new File(configuredTests.get(testSpecificationEntry));
                    log.info("Trying Configured TestSpecification: {}, filename:{}", configuredTests.get(testSpecificationEntry), configuredTests.get(testSpecificationEntry));
                    InputStream file = loadByName(configuredTests.get(testSpecificationEntry));
                    List<TestSpecification> writeTestSpec = new ArrayList<>();
                    writeTestSpec = mapper.readValue(file, new TypeReference<List<TestSpecification>>() {
                    });
                    String loadTestJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(writeTestSpec);

                    jsonResponse = jsonResponse + ",{\n\n\"Write_TestSpecification\": \n" + loadTestJson + "}]";

                    LoadTestExecutorService.setWriteTestSpecificationList(writeTestSpec);
                    log.info("Loaded Configured TestSpecification: {}, filename:{}", configuredTests.get(testSpecificationEntry), configuredTests.get(testSpecificationEntry));
                }
            }
            return Response.ok(jsonResponse).build();
        } catch (Exception e) {
            log.warn("Could not convert to Json, selected preconfigured set number {} \n{}", testSpecificationNumber.toString(), e);
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
                LoadTestResultUtil.printStats(LoadTestExecutorService.getResultList(), true), jsonResponse);  // force statistics

        return Response.ok(response).build();
    }

    @GET
    @Path("/stop")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopLoadTests() {
        LoadTestExecutorService.stop();
        return Response.ok().build();
    }


    @GET
    @Path("/fullstatus")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatusForLoadTestInstance(@PathParam("test_id") String test_run_filename) {
        log.trace("getStatusForLoadTestInstances loadTestId={}", test_run_filename);

        // If we have a specified test, we try to load it - if not, we use current
        if (test_run_filename != null && test_run_filename.contains("json")) {
            return Response.ok(RESULT_FILE_PATH + File.separator + loadByName(test_run_filename)).build();

        }


        return Response.ok(getJsonResultString()).build();
    }

    @GET
    @Path("/fullstatus_csv")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCSVStatusForLoadTestInstance(@PathParam("test_id") String test_run_filename) {
        log.trace("getStatusForLoadTestInstances loadTestId={}", test_run_filename);

        // If we have a specified test, we try to load it - if not, we use current
        if (test_run_filename != null && test_run_filename.contains("json")) {
            return Response.ok(RESULT_FILE_PATH + File.separator + loadByName(test_run_filename)).build();

        }

        return Response.ok(getCSVResultString()).build();
    }


    public static String getCSVResultString() {
        String csvResult = "";
        try {
            CsvSchema csvSchema = csvMapper.schemaFor(LoadTestResult.class);
            if (true) {
                csvSchema = csvSchema.withHeader();
            } else {
                csvSchema = csvSchema.withoutHeader();
            }
            csvResult = csvMapper.writer(csvSchema).writeValueAsString(LoadTestExecutorService.getResultList());
        } catch (IOException e) {
            log.warn("Could not convert to CSV {}", csvResult);
            //     return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return csvResult;
    }

    public static String getJsonResultString() {
        String jsonResult = "[{}]";
        try {
            jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(LoadTestExecutorService.getResultList());
        } catch (IOException e) {
            log.warn("Could not convert to Json {}", jsonResult);
            //       return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return jsonResult;
    }
}
