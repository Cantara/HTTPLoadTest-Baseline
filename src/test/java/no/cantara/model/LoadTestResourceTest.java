package no.cantara.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.service.loadtest.LoadTestResource;
import no.cantara.service.model.LoadTestConfig;
import no.cantara.service.testsupport.TestServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.HttpURLConnection;

import static com.jayway.restassured.RestAssured.given;

public class LoadTestResourceTest {

    private TestServer testServer;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(LoadTestResourceTest.class);

    @BeforeClass
    public void startServer() throws Exception {
        testServer = new TestServer(getClass());
        testServer.start();
    }

    @AfterClass
    public void stop() {
        testServer.stop();
    }


    @Test
    public void testStartLoadTest() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("loadtestconfig.json").getFile());
        LoadTestConfig fileLoadtest = mapper.readValue(file, LoadTestConfig.class);
        String loadTestJson = mapper.writeValueAsString(fileLoadtest);

        given()
                .log().everything()
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .body(loadTestJson)
                .expect()
                .statusCode(HttpURLConnection.HTTP_OK)
                .log().everything()
                .when()
                .post(LoadTestResource.APPLICATION_PATH);


    }

    @Test(priority = 98, enabled = false)
    public void testStartLoadTestForm() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("loadtestconfig.json").getFile());
        LoadTestConfig fileLoadtest = mapper.readValue(file, LoadTestConfig.class);
        String loadTestJson = mapper.writeValueAsString(fileLoadtest);

        given()
                .log().everything()
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("jsonConfig", loadTestJson)
                .expect()
                .statusCode(HttpURLConnection.HTTP_MOVED_TEMP)
                .log().everything()
                .when()
                .post(LoadTestResource.APPLICATION_PATH_FORM);


    }
    @Test
    public void testGetAllLoadTests() throws Exception {
    }

    @Test
    public void testGetStatusForLoadTestInstance() throws Exception {
    }
}