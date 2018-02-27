package no.cantara.simulator.resttest;

import com.jayway.restassured.http.ContentType;
import no.cantara.service.testsupport.TestServer;
import no.cantara.simulator.RestTestResource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;

import static com.jayway.restassured.RestAssured.given;

public class RestTestResourceTest {

    private TestServer testServer;


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
    public void testRestTestPost() throws IOException {
        String json = "banana";
        given()
                .log().everything()
                .contentType(ContentType.JSON)
                .body(json)
                .expect()
                .statusCode(HttpURLConnection.HTTP_OK)
                .log().everything()
                .when()
                .post(RestTestResource.REST_PATH_WRITE);
    }

    @Test
    public void testRestTestGet() throws IOException {
        String fruit = "orange";
        given()
                .log().everything()
                .expect()
                .statusCode(HttpURLConnection.HTTP_OK)
                .log().everything()
                .when()
                .get(RestTestResource.REST_PATH_READ + "/" + fruit);
    }
}
