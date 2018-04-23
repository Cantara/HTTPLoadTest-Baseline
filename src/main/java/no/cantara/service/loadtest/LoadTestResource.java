package no.cantara.service.loadtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import no.cantara.service.Main;
import no.cantara.service.config.ConfigLoadTestResource;
import no.cantara.service.loadtest.util.LoadTestResultUtil;
import no.cantara.service.loadtest.util.UnzipStream;
import no.cantara.service.model.*;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
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
import java.util.SortedMap;

import static no.cantara.service.Main.CONTEXT_PATH;
import static no.cantara.service.loadtest.LoadTestExecutorService.RESULT_FILE_PATH;
import static no.cantara.simulator.RestTestResource.REST_PATH_DEBUG;
import static no.cantara.util.Configuration.loadByName;
import static no.cantara.util.Configuration.loadFromDiskByName;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-09-12.
 */
@Path(LoadTestResource.APPLICATION_PATH)
public class LoadTestResource {
    public static final String APPLICATION_PATH = "/loadTest";
    public static final String APPLICATION_PATH_ZIP = "/loadTest/zip";
    public static final String APPLICATION_PATH_FORM = "/loadTest/form";
    public static final String APPLICATION_PATH_FORM_TRACE = "/loadTest/form/trace";
    public static final String APPLICATION_PATH_FORM_LOAD = "/loadTest/form/load";
    public static final String APPLICATION_PATH_FORM_LOAD_TRACE = "/loadTest/form/load/trace";
    public static final String APPLICATION_PATH_FORM_READ = "/loadTest/form/read";
    public static final String APPLICATION_PATH_FORM_WRITE = "/loadTest/form/write";
    public static final String APPLICATION_PATH_FORM_BENCHMARK = "/loadTest/form/benchmark";
    public static final String APPLICATION_PATH_FORM_SELECT = "/loadTest/form/select";
    public static final String APPLICATION_PATH_STATUS = "/loadTest/status";
    public static final String APPLICATION_PATH_RUNSTATUS = "/loadTest/runstatus";
    public static final String APPLICATION_PATH_FULLSTATUS = "/loadTest/fullstatus";
    public static final String APPLICATION_PATH_FULLSTATUS_CSV = "/loadTest/fullstatus_csv";
    public static final String APPLICATION_PATH_STOP = "/loadTest/stop";
    private static final Logger log = LoggerFactory.getLogger(LoadTestResource.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final CsvMapper csvMapper = new CsvMapper();
    private final String loadTests = "{}";

    @Autowired
    public LoadTestResource() {
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
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
    @Path("/trace")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response startLoadTestTrace(@RequestBody String json) {
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
    @Path("/zip")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response uploadFile(
            @FormDataParam("file") InputStream inputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {
        log.debug("upload - ");

        try {
            UnzipStream.unzip(inputStream);
            log.debug(">>>>> upload size of the file in byte: ");

        } catch (IOException e) {
            log.error("Exception in receiving and handling zip-file with specifications", e);
            return Response.status(500)
                           .entity("Can not create destination folder on server")
                           .build();
        }
        return Response.status(200).entity("ZipFile successfully added ").build();

    }


    @POST
    @Path("/form")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED + ";charset=utf-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
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
    @Path("/form/trace")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED + ";charset=utf-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response startLoadTestFormTrace(@FormParam("jsonConfig") String json) {
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
            return Response.status(Response.Status.FOUND).header("Location", (CONTEXT_PATH + REST_PATH_DEBUG)).build();
//            return Response.temporaryRedirect(new java.net.URI(CONTEXT_PATH + APPLICATION_PATH_STATUS)).build();
        } catch (Exception e) {
            log.warn("/form Could not convert to Json {}, e", json.toString(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/form/load")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED + ";charset=utf-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response updateLoadTestSpecificationForm(@FormParam("jsonConfig") String json) {
        log.trace("Invoked updateLoadTestSpecificationForm with {}", json);

        if (json.startsWith("FILE:")) {
            json = loadFromDiskByName(json.substring(5, json.length()));

        }

        try {

            LoadTestConfig loadTestConfig = mapper.readValue(json, LoadTestConfig.class);
            LoadTestExecutorService.setLoadTestConfig(loadTestConfig);
            // 6. create 302-response with part2 of secret in http Location header
            return Response.status(Response.Status.FOUND)
                    .header("Location", Main.CONTEXT_PATH + ConfigLoadTestResource.CONFIG_PATH).build();
//            return Response.ok(json).build();
        } catch (Exception e) {
            log.warn("/form/load Could not convert to Json {},\n {e}", json, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/form/load/trace")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED + ";charset=utf-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response updateLoadTestSpecificationFormTrace(@FormParam("jsonConfig") String json) {
        log.trace("Invoked updateLoadTestSpecificationForm with {}", json);

        if (json.startsWith("FILE:")) {
            json = loadFromDiskByName(json.substring(5, json.length()));

        }

        try {

            LoadTestConfig loadTestConfig = mapper.readValue(json, LoadTestConfig.class);
            LoadTestExecutorService.setLoadTestConfig(loadTestConfig);
            // 6. create 302-response with part2 of secret in http Location header
            return Response.status(Response.Status.FOUND)
                    .header("Location", Main.CONTEXT_PATH + ConfigLoadTestResource.CONFIG_PATH_TRACE).build();
//            return Response.ok(json).build();
        } catch (Exception e) {
            log.warn("/form/load Could not convert to Json {},\n {e}", json, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/form/read")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED + ";charset=utf-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response updateReadTestSpecificationForm(@FormParam("jsonConfig") String json) {
        log.trace("Invoked updateReadTestSpecificationForm with {}", json);

        if (json.startsWith("FILE:")) {
            json = loadFromDiskByName(json.substring(5, json.length()));

        }

        try {

            List<TestSpecification> readTestSpec = mapper.readValue(json, new TypeReference<List<TestSpecification>>() {
            });
            LoadTestExecutorService.setReadTestSpecificationList(readTestSpec);
            return Response.ok(json).build();
        } catch (Exception e) {
            log.warn("/form/read Could not convert to Json {},\n {e}", json, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/form/write")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED + ";charset=utf-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response updateWriteTestSpecificationForm(@FormParam("jsonConfig") String json) {
        log.trace("Invoked updateWriteTestSpecificationForm with {}", json);

        if (json.startsWith("FILE:")) {
            json = loadFromDiskByName(json.substring(5, json.length()));

        }

        try {

            List<TestSpecification> writeTestSpec = mapper.readValue(json, new TypeReference<List<TestSpecification>>() {
            });
            LoadTestExecutorService.setWriteTestSpecificationList(writeTestSpec);
            return Response.ok(json).build();
        } catch (Exception e) {
            log.warn("/form/write Could not convert to Json {} \n{}", json, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/form/benchmark")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED + ";charset=utf-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response updateBenchmarkSpecificationForm(@FormParam("jsonConfig") String json) {
        log.trace("Invoked updateBenchmarkSpecificationForm with {}", json);

        try {

            LoadTestBenchmark loadTestBenchmark = mapper.readValue(json, LoadTestBenchmark.class);

            LoadTestResultUtil.setLoadTestBenchmark(loadTestBenchmark);
            return Response.ok(json).build();
        } catch (Exception e) {
            log.warn("/form/benchmark Could not convert to Json {} \n{}", json, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/form/select")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED + ";charset=utf-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response updateSelectedTestSpecificationForm(@FormParam("jsonConfig") String testSpecificationNumber) {
        log.trace("Invoked updateSelectedTestSpecificationForm with {}", testSpecificationNumber);

        String jsonResponse = "";
        Map<String, String> configuredTests = TestSpecificationLoader.getPersistedTestSpecificationFilenameMap();
        try {

            for (String testSpecificationEntry : configuredTests.keySet()) {
                if (testSpecificationEntry.contains(testSpecificationNumber + ".readfilename")) {
                    log.info("Trying Configured TestSpecification: {}, filename:{}", configuredTests.get(testSpecificationEntry), configuredTests.get(testSpecificationEntry));
                    String filecontent = loadFromDiskByName(configuredTests.get(testSpecificationEntry));
                    List<TestSpecification> readTestSpec = new ArrayList<>();
                    readTestSpec = mapper.readValue(filecontent, new TypeReference<List<TestSpecification>>() {
                    });
                    String loadTestJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readTestSpec);

                    jsonResponse = "[{\"Read_TestSpecification\": \n" + loadTestJson + "}";
                    LoadTestExecutorService.setReadTestSpecificationList(readTestSpec);
                    log.info("Loaded Configured TestSpecification: {}", configuredTests.get(testSpecificationEntry));
                }
                if (testSpecificationEntry.contains(testSpecificationNumber + ".writefilename")) {
//                    File file = new File(configuredTests.get(testSpecificationEntry));
                    log.info("Trying Configured TestSpecification: {}, filename:{}", configuredTests.get(testSpecificationEntry), configuredTests.get(testSpecificationEntry));
                    String filecontent = loadFromDiskByName(configuredTests.get(testSpecificationEntry));
                    List<TestSpecification> writeTestSpec = new ArrayList<>();
                    writeTestSpec = mapper.readValue(filecontent, new TypeReference<List<TestSpecification>>() {
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
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getAllLoadTestsStatusJson() {
        return getAllLoadTests();
    }

    @GET
    @Path("/runstatus")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getRunStatusJson() {
        boolean isRunning = LoadTestExecutorService.isRunning();
        if (isRunning) {
            return Response.status(409).type(MediaType.APPLICATION_JSON).entity("{\"runstatus\":\"testing in progress\"}").build();
        }

        SortedMap<String, String> resultMap = LoadTestResultUtil.hasPassedBenchmark(LoadTestExecutorService.getResultListSnapshot(), false);
        boolean runSuccess = LoadTestResultUtil.hasPassedBenchmark(resultMap);
        if (runSuccess) {
            resultMap.put("runstatus", "success");
            try {
                return Response.ok(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultMap)).build();

            } catch (Exception e) {
                log.error("Unable to produce jspon from runStatistics");
            }

            return Response.ok("{\"runstatus\":\"success\"}").build();

        }
        resultMap.put("runstatus", "fail");
        try {
            return Response.status(422).type(MediaType.APPLICATION_JSON).entity(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultMap)).build();

        } catch (Exception e) {
            log.error("Unable to produce jspon from runStatistics");
        }
        return Response.status(422).type(MediaType.APPLICATION_JSON).entity("{\"runstatus\":\"fail\"}").build();
    }


    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getAllLoadTests() {
        log.trace("getAllLoadTests");
        String jsonResponse = ""; //LoadTestExecutorService.printStats(LoadTestExecutorService.getResultListSnapshot());
        try {
            jsonResponse = jsonResponse + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(LoadTestExecutorService.getLatestResultListSnapshot());
        } catch (JsonProcessingException e) {
            log.warn("Could not convert to Json {}", loadTests);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        String response = String.format("{ \"HTTPLoadTest-status\": \n\"%s\", \n\n\"test-run-results\": %s}",
                LoadTestResultUtil.printStats(LoadTestExecutorService.getResultListSnapshot(), true), jsonResponse);  // force statistics

        return Response.ok(response).build();
    }

    @GET
    @Path("/stop")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response stopLoadTests() {
        LoadTestExecutorService.stop();
        return Response.ok().build();
    }


    @GET
    @Path("/fullstatus")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
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
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
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
            csvResult = csvMapper.writer(csvSchema).writeValueAsString(LoadTestExecutorService.getResultListSnapshot());
        } catch (IOException e) {
            log.warn("Could not convert to CSV {}", csvResult);
            //     return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return csvResult;
    }

    public static String getJsonResultString() {
        String jsonResult = "[{}]";
        try {
            jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(LoadTestExecutorService.getResultListSnapshot());
        } catch (IOException e) {
            log.warn("Could not convert to Json {}", jsonResult);
            //       return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return jsonResult;
    }
}
