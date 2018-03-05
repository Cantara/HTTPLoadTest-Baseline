package no.cantara.simulator.resttest;

import com.jayway.restassured.http.ContentType;
import no.cantara.service.testsupport.TestServer;
import no.cantara.simulator.RestTestResource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
    public void testRestTestPost() {
        String path = "minapp/test/77/something";
        String json = "{fruit: mango}";
        String xml = "<note><body>small note</body></note>";
        String html = "<!DOCTYPE html><html><head><title>Web Page</title></head><body><p>A paragraph.</p></body></html>";
        String text = "banana(){}.mango";
        given()
                .log().everything()
                .contentType(ContentType.JSON)
                .body(json)
                .expect()
                .statusCode(HttpURLConnection.HTTP_OK)
                .log().everything()
                .when()
                .post(RestTestResource.REST_PATH + "/" + path);

        given()
                .log().everything()
                .contentType(ContentType.XML)
                .body(xml)
                .expect()
                .statusCode(HttpURLConnection.HTTP_OK)
                .log().everything()
                .when()
                .post(RestTestResource.REST_PATH + "/");

        given()
                .log().everything()
                .contentType(ContentType.HTML)
                .body(html)
                .expect()
                .statusCode(HttpURLConnection.HTTP_OK)
                .log().everything()
                .when()
                .post(RestTestResource.REST_PATH + "/");

        given()
                .log().everything()
                .contentType(ContentType.TEXT)
                .body(text)
                .expect()
                .statusCode(HttpURLConnection.HTTP_OK)
                .log().everything()
                .when()
                .post(RestTestResource.REST_PATH + "/" + path);
    }

    @Test
    public void testRestTestGet() {
        String path = "minapp/test/77/something";
        given()
                .log().everything()
                .expect()
                .statusCode(HttpURLConnection.HTTP_OK)
                .log().everything()
                .when()
                .get(RestTestResource.REST_PATH + "/" + path);

        given()
                .log().everything()
                .expect()
                .statusCode(HttpURLConnection.HTTP_OK)
                .log().everything()
                .when()
                .get(RestTestResource.REST_PATH + "/");
    }
}
